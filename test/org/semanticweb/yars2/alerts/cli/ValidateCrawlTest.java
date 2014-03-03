package org.semanticweb.yars2.alerts.cli;

import java.io.IOException;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.parser.ParseException;

public class ValidateCrawlTest extends TestCase{

	public static final String INPUT = "testcases/2009-116/content/data-small.nq";
	
	public static final String OUTPUT_DIR = "testcases/2009-116/content/";

	
	public void testPerformReasoning() throws IOException, ParseException, ClassNotFoundException{
		int argssize = 2;
		String[] args = new String[argssize];
		args[0] = "-i";
		args[1] = INPUT;
		
		ValidateCrawl.main(args);
	}

}
