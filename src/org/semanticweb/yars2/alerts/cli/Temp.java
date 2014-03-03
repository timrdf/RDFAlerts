package org.semanticweb.yars2.alerts.cli;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Nodes;

public class Temp {
	public static void main(String args[]){
		Nodes n = new Nodes(new BNode("blah"));
		System.err.println(n);
	}
}
