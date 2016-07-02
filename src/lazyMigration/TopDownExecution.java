package lazyMigration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import parserPutToDatalog.ParseException;
import datastore.Database;
import datalog.Condition;
import datalog.Fact;
import datalog.MagicCondition;
import datalog.MigrationExecution;
import datalog.PairForMagicCondition;
import datalog.Predicate;
import datalog.Rule;
import datalog.RuleBody;

public class TopDownExecution extends MigrationExecution {

	private Predicate goal; // the top goal for the topdown execution
	private RuleGoalTree tree; // a rule goal tree with top a goal and subgoals
	private List<Map<String, String>> unificationMap; // this map stores
														// attributes and their
														// values for
														// unification
	private ArrayList<Fact> putFacts; // facts that will be write to database
	private List<MagicCondition> magicList = null; // contains all information
													// for magic sets
	private int numberOfPuts = 0; // number of db writes
	private int numberOfMGView = 0; // number of magic set views

	private Database db;

	public int getNumberOfPuts() {
		return numberOfPuts;
	}

	public void setNumberOfPuts(int numberOfPuts) {
		this.numberOfPuts = numberOfPuts;
	}

	// set edb facts, rules
	public TopDownExecution(ArrayList<Fact> facts, ArrayList<Rule> rules) {
		super(facts, rules);
		db = new Database();
	}

	// set edb facts, rules, goal and unificationMap
	public TopDownExecution(ArrayList<Fact> facts, ArrayList<Rule> rules,
			Predicate goal, List<Map<String, String>> unificationMap) {
		super(facts, rules);
		this.goal = goal;
		this.unificationMap = unificationMap;
		db = new Database();
	}

	// Test for top down approach for only one rule
	public ArrayList<ArrayList<String>> getAnswer(Rule rule) {

		rulesRename(rule);
		for (Predicate p : rule.getPredicates()) {
			getFacts(p);
		}

		Predicate temp = join(rule.getPredicates());
		if (rule.getConditions() != null)
			temp = selection(temp, rule.getConditions());

		ArrayList<String> werte = rule.getHead().getScheme();
		ArrayList<ArrayList<String>> answer = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> oneMap : temp.getRelation()) {
			ArrayList<String> oneAnswer = new ArrayList<String>();
			for (String wert : werte)
				if (wert.startsWith("?"))
					oneAnswer.add(oneMap.get(temp.getScheme().indexOf(wert)));
				else
					oneAnswer.add(wert);
			answer.add(oneAnswer);
		}

		return answer;
	}

	public ArrayList<Fact> getAnswers() throws ParseException, IOException,
			URISyntaxException {

		ArrayList<Fact> answer = new ArrayList<Fact>();
		/*
		 * if (existIDForKind(unificationMap.get(0).get("kind"), unificationMap
		 * .get(0).get("value"))) {
		 */
		putFacts = new ArrayList<Fact>();
		ArrayList<Rule> childrenRules = new ArrayList<Rule>();
		for (Rule r : rules) {
			// look for needed goal predicate in rule heads
			// + unify found rules
			Predicate ruleHead = r.getHead();
			if (ruleHead.getKind().equals(goal.getKind())
					&& ruleHead.getNumberSchemeEntries() == goal
							.getNumberSchemeEntries()) {
				Rule unifiedRule = unifyRule(goal, r);
				childrenRules.add(unifiedRule);
			}
		}

		// initialize next level of ruleGoalTree with unified
		// children rules and save the results in Predicate subgoal
		tree = new RuleGoalTree(childrenRules);
		goal.setRelation(getAnswersForSubtree(tree));

		// add results to temp fact list
		if (goal.getRelation() != null) {
			for (ArrayList<String> str : goal.getRelation()) {
				answer.add(new Fact(goal.getKind(), str));
			}
		}

		// add put facts to put list
		if (putFacts.size() != 0) {
			for (Fact f : putFacts) {
				putFactToDB(f);
				numberOfPuts++;
			}

		}
		// }
		return answer;

	}

	private ArrayList<ArrayList<String>> getAnswersForSubtree(RuleGoalTree tree) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

		for (Rule childRule : tree.getChildren()) {
			Predicate resultPredicate = null;
			RuleBody body = childRule.getRuleBody();

			for (Predicate subgoal : body.getPredicates()) {
				// subgoal already exists in fact list?
				if (!getFacts(subgoal)) {
					ArrayList<Rule> unifiedChildrenRules = new ArrayList<Rule>();
					for (Rule r : rules) {
						// look for needed goal predicate in rule heads
						// + unify found rules
						Predicate ruleHead = r.getHead();
						if (ruleHead.getKind().equals(subgoal.getKind())
								&& ruleHead.getNumberSchemeEntries() == subgoal
										.getNumberSchemeEntries()) {
							Rule unifiedRule = unifyRule(subgoal, r);
							unifiedChildrenRules.add(unifiedRule);
						}
					}

					if (unifiedChildrenRules.size() != 0) {
						// initialize next level of ruleGoalTree with unified
						// children rules and save the results in Predicate
						// subgoal
						RuleGoalTree subTree = new RuleGoalTree(
								unifiedChildrenRules);
						subgoal.setRelation(getAnswersForSubtree(subTree));
						generateMagicSet(subgoal.getRelation(),
								subgoal.getKind());
						// add new facts to temp fact list
						for (ArrayList<String> str : subgoal.getRelation()) {
							facts.add(new Fact(subgoal.getKind(), str));
						}
					}
				}
			}

			// get results of rule body (join and conditions)
			if (body.getPredicates().size() > 1)
				resultPredicate = join(body.getPredicates());
			else if (!body.getPredicates().isEmpty())
				resultPredicate = body.getPredicates().get(0);
			if (body.getConditions() != null && !body.getConditions().isEmpty())
				resultPredicate = selection(resultPredicate,
						body.getConditions());

			// save results in head predicate
			childRule.getHead()
					.setRelation(
							getResults(resultPredicate, childRule.getHead()
									.getScheme()));

			result.addAll(childRule.getHead().getRelation());

		}

		if (tree.getGoal().isHead()) {
			// add put facts to put list
			for (ArrayList<String> str : result) {
				Fact newFact = new Fact(tree.getGoal().getKind(), str);
				if (!factExists(facts, newFact)
						&& !factExists(putFacts, newFact)
						&& !newFact.getKind().startsWith("get"))
					putFacts.add(newFact);
			}
		}
		return result;
	}

	// checks whether a fact exists in an arrayList<Fact>
	private boolean factExists(ArrayList<Fact> factList, Fact putFact) {
		boolean exists = false;
		ArrayList<String> valuesPutFact = putFact.getListOfValues();
		for (Fact f : factList) {
			ArrayList<String> valuesFactList = f.getListOfValues();

			if (valuesPutFact.size() == valuesFactList.size()) {
				boolean hasSameAttributes = true;
				for (int i = 0; i < valuesPutFact.size(); i++) {
					if (!valuesFactList.get(i).equals(valuesPutFact.get(i))) {
						hasSameAttributes = false;
						break;
					}
				}
				if (hasSameAttributes && putFact.getKind().equals(f.getKind())) {
					exists = true;
				} else
					exists = false;
				if (exists == true)
					break;
			}

		}
		return exists;
	}

	// only get the result-attributes of predicate which are in the scheme of
	// the head
	// Example A(?x):-B(?x,?y)--> only extract the ?x attribute of B
	private ArrayList<ArrayList<String>> getResults(Predicate results,
			ArrayList<String> scheme) {
		ArrayList<ArrayList<String>> answer = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> oneMap : results.getRelation()) {
			ArrayList<String> oneAnswer = new ArrayList<String>();
			for (String wert : scheme)
				if (wert.startsWith("?"))
					if (results.getScheme().contains(wert))
						oneAnswer.add(oneMap.get(results.getScheme().indexOf(
								wert)));
					else {
						oneAnswer.add("");
					}
				else
					oneAnswer.add(wert);
			boolean alreadyExist = false;
			for (ArrayList<String> iterateAnswer : answer)
				if (oneAnswer.containsAll(iterateAnswer)
						&& iterateAnswer.containsAll(oneAnswer))
					alreadyExist = true;
			if (!alreadyExist) {
				answer.add(oneAnswer);
			}
		}
		return answer;
	}

	// unificate all attributes which are in the unificationMap
	private Rule unifyRule(Predicate goal, Rule childrenRule) {
		// first rename all rules according to conditions
		rulesRename(childrenRule);
		Predicate head = childrenRule.getHead();
		RuleBody body = childrenRule.getRuleBody();
		if (!goal.getKind().equals(head.getKind())
				|| goal.getNumberSchemeEntries() != head
						.getNumberSchemeEntries()) {
		} else {
			for (Map<String, String> unificationMapEntry : unificationMap) {
				String kind = unificationMapEntry.get("kind");
				int positionValue = Integer.parseInt(unificationMapEntry
						.get("position"));
				String value = unificationMapEntry.get("value");
				if ((head.getKind().startsWith("latest" + kind)
						|| head.getKind().startsWith("legacy" + kind)
						|| head.getKind().startsWith("get" + kind) || head
						.getKind().startsWith(kind))) {
					ArrayList<String> attributesRuleHead = head.getScheme();
					String idValue = attributesRuleHead.get(positionValue);
					// unificate head-predicates which match with kind
					unificatePredicate(attributesRuleHead, idValue, value);

				}
				// unificate body-predicates which match with kind
				String id = null;
				for (Predicate p : body.getPredicates()) {
					if ((p.getKind().startsWith("latest" + kind)
							|| p.getKind().startsWith("legacy" + kind)
							|| p.getKind().startsWith("get" + kind) || p
							.getKind().startsWith(kind))) {
						ArrayList<String> attributesRule = p.getScheme();
						String idValue = attributesRule.get(positionValue);
						if (id == null)
							id = idValue;
						unificatePredicate(attributesRule, idValue, value);
					}
				}
				if (id != null) {
					if (!(head.getKind().startsWith("latest" + kind)
							|| head.getKind().startsWith("legacy" + kind)
							|| head.getKind().startsWith("get" + kind) || head
							.getKind().startsWith(kind))) {
						// unificate head-predicates which don't match with kind
						unificatePredicate(head.getScheme(), id, value);
					}
					// unificate body-predicates which don't match with kind
					for (Predicate p : body.getPredicates()) {
						if (!(p.getKind().startsWith("latest" + kind)
								|| p.getKind().startsWith("legacy" + kind)
								|| p.getKind().startsWith("get" + kind) || p
								.getKind().startsWith(kind))) {
							unificatePredicate(p.getScheme(), id, value);
						}
					}
				}
			}
		}

		return childrenRule;
	}

	// unification of Rule: e.g. A(?x,?y) and method call
	// unificatePredicate(["?x","?y"],"?x","1") --> A(1,?y)
	private void unificatePredicate(ArrayList<String> attributesRule,
			String idValue, String value) {
		if (attributesRule.contains(idValue)) {
			attributesRule.set(attributesRule.indexOf(idValue), value);
		}

	}

	private void putFactToDB(Fact newFact) throws URISyntaxException,
			ParseException, IOException {
		Database db = new Database();
		// put fact to database: "Player2(4,'Lisa',40)" (timestamp will be added
		// automatically)
		db.putToDatabase(newFact.toString(), newFact.getListOfValues().get(0));
	}

	// generate temporary results of all joins of a rule step by step
	private Predicate join(ArrayList<Predicate> predicates) {
		Predicate temp = null;
		if (predicates.size() > 0) {
			temp = predicates.get(0);
			if (temp.getRelation() != null) {
				if (!temp.getRelation().isEmpty()) {
					ArrayList<ArrayList<String>> facts2;
					for (int i = 1; i < predicates.size(); i++) {
						Predicate p2 = predicates.get(i);
						ArrayList<PairofInteger> equalList = getEqualList(
								temp.getScheme(), p2.getScheme());
						ArrayList<ArrayList<String>> restemp = new ArrayList<ArrayList<String>>();
						facts2 = p2.getRelation();

						if (predicates.get(i).isNot()) {
							for (ArrayList<String> fact1 : temp.getRelation()) {
								if (facts2 == null
										|| getTempNotResult(fact1, equalList,
												facts2)) {
									restemp.add(fact1);
								}
							}
							temp = new Predicate("temp", temp.getScheme()
									.size(), temp.getScheme(), restemp);
						}

						else {
							if (facts2 != null) {
								for (ArrayList<String> fact1 : temp
										.getRelation()) {
									ArrayList<ArrayList<String>> results = getTempJoinResult(
											fact1, equalList, facts2);
									if (results != null)
										restemp.addAll(results);
								}
							}
							ArrayList<Integer> liste = new ArrayList<Integer>();
							for (PairofInteger wert : equalList)
								liste.add(wert.getP2());
							ArrayList<String> newSchema = new ArrayList<String>();
							newSchema.addAll(temp.getScheme());
							for (int j = 0; j < p2.getScheme().size(); j++)
								if (!liste.contains(j))
									newSchema.add(p2.getScheme().get(j));
							temp = new Predicate("temp", newSchema.size(),
									newSchema, restemp);
						}

					}
				}
			}
		}
		return temp;

	}

	// generate temporary results of join, e.g.: C(?y,?z) :-
	// A(?x,?y),B(?x,?z).
	// join values of A und B on attribute ?x
	private ArrayList<ArrayList<String>> getTempJoinResult(
			ArrayList<String> fact1, ArrayList<PairofInteger> equalList,
			ArrayList<ArrayList<String>> fact2) {
		ArrayList<ArrayList<String>> result = null;
		ArrayList<Integer> liste = new ArrayList<Integer>();
		for (PairofInteger wert : equalList)
			liste.add(wert.getP2());

		for (ArrayList<String> oneOfFact2 : fact2) {
			boolean joinPredicate = true;
			for (PairofInteger wert : equalList) {
				if (!fact1.get(wert.getP1()).equals(
						oneOfFact2.get(wert.getP2())))
					joinPredicate = false;
			}
			if (joinPredicate == true) {
				ArrayList<String> temp = new ArrayList<String>();
				temp.addAll(fact1);

				for (int i = 0; i < oneOfFact2.size(); i++) {
					if (!liste.contains(i))
						temp.add(oneOfFact2.get(i));
				}

				if (result == null)
					result = new ArrayList<ArrayList<String>>();
				result.add(temp);
			}
		}

		return result;

	}

	// generate temporary results of not, e.g.: C(?y,?z) :- A(?x,?y),
	// not B(?x,?z).
	// Put all values of ?x from A which aren't in the ?x values of B
	private boolean getTempNotResult(ArrayList<String> fact1,
			ArrayList<PairofInteger> equalList,
			ArrayList<ArrayList<String>> fact2) {
		ArrayList<Integer> liste = new ArrayList<Integer>();
		for (PairofInteger wert : equalList)
			liste.add(wert.getP2());

		for (ArrayList<String> oneOfFact1 : fact2) {
			boolean joinPredicate = true;
			for (PairofInteger wert : equalList) {
				if (!fact1.get(wert.getP1()).equals(
						oneOfFact1.get(wert.getP2())))
					joinPredicate = false;
			}

			if (joinPredicate == true) {
				return false;
			}
		}

		return true;
	}

	// generate results of a predicate based on the facts, e.g. A(?x,?y) and
	// edb-fact
	// A(1,2)
	// result is [1,2] and return is true if there are results
	private boolean getFacts(Predicate predicate) {
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		String kind = predicate.getKind();
		int number = predicate.getNumberSchemeEntries();
		boolean found = false;
		for (Fact value : facts) {
			if (value.getKind().equals(kind)
					&& value.getListOfValues().size() == number) {
				found = true;
				boolean set = true;
				int i = 0;
				for (String wert : predicate.getScheme()) {
					if (!wert.startsWith("?"))
						if (!value.getListOfValues().get(i).equals(wert)) {
							set = false;
							break;
						}
					i++;

				}
				if (set)
					set = testIfMagicSetExists(value);
				if (set)
					values.add(value.getListOfValues());
			}
		}
		if (values.isEmpty())
		// test if entity exists in datastore
		{
			if (!predicate.getScheme().get(0).startsWith("?")) {
				// DatastoreService ds = DatastoreServiceFactory
				// .getDatastoreService();
				//
				// Query playerQuery = new Query(kind).setAncestor(
				// KeyFactory.createKey(kind.replaceAll("\\d", ""),
				// predicate.getScheme().get(0))).addSort("ts",
				// SortDirection.DESCENDING);
				// List<Entity> results = ds.prepare(playerQuery).asList(
				// FetchOptions.Builder.withDefaults().limit(1));
				String id = predicate.getScheme().get(0);
				Entity results = null;
				if (!(kind.startsWith("latest") || kind.startsWith("legacy"))) {
					results = db.getLatestEntity(kind, id);
				}
				if (results != null) {

					// if (results.get(0).getProperties().size() == number) {
					found = true;
					boolean set = true;
					ArrayList<String> valueNew = new ArrayList<String>();
					for (String wert : predicate.getScheme()) {
						if (!wert.startsWith("?"))
							valueNew.add(wert);
						else {
							if (wert.equals("?id2")) {

								if (results.getProperty("pid") == null)
									valueNew.add("null");
								else if (results.getProperty("pid").getClass()
										.equals(String.class))
									valueNew.add("'"
											+ results.getProperty(("pid"))
													.toString() + "'");

								else
									valueNew.add(results.getProperty(("pid"))
											.toString());

							} else {
								if (results.getProperty(wert.substring(1)
										.replaceAll("\\d", "")) == null)
									valueNew.add("null");
								else if (results

										.getProperty(
												wert.substring(1).replaceAll(
														"\\d", "")).getClass()
										.equals(String.class))
									valueNew.add("'"
											+ results

											.getProperty(
													wert.substring(1)
															.replaceAll("\\d",
																	""))
													.toString() + "'");

								else
									valueNew.add(results

									.getProperty(
											wert.substring(1).replaceAll("\\d",
													"")).toString());
							}
						}
					}
					Fact v = new Fact(kind, valueNew);
					if (!factExists(facts, v))
						facts.add(v);
					if (set)
						set = testIfMagicSetExists(v);
					if (set)
						values.add(valueNew);
					// }
				}
			}

			/*
			 * else { DatastoreService ds = DatastoreServiceFactory
			 * .getDatastoreService();
			 * 
			 * Query playerQuery = new Query(kind).setAncestor(
			 * KeyFactory.createKey(kind.replaceAll("\\d", ""),
			 * unificationMap.get(0).get("value"))).addSort( "ts",
			 * SortDirection.DESCENDING); List<Entity> results =
			 * ds.prepare(playerQuery).asList(
			 * FetchOptions.Builder.withDefaults()); if (!results.isEmpty()) {
			 * for (Entity result : results) { if (result.getProperties().size()
			 * == number) { found = true; boolean set = true; ArrayList<String>
			 * valueNew = new ArrayList<String>(); for (String wert :
			 * predicate.getScheme()) { if (!wert.startsWith("?"))
			 * valueNew.add(wert); else { if (results.get(0)
			 * .getProperty(wert.substring(1)) .getClass().equals(String.class))
			 * valueNew.add("'" + results .get(0) .getProperty(
			 * wert.substring(1)) .toString() + "'");
			 * 
			 * else valueNew.add(results.get(0) .getProperty(wert.substring(1))
			 * .toString()); } } Fact v = new Fact(kind, valueNew); if
			 * (!factExists(facts, v)) facts.add(v); if (set) set =
			 * testIfMagicSetExists(v); if (set) values.add(valueNew); } } } }
			 */
		}
		predicate.setRelation(values);
		if (found && !values.isEmpty()) {
			generateMagicSet(values, predicate.getKind());
			return true;
		} else
			return false;

	}

	// test if a value of a Fact exists in the MagicSet View
	private boolean testIfMagicSetExists(Fact value) {
		boolean exists = true;
		if (magicList != null)
			for (MagicCondition m : magicList) {
				for (PairForMagicCondition pm : m.getRight())
					if (value.getKind().equals(pm.getKind())) {
						if (m.getNameOfMagicView() != null) {
							ArrayList<String> factsOfMGView = getMGViewFacts(m
									.getNameOfMagicView());
							if (factsOfMGView != null)
								if (!factsOfMGView.contains(value
										.getListOfValues().get(
												pm.getPositionId())))
									exists = false;
						}
					}
			}
		return exists;
	}

	// get Facts of MagicSet View
	private ArrayList<String> getMGViewFacts(String nameOfMagicView) {
		ArrayList<String> mgViewFacts = null;
		for (Fact value : facts) {
			if (value.getKind().equals(nameOfMagicView)
					&& value.getListOfValues().size() == 1) {
				if (mgViewFacts == null)
					mgViewFacts = new ArrayList<String>();
				mgViewFacts.add(value.getListOfValues().get(0));
			}
		}
		return mgViewFacts;
	}

	// generate MagicSet View in Facts
	private ArrayList<ArrayList<String>> generateMagicSet(
			ArrayList<ArrayList<String>> values, String kind) {

		if (magicList != null && !magicList.isEmpty()) {

			for (MagicCondition magicCondition : magicList) {
				if (magicCondition.getLeft().getKind().equals(kind)) {
					if (magicCondition.hasAlreadyResults() == false) {
						String newViewName = "MG" + numberOfMGView;
						if (values.isEmpty()) {
							ArrayList<String> nullList = new ArrayList<String>();
							nullList.add("null");
							facts.add(new Fact(newViewName, nullList));
						} else
							for (ArrayList<String> valueList : values) {
								facts.add(new Fact(
										newViewName,
										new ArrayList<String>(
												valueList
														.subList(
																magicCondition
																		.getLeft()
																		.getPositionId(),
																magicCondition
																		.getLeft()
																		.getPositionId() + 1))));
							}
						magicCondition.setAlreadyFoundResults(true);
						magicCondition.setNameOfMagicView(newViewName);
						// setNewMagicPredicateToCorrespondingRules(
						// magicCondition, newViewName);
						if (!values.isEmpty())
							setNewMagicIdToCorrespondingRules(
									magicCondition,
									values.get(0).get(
											magicCondition.getLeft()
													.getPositionId()));
						numberOfMGView++;
					}

				}

			}
		}
		return values;
	}

	// set MagicSet Id value to corresPondingRules
	private void setNewMagicIdToCorrespondingRules(MagicCondition m,
			String idValue) {
		for (Rule rule : rules) {
			for (PairForMagicCondition pm : m.getRight()) {
				if (rule.getHead().getKind().equals(pm.getKind())) {
					rule.getHead().getScheme().set(pm.getPositionId(), idValue);
				}
				for (Predicate p : rule.getPredicates()) {
					if (p.getKind().equals(pm.getKind())) {
						p.getScheme().set(pm.getPositionId(), idValue);
					}
				}
			}
		}
	}

	// set MagicSet View as an extra Predicate to corresPondingRules
	private void setNewMagicPredicateToCorrespondingRules(MagicCondition m,
			String newViewName) {
		for (Rule rule : rules) {
			for (PairForMagicCondition pm : m.getRight())
				if (rule.getHead().getKind().equals(pm.getKind())) {
					ArrayList<String> scheme = new ArrayList<String>();
					scheme.add(rule.getHead().getScheme()
							.get(pm.getPositionId()));
					rule.getPredicates().add(
							new Predicate(newViewName, 1, scheme));
				}
		}
	}

	// rename rules and delete Conditions
	private void rulesRename(Rule rule) {
		if (rule.getConditions() != null)
			for (Iterator<Condition> iterator = rule.getConditions().iterator(); iterator
					.hasNext();) {
				Condition cond = iterator.next();
				if (cond.getOperator().equals("=")
						&& cond.getLeftOperand().startsWith("?")
						&& cond.getRightOperand().startsWith("?")) {

					String left = cond.getLeftOperand();
					String right = cond.getRightOperand();
					generateMagicCondition(rule.getRuleBody().getPredicates(),
							left, right);
					renameVariablesOfAllPredicates(rule, left, right);
					iterator.remove();
				}
			}
	}

	// if a condition for a magic set exists--> generate new MagicCondition with
	// all rules which need this magic set
	private void generateMagicCondition(ArrayList<Predicate> predicates,
			String left, String right) {
		for (int i = 0; i < predicates.size(); i++) {
			for (int j = i + 1; j < predicates.size(); j++) {
				if (predicates.get(i).getScheme().contains(right)
						&& predicates.get(j).getScheme().contains(left)) {
					PairForMagicCondition leftPair = new PairForMagicCondition(
							predicates.get(i).getKind(), predicates.get(i)
									.getScheme().indexOf(right));
					PairForMagicCondition rightPair = new PairForMagicCondition(
							predicates.get(j).getKind(), predicates.get(j)
									.getScheme().indexOf(left));
					MagicCondition mCondition = null;
					if (magicList != null)
						if (!magicList.isEmpty()) {
							boolean found = false;
							int position = 0;
							for (int x = 0; x < magicList.size(); x++)
								if (magicList.get(x).getLeft().equals(leftPair)) {
									found = true;
									position = x;
								}

							if (found) {
								mCondition = magicList.get(position);
								if (!mCondition.contains(rightPair)) {
									mCondition.getRight().add(rightPair);
								}
							} else {
								mCondition = new MagicCondition(leftPair,
										rightPair);
								magicList.add(mCondition);
							}

						} else {
							mCondition = new MagicCondition(leftPair, rightPair);
							magicList.add(mCondition);
						}

					else {
						magicList = new ArrayList<MagicCondition>();
						mCondition = new MagicCondition(leftPair, rightPair);
						magicList.add(mCondition);
					}
					generateSubMagicCondition(mCondition, predicates.get(j)
							.getKind(),
							predicates.get(j).getScheme().indexOf(left),
							predicates.get(j).getScheme().size());

				}
			}

		}
	}

	// insert rules in MagicCondition that depend on this MagicCondition
	private void generateSubMagicCondition(MagicCondition mCondition,
			String kind, int index, int number) {
		for (Rule r : rules)
			if (r.getHead().getKind().equals(kind)
					&& r.getHead().getScheme().size() == number) {
				String id = r.getHead().getScheme().get(index);
				for (Predicate p : r.getPredicates()) {
					ArrayList<String> schema = p.getScheme();
					if (p.getScheme().contains(id)) {
						PairForMagicCondition rightPair = new PairForMagicCondition(
								p.getKind(), schema.indexOf(id));
						if (!mCondition.contains(rightPair)) {
							mCondition.getRight().add(rightPair);
							generateSubMagicCondition(mCondition, p.getKind(),
									schema.indexOf(id), schema.size());
						}
					}
				}
			}
	}

	// tests if kind with ?id is avalaible in facts
	private boolean existIDForKind(String kind, String id) {
		for (Fact value : facts) {
			if ((value.getKind().startsWith("latest" + kind)
					|| value.getKind().startsWith("legacy" + kind)
					|| value.getKind().startsWith("get" + kind) || value
					.getKind().startsWith(kind))
					&& value.getListOfValues().get(0).equals(id)) {
				return true;
			}
		}
		return false;
	}

}
