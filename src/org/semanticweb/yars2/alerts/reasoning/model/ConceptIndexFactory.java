package org.semanticweb.yars2.alerts.reasoning.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars2.alerts.reasoning.model.concepts.MoreClass;
import org.semanticweb.yars2.alerts.reasoning.model.concepts.MoreProperty;
import org.semanticweb.yars2.reasoning.engine.ji.Reasoner;
import org.semanticweb.yars2.reasoning.model.concepts.Cardinality;
import org.semanticweb.yars2.reasoning.model.concepts.Class;
import org.semanticweb.yars2.reasoning.model.concepts.HasValue;
import org.semanticweb.yars2.reasoning.model.concepts.Individual;
import org.semanticweb.yars2.reasoning.model.concepts.List;
import org.semanticweb.yars2.reasoning.model.concepts.Property;
import org.semanticweb.yars2.reasoning.model.concepts.Set;
import org.semanticweb.yars2.reasoning.model.concepts.XValuesFrom;
import org.semanticweb.yars2.reasoning.model.concepts.List.AuthorisedNode;

/**
 * ConceptIndexFactory builds ConceptIndex (TBox in memory)
 * @author aidhog
 */
public class ConceptIndexFactory {

	public ConceptIndexFactory(){
		;
	}

	public static ConceptIndex buildConceptIndex(Iterator<Node[]> iter) throws IOException{

		long b4bci = System.currentTimeMillis();
		ConceptIndex ci = new ConceptIndex();	

		Node[] line = null;
		Node olds = null;
		boolean c = false, p = false, r = false, co = false;
		boolean ifp = false, fp = false, tp = false, sp = false;
		boolean dp = false, op = false;//, ontp = false, ap = false;

		TreeSet<Class> domain = null, range = null, subclassOf = null, equivclass = null, disjointwith = null, complementof = null;
		TreeSet<Property> subpropertyof = null, inverseof = null, equivproperty = null;

		//unidirectional for authority... only authorised in one direction
		TreeSet<Class> inequivclass = null, outequivclass = null;
		TreeSet<Property> ininverseof = null, outinverseof = null, inequivproperty = null, outequivproperty = null;

		ArrayList<Set<Node>> enums = new ArrayList<Set<Node>>();
		ArrayList<Set<Class>> us = new ArrayList<Set<Class>>();
		ArrayList<Set<Class>> is = new ArrayList<Set<Class>>();

		boolean deprecated = false;

		Node oneof = null, union = null, intersection = null, first = null, rest = null, hasvalue = null;
		int maxcardinality = -1, cardinality = -1, mincardinality = -1;

		MoreProperty onproperty = null;
		MoreClass allsubjclass = null, somesubjclass = null;

		//not authorised
		Node nafirst = null; 
		MoreProperty naonproperty = null;
		MoreClass nasomesubjclass = null;

		TreeSet<Node> banOP = new TreeSet<Node>();

		boolean done = false;
		int count = 0;
		if(!iter.hasNext())
			return null;
		else while(!done){
			if(!iter.hasNext()){
				System.out.println(olds);
				done = true;
			}
			else
				line = iter.next();

			count++;
			if(count%10000==0){
				System.err.println("Done "+count);
			}
			
//			if(line[0] instanceof BNode && line[0].toString().contains("medyaleedu")){
//				continue;
//			}
			
			if((olds!=null && !olds.equals(line[0])) || done){
				if(done)
					olds = line[0];

				banOP= new TreeSet<Node>();

				if(c){
					MoreClass cl = ci.getOrCreateClass(olds); 

					if(subclassOf!=null){
						cl.addAllSuperClass(subclassOf);
						subclassOf = null;
					}
					
					if(disjointwith!=null){
						cl.addAllDisjointClass(disjointwith);
						disjointwith = null;
					}
					
					if(complementof!=null){
						cl.addAllSuperClass(complementof);
						complementof = null;
					}

					if(equivclass!=null){
						cl.addAllEquivalentClass(equivclass);
						for(Class sc:equivclass)
							sc.addInEquivalentClass(cl);
						equivclass = null;
					}

					if(outequivclass!=null){
						cl.addAllEquivalentClass(outequivclass);
						outequivclass = null;
					}

					if(inequivclass!=null){
						for(Class sc:inequivclass)
							sc.addInEquivalentClass(cl);
						inequivclass = null;
					}

//					if(sameas!=null){
//					for(Node n:sameas){
//					Class sc = ci.getOrCreateClass(n);
//					cl.addSameAs(sc);
//					sc.addSameAs(cl);
//					}
//					sameas = null;
//					}

//					if(outsameas!=null){
//					for(Node n:outsameas){
//					Class sc = ci.getOrCreateClass(n);
//					cl.addSameAs(sc);
//					}
//					outsameas = null;
//					}

//					if(insameas!=null){
//					for(Node n:insameas){
//					Class sc = ci.getOrCreateClass(n);
//					sc.addSameAs(cl);
//					}
//					insameas = null;
//					}


					if(oneof!=null){
						Set<Node> enu = new Set<Node>(cl, oneof);
						enums.add(enu);
						oneof = null;
					}

					if(union!=null){
						Set<Class> uni = new Set<Class>(cl, union);
						us.add(uni);
						cl.addInUnion(uni);
						union = null;
					}

					if(intersection!=null){
						Set<Class> inter = new Set<Class>(cl, intersection);
						is.add(inter);
						cl.addIntersection(inter);
						intersection = null;
					}

					if(deprecated) {
						cl.flagDeprecated();
						deprecated = false;
					}

					if(r && (onproperty!=null || naonproperty!=null)){
						if(somesubjclass!=null || nasomesubjclass!=null){
							if(nasomesubjclass!=null && onproperty!=null){
								XValuesFrom svf = new XValuesFrom(cl, onproperty, nasomesubjclass);
								onproperty.addSomeValuesFrom(svf);
								nasomesubjclass.addInSomeValuesFrom(svf);
							} else if(somesubjclass!=null && onproperty!=null){
								XValuesFrom svf = new XValuesFrom(cl, onproperty, somesubjclass);
								onproperty.addSomeValuesFrom(svf);
								somesubjclass.addInSomeValuesFrom(svf);
							} else if(somesubjclass!=null && naonproperty!=null){
								XValuesFrom svf = new XValuesFrom(cl, naonproperty, somesubjclass);
								naonproperty.addSomeValuesFrom(svf);
								somesubjclass.addInSomeValuesFrom(svf);
							}
							somesubjclass = null;
							nasomesubjclass = null;

						}
						else if(allsubjclass!=null){
							if(onproperty!=null){
								XValuesFrom avf = new XValuesFrom(cl, onproperty, allsubjclass);
								cl.addAllValuesFrom(avf);
								onproperty.addAllValuesFrom(avf);
							} else if(naonproperty!=null){
								XValuesFrom avf = new XValuesFrom(cl, naonproperty, allsubjclass);
								cl.addAllValuesFrom(avf);
								naonproperty.addAllValuesFrom(avf);
							}
							allsubjclass = null;
						}
						else if(hasvalue!=null){
							if(onproperty!=null){
								HasValue hv = new HasValue(cl, onproperty, hasvalue); 
								cl.addHasValue(hv);
								onproperty.addHasValue(hv);
							} else if(naonproperty!=null){
								HasValue hv = new HasValue(cl, naonproperty, hasvalue); 
								cl.addHasValue(hv);
							}
							hasvalue = null;
						}
						else if(maxcardinality==1 || cardinality==1 || (mincardinality==1 && onproperty!=null)){
							if(onproperty!=null){
								Cardinality card = new Cardinality(cl, onproperty, maxcardinality, mincardinality, cardinality);
								cl.addCardinality(card);
								onproperty.addCardinality(card);
							} else if(naonproperty!=null){
								Cardinality card = new Cardinality(cl, naonproperty, maxcardinality, mincardinality, cardinality);
								cl.addCardinality(card);
								naonproperty.addCardinality(card);
							}
							mincardinality = -1;
							maxcardinality = -1;
							cardinality = -1;
						}
						if(cl!=null)
							ci.addClass(cl);

						onproperty = null;
						naonproperty = null;
					}
				}

				if(p){
					MoreProperty pr = ci.getOrCreateProperty(olds); 
					if(ifp){
						pr.flagInverseFunctional();
						ifp = false;
					}
					if(fp){
						pr.flagFunctional();
						fp = false;
					}
					if(sp){
						pr.flagSymmetric();
						sp = false;
					}
					if(tp){
						pr.flagTransitive();
						tp = false;
					}
					if(dp){
						pr.flagDatatype();
						dp = false;
					}
//					if(ontp){
//						pr.flagOntology();
//						ontp = false;
//					}
//					if(ap){
//						pr.flagAnnotation();
//						ap = false;
//					}
					if(deprecated){
						pr.flagDeprecated();
						deprecated = false;
					}
					if(op){
						if(dp){
							System.err.println(pr.getURI()+" is a datatype and object property!");
						}
						pr.flagObject();
						op = false;
					}

					if(domain!=null){
						pr.addAllDomain(domain);
						domain = null;
					}
					if(range!=null){
						pr.addAllRange(range);
						range = null;
					}	
					if(inverseof!=null){
						pr.addAllInverseOf(inverseof);
						for(Property inv:inverseof){
							inv.addInInverseOf(pr);
						}
						inverseof = null;
					}
					if(outinverseof!=null){
						pr.addAllInverseOf(outinverseof);
						outinverseof = null;
					}
					if(ininverseof!=null){
						for(Property inv:ininverseof){
							inv.addInInverseOf(pr);
						}
						ininverseof = null;
					}
					if(subpropertyof!=null){
						pr.addAllSuperProperty(subpropertyof);
						subpropertyof = null;
					}
					if(equivproperty!=null){
						pr.addAllEquivalentProperty(equivproperty);
						for(Property samep:equivproperty)
							samep.addInEquivalentProperty(pr);
						equivproperty = null;
					}
					if(outequivproperty!=null){
						pr.addAllEquivalentProperty(outequivproperty);
						outequivproperty = null;
					}
					if(inequivproperty!=null){
						for(Property samep:inequivproperty)
							samep.addInEquivalentProperty(pr);
						inequivproperty = null;
					}
//					if(sameas!=null){
//						for(Node n:sameas){
//							Property ep = ci.getOrCreateProperty(n);
//							pr.addSameAs(ep);
//							ep.addSameAs(pr);
//						}
//						sameas = null;
//					}
//					if(outsameas!=null){
//						for(Node n:outsameas){
//							Property ep = ci.getOrCreateProperty(n);
//							pr.addSameAs(ep);
//						}
//						outsameas = null;
//					}
//					if(insameas!=null){
//						for(Node n:insameas){
//							Property ep = ci.getOrCreateProperty(n);
//							ep.addSameAs(pr);
//						}
//						insameas = null;
//					}
				}
				if(co){
//					if(first==null)
//					System.err.println("No first defined for list "+old);
//					if(rest==null)
//					System.err.println("No rest defined for list "+old);
					if(first!=null || nafirst!=null || rest!=null){
						if(first!=null)
							ci.addCollectionSegment(olds, first, true, rest);
						else if(nafirst!=null)
							ci.addCollectionSegment(olds, nafirst, false, rest);
						else
							ci.addCollectionSegment(olds, null, true, rest);
					}
					nafirst = null;
					first = null;
					rest = null;
				}
				c = false;
				co = false;
				p = false;
				r = false;
			}

			if(done)
				break;

			olds = line[0];
//			//following overrides checkAuth
//			if(!checkAuth){
//			String pred = line[1].toString();
//			if(pred.endsWith(Reasoner.NA_OBJ)){
//			authObj = false;
//			line[1] = new Resource(pred.substring(0, pred.toString().length()-Reasoner.NA_OBJ.length()));
//			} else if(pred.endsWith(Reasoner.NA_SUBJ)){
//			authSub = false;
//			line[1] = new Resource(pred.substring(0, pred.toString().length()-Reasoner.NA_SUBJ.length()));
//			} else if(pred.endsWith(Reasoner.NA_BOTH)){
//			authSub = false;
//			authObj = false; 
//			line[1] = new Resource(pred.substring(0, pred.toString().length()-Reasoner.NA_BOTH.length()));
//			}
//			}

			if(line[1].equals(RDF.TYPE)){
				if(line[2].equals(RDFS.CLASS) || line[2].equals(OWL.CLASS))
					c = true;
				else if(line[2].equals(OWL.DEPRECATEDCLASS)){
					c = true;
					deprecated = true;
				}
				else if (line[2].equals(RDF.PROPERTY))
					p = true;
				else if(line[2].equals(OWL.DEPRECATEDPROPERTY)){
					p = true;
					deprecated = true;
				}
				else if(line[2].equals(OWL.OBJECTPROPERTY)){
					p = true;
					op = true;
				}
				else if(line[2].equals(OWL.DATATYPEPROPERTY)){
					p = true;
					dp = true;
				}
				else if(line[2].equals(OWL.INVERSEFUNCTIONALPROPERTY)){
					p = true;
					ifp = true;
				}
				else if(line[2].equals(OWL.FUNCTIONALPROPERTY)){
					p = true;
					fp = true;
				}
				else if(line[2].equals(OWL.TRANSITIVEPROPERTY)){
					p = true;
					tp = true;
				}
				else if(line[2].equals(OWL.SYMMETRICPROPERTY)){
					p = true;
					sp = true;
				}
//				else if(line[2].equals(OWL.ONTOLOGYPROPERTY)){
//					p = true;
//					ontp = true;
//				}
//				else if(line[2].equals(OWL.ANNOTATIONPROPERTY)){
//					p = true;
//					ap = true;
//				}
				else if(line[2].equals(OWL.RESTRICTION)){
					r = true;
					c = true;
				}
			}
			else if(line[1].equals(RDFS.SUBCLASSOF)){
				if(subclassOf == null){
					subclassOf = new TreeSet<Class>();
				}
				MoreClass sc = ci.getOrCreateClass(line[2]);

				subclassOf.add(sc);
				c = true;
			}
			else if(line[1].equals(OWL.EQUIVALENTCLASS)){
				MoreClass sc = ci.getOrCreateClass(line[2]);
				if(equivclass == null){
					equivclass = new TreeSet<Class>();
				}
				equivclass.add(sc);
				c = true;
			} else if(line[1].equals(Reasoner.OWL_EQUIVALENTCLASS_NA_OBJ)){
				MoreClass sc = ci.getOrCreateClass(line[2]);
				if(outequivclass == null){
					outequivclass = new TreeSet<Class>();
				}
				outequivclass.add(sc);
				c = true;
			} else if(line[1].equals(Reasoner.OWL_EQUIVALENTCLASS_NA_SUBJ)){
				MoreClass sc = ci.getOrCreateClass(line[2]);
				if(inequivclass == null){
					inequivclass = new TreeSet<Class>();
				}
				inequivclass.add(sc);
				c = true;
			} else if(line[1].equals(OWL.DISJOINTWITH)){
				MoreClass sc = ci.getOrCreateClass(line[2]);
				if(disjointwith==null){
					disjointwith = new TreeSet<Class>();
				}
				disjointwith.add(sc);
				c = true;
			} else if(line[1].equals(OWL.COMPLEMENTOF)){
				MoreClass sc = ci.getOrCreateClass(line[2]);
				if(complementof==null){
					complementof = new TreeSet<Class>();
				}
				complementof.add(sc);
				c = true;
			}
			else if(line[1].equals(OWL.ONEOF)){
				oneof = line[2];
				c = true;
			}
			else if(line[1].equals(OWL.UNIONOF)){
				union = line[2];
				c = true;
			}
			else if(line[1].equals(OWL.INTERSECTIONOF)){
				intersection = line[2];
				c = true;
			}
			else if(line[1].equals(RDF.FIRST)){
				first = line[2];
				co = true;
			} else if(line[1].equals(Reasoner.RDF_FIRST_NA_OBJ)){
				nafirst = line[2];
				co = true;
			}
			else if(line[1].equals(RDF.REST)){
				rest = line[2];
				co = true;
			}
			else if(line[1].equals(RDFS.DOMAIN)){
				if(domain==null){
					domain = new TreeSet<Class>();
				}
				MoreClass d = ci.getOrCreateClass(line[2]);

				domain.add(d);
				p = true;
			}
			else if(line[1].equals(RDFS.RANGE)){
				if(range==null){
					range = new TreeSet<Class>();
				}
				MoreClass cr = ci.getOrCreateClass(line[2]);

				range.add(cr);
				p = true;
			}
			else if(line[1].equals(RDFS.SUBPROPERTYOF)){
				if(subpropertyof == null){
					subpropertyof = new TreeSet<Property>();
				}
				MoreProperty subp = ci.getOrCreateProperty(line[2]);

				subpropertyof.add(subp);
				p = true;
			}
			else if(line[1].equals(OWL.EQUIVALENTPROPERTY)){
				MoreProperty samep = ci.getOrCreateProperty(line[2]);
				if(equivproperty == null){
					equivproperty = new TreeSet<Property>();
				}
				equivproperty.add(samep);
				p = true;
			} else if(line[1].equals(Reasoner.OWL_EQUIVALENTPROPERTY_NA_OBJ)){
				MoreProperty samep = ci.getOrCreateProperty(line[2]);
				if(outequivproperty == null){
					outequivproperty = new TreeSet<Property>();
				}
				outequivproperty.add(samep);
				p = true;
			} else if(line[1].equals(Reasoner.OWL_EQUIVALENTPROPERTY_NA_SUBJ)){
				MoreProperty samep = ci.getOrCreateProperty(line[2]);
				if(inequivproperty == null){
					inequivproperty = new TreeSet<Property>();
				}
				inequivproperty.add(samep);
				p = true;
			}
			else if(line[1].equals(OWL.INVERSEOF)){
				MoreProperty invp = ci.getOrCreateProperty(line[2]);
				if(inverseof == null){
					inverseof = new TreeSet<Property>();
				}
				inverseof.add(invp);
				p=true;
			} else if(line[1].equals(Reasoner.OWL_INVERSEOF_NA_OBJ)){
				MoreProperty invp = ci.getOrCreateProperty(line[2]);
				if(outinverseof == null){
					outinverseof = new TreeSet<Property>();
				}
				outinverseof.add(invp);
				p=true;
			} else if(line[1].equals(Reasoner.OWL_INVERSEOF_NA_SUBJ)){
				MoreProperty invp = ci.getOrCreateProperty(line[2]);
				if(ininverseof == null){
					ininverseof = new TreeSet<Property>();
				}
				ininverseof.add(invp);
				p=true;
			}
			else if(line[1].equals(OWL.ONPROPERTY)){
				if(banOP.size()>0 && !banOP.contains(line[2])){
					System.err.println("Another owl:onProperty values defined for "+line[0]+" in "+line[3]+". Dropping!");
					banOP.add(line[2]);
					System.err.println("Banned "+banOP.size()+" "+banOP);
				} else if(onproperty!=null && !onproperty.getURI().equals(line[2])){
					System.err.println("Multiple owl:onProperty values defined for "+line[0]+" in "+line[3]+". Dropping both!");
					banOP.add(onproperty.getURI());
					banOP.add(line[2]);
					System.err.println("Banned "+banOP.size()+" "+banOP);
					onproperty = null;
				} else if(naonproperty!=null && !naonproperty.getURI().equals(line[2])){
					System.err.println("Multiple owl:onProperty values defined for "+line[0]+" in "+line[3]+". Dropping both!");
					banOP.add(naonproperty.getURI());
					banOP.add(line[2]);
					System.err.println("Banned "+banOP.size()+" "+banOP);
					naonproperty = null;
				} else{
					onproperty = ci.getOrCreateProperty(line[2]);
					r=true;
					c=true;
				}
			} else if(line[1].equals(Reasoner.OWL_ONPROPERTY_NA_OBJ)){
				if(banOP.size()>0 && !banOP.contains(line[2])){
					System.err.println("Another owl:onProperty values defined for "+line[0]+" in "+line[3]+". Dropping!");
					banOP.add(line[2]);
					System.err.println("Banned "+banOP.size()+" "+banOP);
				} else if(onproperty!=null && !onproperty.getURI().equals(line[2])){
					System.err.println("Multiple owl:onProperty values defined for "+line[0]+" in "+line[3]+". Dropping both!");
					banOP.add(onproperty.getURI());
					banOP.add(line[2]);
					System.err.println("Banned "+banOP.size()+" "+banOP);
					onproperty = null;
				} else if(naonproperty!=null && !naonproperty.getURI().equals(line[2])){
					System.err.println("Multiple owl:onProperty values defined for "+line[0]+" in "+line[3]+". Dropping both!");
					banOP.add(naonproperty.getURI());
					banOP.add(line[2]);
					System.err.println("Banned "+banOP.size()+" "+banOP);
					naonproperty = null;
				} else{
					naonproperty = ci.getOrCreateProperty(line[2]);
					r=true;
					c=true;
				}
			}
			else if(line[1].equals(OWL.SOMEVALUESFROM)){
				somesubjclass= ci.getOrCreateClass(line[2]);
				r=true;
				c=true;
			} else if(line[1].equals(Reasoner.OWL_SOME_VALUES_FROM_NA_OBJ)){
				nasomesubjclass = ci.getOrCreateClass(line[2]);
				r=true;
				c=true;
			}
			else if(line[1].equals(OWL.ALLVALUESFROM)){
				allsubjclass= ci.getOrCreateClass(line[2]);
				r=true;
				c=true;
			}
			else if(line[1].equals(OWL.HASVALUE)){
				hasvalue = line[2];
				r=true;
				c=true;
			}
			else if(line[1].equals(OWL.CARDINALITY)){
				try{
					int card = Integer.parseInt(line[2].toString());
					cardinality = card;
					r=true;
					c=true;
				} catch(NumberFormatException e){
					try{
						float card = Float.parseFloat(line[2].toString());
						cardinality = (int) card;
						r = true;
						c = true;
					} catch(NumberFormatException e2){
						System.err.println("Cannot parse cardinality value from literal "+line[2].toN3());
					}
				}
			}
			else if(line[1].equals(OWL.MAXCARDINALITY)){
				try{
					int card = Integer.parseInt(line[2].toString());
					maxcardinality = card;
					r=true;
					c=true;
				} catch(NumberFormatException e){
					try{
						float card = Float.parseFloat(line[2].toString());
						maxcardinality = (int) card;
						r = true;
						c = true;
					} catch(NumberFormatException e2){
						System.err.println("Cannot parse cardinality value from literal "+line[2].toN3());
					}
				}
			}
			else if(line[1].equals(OWL.MINCARDINALITY)){
				try{
					int card = Integer.parseInt(line[2].toString());
					mincardinality = card;
					r=true;
					c=true;
				} catch(NumberFormatException e){
					try{
						float card = Float.parseFloat(line[2].toString());
						mincardinality = (int) card;
						r = true;
						c = true;
					} catch(NumberFormatException e2){
						System.err.println("Cannot parse cardinality value from literal "+line[2].toN3());
					}
				}
			}
//			else if(line[1].equals(OWL.SAMEAS)){
//			if(authSub && authObj){
//			if(sameas == null){
//			sameas = new TreeSet<Node>();
//			}
//			sameas.add(line[2]);
//			} else if(authSub){
//			if(outsameas == null){
//			outsameas = new TreeSet<Node>();
//			}
//			outsameas.add(line[2]);
//			} else if(authObj){
//			if(insameas == null){
//			insameas = new TreeSet<Node>();
//			}
//			insameas.add(line[2]);
//			}
//			}
		}
		consolidate(ci, enums, us, is);

		System.out.println("Building concept index took "+(System.currentTimeMillis() - b4bci));
		return ci;
	}

	@SuppressWarnings("unchecked")
	private static void consolidate(ConceptIndex ci, Collection<Set<Node>> enums, Collection<Set<Class>> us,  Collection<Set<Class>> is){
		for(Set<Node> enu:enums){
			Node head = enu.getHeadURI();
			List l = ci.getCollection(head);
			if(l==null || !l.isValid()){
				continue;
			}
			for(AuthorisedNode an:l.getValues()){
				Individual i = ci.getOrCreateIndividual(an.getNode());
				i.addIsOneOf(enu.getSubjectClass());
			}
		}

		for(Set<Class> u:us){
			Node head = u.getHeadURI();
			List l = ci.getCollection(head);
			if(l==null || !l.isValid()){
				u.getSubjectClass().removeInUnion(head);
				continue;
			}

			for(AuthorisedNode an:l.getValues()){
				if(an.isAuthorised()){
					u.addElement(ci.getOrCreateClass(an.getNode()));
				}
			}

			if(u.getElements().size()==0)
				u.getSubjectClass().removeInUnion(head);

			u.removeHeadURI();
		}

		for(Set<Class> i:is){
			Node head = i.getHeadURI();
			List l = ci.getCollection(head);
			if(l==null || !l.isValid()){
				i.getSubjectClass().removeIntersection(head);
				continue;
			}

			boolean auth = false;
			for(AuthorisedNode an:l.getValues()){
				auth |= an.isAuthorised();
			}

			if(auth){
				for(AuthorisedNode an:l.getValues()){
					MoreClass c = ci.getOrCreateClass(an.getNode());
					i.addElement(c);
					c.addInIntersection(i);
				}
			}
			i.removeHeadURI();
		}
	}
}

//for(Node curi:curis){
//Class c = ci.getClass(curi);
//Collection<Class> subclasses = c.getSuperClasses();
//ArrayList<Class> temp = new ArrayList<Class>();
//temp.addAll(subclasses);
//for(int i=0; i<temp.size(); i++){
//Class sc = temp.get(i);
//if(sc instanceof HasValue)
//sc.addHasValue((HasValue)r);
//else if(r instanceof AllValuesFrom)
//c.addAllValuesFrom((AllValuesFrom)r);
//else if(r instanceof SomeValuesFrom)
//((SomeValuesFrom)r).getObjClass().addSomeValuesFrom((SomeValuesFrom)r);
//}
//}

//ci.deleteRestrictions();

//private static void updateEquivalentConcepts(ConceptIndex ci, SameAsList sameClass, SameAsList sameProperty){
//Iterator<TreeSet<Node>> iter = sameClass.iterator();
//while(iter.hasNext()){
//ci.addEquivalentClasses(iter.next());
//}

//iter = sameProperty.iterator();
//while(iter.hasNext()){
//ci.addEquivalentProperties(iter.next());
//}

//sameClass = null;
//sameProperty = null;
//}

//private static void updateInverseOf(ConceptIndex ci, Hashtable<String,TreeSet<String>> inverseOf){
//Enumeration<String> keys = inverseOf.keys();
//Enumeration<TreeSet<String>> els = inverseOf.elements();

//while(keys.hasMoreElements()){
//String pred = keys.nextElement();
//TreeSet<String> inv = els.nextElement();
//ci.addInverseOf(pred, inv);
//}

//inverseOf = null;
//}

//private static void updateDomainRange(ConceptIndex ci, Hashtable<String,TreeSet<String>> domains, Hashtable<String,TreeSet<String>> ranges){
//Enumeration<String> keys = domains.keys();
//Enumeration<TreeSet<String>> els = domains.elements();

//while(keys.hasMoreElements()){
//String pred = keys.nextElement();
//TreeSet<String> curis = els.nextElement();
//ci.addDomain(pred, curis);
//}

//keys = ranges.keys();
//els = ranges.elements();
//while(keys.hasMoreElements()){
//String pred = keys.nextElement();
//TreeSet<String> curis = els.nextElement();
//ci.addRange(pred, curis);
//}

//domains = null;
//ranges = null;
//}

//private static void updateConceptHierarchy(ConceptIndex ci, Hashtable<String,TreeSet<String>> subClass, Hashtable<String,TreeSet<String>> subProperty){
//Enumeration<String> keys = subClass.keys();
//Enumeration<TreeSet<String>> els = subClass.elements();
//while(keys.hasMoreElements()){
//String c = keys.nextElement();
//TreeSet<String> curis = els.nextElement();
//ci.addSubclasses(c, curis);
//}

//keys = subProperty.keys();
//els = subProperty.elements();
//while(keys.hasMoreElements()){
//String pred = keys.nextElement();
//TreeSet<String> puris = els.nextElement();
//ci.addSubproperties(pred, puris);
//}

//subClass = null;
//subProperty = null;
//}

