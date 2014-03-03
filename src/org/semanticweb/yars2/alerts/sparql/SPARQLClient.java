package org.semanticweb.yars2.alerts.sparql;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

public class SPARQLClient {
	private String _endpoint;
	
	public SPARQLClient(String endpoint){
		_endpoint = endpoint;
	}
	
	public void query(String query, ResultsHandler rh) throws IOException, ParseException{
		String queryURL = _endpoint+"/query?query=";
		try {
			queryURL += URLEncoder.encode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		queryURL += "&accept=";
		try {
			queryURL += URLEncoder.encode("application/rdf+nq", "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		
//		System.out.println(queryURL);
	    URL u = new URL(queryURL);
//	    InputStream is = u.openStream();
//	        
//	    NxParser nxp = new NxParser(is);
//	    ArrayList<Node[]> ans = new ArrayList<Node[]>();
//	    while(nxp.hasNext()){
//	    	ans.add(nxp.next());
//	    }
	    
	    new NxParser(u, rh);
	}
	

	public static void main(String[] args) throws IOException, ParseException {
		SPARQLClient spc = new SPARQLClient("http://swse.deri.org/yars2");
		spc.query("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT DISTINCT ?s ?o WHERE {  ?s owl:complementOf ?o . } LIMIT 200", new ResultsCollector());
	}
}
