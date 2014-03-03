package org.semanticweb.yars2.alerts.cli;

import java.io.IOException;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.parser.ParseException;

public class AnalyseCrawlTest extends TestCase{

	public static final String INPUT_HEADER = "testcases/2009-116/stats-header.nq";
	public static final String INPUT_GOODBAD_XML = "testcases/2009-116/stats-goodbadxml.nq";
	public static final String INPUT_GOODBAD_RDFXML = "testcases/2009-116/stats-goodbadrdf.nq";
	public static final String INPUT_XMLLINT = "testcases/2009-116/stats-xmllint";
	
	public static final String OUTPUT_DIR = "testcases/2009-116/";

	
	public void testPerformReasoning() throws IOException, ParseException, ClassNotFoundException{
		int argssize = 10;
		String[] args = new String[argssize];
		args[0] = "-ih";
		args[1] = INPUT_HEADER;
		args[2] = "-ix";
		args[3] = INPUT_GOODBAD_XML;
		args[4] = "-ir";
		args[5] = INPUT_GOODBAD_RDFXML;
		args[6] = "-ixl";
		args[7] = INPUT_XMLLINT;
		
		args[8] = "-d";
		args[9] = OUTPUT_DIR;
		
		
		AnalyseCrawl.main(args);
	}

}
