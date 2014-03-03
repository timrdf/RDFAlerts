package org.semanticweb.yars2.alerts;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars2.Yars2;
import org.semanticweb.yars2.alerts.reasoning.model.AuthorityIterator;
import org.semanticweb.yars2.alerts.reasoning.model.ConceptIndex;
import org.semanticweb.yars2.alerts.reasoning.model.ConceptIndexFactory;
import org.semanticweb.yars2.alerts.reasoning.model.concepts.MoreClass;
import org.semanticweb.yars2.alerts.reasoning.model.concepts.MoreProperty;
import org.semanticweb.yars2.alerts.sparql.ResultsCollector;
import org.semanticweb.yars2.alerts.sparql.SPARQLClient;
import org.semanticweb.yars2.alerts.tracker.IssueCounter;
import org.semanticweb.yars2.index.disk.QuadStringScanIterator;
import org.semanticweb.yars2.index.disk.block.NodeBlockInputStream;
import org.semanticweb.yars2.index.disk.block.NodeBlockOutputStream;
import org.semanticweb.yars2.reasoning.engine.ji.Reasoner;
import org.semanticweb.yars2.reasoning.model.AuthoritativeSources;
import org.semanticweb.yars2.reasoning.model.BufferTBoxIterator;
import org.semanticweb.yars2.reasoning.model.PersistantConceptIndex;
import org.semanticweb.yars2.reasoning.model.concepts.Class;
import org.semanticweb.yars2.sort.Sorter;

public class Validator {
	private String _document;
	private String _input;
	private String _index;
	private String _rindex;
	private String _redirects;
	private String _dir;
	private String _rdir;
	private String _cif;
	private String _sameas;

	private SPARQLClient _sparql;

	private ConceptIndex _ci;

	private boolean _note = true;
	private boolean _warning = true;
	private boolean _error = true;
	
	private boolean _crawl = false;
	
	//reasoning issues
	private Count<Nodes> _disjoints;
	private IssueCounter<Nodes> _ifps;
	
	//datatype/object property
	private Count<String> _datatypes;
	private IssueCounter<String> _bdts;
	
	//datatype/object property
	private IssueCounter<Node> _dpo;
	private IssueCounter<Node> _opd;
	
	//core non-standard use
	private IssueCounter<Node> _ps;
	private IssueCounter<Node> _po;
	private IssueCounter<Node> _dpoc;
	private IssueCounter<Node> _opdc;
	private IssueCounter<Node> _dp;
	private IssueCounter<Node> _pnd;
	
	private IssueCounter<Node> _cs;
	private IssueCounter<Node> _cp;
	private IssueCounter<Node> _cont;
	private IssueCounter<Node> _dc;
	private IssueCounter<Node> _cnd;
	
	private IssueCounter<Node> _on;
	
	public static final Node[] IFP_BLACKLIST = { 
		new Literal("08445a31a78661b5c746feff39a9db6e4e2cc5cf"),
		new Literal("da39a3ee5e6b4b0d3255bfef95601890afd80709"),
		new Literal(""),
		new Literal("N/A"),
		new Literal("n/a"),
		new Literal("none"),
		new Literal("no"),
		new Literal("n"),
		new Resource(""),
		new Resource("mailto:"),
		new Resource("http://")
	};
	
	private static HashSet<Node> IFP_BLACKLIST_HS = new HashSet<Node>();
	{
		for(Node n:IFP_BLACKLIST){
			IFP_BLACKLIST_HS.add(n);
		}
	}
	
	public static final Node[] RDF_P = { 
		RDF.FIRST, RDF.OBJECT, RDF.VALUE
	};
	
	public static final Node[] RDF_PO = { 
		RDF.TYPE, RDF.REST, RDF.SUBJECT, RDF.PREDICATE
	};
	
	public static final Node[] RDF_C = { 
		RDF.ALT, RDF.BAG, RDF.LIST, RDF.PROPERTY, RDF.RESOURCE, RDF.SEQ, RDF.STATEMENT, RDF.XMLLITERAL
	};
	
	public static final Node[] RDFS_P = { 
		RDFS.MEMBER
	};
	
	public static final Node[] RDFS_PO = { 
		RDFS.DOMAIN, RDFS.RANGE, RDFS.ISDEFINEDBY, RDFS.SEEALSO, RDFS.SUBCLASSOF, 
		RDFS.SUBPROPERTYOF
	};
	
	public static final Node[] RDFS_PD = {
		RDFS.COMMENT, RDFS.LABEL
	};
	
	public static final Node[] RDFS_C = { 
		RDFS.CLASS, RDFS.RESOURCE, RDFS.LITERAL, RDFS.DATATYPE, RDFS.CONTAINER, 
		RDFS.CONTAINERMEMBERSHIPPROPERTY
	};
	
	public static final Node[] OWL_P = { 
		OWL.HASVALUE
	};
	
	public static final Node[] OWL_PO = { 
		OWL.ALLVALUESFROM, OWL.BACKWARDCOMPATIBLEWITH, OWL.COMPLEMENTOF, OWL.DIFFERENTFROM,
		OWL.DISJOINTWITH, OWL.DISTINCTMEMBERS, OWL.EQUIVALENTCLASS,
		OWL.EQUIVALENTPROPERTY, OWL.HASVALUE, OWL.IMPORTS, OWL.INCOMPATIBLEWITH,
		OWL.INTERSECTIONOF, OWL.INVERSEOF, OWL.ONEOF, OWL.ONPROPERTY, OWL.PRIORVERSION,
		OWL.SAMEAS, OWL.SOMEVALUESFROM, OWL.UNIONOF
	};
	
	public static final Node[] OWL_PD = {
		OWL.CARDINALITY, OWL.MAXCARDINALITY, OWL.MINCARDINALITY, OWL.VERSIONINFO
	};
	
	public static final Node[] OWL_C = { 
		OWL.ALLDIFFERENT, OWL.ANNOTATIONPROPERTY, OWL.CLASS, OWL.DATARANGE,
		OWL.DATATYPEPROPERTY, OWL.DEPRECATEDCLASS, OWL.DEPRECATEDPROPERTY,
		OWL.FUNCTIONALPROPERTY, OWL.INVERSEFUNCTIONALPROPERTY, OWL.NOTHING,
		OWL.OBJECTPROPERTY, OWL.ONTOLOGYPROPERTY, OWL.RESTRICTION, OWL.SYMMETRICPROPERTY,
		OWL.THING, OWL.TRANSITIVEPROPERTY
	};
	
	private static HashSet<Node> CORE_P_HS = new HashSet<Node>();
	private static HashSet<Node> CORE_PD_HS = new HashSet<Node>();
	private static HashSet<Node> CORE_PO_HS = new HashSet<Node>();
	private static HashSet<Node> CORE_C_HS = new HashSet<Node>();
	{
		for(Node n:RDF_P){
			CORE_P_HS.add(n);
		}
		for(Node n:RDFS_P){
			CORE_P_HS.add(n);
		}
		for(Node n:OWL_P){
			CORE_P_HS.add(n);
		}
		
		for(Node n:RDFS_PD){
			CORE_PD_HS.add(n);
		}
		for(Node n:OWL_PD){
			CORE_PD_HS.add(n);
		}
		
		for(Node n:RDF_PO){
			CORE_PO_HS.add(n);
		}
		for(Node n:RDFS_PO){
			CORE_PO_HS.add(n);
		}
		for(Node n:OWL_PO){
			CORE_PO_HS.add(n);
		}
		
		for(Node n:RDF_C){
			CORE_C_HS.add(n);
		}
		for(Node n:RDFS_C){
			CORE_C_HS.add(n);
		}
		for(Node n:OWL_C){
			CORE_C_HS.add(n);
		}
	}

	private static final int WARN = 200;

	private static final String[] IFP_QUERY = 
	{ "SELECT DISTINCT ?s WHERE { ?s "," "," . } LIMIT "+(WARN+1) };
//	private static final String[] FP_QUERY = 
//	{ "SELECT DISTINCT ?s WHERE { "," "," ?o . } LIMIT"+(WARN+1) };

	public Validator(String input, String redirects) throws ParseException, IOException, ClassNotFoundException{
		_note = false;
		_warning = false;
		_error = false;
		
		_document = null;
		_sparql = null;
		
		_input = input;
		_redirects = redirects;
		
		_crawl = true;
		
		_datatypes = new Count<String>();
		_bdts = new IssueCounter<String>("DATATYPES WITH ILLEGAL VALUES");
		
		_disjoints = new Count<Nodes>();
		_ifps = new IssueCounter<Nodes>("BLACKLISTED IFPS");
		
		_dpo = new IssueCounter<Node>("DATATYPE PROP WITH NON LITERAL OBJ");
		_opd = new IssueCounter<Node>("OBJECT PROP WITH LITERAL OBJ");
		
		//core non-standard use
		_ps = new IssueCounter<Node>("CORE PROP IN SUBJ");
		_po = new IssueCounter<Node>("CORE PROP IN OBJ");
		_dpoc = new IssueCounter<Node>("CORE DATATYPE PROP WITH NON LITERAL OBJ");
		_opdc = new IssueCounter<Node>("CORE OBJECT PROP WITH LITERAL OBJ");
		_dp = new IssueCounter<Node>("DEPRECATED PROP MEMBERS");
		_pnd = new IssueCounter<Node>("PROP MEMBER NOT DEFINED");
		
		_cs = new IssueCounter<Node>("CORE CLASS IN SUBJ");
		_cp = new IssueCounter<Node>("CORE CLASS IN PRED");
		_cont = new IssueCounter<Node>("CORE CLASS IN OBJ OF NON-TYPE");
		_dc = new IssueCounter<Node>("DEPRECATED CLASS MEMBERS");
		_cnd = new IssueCounter<Node>("CLASS MEMBER NOT DEFINED");
		
		_on = new IssueCounter<Node>("OWL NOTHING MEMBERS");
		
		setupDirectories();
		reason(input);
	}
	
	public Validator(String document, String input, String sparql, String redirects) throws IOException, ParseException, ClassNotFoundException{
		this(document, input, sparql, redirects, true, true, true);
	}

	public Validator(String document, String input, String sparql, String redirects , boolean note, boolean warning, boolean error) throws IOException, ParseException, ClassNotFoundException{
		_note = note;
		_warning = warning;
		_error = error;

		_document = document;
		_sparql = new SPARQLClient(sparql);
		
		_input = input;
		_redirects = redirects;

		setupDirectories();
		
		reason(input);
	}
	
	private void setupDirectories(){
		_dir = Yars2.initTempFolder();
		_rdir = _dir+"r/";
		_index = _dir+"spoc.idx";
		_rindex = _dir+"spoc_r.idx";
		_cif = _dir+"concepts.ci";
	}
	
	private void reason(String input) throws ParseException, IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(input);

		NodeBlockInputStream._DEFAULT_PARSE_DTS = false;
		
		NxParser nxp = new NxParser(fis, false,false);
		NodeBlockOutputStream nbos = new NodeBlockOutputStream(_index);
		Sorter.sort(_dir, nxp, nbos);
		nbos.close();

		NodeBlockInputStream nbis = new NodeBlockInputStream(_index);
		QuadStringScanIterator qssi = new QuadStringScanIterator(nbis);
		_ci = loadTBox(qssi);
		nbis.close();
		ConceptIndex.serialise(_ci, _cif);
		PersistantConceptIndex pci = new PersistantConceptIndex(_cif, _ci);
		Reasoner r = new Reasoner(_rdir, pci);
		r.skipConsolidation();
		r.performReasoning(_index, true);
		String reasoned = r.getUnconsolidatedOutput();
		_sameas = r.getSameAsIndex();

		NodeBlockInputStream nbisr = new NodeBlockInputStream(reasoned);
		nbos = new NodeBlockOutputStream(_rdir+"inferred.idx");

		Sorter.sort(_dir, nbisr, nbos);
		nbos.close();

		nbisr = new NodeBlockInputStream(nbos.toString());
		nbis = new NodeBlockInputStream(_index);
		nbos = new NodeBlockOutputStream(_rindex);
		NodeBlockInputStream nbiss[] = {nbis, nbisr};
		Sorter.merge(nbiss, nbos);
		nbos.close();
		nbisr.close();
		nbis.close();
	}

	public void validate() throws IOException, ParseException, ClassNotFoundException{
		NodeBlockInputStream nbis = new NodeBlockInputStream(_rindex);
		QuadStringScanIterator qssi = new QuadStringScanIterator(nbis);

		Node old = null;
		Node[] next = null;
		TreeSet<MoreClass> types = new TreeSet<MoreClass>();

		boolean core = false;
		boolean done = !qssi.hasNext();
		while(!done){
			if(qssi.hasNext())
				next = qssi.next();
			else
				done = true;
			
			core = false;
			for(String ns:AuthoritativeSources.PROTECTED_NS){
				if(next[3].toString().startsWith(ns)){
					core = true;
				}
			}

			if(old == null){
				old = next[0];
			} else if(done || !old.equals(next[0])){
				TreeSet<Node[]> disjs = findDisjointPairs(types);
				for(Node[] disj:disjs){
					if(_error) System.err.println("ERROR: *resource "+old+" instance of disjoint classes "+disj[0]+" "+disj[1]);
					else if(_crawl) _disjoints.add(new Nodes(disj));
				}
				types = new TreeSet<MoreClass>();
				old = next[0];
			}

			if(!done){
				if(next[2] instanceof Literal){
					Literal l = (Literal)next[2];
					Resource dt = l.getDatatype();
					if(dt!=null){
						_datatypes.add(dt.toString());
						try{
							l.getDatatypeObject();
						} catch(DatatypeParseException dpe){
							_bdts.add(dt.toString(), next[3]);
						}
					} else {
						_datatypes.add("null");
					}
				}
				
				if(!core && (CORE_P_HS.contains(next[0]) || CORE_PO_HS.contains(next[0]))){
					if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *nonstandard use of core property: "+next[0]+" in subject position of a triple");
					else if(_crawl){ 
						_ps.add(next[0], next[3]); 
					}
				} else if(!core && CORE_C_HS.contains(next[0])){
					if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *nonstandard use of core class: "+next[0]+" in subject position of a triple");
					else if(_crawl){
						_cs.add(next[0], next[3]); 
					}
				}
				
				if(!core && (CORE_P_HS.contains(next[2]) || CORE_PO_HS.contains(next[2]))){
					if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *nonstandard use of core property: "+next[2]+" in object position of a triple");
					else if(_crawl){
						_po.add(next[2], next[3]);
					}
				}
				
				if(next[1].equals(RDF.TYPE)){
					MoreClass c = _ci.getClass(next[2]);
					if(c!=null){
						types.add(c);
						if(c.isDeprecated()){
							if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *instance of deprecated class "+next[2]);
							else if(_crawl){
								_dc.add(next[2], next[3]);
							}
						}
					} else{
						if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *could not find a definition for Class "+next[2]);
						else if(_crawl){
							_cnd.add(next[2], next[3]);
						}
					}
					if(next[2].equals(OWL.NOTHING)){
						if(_error) System.err.println("ERROR: "+Nodes.toN3(next)+" *instance of owl:Nothing found");
						else if(_crawl){
							_on.add(next[0], next[3]);
						}
					}
				} else{
					MoreProperty p = _ci.getProperty(next[1]);
					if(p!=null){
						if(p.isDeprecated()){
							if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *instance of deprecated property "+next[1]);
							else if(_crawl) _dp.add(next[1], next[3]);
						} else if(p.isObject() && (next[2] instanceof Literal)){
							if(_error) System.err.println("ERROR: "+Nodes.toN3(next)+" *instance of owl:ObjectProperty "+next[1]+" used with literal value "+next[2]);
							else if(_crawl) _opd.add(next[1], next[3]);
						} else if(p.isDatatype() && !(next[2] instanceof Literal)){
							if(_error) System.err.println("ERROR: "+Nodes.toN3(next)+" *instance of owl:DatatypeProperty "+next[1]+" used with non-literal value "+next[2]);
							else if(_crawl) _dpo.add(next[1], next[3]);
						} else if(p.isInverseFunctional()){
							Node[] ifp = {next[1], next[2]};
							if(IFP_BLACKLIST_HS.contains(ifp[1])){
								if (_error) System.err.println("ERROR: "+Nodes.toN3(next)+" *blacklisted value "+ifp[1]+" used for InverseFunctionalProperty "+ifp[0]);
								else if(_crawl) _ifps.add(new Nodes(ifp), next[3]);
							}
						} 
					} else{
						if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *could not find a definition for Property "+next[1]);
						else if(_crawl) _pnd.add(next[1], next[3]);
					}
					if(!core && CORE_C_HS.contains(next[1])){
						if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *nonstandard use of core class: "+next[1]+" in predicate position of a triple");
						else if(_crawl) _cp.add(next[1], next[3]);
					} else if(CORE_PO_HS.contains(next[1]) && (next[2] instanceof Literal)){
						if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *use of core object property: "+next[1]+" in triple with literal object");
						else if(_crawl) _opdc.add(next[1], next[3]);
					} else if(CORE_PD_HS.contains(next[1]) && !(next[2] instanceof Literal)){
						if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *use of core datatype property: "+next[1]+" in triple with non-literal object");
						else if(_crawl) _dpoc.add(next[1], next[3]);
					} 
					
					if(!core && CORE_C_HS.contains(next[2])){
						if(_warning) System.err.println("WARNING: "+Nodes.toN3(next)+" *nonstandard use of core class: "+next[2]+" in object position of a non-rdf:type triple");
						else if(_crawl) _cont.add(next[2], next[3]);
					}
				}
			}
		}
		
		if(_crawl){
			System.out.println("DATATYPE DISTRIBUTION");
			_datatypes.printOrderedStats();
			System.out.println();
			
			_bdts.printStats();
			
			System.out.println("DISJOINT CLASSES");
			_disjoints.printOrderedStats();
			System.out.println();
			
			_ifps.printStats();
			
			_dpo.printStats();
			_opd.printStats();
			
			//core non-standard use
			_ps.printStats();
			_po.printStats();
			_dpoc.printStats();
			_opdc.printStats();
			_dp.printStats();
//			_pnd.printStats();
			
			_cs.printStats();
			_cp.printStats();
			_cont.printStats();
			_dc.printStats();
//			_cnd.printStats();
			
			_on.printStats();
		}
	}

	private TreeSet<Node[]> findDisjointPairs(TreeSet<MoreClass> classes){
		TreeSet<Node[]> disjs = new TreeSet<Node[]>(NodeComparator.NC);
		for(MoreClass c:classes){
			TreeSet<Class> ds = c.getDisjointClasses();
			if (ds!=null) for(Class d:ds){
				if(classes.contains(d)){
					Node[] disj = new Node[2];
					Node cu = c.getURI();
					Node du = d.getURI();
					if(cu.compareTo(du)>0){
						disj[0] = cu;
						disj[1] = du;
					} else{
						disj[0] = du;
						disj[1] = cu;
					}
					disjs.add(disj);
				}
			}
		}
		return disjs;
	}

	public void identity() throws IOException, ParseException{
		NodeBlockInputStream nbis = null;
		QuadStringScanIterator qssi = null;

		Node old = null;
		Node[] next = null;
		boolean done = false;

		if(_note){
			nbis = new NodeBlockInputStream(_sameas);
			qssi = new QuadStringScanIterator(nbis);
			done = !qssi.hasNext();

			TreeSet<Node> equivalent = new TreeSet<Node>();
			while(!done){
				if(qssi.hasNext())
					next = qssi.next();
				else
					done = true;

				if(old == null){
					old = next[0];
					equivalent.add(old);
				} else if(done || !old.equals(next[0])){
					old = next[0];
					if(equivalent.size()>1){
						if(_note) System.err.print("NOTE: *found the following local resources to be equivalent:");
						for(Node n:equivalent){
							System.err.print(" "+n.toN3());
						}
						System.err.println();
					}
					equivalent = new TreeSet<Node>();
					equivalent.add(next[0]);
				}

				if(!done){
					if(next[0].compareTo(next[2])<0)
						equivalent.add(next[2]);
				}
			}
		}
		
		nbis = new NodeBlockInputStream(_rindex);
		qssi = new QuadStringScanIterator(nbis);

		old = null;
		next = null;
		TreeSet<Node[]> ifps = new TreeSet<Node[]>(NodeComparator.NC);
		//TreeSet<Node[]> fps = new TreeSet<Node[]>(NodeComparator.NC);

		done = !qssi.hasNext();
		while(!done){
			if(qssi.hasNext())
				next = qssi.next();
			else
				done = true;

			if(old == null){
				old = next[0];
			} else if(done || !old.equals(next[0])){
				if(_note||_warning){
					for(Node[] ifp:ifps){
						String query = constructQuery(IFP_QUERY, ifp);
						ResultsCollector rc = new ResultsCollector();
						_sparql.query(query, rc);
	
						if(rc.getResults().size()>WARN){
							if(_warning) System.err.println("WARNING: *over "+WARN+" equivalent instances found from the Web for "+old+" through value "+ ifp[1]+" on owl:InverseFunctionalProperty "+ifp[0]+"... Is this value valid?");
						}
						else for(Node[] ans:rc.getResults()){
							if((ans[0] instanceof Resource) && !ans[0].equals(old)){
								if(_note) System.err.println("NOTE: *"+ans[0]+" found to be the same as "+old+" from the Web through value "+ ifp[1]+" for owl:InverseFunctionalProperty "+ifp[0]);
							}
						}
					}
				}
				old = next[0];
				ifps = new TreeSet<Node[]>(NodeComparator.NC);
				//fps = new TreeSet<Node[]>(NodeComparator.NC);
			}

			if(!done){
				if(next[1].equals(RDF.TYPE)){
					continue;
				} else{
					MoreProperty p = _ci.getProperty(next[1]);
					if(p!=null){
						if(p.isInverseFunctional()){
							Node[] ifp = {next[1], next[2]};
							if(IFP_BLACKLIST_HS.contains(ifp[1])){
								;
							}else ifps.add(ifp);
						} 
//						else if(p.isFunctional()){
//						Node[] fp = {next[0], next[1]};
//						fps.add(fp);
//						} 
//						else if(p.getCardinalities()!=null && p.getCardinalities().size()>0 && !(next[2] instanceof Literal)){

//						}
					} 
				}
			}
		}
	}

	private String constructQuery(String[] qFrags, Node[] cons){
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<qFrags.length; i++){
			buf.append(qFrags[i]);
			if(cons.length>i){
				buf.append(cons[i].toN3());
			}
		}
		return buf.toString();
	}

	private ConceptIndex loadTBox(Iterator<Node[]> iter) throws ParseException, IOException{
		TreeSet<Resource> classes = new TreeSet<Resource>();
		for(Resource c:BufferTBoxIterator.DEFAULT_CLASSES){
			classes.add(c);
		}
		classes.add(OWL.DEPRECATEDCLASS);
		classes.add(OWL.DEPRECATEDPROPERTY);
		classes.add(OWL.OBJECTPROPERTY);
		classes.add(OWL.DATATYPEPROPERTY);
		
		TreeSet<Resource> preds = new TreeSet<Resource>();
		for(Resource p:BufferTBoxIterator.DEFAULT_PROPERTIES){
			preds.add(p);
		}
		preds.add(OWL.COMPLEMENTOF);
		preds.add(OWL.DISJOINTWITH);
		
		BufferTBoxIterator bti = new BufferTBoxIterator(iter, preds, classes);
		AuthorityIterator ai = new AuthorityIterator(bti);

		return ConceptIndexFactory.buildConceptIndex(ai);
	}

	public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException{
		Validator v = new Validator("http://blah.com", "test/alerts/aidan2ns/data-all.nq", "http://swse.deri.org/yars2/", "test/alerts/aidan2ns/redirects-all.nq", true, true, true);
		v.validate();
		v.identity();
	}
}
