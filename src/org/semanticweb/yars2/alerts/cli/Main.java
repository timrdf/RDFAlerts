package org.semanticweb.yars2.alerts.cli;

import java.lang.reflect.Method;

public class Main {
	private static final String USAGE = "usage: org.semanticweb.yars.nx.cli.Main <utility> [options...]";
	private static final String PREFIX = "org.semanticweb.yars2.alerts.cli.";

	public static void main(String[] args) {		
		try {
			if (args.length < 1) {			
				StringBuffer sb = new StringBuffer();
				sb.append("where <utility> one of");
				sb.append("\n\tAnalyseCrawl      Analyse the statistics from a crawl");
				sb.append("\n\tValidate      Validate some data");
				sb.append("\n\tValidate      Validate a crawl of data");
				usage(sb.toString());
			}
			
			Class cls = Class.forName(PREFIX + args[0]);
			
			Method mainMethod = cls.getMethod("main", new Class[] { String[].class });

			String[] mainArgs = new String[args.length - 1];
			System.arraycopy(args, 1, mainArgs, 0, mainArgs.length);
			
			long time = System.currentTimeMillis();
			
			mainMethod.invoke(null, new Object[] { mainArgs });
			
			long time1 = System.currentTimeMillis();
			
			System.err.println("time elapsed " + (time1-time) + " ms");
		} catch (Throwable e) {
			e.printStackTrace();
			Throwable cause = e.getCause();
			cause.printStackTrace();
			usage(e.toString());
		}
	}

	private static void usage(String msg) {
		System.err.println(USAGE);
		System.err.println(msg);
		System.exit(-1);
	}
}

