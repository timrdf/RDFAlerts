package org.semanticweb.yars2.alerts.reasoning.model;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars2.reasoning.engine.ji.Reasoner;
import org.semanticweb.yars2.reasoning.model.AuthoritativeSources;

/**
 * CheckAuthority checks the authority of statements in a given TBox 
 * index file
 * @author aidhog
 */
public class AuthorityIterator implements Iterator<Node[]> {
	private PrintStream _out = null;
	
	private Iterator<Node[]> _in;
	private Node[] _current = null;
	
	private int _drop = 0, _ao = 0, _as = 0, _ab = 0, _count = 0;
	private AuthoritativeSources _authSources;
	private Node _olds = null, _oldc = null, _oldo = null;
	private HashMap<Node, Counts> _ht;
	private boolean _authSub = false, _authObj = false;
	
	public AuthorityIterator(Iterator<Node[]> in, String[] ac, PrintStream out){
		_in = in;
		_authSources = new AuthoritativeSources(ac);
		_ht = new HashMap<Node, Counts>();
		getNext();
	}
	
	public AuthorityIterator(Iterator<Node[]> in, PrintStream out){
		this(in, null, out);
	}
	
	public AuthorityIterator(Iterator<Node[]> in){
		this(in, null, null);
	}
	
	private void getNext(){
		_current = null;
		boolean d = false;
		do{
			d=false;
			
			if(!_in.hasNext())
				return;
			
			Node[] line = _in.next();
			
			if(_olds==null || !_olds.equals(line[0]) || 
					_oldc==null || !_oldc.equals(line[3])){
				_authSub = _authSources.isAuthoritative(line[0], line[3]);
			} 
			if(_oldo==null || !_oldo.equals(line[2]) || 
					_oldc==null || !_oldc.equals(line[3])){
				_authObj = _authSources.isAuthoritative(line[2], line[3]);
			}
	
			_count++;
	
			_olds = line[0];
			_oldo = line[2];
			_oldc = line[3];
	
			Counts c;
	
			if(line[1].equals(RDF.TYPE)){
				c = _ht.get(line[2]);
				if(c==null){
					c = new Counts();
					_ht.put(line[2], c);
				}
	
				if(_authSub){
					_current = line;
				} else{
					d = true;
				}
			}
			else{
				c = _ht.get(line[1]);
				if(c==null){
					c = new Counts();
					_ht.put(line[1], c);
				}
	
				if(line[1].equals(RDFS.SUBCLASSOF)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.EQUIVALENTCLASS)){
					if(_authSub&& _authObj){
						_current = line;
					} else if(_authSub){
						line[1] = Reasoner.OWL_EQUIVALENTCLASS_NA_OBJ;
						_current = line;
					} else if(_authObj){
						line[1] = Reasoner.OWL_EQUIVALENTCLASS_NA_SUBJ;
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.ONEOF)){
					if(_authSub && _authObj){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.UNIONOF)){
					if(_authSub && _authObj){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.INTERSECTIONOF)){
					if(_authSub && _authObj){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(RDF.FIRST)){
					if(_authSub){
						if(!_authObj){
							line[1] = Reasoner.RDF_FIRST_NA_OBJ;
						}
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(RDF.REST)){
					if(_authSub && (_authObj || line[2].equals(RDF.NIL))){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(RDFS.DOMAIN)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(RDFS.RANGE)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(RDFS.SUBPROPERTYOF)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.EQUIVALENTPROPERTY)){
					if(_authSub&& _authObj){
						_current = line;
					} else if(_authSub){
						line[1] = Reasoner.OWL_EQUIVALENTPROPERTY_NA_OBJ;
						_current = line;
					} else if(_authObj){
						line[1] = Reasoner.OWL_EQUIVALENTPROPERTY_NA_SUBJ;
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.INVERSEOF)){
					if(_authSub && _authObj){
						_current = line;
					} else if(_authSub){
						line[1] = Reasoner.OWL_INVERSEOF_NA_OBJ;
						_current = line;
					} else if(_authObj){
						line[1] = Reasoner.OWL_INVERSEOF_NA_SUBJ;
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.ONPROPERTY)){
					if(_authSub){
						if(!_authObj){
							line[1] = Reasoner.OWL_ONPROPERTY_NA_OBJ;
						} 
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.SOMEVALUESFROM)){
					if(_authSub){
						if(!_authObj){
							line[1] = Reasoner.OWL_SOME_VALUES_FROM_NA_OBJ;
						}
						_current = line;
					} else{ 
						d=true;
					}
				}
				else if(line[1].equals(OWL.ALLVALUESFROM)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.HASVALUE) && _authSub){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.CARDINALITY)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.MAXCARDINALITY)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.MINCARDINALITY)){
					if(_authSub){
						_current = line;
					} else{
						d = true;
					}
				}
				else if(line[1].equals(OWL.DISJOINTWITH)){
					_current = line;
				}
				else if(line[1].equals(OWL.COMPLEMENTOF)){
					_current = line;
				}
				else{
					d = true;
				}
			}
	
			c.incrementCount();
			if(_authSub && _authObj){
				_ab++;
				_as++;
				_ao++;
				c.incrementAuthBoth();
			} else if(_authSub){
				_as++; 
				c.incrementAuthSub();
			} else if(_authObj){
				_ao++;
				c.incrementAuthObj();
			}
	
			if(d){
				_drop++;
				c.incrementDropped();
			}
		} while(d && _in.hasNext());
	}
	
	public boolean hasNext(){
		if(_current==null && _out!=null){
			printStats();
			_out = null;
		}
		return _current!=null;
	}
	
	public Node[] next(){
		if(_current==null)
			throw new NoSuchElementException();
		Node[] ans = new Node[_current.length];
		System.arraycopy(_current, 0, ans, 0, ans.length);
		getNext();
		return ans;
	}
	
	
	public void printStats(){
		if(_out!=null){
			_out.println("=======================================");
			_out.println("AUTHORITY ITERATOR STATS");
			_out.println("=======================================");
			_out.println("CONCEPT\tTOTAL\tDROP\tAUTHSUB\tAUTHOBJ\tAUTHBOTH");
			_out.println("OVERALL\t"+_count+"\t"+_drop+"\t"+(_as-_ab)+"\t"+(_ao-_ab)+"\t"+_ab);
			_out.println("=======================================");
	
			for(Entry<Node,Counts> e:_ht.entrySet())
				_out.println(e.getKey()+"\t"+e.getValue());
			_out.println("=======================================");
			_authSources.printStats(_out);
	
			_out.flush();
		}
	}

	public static class Counts{
		private int _count, _drop, _as, _ao, _ab;

		public Counts(){
			_count = 0;
			_drop = 0;
			_as = 0;
			_ao = 0;
			_ab = 0;
		}

		public int getCount(){
			return _count;
		}

		public int getDropped(){
			return _drop;
		}

		public int getAuthSub(){
			return _as;
		}

		public int getAuthObj(){
			return _ao;
		}

		public int getAuthBoth(){
			return _ab;
		}

		public void incrementCount(){
			_count++;
		}

		public void incrementDropped(){
			_drop++;
		}

		public void incrementAuthSub(){
			_as++;
		}

		public void incrementAuthObj(){
			_ao++;
		}

		public void incrementAuthBoth(){
			_ab++;
			_as++;
			_ao++;
		}

		public String toString(){
			return _count+"\t"+_drop+"\t"+(_as-_ab)+"\t"+(_ao-_ab)+"\t"+_ab;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}