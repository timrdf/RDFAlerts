package org.semanticweb.yars2.alerts.cli;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.yars2.alerts.Validator;

public class ValidateCrawl {
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws org.semanticweb.yars.nx.parser.ParseException 
	 */
	public static void main(String[] args) throws IOException, org.semanticweb.yars.nx.parser.ParseException, ClassNotFoundException {
		Option inputO = new Option("i", "name of input file to read");
		inputO.setArgs(1);
		inputO.setRequired(true);
		
		Option nqO = new Option("nq", "format of input file is nq");
		nqO.setArgs(0);
		nqO.setRequired(false);
		
		Option redirectsO = new Option("r", "redirects file");
		redirectsO.setArgs(1);
		redirectsO.setRequired(false);
		
		Option helpO = new Option("h", "print help");
		
		Options options = new Options();
		options.addOption(inputO);
		options.addOption(nqO);
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
		
		String input = cmd.getOptionValue('i');
		String redirects = cmd.getOptionValue('r');
		
		Validator v = new Validator(input, redirects);
		v.validate();
	}
}
