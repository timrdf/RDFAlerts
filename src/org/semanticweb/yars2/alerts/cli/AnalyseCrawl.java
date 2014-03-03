package org.semanticweb.yars2.alerts.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars.tld.URIHandler;
import org.semanticweb.yars2.index.disk.QuadStringScanIterator;
import org.semanticweb.yars2.index.disk.block.NodeBlockInputStream;
import org.semanticweb.yars2.index.disk.block.NodeBlockOutputStream;
import org.semanticweb.yars2.index.disk.block.PersistantNodeArrayIterator;
import org.semanticweb.yars2.sort.MergeSortIterator;
import org.semanticweb.yars2.sort.SortIterator;

public class AnalyseCrawl {
	private static final String NEW_HEADER_FILE = "stats-header.nqx";
	private static final String NEW_XMLLINT_FILE = "stats-xmllint.nqx";
	private static final String NEW_RAPPER_FILE = "stats-rapper.nqx";
	
	private static final String SORTED_INDEX = "stats.idx";
	
	private static final Resource CONTENT_TYPE = new Resource("http://www.w3.org/2006/http#content-type");
	private static final Resource RESPONSE_CODE = new Resource("http://www.w3.org/2006/http#responseCode");
	
	private static final Resource IS_VALID_RDFXML = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#isValidRDFXML");
	private static final Resource IS_WELL_FORMED_XML = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#isWellFormedXml");
	
	private static final Resource XMLLINT_ERROR_CODE = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#xmllintErrorCode");
	
	private static final Resource XMLLINT_CONTEXT = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#xmllint");
	
	private static final String[] XMLLINT_ERRORS = {
		"Start tag expected", 
		"Opening and ending tag mismatch",
		"Namespace prefix",
		" not defined",
		"Premature end of data in tag",
		"xmlParseEntityRef: no name",
		"Specification mandate value for attribute",
		"EntityRef: expecting ';'",
		"StartTag: invalid element name",
		"expected '>'",
		"Sequence",
		"internal error",
		"Extra content at the end of the document",
		"Couldn't find end of Start Tag",
		"error parsing attribute name",
		"attributes construct error",
		"AttValue:",
		"Comment not terminated",
		"is not absolute",
		"Space required after the Public Identifier",
		"SystemLiteral",
		"SYSTEM or PUBLIC",
		"Malformed declaration",
		"Blank needed",
		"Attribute border",
		"not proper UTF-8",
		"Unescaped '<' not allowed",
		"double-hyphen",
		"DOCTYPE",
		"not a valid URI",
		"CharRef: invalid decimal value",
		"CharRef: invalid hexadecimal value",
		"xmlParsePITarget",
		"invalid xmlChar value",
		"Failed to parse QName",
		"redefined",
		"Excessive depth in document",
		"XML declaration allowed",
		"CData section not finished",
		"PCDATA invalid Char value",
		"expected '='",
		"parsing XML declaration: '?>' expected",
		"Unsupported version",
		"xmlParsePI : no target name",
		"never end ...",
		"ParsePI: ",
		"Invalid XML encoding name",
		"Unsupported encoding ",
		"Document is empty",
		"invalid character in attribute value",
		"Space required after 'PUBLIC'",
		"Public Identifier is missing"
	};
	
	private static final Resource RAPPER_ERROR_CODE = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#rapperErrorCode");
//	private static final Resource RAPPER_WARNING_CODE = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#rapperWarningCode");
	
	private static final Resource RAPPER_CONTEXT = new Resource("http://sw.deri.org/2004/06/multicrawler/vocab#rapper");
	
	private static final String[] RAPPER_ERRORS = {
		"without a namespace", 
		"multiple object node elements",
		"RDF term resource",
		"Illegal rdf:ID value",
		"namespace prefix",
		"Duplicated rdf:ID value",
		"The namespace URI for prefix",
		"Literal property element",
		"rdf:Description is forbidden as a property element",
		"Illegal rdf:nodeID",
		"rdf:li is forbidden as a node element",
		"RDF term about is forbidden as a property attribute",
		"not in Unicode Normal Form",
		"Unknown RDF namespace property attribute",
		"Unknown rdf:parseType value",
		"is an unknown RDF namespaced element"
	};
	
//	private static final String[] RAPPER_WARNINGS = {
//		
//	};
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws org.semanticweb.yars.nx.parser.ParseException 
	 */
	public static void main(String[] args) throws IOException, org.semanticweb.yars.nx.parser.ParseException {
		Option inputhO = new Option("ih", "header information");
		inputhO.setArgs(1);
		inputhO.setRequired(true);
		
		Option inputxlO = new Option("ixl", "input xmllint file");
		inputxlO.setArgs(1);
		inputxlO.setRequired(true);
		
		Option inputxO = new Option("ix", "good/bad xml input");
		inputxO.setArgs(1);
		inputxO.setRequired(true);
		
		Option inputrO = new Option("ir", "good/bad rdf input");
		inputrO.setArgs(1);
		inputrO.setRequired(true);
		
		Option inputrpO = new Option("irp", "good/bad rdf input");
		inputrpO.setArgs(1);
		inputrpO.setRequired(true);
		
		Option dirO = new Option("d", "output dir");
		dirO.setArgs(1);
		dirO.setRequired(true);
		
		Option helpO = new Option("h", "print help");
		
		Options options = new Options();
		options.addOption(inputhO);
		options.addOption(inputxO);
		options.addOption(inputxlO);
		options.addOption(inputrO);
		options.addOption(inputrpO);
		options.addOption(dirO);
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

		String dir = cmd.getOptionValue("d");
		dir+="/";
		
		String inputh = cmd.getOptionValue("ih");
		String outputh = dir+NEW_HEADER_FILE;
		System.err.println("Preparing header file");
		prepareHeader(inputh, outputh);
		
		String inputxl = cmd.getOptionValue("ixl");
		String outputxl = dir+NEW_XMLLINT_FILE;
		System.err.println("Preparing xmllint file");
		prepareXMLLint(inputxl, outputxl);
		
		String inputrp = cmd.getOptionValue("irp");
		String outputrp = dir+NEW_RAPPER_FILE;
		System.err.println("Preparing rapper file");
		prepareRapper(inputrp, outputrp);
		
		String inputx = cmd.getOptionValue("ix");
		String inputr = cmd.getOptionValue("ir");
		
		NodeBlockInputStream nbos0 = new NodeBlockInputStream(outputh);
		QuadStringScanIterator iter0 = new QuadStringScanIterator(nbos0);
		SortIterator s0 = new SortIterator(iter0);
		
		NodeBlockInputStream nbos1 = new NodeBlockInputStream(outputxl);
		QuadStringScanIterator iter1 = new QuadStringScanIterator(nbos1);
		SortIterator s1 = new SortIterator(iter1);
		
		NodeBlockInputStream nbos2 = new NodeBlockInputStream(outputrp);
		QuadStringScanIterator iter2 = new QuadStringScanIterator(nbos2);
		SortIterator s2 = new SortIterator(iter2);
		
		FileInputStream fis0 = new FileInputStream(inputx);
		NxParser nxp0 = new NxParser(fis0);
		SortIterator s3 = new SortIterator(nxp0);
		
		FileInputStream fis1 = new FileInputStream(inputr);
		NxParser nxp1 = new NxParser(fis1);
		SortIterator s4 = new SortIterator(nxp1);
		
		System.err.println("Merge sorting all input");
		String outputs = dir+SORTED_INDEX;
		NodeBlockOutputStream nbos = new NodeBlockOutputStream(outputs); 
		MergeSortIterator msi = new MergeSortIterator(s0, s1, s2, s3, s4);
		PersistantNodeArrayIterator pnai = new PersistantNodeArrayIterator(msi, nbos, true);

		analyse(pnai);
	}
	
	public static void analyse(Iterator<Node[]> iter){
		Node olds = null;
		
		int rc = -1;
		int allc = 0, xc = 0, rxc = 0, rnxc = 0, cc = 0, ncc = 0;
		
		Count<String> allh = new Count<String>();
		Count<String> xh = new Count<String>();
		Count<String> rxh = new Count<String>();
		Count<String> rnxh = new Count<String>();
		Count<String> ch = new Count<String>();
		Count<String> nch = new Count<String>();
		
		boolean validrdf = false, validxml = false;
		Count<String> cct = new Count<String>();
		Count<String> cctc = new Count<String>();
		Count<String> cctn = new Count<String>();
		Count<String> cctx = new Count<String>();
		Count<String> cctr = new Count<String>();
		
		Count<Integer> crc = new Count<Integer>();
		Count<Integer> crcc = new Count<Integer>();
		Count<Integer> crcn = new Count<Integer>();
		Count<Integer> crcx = new Count<Integer>();
		Count<Integer> crcr = new Count<Integer>();
		
		Count<Integer> cxler = new Count<Integer>();
		Count<Integer> cxlwr = new Count<Integer>();
		
		Count<Integer> crer = new Count<Integer>();
//		Count<Integer> crwr = new Count<Integer>();
		
		HashSet<Integer> xlecs = new HashSet<Integer>();
//		HashSet<Integer> rwcs = new HashSet<Integer>();
		HashSet<Integer> recs = new HashSet<Integer>();
		
		String ct = null;
		boolean done = !iter.hasNext();
		while(!done){
			Node[] line = iter.next();
			done = !iter.hasNext();
			
			if(olds==null){
				olds = line[0];
			} else if(!olds.equals(line[0]) || done){
				String host = URIHandler.getPLD(line[0].toString());
				if(host==null){
					host = "null";
				}
				allc++;
				allh.add(host);
				
//				if(validxml && xlecs.size()>0){
//					System.err.println(olds+" is valid xml but with errors "+xlecs);
//				} 
				if(rc==200 && !validxml && xlecs.size()==0){
					System.err.println(olds+" is invalid xml but without xmllint errors "+xlecs);
				} else if(validrdf && !validxml){
					System.err.println(olds+" is valid rdf but not valid xml "+xlecs);
				} else if(validrdf && recs.size()>0){
					System.err.println(olds+" is valid rdf but with errors "+recs);
				} else if(rc==200 && validxml && !validrdf && recs.size()==0){
					System.err.println(olds+" is valid xml, invalid rdf, but without a rapper error.");
				} 
//				else if(!validxml && rec!=-1){
//					System.err.println(olds+" is invalid xml but with rapper error "+rec+" (xmlint errors "+xlecs+")");
//				}
				
				if(ct==null){
					ct = "null";
					crcn.add(rc);
				} else if(ct.equals("application/rdf+xml") && rc ==200){
					for(Integer rec:recs){
						crer.add(rec);
					}
//					for(Integer i:rwcs){
//						crwr.add(i);
//					}
					if(validxml) for(Integer i:xlecs)
						cxlwr.add(i);
					else for(Integer i:xlecs)
						cxler.add(i);
				}
				
				cct.add(ct);
				crc.add(rc);
				if(validrdf){
					cctr.add(ct);
					crcr.add(rc);
					if(validxml){
						rxc++;
						rxh.add(host);
					} else{
						rnxc++;
						rnxh.add(host);
					}
				}else if(validxml){
					cctx.add(ct);
					crcx.add(rc);
					xc++;
					xh.add(host);
				}else if(rc==200){
					cctc.add(ct);
					crcc.add(rc);
					cc++;
					ch.add(host);
				}else{
					cctn.add(ct);
					crcc.add(rc);
					ncc++;
					nch.add(host);
				}
				
				validrdf = false;
				validxml = false;
				ct = null;
				rc = 0;
				xlecs = new HashSet<Integer>();
//				rwcs = new HashSet<Integer>();
				recs = new HashSet<Integer>();
				
				olds = line[0];
				
			}
			
			if(line[1].equals(IS_VALID_RDFXML)){
				if(line[2].toString().equals("true"))
					validrdf = true;
				else validrdf = false;
			} else if(line[1].equals(IS_WELL_FORMED_XML)){
				if(line[2].toString().equals("true"))
					validxml = true;
				else validxml = false;
			} else if(line[1].equals(CONTENT_TYPE)){
				ct = parseContentType(line[2].toString());
			} else if(line[1].equals(RESPONSE_CODE)){
				try{
					rc = Integer.parseInt(line[2].toString());
				} catch (Exception e){
					rc = 0;
				}
			} else if(line[1].equals(XMLLINT_ERROR_CODE)){
				xlecs.add(Integer.parseInt(line[2].toString()));
			} else if(line[1].equals(RAPPER_ERROR_CODE)){
				recs.add(Integer.parseInt(line[2].toString()));
			} 
//			else if(line[1].equals(RAPPER_WARNING_CODE)){
//				rwcs.add(Integer.parseInt(line[2].toString()));
//			}
		}
		
		System.out.println("Analysed "+allc+" URLs.");
		System.out.println("Hosts:\n");
		allh.printOrderedStats(50);
		System.out.println("Valid RDF & XML: "+rxc);
		System.out.println("Hosts:\n");
		rxh.printOrderedStats(50);
		System.out.println("Valid XML, Invalid RDF: "+xc);
		System.out.println("Hosts:\n");
		xh.printOrderedStats(50);
		System.out.println("Invalid XML, Valid RDF: "+rnxc);
		System.out.println("Hosts:\n");
		rnxh.printOrderedStats(50);
		System.out.println("Invalid XML & RDF RC=200: "+cc);
		System.out.println("Hosts:\n");
		ch.printOrderedStats(50);
		System.out.println("Invalid XML & RDF RC!=200: "+ncc);
		System.out.println("Hosts:\n");
		nch.printOrderedStats(50);
		
		System.out.println("\nCONTENT TYPES");
		System.out.println("\nAll:");
		cct.printOrderedStats();
		System.out.println("\nValid RDF & XML:");
		cctr.printOrderedStats();
		System.out.println("\nValid XML, Invalid RDF:");
		cctx.printOrderedStats();
		System.out.println("\nInvalid XML & RDF:");
		cctc.printOrderedStats();
		System.out.println("\nNo Content-Type:");
		cctn.printOrderedStats();
		
		
		System.out.println("\nRESPONSE CODES");
		System.out.println("\nAll:");
		crc.printOrderedStats();
		System.out.println("\nValid RDF & XML:");
		crcr.printOrderedStats();
		System.out.println("\nValid XML, Invalid RDF:");
		crcx.printOrderedStats();
		System.out.println("\nInvalid XML & RDF:");
		crcc.printOrderedStats();
		System.out.println("\nNo Content Type");
		crcn.printOrderedStats();
		
		System.out.println("\napplication/rdf+xml RC:200");
		System.out.println("\nXMLLint Errors:");
		cxler.printOrderedStats();
		System.out.println("\nXMLLint Warnings:");
		cxlwr.printOrderedStats();
		System.out.println("\nRapper Errors:");
		crer.printOrderedStats();
//		System.out.println("\nRapper Warnings:");
//		crwr.printOrderedStats();
	}
	
	public static String parseContentType(String fullct){
		if(fullct==null || fullct.equals(""))
			return null;
		StringTokenizer tok = new StringTokenizer(fullct, ";");
		return tok.nextToken();
	}
	
	public static void prepareHeader(String in, String out) throws org.semanticweb.yars.nx.parser.ParseException, IOException{
		FileInputStream fis = new FileInputStream(in);
		NxParser nxp = new NxParser(fis);
		
		NodeBlockOutputStream nbos = new NodeBlockOutputStream(out);
		
		Node sub = null;
		while(nxp.hasNext()){
			Node[] line = nxp.next();
			if(line[0] instanceof Resource){
				sub = line[0];
			} else if(line[1].equals(CONTENT_TYPE)|| line[1].equals(RESPONSE_CODE)){
				line[0] = sub;
				nbos.write(line);
			}
		}
		nbos.close();
		fis.close();
	}
	
	public static void prepareXMLLint(String in, String out) throws org.semanticweb.yars.nx.parser.ParseException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		NodeBlockOutputStream nbos = new NodeBlockOutputStream(out);
		
		String line = null;
		Resource r = null;
		HashSet<Integer> ecs = new HashSet<Integer>();
		while((line = br.readLine())!=null){
			if(line.startsWith("processing")){
				StringTokenizer tok = new StringTokenizer(line, " ");
				tok.nextToken();
				r = new Resource(tok.nextToken());
				ecs = new HashSet<Integer>();
			} else if(line.startsWith("/tmp/out.rdf")){
				boolean found = false;
				for(int i=0; i<XMLLINT_ERRORS.length; i++){
					if(line.contains(XMLLINT_ERRORS[i])){
						if(ecs.add(i)){
							Node[] na = { r, XMLLINT_ERROR_CODE, new Literal(i+""), XMLLINT_CONTEXT};
							nbos.write(na);
						}
						found = true;
						break;
					}
				}
				
				if(!found){
					System.err.println("Could not find error: "+line);
				}
			}
		}
		nbos.close();
		br.close();
	}
	
	public static void prepareRapper(String in, String out) throws org.semanticweb.yars.nx.parser.ParseException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		NodeBlockOutputStream nbos = new NodeBlockOutputStream(out);
		
		String line = null;
		Resource r = null;
		HashSet<Integer> ecs = new HashSet<Integer>();
		HashSet<Integer> wcs = new HashSet<Integer>();
		while((line = br.readLine())!=null){
			if(line.startsWith("processing")){
				StringTokenizer tok = new StringTokenizer(line, " ");
				tok.nextToken();
				r = new Resource(tok.nextToken());
				ecs = new HashSet<Integer>();
				wcs = new HashSet<Integer>();
			} else if((line.startsWith("rapper: Error") || line.startsWith("rapper: Warning")) && !line.contains("XML parser error:") && !line.contains("XML Parsing failed")){
				boolean found = false;
				for(int i=0; i<RAPPER_ERRORS.length; i++){
					if(line.contains(RAPPER_ERRORS[i])){
						if(ecs.add(i)){
							Node[] na = { r, RAPPER_ERROR_CODE, new Literal(i+""), RAPPER_CONTEXT};
							nbos.write(na);
						}
						found = true;
						break;
					}
				}
				
				if(!found){
					System.err.println("Could not find error: "+line);
				}
			} 
//			else if(line.startsWith("rapper: Warning")){
//				boolean found = false;
//				if(line.contains("without a namespace")){ 
//					if(ecs.add(0)){
//						Node[] na = { r, RAPPER_ERROR_CODE, new Literal(0+""), RAPPER_CONTEXT};
//						nbos.write(na);
//					}
//					found = true;
//				} else if(line.contains("not in Unicode Normal Form")){ 
//					if(ecs.add(12)){
//						Node[] na = { r, RAPPER_ERROR_CODE, new Literal(12+""), RAPPER_CONTEXT};
//						nbos.write(na);
//					}
//					found = true;
//				} else for(int i=0; i<RAPPER_WARNINGS.length; i++){
//					if(line.contains(RAPPER_WARNINGS[i])){
//						if(wcs.add(i)){
//							Node[] na = { r, RAPPER_WARNING_CODE, new Literal(i+""), RAPPER_CONTEXT};
//							nbos.write(na);
//						}
//						found = true;
//						break;
//					}
//				}
//				
//				if(!found){
//					System.err.println("Could not find warning: "+line);
//				}
//			}
		}
		nbos.close();
		br.close();
	}
}

