package org.semanticweb.yars2.alerts.sparql;

import java.util.ArrayList;

import org.semanticweb.yars.nx.Node;

public class ResultsCollector extends ResultsHandler{
	private ArrayList<Node[]> _results;
	
	public ResultsCollector(){
		_results = new ArrayList<Node[]>();
	}
	
	public void startDocument(){
		;
	}
	public void endDocument(){
		;
	}
	
	public void processStatement(Node[] ns){
		_results.add(ns);
	}
	
	public ArrayList<Node[]> getResults(){
		return _results;
	}
}
