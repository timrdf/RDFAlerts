package org.semanticweb.yars2.alerts.tracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars.tld.URIHandler;
import org.semanticweb.yars2.reasoning.engine.ji.Reasoner;

public class IssueCounter<T> {
	private static final String REASONING = "SAOR";
	
	private int _c;
	private Count<T> _count;
	private Count<String> _hosts;
	private HashSet<String> _docs;
	
	private HashMap<String,String> _ex;
	
	private String _label;
	
	public IssueCounter(String label){
		_c = 0;
		_count = new Count<T>();
		_hosts = new Count<String>();
		_docs = new HashSet<String>();
		_ex = new HashMap<String,String>();
		_label = label;
	}
	
	public void add(T info, Node context){
		_c++;
		_count.add(info);
		if(context.toString().startsWith(Reasoner.CONTEXT_STR)){
			_hosts.add(REASONING);
		} else{
			String host = URIHandler.getPLD(context.toString());
			_hosts.add(host);
			_docs.add(context.toString());
			if(_ex.get(host)==null){
				_ex.put(host, context.toString());
			}
		}
	}
	
	public void printStats(){
		System.out.println("IssueCounter: "+_label);
		System.out.println("Occurances: "+_c);
		System.out.println("Number of documents: "+_docs.size());
		System.out.println("Distribution:");
		_count.printOrderedStats();
		System.out.println("Host Distribution:");
		_hosts.printOrderedStats(); 
		System.out.println("Host Examples:");
		for(Map.Entry<String,String> ex:_ex.entrySet()){
			System.out.println(ex.getKey()+" "+ex.getValue());
		}
		System.out.println();
	}
}
