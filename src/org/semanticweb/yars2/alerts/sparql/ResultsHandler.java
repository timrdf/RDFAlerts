package org.semanticweb.yars2.alerts.sparql;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.Callback;

public class ResultsHandler implements Callback{
	public void startDocument(){
		;
	}
	public void endDocument(){
		;
	}
	
	public void processStatement(Node[] ns){
		System.out.println(Nodes.toN3(ns));
	}
}
