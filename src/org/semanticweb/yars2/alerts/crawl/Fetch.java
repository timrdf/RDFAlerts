package org.semanticweb.yars2.alerts.crawl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.util.CallbackContextSet;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

public class Fetch {
	Logger _log = Logger.getLogger(this.getClass().getName());

	static final String USERAGENT = "alerts (http://sw.deri.org/2008/12/alerts/robot.html)";
	static final Header[] HEADERS = {
		new Header("Accept", "application/rdf+xml"),
		new Header("User-Agent", USERAGENT),	
	};

	Resource _r;
	
	Set<Node[]> _data;
	
	Hashtable<String, Robots> _robots;
	
	int _e;
	
	public Fetch(Resource r, int e) {
		_r = r;
		_e = e;
		
		_data = new TreeSet<Node[]>(NodeComparator.NC);
		
		_robots = new Hashtable<String, Robots>();
	}
	
	public Set<Node[]> getData() {
		return _data;
	}
	
	public void fetch() {
		long time = System.currentTimeMillis();
		
		Set<Node> seen = new HashSet<Node>();
		Queue togo = new Queue();
		
		togo.add(_r);

		for (int i = 0; i < _e; i++) {
			List<FetchThread> tli = new ArrayList<FetchThread>();
			List<CallbackContextSet> ccsli = new ArrayList<CallbackContextSet>();

			for (Node n : togo) {
				if (n instanceof Resource) {
					try {
						URL u = new URL(n.toString());
						if (u.getProtocol().equals("http")) {
							CallbackContextSet ccs = new CallbackContextSet();
							ccsli.add(ccs);
							
							tli.add(new FetchThread(u, ccs, _robots));

							tli.get(tli.size()-1).run();
						}
					} catch (MalformedURLException ex) {
						System.err.println(ex.getMessage());
					}
				}
			}

			for (FetchThread lt : tli) {
				try {
					lt.join();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}				
			}

			seen.addAll(togo);
			
			for (CallbackContextSet ccs : ccsli) {
				_data.addAll(ccs.getSet());
			}

			togo = new Queue();
			
			for (Node[] nx : _data) {
				for (int j = 0; j < nx.length; j++) {
					Node n = nx[j];

					// only look up "ontology" uris
					if (j == 1 || (nx[1].equals(RDF.TYPE) && j == 2) || (nx[1].equals(OWL.IMPORTS) && j == 2) && n instanceof Resource) {
						if (!seen.contains((Resource)n)) {
							togo.add((Resource)n);
						}
					}
				}
			}
		}
		
		long time1 = System.currentTimeMillis();

		_log.info("time elapsed " + (time1-time) + " ms");

		_log.info("quads retrieved " + _data.size());
	}
}

class FetchThread extends Thread {
	Logger _log = Logger.getLogger(this.getClass().getName());

	URL _u;
	Callback _c;
	Hashtable<String, Robots> _robots = new Hashtable<String, Robots>();

	public FetchThread(URL u, Callback c, Hashtable<String, Robots> robots) {
		_u = u;
		_c = c;
		_robots = robots;
	}
	
	public void run() {	
		long time = System.currentTimeMillis();

		boolean ok = false;
		
		String host = _u.getHost();
		
		Robots r = null;
		
		if (_robots.containsKey(host)) {
			r = _robots.get(host);
		} else {
			r = new Robots(host);
			_robots.put(host, r);
		}
		
		if (!r.accessOK(_u)) {
			_log.info("access denied per robots.txt for " + _u);
			return;
		}

		try {
			RDFXMLParser rp = new RDFXMLParser(_u, true, true, _c);
			ok = true;
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		long time1 = System.currentTimeMillis();
		
		_log.info("looking up " + _u + (ok ? " ok " : " error ") + (time1-time) + " ms");
	}
}

class Queue extends HashSet<Resource> {
	public boolean add(Resource r) {
		String u = r.toString();
		
		if (u.indexOf('#') > 0) {
			r = new Resource(u.substring(0, u.indexOf('#')));
		}
		return super.add(r);
	}
}
