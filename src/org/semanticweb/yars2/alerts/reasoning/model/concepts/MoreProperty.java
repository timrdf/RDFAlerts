package org.semanticweb.yars2.alerts.reasoning.model.concepts;

import org.semanticweb.yars.nx.Node;

/**
 * Represents a an rdf:Property or one of it's owl subclasses
 * @author aidhog
 *
 */
public class MoreProperty extends org.semanticweb.yars2.reasoning.model.concepts.Property {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean _isObject = false;
	private boolean _isDatatype = false;
	
	private boolean _isDeprecated = false;
	
	public MoreProperty(Node uri){
		super(uri);
	}
	
	public void flagDeprecated(){
		_isDeprecated = true;
	}
	
	public boolean isDeprecated(){
		return _isDeprecated;
	}
	
	public void flagObject(){
		_isObject = true;
	}
	
	public boolean isObject(){
		return _isObject;
	}
	
	public void flagDatatype(){
		_isDatatype = true;
	}
	
	public boolean isDatatype(){
		return _isDatatype;
	}
	
	public String toString(){
		return "Property: "+_uri;
	}
}
