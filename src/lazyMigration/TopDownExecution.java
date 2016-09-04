package lazyMigration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import parserPutToDatalog.ParseException;
import datastore.DatalutionDatastoreService;
import datalog.Condition;
import datalog.Fact;
import datalog.MagicCondition;
import datalog.MigrationExecution;
import datalog.PairForMagicCondition;
import datalog.Predicate;
import datalog.Rule;
import datalog.RuleBody;

/**
 * This class executes datalog rules in a top down approach it is derived of @see
 * MigrationExecution
 * 
 * @author Stephanie Sombach and Katharina Wiech
 * @version 1.0
 */

public class TopDownExecution extends MigrationExecution {

	/** the top goal for the topdown execution */
	private Predicate goal;
	/** a rule goal tree with a top goal and subgoals */
	private RuleGoalTree tree;
	/** this map stores attributes and their values for unification */
	private List<Map<String, String>> unificationMap;
	/** facts that will be written to database */
	private ArrayList<Fact> putFacts;
	/** contains all information for magic sets */
	private List<MagicCondition> magicList = null;

	/** Datastore wrapper for commands to the datastore */
	private DatalutionDatastoreService db = new DatalutionDatastoreService();

	/**
	 * constuctor: set edb facts, rules, goal and unificationMap
	 * 
	 * @param facts
	 *            edb facts
	 * @param rules
	 *            datalog rules
	 * @param goal
	 *            top goal for topdown execution
	 * @param unificationMap
	 *            stores information for unification
	 */
	public TopDownExecution(ArrayList<Fact> facts, ArrayList<Rule> rules,
			Predicate goal, List<Map<String, String>> unificationMap) {
		super(facts, rules);
		this.goal = goal;
		this.unificationMap = unificationMap;
	}

	/**
	 * this method is the starting point of top down execution of datalog rules
	 * 
	 * @return all facts that match the goal
	 * @see Fact
	 */
	public ArrayList<Fact> getAnswers() throws ParseException, IOException,
			URISyntaxException, InputMismatchException, EntityNotFoundException {

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
				Rule unifiedRule = unifyRule(r);
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
			}

		}
		// }
		return answer;

	}

	/**
	 * this method recursively executes all datalog rules
	 * 
	 * @param tree
	 *            every datalog rule represents a rulegoal tree which is
	 *            executed top down
	 * @return all temporary facts that match the rule head
	 * @see RuleGoalTree
	 */
	private ArrayList<ArrayList<String>> getAnswersForSubtree(RuleGoalTree tree)
			throws NumberFormatException, EntityNotFoundException {
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
							Rule unifiedRule = unifyRule(r);
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

	/**
	 * this method checks whether a fact exists in the facts table
	 * 
	 * @param factList
	 *            facts table
	 * @param putFact
	 *            fact that is checked
	 * @return true if fact already exists
	 * @see Fact
	 */
	private boolean factExists(ArrayList<Fact> factList, Fact putFact) {
		for (Fact f : factList) {
			if (f.equals(putFact))
				return true;
		}
		return false;
	}

	/**
	 * only get the result-attributes of predicate which are in the scheme of
	 * the head Example A(?x):-B(?x,?y)--> only extract the ?x attribute of B
	 * 
	 * @param results
	 *            all results of rule body stored in a temp Predicate
	 * @param scheme
	 *            scheme of rule head
	 * @return a list of results stored as strings
	 * @see Predicate
	 */
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
						oneAnswer.add(""); // unlimited variable
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

	/**
	 * unificate all attributes which are in the unificationMap
	 * 
	 * @param goal
	 *            unused?
	 * @param childrenRule
	 *            rule that has to be unificated
	 * @return unificated rule
	 * @see Predicate
	 * @see Rule
	 */
	private Rule unifyRule(Rule childrenRule) {
		// first rename all rules according to conditions
		rulesRename(childrenRule);
		Predicate head = childrenRule.getHead();
		RuleBody body = childrenRule.getRuleBody();
		for (Map<String, String> unificationMapEntry : unificationMap) {
			String kind = unificationMapEntry.get("kind");
			int positionValue = Integer.parseInt(unificationMapEntry
					.get("position"));
			String value = unificationMapEntry.get("value");
			if (head.getKind().replaceAll("\\d", "").equals(kind)) {
				ArrayList<String> attributesRuleHead = head.getScheme();
				String idValue = attributesRuleHead.get(positionValue);
				// unificate head-predicates which match with kind
				unificatePredicate(attributesRuleHead, idValue, value);

			}
			// unificate body-predicates which match with kind
			String id = null;
			for (Predicate p : body.getPredicates()) {
				if (p.getKind().replaceAll("\\d", "").equals(kind)) {
					ArrayList<String> attributesRule = p.getScheme();
					String idValue = attributesRule.get(positionValue);
					if (id == null)
						id = idValue;
					unificatePredicate(attributesRule, idValue, value);
				}
			}
			if (id != null) {
				if (!head.getKind().replaceAll("\\d", "").equals(kind)) {
					// unificate head-predicates which don't match with kind
					unificatePredicate(head.getScheme(), id, value);
				}
				// unificate body-predicates which don't match with kind
				for (Predicate p : body.getPredicates()) {
					if (!p.getKind().replaceAll("\\d", "").equals(kind)) {
						unificatePredicate(p.getScheme(), id, value);
					}
				}
			}
		}

		return childrenRule;
	}

	/**
	 * unification scheme of Predicate: e.g. A(?x,?y) and method call example:
	 * unificatePredicate(["?x","?y"],"?x","1") --> A(1,?y)
	 * 
	 * @param attributesRule
	 *            rule to be unificated
	 * @param idValue
	 *            variable that has to be changed
	 * @param value
	 *            change value
	 */
	private void unificatePredicate(ArrayList<String> attributesRule,
			String idValue, String value) {
		if (attributesRule.contains(idValue)) {
			attributesRule.set(attributesRule.indexOf(idValue), value);
		}

	}

	/**
	 * put fact to database (timestamp will be added automatically)
	 * 
	 * @param newFact
	 *            fact that will be added to datastore
	 * @see Fact
	 */
	private void putFactToDB(Fact newFact) throws URISyntaxException,
			ParseException, IOException, InputMismatchException,
			EntityNotFoundException {
		DatalutionDatastoreService db = new DatalutionDatastoreService();

		db.putToDatabase(newFact.toString());
	}

	/**
	 * generate temporary results of all joins of a rule step by step
	 * 
	 * @param predicates
	 *            predicates of rule body that have to be joined
	 * @return joined rule body stored in a temp predicate
	 * @see Predicate
	 */
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

	/**
	 * generate temporary results of join, e.g.: C(?y,?z) :- A(?x,?y),B(?x,?z).
	 * join values of A und B on attribute ?x
	 * 
	 * @param fact1
	 *            list of facts that will be joined with fact2
	 * @param equaList
	 *            attribute list for joining
	 * @param fact2
	 *            list of facts that will be joined with fact1
	 * @return list of results of join
	 */
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

	/**
	 * generate temporary results of not, e.g.: C(?y,?z) :- A(?x,?y), not
	 * B(?x,?z). get all values of ?x from A which aren't in the ?x values of
	 * Bgenerate temporary results of join, e.g.: C(?y,?z) :- A(?x,?y),B(?x,?z).
	 * 
	 * @param fact1
	 *            first list of facts for not operation
	 * @param equaList
	 *            attribute list for not operation
	 * @param fact2
	 *            second list of facts for not operation
	 * @return list of results of not operation
	 */
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

	/**
	 * generate results of a predicate based on the facts, e.g. A(?x,?y) and
	 * edb-fact A(1,2) result is [1,2] and return is true if there are results
	 * 
	 * @param predicate
	 *            Predicate that has information
	 * @return true if facts exists
	 */
	private boolean getFacts(Predicate predicate) throws NumberFormatException,
			EntityNotFoundException {
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
		String kind = predicate.getKind();
		int number = predicate.getNumberSchemeEntries();
		boolean getLatest = predicate.isLatest();
		boolean found = false;

		for (Fact value : facts) {
			if (value.getKind().equals(kind)
					&& value.getListOfValues().size() == number) {
				found = true;
				int i = 0;
				for (String wert : predicate.getScheme()) {
					if (!wert.startsWith("?"))
						if (!value.getListOfValues().get(i).equals(wert)) {
							break;
						}
					i++;

				}
				values.add(value.getListOfValues());
			}
		}
		if (values.isEmpty())
		// test if entity exists in datastore
		{
			if (!predicate.getScheme().get(0).startsWith("?")) {

				String id = predicate.getScheme().get(0);
				Entity results = null;
				if (getLatest) {
					results = db.getLatestEntity(kind, Integer.parseInt(id));
				}
				if (results != null) {

					if (results.getProperties().size() == number) {
						found = true;
						ArrayList<String> valueNew = new ArrayList<String>();
						for (String wert : predicate.getScheme()) {
							if (!wert.startsWith("?"))
								valueNew.add(wert);
							else {
								String property;
								if (mapForRenamedVariables.containsKey(predicate.getKind())) {
									String[] attributes = mapForRenamedVariables.get(predicate
											.getKind());

									if (attributes[0].equals(wert))

										property = attributes[1].substring(1)
												.replaceAll("\\d", "");
									else
										property = wert.substring(1)
												.replaceAll("\\d", "");
								} else {
									property = wert.substring(1).replaceAll(
											"\\d", "");
								}
								if (results.getProperty(property) == null)
									valueNew.add("null");

								else if (results.getProperty(property)
										.getClass().equals(String.class))
									valueNew.add("'"
											+ results.getProperty(property)
													.toString() + "'");

								else
									valueNew.add(results.getProperty(property)
											.toString());

							}
						}
						Fact v = new Fact(kind, valueNew);
						if (!factExists(facts, v))
							facts.add(v);
						values.add(valueNew);
					}
				}
			}
		}
		predicate.setRelation(values);
		if (found && !values.isEmpty()) {
			generateMagicSet(values, predicate.getKind());
			return true;
		} else
			return false;

	}

	/**
	 * generate Magic Set id for rules
	 * 
	 * @param values
	 *            contains Magic Set id
	 * @param kind
	 *            value of kind
	 * @return unused?
	 */
	private ArrayList<ArrayList<String>> generateMagicSet(
			ArrayList<ArrayList<String>> values, String kind) {

		if (magicList != null && !magicList.isEmpty()) {

			for (MagicCondition magicCondition : magicList) {
				if (magicCondition.getLeft().getKind().equals(kind)) {
					if (magicCondition.hasAlreadyResults() == false) {

						magicCondition.setAlreadyFoundResults(true);

						if (!values.isEmpty())
							setNewMagicIdToCorrespondingRules(
									magicCondition,
									values.get(0).get(
											magicCondition.getLeft()
													.getPositionId()));
					}

				}

			}
		}
		return values;
	}

	/**
	 * set MagicSet Id value to corresPondingRules
	 * 
	 * @param m
	 *            MagicCondition that holds information of rules that has to be
	 *            changed with idValue
	 * @param idValue
	 *            value of MagicSet Id
	 * @return true if facts exists
	 */
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

	/**
	 * rename rules and delete Conditions and generate MagicCondtion @see
	 * 
	 * @param rule
	 *            rule that will be renamed
	 */
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

	/**
	 * if a condition for a magic set exists--> generate new MagicCondition @see
	 * MagicCondition with all rules which need this magic set
	 * 
	 * @param predicates
	 *            predicates that will be identified for magicSet
	 * @param left
	 *            left value of MagicCondition
	 * @param right
	 *            right value of MagicCondition
	 */
	private void generateMagicCondition(ArrayList<Predicate> predicates,
			String left, String right) {
		for (int i = 0; i < predicates.size(); i++) {
			for (int j = i + 1; j < predicates.size(); j++) {
				Predicate first = predicates.get(i);
				Predicate second = predicates.get(j);
				if (first.getScheme().contains(right)
						&& second.getScheme().contains(left)) {
					PairForMagicCondition leftPair = new PairForMagicCondition(
							first.getKind(), first.getScheme().indexOf(right));
					PairForMagicCondition rightPair = new PairForMagicCondition(
							second.getKind(), second.getScheme().indexOf(left));
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
					generateSubMagicCondition(mCondition, second.getKind(),
							second.getScheme().indexOf(left), second
									.getScheme().size());

				}
			}

		}
	}

	/**
	 * insert rules in MagicCondition that depend on one MagicCondition
	 * 
	 * @param mCondition
	 *            MagicCondition that will be investigated
	 * @param kind
	 * @param index
	 * @param number
	 */
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
	/*
	 * private boolean existIDForKind(String kind, String id) { for (Fact value
	 * : facts) { if ((value.getKind().startsWith("latest" + kind) ||
	 * value.getKind().startsWith("legacy" + kind) ||
	 * value.getKind().startsWith("get" + kind) || value
	 * .getKind().startsWith(kind)) &&
	 * value.getListOfValues().get(0).equals(id)) { return true; } } return
	 * false; }
	 */

}
