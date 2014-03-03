package org.semanticweb.yars2.alerts.reasoning.model.concepts;

import java.util.TreeSet;

import org.semanticweb.yars.nx.Node;

import org.semanticweb.yars2.reasoning.model.concepts.Class;

/**
 * Super-class for representing all rdfs or owl Class descriptions
 * @author aidhog
 *
 */
public class MoreClass extends org.semanticweb.yars2.reasoning.model.concepts.Class{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//stores disjoint classes
	private TreeSet<Class> _disjointWith = null;
	
	//stores complement classes
	private TreeSet<Class> _complementOf = null;
	
	//is deprecated
	private boolean _deprecated = false;
	
	
	public MoreClass(Node uri){
		super(uri);
	}
	
	public void flagDeprecated(){
		_deprecated = true;
	}
	
	public void setDeprecated(boolean deprecated){
		_deprecated = deprecated;
	}
	
	public boolean isDeprecated(){
		return _deprecated;
	}
	
	public boolean addAllDisjointClass(TreeSet<Class> disjointWith){
		if(_disjointWith==null){
			_disjointWith = new TreeSet<Class>();
		}
		return _disjointWith.addAll(disjointWith);
	}
	
	public boolean addDisjointClass(Class disjointWith){
		if(_disjointWith==null){
			_disjointWith = new TreeSet<Class>();
		}
		return _disjointWith.add(disjointWith);
	}
	
	public TreeSet<Class> getDisjointClasses(){
		return _disjointWith;
	}
	
	public boolean addAllComplementClass(TreeSet<Class> complementOf){
		if(_complementOf==null){
			_complementOf = new TreeSet<Class>();
		}
		return _complementOf.addAll(complementOf);
	}
	
	public boolean addComplementClass(Class complementOf){
		if(_complementOf==null){
			_complementOf = new TreeSet<Class>();
		}
		return _complementOf.add(complementOf);
	}
	
	public TreeSet<Class> getComplementClasses(){
		return _complementOf;
	}
}
