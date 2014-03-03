package org.semanticweb.yars2.alerts.cli;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Validate {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Option inputO = new Option("i", "name of nq file to read");
		inputO.setArgs(1);
		inputO.setRequired(true);
		
		Option docO = new Option("d", "URL of document to verify");
		docO.setArgs(1);
		docO.setRequired(true);
		
		Option sparqlO = new Option("s", "sparql endpoint");
		sparqlO.setArgs(1);
		sparqlO.setRequired(false);
		
		Option redirectsO = new Option("r", "redirects file");
		sparqlO.setArgs(1);
		sparqlO.setRequired(false);
		
		Option helpO = new Option("h", "print help");
		
		Options options = new Options();
		options.addOption(inputO);
		options.addOption(docO);
		options.addOption(sparqlO);
		options.addOption(redirectsO);
		options.addOption(helpO);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}
		
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}

	}
}
