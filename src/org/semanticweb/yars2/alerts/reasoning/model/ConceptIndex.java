package org.semanticweb.yars2.alerts.reasoning.model;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars2.alerts.reasoning.model.concepts.MoreClass;
import org.semanticweb.yars2.alerts.reasoning.model.concepts.MoreProperty;
import org.semanticweb.yars2.reasoning.model.concepts.Individual;
import org.semanticweb.yars2.reasoning.model.concepts.List;


/**
 * ConceptIndex represents the TBox in memory
 * @author aidhog
 */

public class ConceptIndex extends org.semanticweb.yars2.reasoning.model.ConceptIndex{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String CONCEPT_INDEX_FILENAME = "concepts.ci";
	
	private HashMap<Node,MoreClass> _classLT;
	private HashMap<Node,MoreProperty> _propLT;
	private HashMap<Node,Individual> _indivLT;
	
	private transient HashMap<Node,List> _collectionHead;
	private transient HashMap<Node,List> _collectionTail;
	
	public ConceptIndex(){
		_classLT = new HashMap<Node,MoreClass>();
		_propLT = new HashMap<Node,MoreProperty>();
		_indivLT = new HashMap<Node,Individual>();
		
		_collectionHead = new HashMap<Node,List>();
		_collectionTail = new HashMap<Node,List>();
	}
	
	//not fully tolerant to all possible uses on the web, such as 
	//cyclic rdf:first definitions, reuse of collection segments, etc.
	//tolerant to some stuff, e.g., collection must end with rdf:nil
	protected void addCollectionSegment(Node head, Node value, boolean auth, Node tail){
		if(value == null){
			_collectionTail.remove(head);
			if(tail!=null){
				_collectionHead.remove(tail);
			}
			return;
		} else if(tail==null){
			_collectionTail.remove(head);
			return;
		} 
		
		List next = null;
		boolean rdfnil = tail.equals(RDF.NIL);
		
		if(!rdfnil){
			next = _collectionHead.get(tail);
		}
		List prev = _collectionTail.get(head);
		
		List c = null;

		if(prev!=null){
			c = new List(prev.getURI());
			c.addAllValues(prev.getValues());
			c.addValue(value, auth);
			if(prev.getTailNode()!=null)
				_collectionTail.remove(prev.getTailNode());
		} else{
			c = new List(head);
			if(value!=null)
				c.addValue(value, auth);
		}
		
		if(rdfnil){
			c.setTailNode(RDF.NIL);
		} else if(next!=null){
			c.setTailNode(next.getTailNode());
			c.addAllValues(next.getValues());
			_collectionHead.remove(next.getURI());
		} else{
			c.setTailNode(tail);
		}
		
		_collectionHead.put(c.getURI(), c);
		if(!rdfnil && c.getTailNode()!=null)
			_collectionTail.put(c.getTailNode(), c);
	}
	
	protected boolean removeCollectionSegment(String uri){
		boolean rhead = (_collectionHead.remove(uri)!=null);
		boolean rtail = (_collectionTail.remove(uri)!=null);
		
		return rhead || rtail;
	}
	
	protected java.util.Collection<List> getCollections(){
		return _collectionHead.values();
	}
	
	protected List getCollection(Node headuri){
		return _collectionHead.get(headuri);
	}
	
	protected void addClass(MoreClass c){
		_classLT.put(c.getURI(), c);
	}
	
	public MoreClass getClass(Node curi){
		return _classLT.get(curi);
	}
	
	public java.util.Collection<Node> getClassKeys(){
		return _classLT.keySet();
	}
	
	public java.util.Collection<Node> getPropertyKeys(){
		return _propLT.keySet();
	}
	
	protected MoreClass getOrCreateClass(Node cnode){
		MoreClass c= _classLT.get(cnode);
		if(c==null){
			c = new MoreClass(cnode);
			_classLT.put(cnode, c);
		}
		return c;
	}
	
	protected void addProperty(MoreProperty p){
		_propLT.put(p.getURI(), p);
	}
	
	public MoreProperty getProperty(Node puri){
		return _propLT.get(puri);
	}
	
	protected MoreProperty getOrCreateProperty(Node puri){
		MoreProperty p= _propLT.get(puri);
		if(p==null){
			p = new MoreProperty(puri);
			_propLT.put(puri, p);
		}
		return p;
	}
	
	public java.util.Collection<Node> getIndividualKeys(){
		return _indivLT.keySet();
	}
	
	protected void addIndividual(Individual v){
		_indivLT.put(v.getURI(), v);
	}
	
	public Individual getIndividual(Node iuri){
		return _indivLT.get(iuri);
	}
	
	
	protected Individual getOrCreateIndividual(Node iuri){
		Individual i= _indivLT.get(iuri);
		if(i==null){
			i = new Individual(iuri);
			_indivLT.put(iuri, i);
		}
		return i;
	}
	
	/**
	 * Removes collections after factory consolidates to free up mem
	 * Not needed post factory creation.
	 */
	protected void removeCollections(){
		_collectionHead = null;
		_collectionTail = null;
	}
	
	/**
	 * Used to serialise a given ConceptIndex object to a file
	 * @param a ConceptIndex object to serialise
	 * @param destination filename
	 * @throws IOException 
	 */
	public static void serialise(ConceptIndex ci, String file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ci);
		fos.close();
		oos.close();
	}
	
	/**
	 * Used to deserialise and return a ConceptIndex object from a file
	 * @param a file containing a serialised ConceptIndex object
	 * @param the ConceptIndex object
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static ConceptIndex deserialise(String file) throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		ConceptIndex ci = (ConceptIndex)ois.readObject();
		return ci;
	}
	
//	private void updateSuperClassInheritance(Class c, TreeSet<Integer> done){
//		done.add(_classLT.findConcept(c));
//		TreeSet<Integer> scs = c.getSuperClasses();
//		//avoid concurrent modification
//		TreeSet<Integer> copy = new TreeSet<Integer>();
//		copy.addAll(scs);
//		for(Integer i:copy){
//			Class sc = _classLT.getConcept(i);
//			if(!done.contains(i))
//				updateSuperClassInheritance(sc, done);
//			c.addAllSuperClass(sc.getEquivalentClasses());
//			c.addAllSuperClass(sc.getSuperClasses());	
//		}
//		
//		scs = c.getEquivalentClasses();
//		copy = new TreeSet<Integer>();
//		copy.addAll(scs);
//		for(Integer i:copy){
//			Class sc = _classLT.getConcept(i);
//			if(!done.contains(i))
//				updateSuperClassInheritance(sc, done);
//			c.addAllSuperClass(sc.getSuperClasses());	
//		}
//		_classLT.putConcept(c);
//	}
//	
//	private void updateSubClassInheritance(Class c, TreeSet<Integer> done){
//		done.add(_classLT.findConcept(c));
//		TreeSet<Integer> scs = c.getSubClasses();
//		//avoid concurrent modification
//		TreeSet<Integer> copy = new TreeSet<Integer>();
//		copy.addAll(scs);
//		for(Integer i:copy){
//			Class sc = _classLT.getConcept(i);
//			if(!done.contains(i))
//				updateSubClassInheritance(sc, done);
//			c.addAllSubClass(sc.getEquivalentClasses());
//			c.addAllSubClass(sc.getSubClasses());
//		}
//		
//		scs = c.getEquivalentClasses();
//		copy = new TreeSet<Integer>();
//		copy.addAll(scs);
//		for(Integer i:copy){
//			Class sc = _classLT.getConcept(i);
//			if(!done.contains(i))
//				updateSubClassInheritance(sc, done);
//			c.addAllSubClass(sc.getSubClasses());
//		}
//		_classLT.putConcept(c);
//	}
//	
//	private void updateSubPropertyInheritance(Property p, TreeSet<Integer> done){
//		done.add(_propLT.findConcept(p));
//		TreeSet<Integer> sps = p.getSubProperties();
////		avoid concurrent modification
//		TreeSet<Integer> copy = new TreeSet<Integer>();
//		copy.addAll(sps);
//		for(Integer i:copy){
//			Property sp = _propLT.getConcept(i);
//			if(!done.contains(i))
//				updateSubPropertyInheritance(sp, done);
//			p.addAllSubProperty(sp.getEquivalentProperties());
//			p.addAllSubProperty(sp.getSubProperties());
//		}
//		
//		sps = p.getEquivalentProperties();
//		copy = new TreeSet<Integer>();
//		copy.addAll(sps);
//		for(Integer i:copy){
//			Property sp = _propLT.getConcept(i);
//			if(!done.contains(i))
//				updateSubPropertyInheritance(sp, done);
//			p.addAllSubProperty(sp.getSubProperties());
//		}
//		_propLT.putConcept(p);
//	}
//	
//	private void updateSuperPropertyInheritance(Property p, TreeSet<Integer> done){
//		done.add(_propLT.findConcept(p));
//		TreeSet<Integer> sps = p.getSuperProperties();
//		TreeSet<Integer> copy = new TreeSet<Integer>();
//		copy.addAll(sps);
//		for(Integer i:copy){
//			Property sp = _propLT.getConcept(i);
//			if(!done.contains(i))
//				updateSuperPropertyInheritance(sp, done);
//			p.addAllSuperProperty(sp.getEquivalentProperties());
//			p.addAllSuperProperty(sp.getSuperProperties());
//		}
//		
//		sps = p.getEquivalentProperties();
//		copy = new TreeSet<Integer>();
//		copy.addAll(sps);
//		for(Integer i:sps){
//			Property sp = _propLT.getConcept(i);
//			if(!done.contains(i))
//				updateSuperPropertyInheritance(sp, done);
//			p.addAllSuperProperty(sp.getSuperProperties());
//		}
//		_propLT.putConcept(p);
//	}
//	
//	public void updateHierarchy(){
//		TreeSet<Integer> done = new TreeSet<Integer>();
//		for(int i=0; i<_classLT.size(); i++)
//			if(!done.contains(i))
//				updateSuperClassInheritance(_classLT.getConcept(i),done);
//		
//		done = new TreeSet<Integer>();
//		for(int i=0; i<_classLT.size(); i++)
//			if(!done.contains(i))
//				updateSubClassInheritance(_classLT.getConcept(i),done);
//		
//		done = new TreeSet<Integer>();
//		for(int i=0; i<_propLT.size(); i++)
//			if(!done.contains(i))
//				updateSuperPropertyInheritance(_propLT.getConcept(i),done);
//		
//		done = new TreeSet<Integer>();
//		for(int i=0; i<_propLT.size(); i++)
//			if(!done.contains(i))
//				updateSubPropertyInheritance(_propLT.getConcept(i),done);
//	}
//	
//	public void addDomain(String puri, TreeSet<String> curis){
//		int pi = _propLT.findConcept(new Property(puri));
//		if(pi<0)
//			pi = _propLT.addConcept(new Property(puri));
//		
//		Property p = _propLT.getConcept(pi);
//		
//		for(String curi : curis){
//			int ci = _classLT.findConcept(new Class(curi));
//			if(ci<0)
//				ci = _classLT.addConcept(new Class(curi));
//			Class c = (Class)_classLT.getConcept(ci);
//			p.addDomain(ci);
//			c.addInDomainOf(pi);
//			_classLT.putConcept(c);
//		}
//		
//		_propLT.putConcept(p);
//	}
//	
//	public void addRange(String puri, TreeSet<String> curis){
//		int pi = _propLT.findConcept(new Property(puri));
//		if(pi<0){
//			pi = _propLT.addConcept(new Property(puri));
//		}
//		Property p = (Property)_propLT.getConcept(pi);
//
//		for(String curi : curis){
//			int ci = _classLT.findConcept(new Class(curi));
//			if(ci<0)
//				ci = _classLT.addConcept(new Class(curi));
//			
//			Class c = (Class)_classLT.getConcept(ci);
//			p.addRange(ci);
//			c.addInRangeOf(pi);
//			_classLT.putConcept(c);
//			
//			if(curi.equals(RDFSLITERAL)) {
//				p.flagDatatype();
//			}
//			else if(curi.equals(RDFSRESOURCE)) {
//				p.flagObject();
//			}
//		}
//		
//		_propLT.putConcept(p);
//	}
//
//	public void addInverseOf(String puri, TreeSet<String> invuris){
//		int pi = _propLT.findConcept(new Property(puri));
//		if(pi<0){
//			pi = _propLT.addConcept(new Property(puri));
//		}
//		Property p = _propLT.getConcept(pi);
//		
//		for(String invuri : invuris){
//			int invpi = _propLT.findConcept(new Property(invuri));
//			if(invpi<0){
//				invpi =  _propLT.addConcept(new Property(invuri));
//			}
////			Property invp = (Property)_propLT.getConcept(pi);
//			p.addInverseOf(invpi);
////			inverseOf not transitive			
////			invp.addInverseOf(pi);
////			_propLT.putConcept(invp);
//		}
//		
//		_propLT.putConcept(p);
//	}
//	
//	public void addSubclasses(String curi, TreeSet<String> curis){
//		int cin = _classLT.findConcept(new Class(curi));
//		if(cin<0){
//			cin = _classLT.addConcept(new Class(curi));
//		}
//		
//		Class c = _classLT.getConcept(cin);
//		TreeSet<Integer> indices = new TreeSet<Integer>();
//		
//		for(String uri : curis){
//			int ci = _classLT.findConcept(new Class(uri));
//			if(ci<0)
//				ci = _classLT.addConcept(new Class(uri));
//			
//			indices.add(new Integer(ci));
//			
//			Class superc = _classLT.getConcept(ci);
//			superc.addSubClass(new Integer(cin));
//			putClass(superc);
//		}
//		
//		if(indices.size()>0)
//			c.addAllSuperClass(indices);
//		
//		putClass(c);
//		//if(indices.size()>0)
//		//	_classH.addChildParents(new Integer(cin), indices);
//	}
//	
//	public void addSubproperties(String puri, TreeSet<String>puris){
//		int pin = _propLT.findConcept(new Property(puri));
//
//		if(pin<0){
//			pin = _propLT.addConcept(new Property(puri));
//		}
//		
//		Property p = _propLT.getConcept(pin);
//		TreeSet<Integer> indices = new TreeSet<Integer>();
//		for(String uri : puris){
//			int pi = _propLT.findConcept(new Property(uri));
//			if(pi<0)
//				pi = _propLT.addConcept(new Property(uri));
//			
//			indices.add(new Integer(pi));
//			
//			Property superp = _propLT.getConcept(pi);
//			
//			superp.addSubProperty(new Integer(pin));
//			putProperty(superp);
//		}
//		
//		if(indices.size()>0)
//			p.addAllSuperProperty(indices);
//		putProperty(p);
//		//if(indices.size()>0)
//		//	_propH.addChildParents(new Integer(pin), indices);
//	}
//	
//	//@@@todo: faster solutions possible
//	public void addEquivalentProperties(TreeSet<Node>props){
//		TreeSet<Node> copy = new TreeSet<Node>();
//		copy.addAll(props);
//		
//		for(Node n:copy){
//			Property p1 = new Property(n.toString());
//			int pi1 = _propLT.findConcept(p1);
//			if(pi1<0)
//				pi1 = _propLT.addConcept(p1);
//			
//			for(Node m:props){
//				if(!n.equals(m)){
//					Property p2 = new Property(m.toString());
//					int pi2 = _propLT.findConcept(p2);
//					if(pi2<0)
//						pi2 = _propLT.addConcept(p2);
//					
//					p1.addEquivalentProperty(pi2);
//					p2.addEquivalentProperty(pi1);
//					
//					_propLT.putConcept(p2);
//				}
//				_propLT.putConcept(p1);
//			}
//		}
//	}
//	
////	@@@todo: faster solutions possible
//	public void addEquivalentClasses(TreeSet<Node> classes){
//		TreeSet<Node> copy = new TreeSet<Node>();
//		copy.addAll(classes);
//		
//		for(Node n:copy){
//			Class c1 = new Class(n.toString());
//			int ci1 = _classLT.findConcept(c1);
//			if(ci1<0)
//				ci1 = _classLT.addConcept(c1);
//			for(Node m:classes){
//				if(!n.equals(m)){
//					Class c2 = new Class(m.toString());
//					int ci2 = _classLT.findConcept(c2);
//					if(ci2<0)
//						ci2 = _classLT.addConcept(c2);
//					
//					c1.addEquivalentClass(ci2);
//					c2.addEquivalentClass(ci1);
//					
//					
//					_classLT.putConcept(c2);
//				}
//				_classLT.putConcept(c1);
//			}
//		}
//	}
//	
//	public ArrayList<Class> getSuperClasses(String curi){
//		return getSuperClasses(_classLT.getConcept(new Class(curi)));
//	}
//	
//	public ArrayList<Class> getSuperClasses(Class c){
//		TreeSet<Integer> supers = c.getSuperClasses();
//		return groupClassLookups(supers);
//	}
//	
//	public ArrayList<Class> getSubClasses(String curi){
//		return getSubClasses(_classLT.getConcept(new Class(curi)));
//	}
//	
//	public ArrayList<Class> getSubClasses(Class c){
//		TreeSet<Integer> subs = c.getSubClasses();
//		return groupClassLookups(subs);
//	}
//	
//	public ArrayList<Class> getEquivalentClasses(String curi){
//		return getEquivalentClasses(_classLT.getConcept(new Class(curi)));
//	}
//	
//	public ArrayList<Class> getEquivalentClasses(Class c){
//		TreeSet<Integer> subs = c.getEquivalentClasses();
//		return groupClassLookups(subs);
//	}
//	
//	public ArrayList<Property> getSuperProperties(String puri){
//		return getSuperProperties(_propLT.getConcept(new Property(puri)));
//	}
//	
//	public ArrayList<Property> getSuperProperties(Property p){
//		TreeSet<Integer> supers = p.getSuperProperties();
//		return groupPropertyLookups(supers);
//	}
//	
//	public ArrayList<Property> getSubProperties(String puri){
//		return getSubProperties(_propLT.getConcept(new Property(puri)));
//	}
//	
//	public ArrayList<Property> getSubProperties(Property p){
//		TreeSet<Integer> subs = p.getSubProperties();
//		return groupPropertyLookups(subs);
//	}
//	
//	public ArrayList<Property> getEquivalentProperties(String puri){
//		return getEquivalentProperties(_propLT.getConcept(new Property(puri)));
//	}
//	
//	public ArrayList<Property> getEquivalentProperties(Property p){
//		TreeSet<Integer> equivs = p.getEquivalentProperties();
//		return groupPropertyLookups(equivs);
//	}
//	
//	public ArrayList<Property> getInverseProperties(String puri){
//		return getInverseProperties(_propLT.getConcept(new Property(puri)));
//	}
//	
//	public ArrayList<Property> getInverseProperties(Property p){
//		TreeSet<Integer> invs = p.getInversesOf();
//		return groupPropertyLookups(invs);
//	}
//	
//	private ArrayList<Class> groupClassLookups(Collection<Integer> indices){
//		ArrayList<Class> results = new ArrayList<Class>();
//		for(Integer index : indices){
//			results.add(_classLT.getConcept(index.intValue()));
//		}
//		return results;
//	}
//	
//	private ArrayList<Property> groupPropertyLookups(Collection<Integer> indices){
//		ArrayList<Property> results = new ArrayList<Property>();
//		for(Integer index : indices){
//			results.add(_propLT.getConcept(index.intValue()));
//		}
//		return results;
//	}
//	
//	public String toString(){
//		StringBuffer buf = new StringBuffer();
//		buf.append("Classes :\n-------------------\n");
//		for(Class c:_classLT._indexToC){
//			buf.append(toString(c)+"\n\n");
//		}
//		
//		buf.append("\n\nProperties :\n-------------------\n");
//		for(Property p:_propLT._indexToC){
//			buf.append(toString(p)+"\n\n");
//		}
//		
//		return buf.toString();
//	}
//	
//	private String toString(Class cl){
//		StringBuffer buf = new StringBuffer();
//		buf.append(cl.toString());
//		
//		TreeSet<Class> cs = cl.getSubClasses();
//		buf.append("\n--Subclasses--\n");
//		for(Class c:cs){
//			buf.append("\t"+c.toString());
//		}
//		
//		cs = cl.getSuperClasses();
//		buf.append("\n--Superclasses--\n");
//		for(Class c:cs){
//			buf.append("\t"+c.toString());
//		}
//		
//		cs = cl.getEquivalentClasses();
//		buf.append("\n--Equivalent Classes--\n");
//		for(Class c:cs){
//			buf.append("\t"+c.toString());
//		}
//		
//		Collection<Property> ps = cl.getInDomainOf();
//		buf.append("\n--In Domain Of--\n");
//		for(Property p:ps){
//			buf.append("\t"+p.toString());
//		}
//		
//		ps = cl.getInRangeOf();
//		buf.append("\n--In Range Of--\n");
//		for(Property p:ps){
//			buf.append("\t"+p.toString());
//		}
//		
//		buf.append("\n");
//		
//		return buf.toString();
//	}
//	
//	private String toString(Property pr){
//		StringBuffer buf = new StringBuffer();
//		buf.append(pr.toString());
//		buf.append("Inverse Functional :"+pr.isInverseFunctional()+"\n");
//		buf.append("Functional :"+pr.isFunctional()+"\n");
//		buf.append("Object :"+pr.isObject()+"\n");
//		buf.append("Datatype :"+pr.isDatatype()+"\n");
//		buf.append("Symmetric :"+pr.isSymmetric()+"\n");
//		buf.append("Transitive :"+pr.isTransitive()+"\n");
//		
//		Collection<Property> ps = pr.getEquivalentProperties();
//		buf.append("\n--Equivalent Properties--\n");
//		for(Property p:ps){
//			buf.append("\t"+p.toString());
//		}
//		
//		ps = pr.getInversesOf();
//		buf.append("\n--Inverse Of--\n");
//		for(Property p:ps){
//			buf.append("\t"+p.toString());
//		}
//		
//		ps = pr.getSubProperties();
//		buf.append("\n--Subproperties--\n");
//		for(Property p:ps){
//			buf.append("\t"+p.toString());
//		}
//		
//		ps = pr.getSuperProperties();
//		buf.append("\n--Superproperties--\n");
//		for(Property p:ps){
//			buf.append("\t"+p.toString());
//		}
//		
//		Collection<Class> cs = pr.getDomains();
//		buf.append("\n--Domains--\n");
//		for(Class c:cs){
//			buf.append("\t"+c.toString());
//		}
//		
//		cs = pr.getRanges();
//		buf.append("\n--Ranges--\n");
//		for(Class c:cs){
//			buf.append("\t"+c.toString());
//		}
//		
//		buf.append("\n");
//		
//		return buf.toString();
//	}
}
