package org.semanticweb.yars2.alerts.crawl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.util.CallbackContextSet;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;


public class CrawlTest extends TestCase {
	static final String USERAGENT = "alerts (http://sw.deri.org/2008/12/alerts/robot.html)";
	static final Header[] HEADERS = {
		new Header("Accept", "application/rdf+xml"),
		new Header("User-Agent", USERAGENT),	
	};

	public void testLookup() throws FileNotFoundException, ParseException, IOException {
		long time = System.currentTimeMillis();
		
		Fetch f = new Fetch(new Resource("http://sw.deri.org/~aidanh/foaf/foaf.rdf#Aidan_Hogan"), 2);
		//Fetch f = new Fetch(new Resource("http://harth.org/andreas/foaf#ah"));
		//Fetch f = new Fetch(new Resource("http://www.umbrich.net/foaf.rdf#me"));

		f.fetch();
		
		long time1 = System.currentTimeMillis();

		System.err.println("time elapsed " + (time1-time) + " ms");

		System.err.println("quads retrieved " + f.getData().size());
	}
}