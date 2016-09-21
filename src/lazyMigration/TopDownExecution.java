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
import datalog.Condition;
import datalog.Fact;
import datalog.MagicCondition;
import datalog.MigrationExecution;
import datalog.PairForMagicCondition;
import datalog.Predicate;
import datalog.Rule;
import datalog.RuleBody;
import datastore.DatalutionDatastoreService;

/**
 * This class executes datalog rules in a top down approach it is derived of
 * MigrationExecution class
 * 
 * @author Stephanie Sombach and Katharina Wiech
 */

public class TopDownExecution extends MigrationExecution {

	/** the top goal for the topdown execution */
	private Predicate goal;
	/** a rule goal tree with a top goal and subgoals */
	private RuleGoalTree tree;
	/** this map stores attributes and their values for unification */
	private Map<String, String> unificationMap;
	/** facts that will be written to database */
	private ArrayList<Fact> putFacts;
	/** contains all information for magic sets */
	private List<MagicCondition> magicList = null;
	/** Datastore wrapper for commands to the datastore */
	private DatalutionDatastoreService db = new DatalutionDatastoreService();

	/**
	 * Constructor: set edb facts, rules, goal and unificationMap
	 * 
	 * @param txn
	 *            transaction for datastore operations
	 * @param facts
	 *            edb facts
	 * @param rules
	 *            datalog rules
	 * @param goal
	 *            top goal for topdown execution
	 * @param unificationMap
	 *            stores information for unification
	 */
	public TopDownExecution(ArrayList<Fact> facts,
			ArrayList<Rule> rules, Predicate goal,
			Map<String, String> unificationMap) {
		super(facts, rules);
		this.goal = goal;
		this.unificationMap = unificationMap;
	}

	/**
	 * this method is the starting point of top down execution of datalog rules
	 * 
	 * @return all facts that match the goal
	 * @see datalog.Fact
	 */
	public ArrayList<Fact> getAnswers() throws ParseException, IOException,
			URISyntaxException, InputMismatchException, EntityNotFoundException {

		ArrayList<Fact> answer = new ArrayList<Fact>();
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
			for (ArrayList<String> oneResult : goal.getRelation()) {
				answer.add(new Fact(goal.getKind(), oneResult));
			}
		}

		// add put facts to put list
		if (putFacts.size() != 0) {
			for (Fact fact : putFacts) {
				putFactToDB(fact);
			}

		}
		return answer;

	}

	/**
	 * this method recursively executes all datalog rules
	 * 
	 * @param tree
	 *            every datalog rule represents a rulegoal tree which is
	 *            executed top down
	 * 
	 * @return all temporary facts that match the rule head
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

		// add put facts to put list
		for (ArrayList<String> oneResult : result) {
			Fact newFact = new Fact(tree.getGoal().getKind(), oneResult);
			if (!factExists(facts, newFact) && !factExists(putFacts, newFact)
					&& !newFact.getKind().startsWith("get"))
				putFacts.add(newFact);

		}
		return result;
	}

	/**
	 * this method checks whether a fact exists in the facts list
	 * 
	 * @param factList
	 *            facts list
	 * @param putFact
	 *            fact that is checked
	 * @return true if fact already exists
	 */
	private boolean factExists(ArrayList<Fact> factList, Fact putFact) {
		for (Fact fact : factList) {
			if (fact.equals(putFact))
				return true;
		}
		return false;
	}

	/**
	 * only get the result-variables of predicate which are in the scheme of the
	 * head Example A(?x):-B(?x,?y)--> only extract the ?x variable of B
	 * 
	 * @param results
	 *            all results of rule body stored in a temp Predicate
	 * @param scheme
	 *            scheme of rule head
	 * @return a list of results stored in a list of strings
	 */
	private ArrayList<ArrayList<String>> getResults(Predicate results,
			ArrayList<String> scheme) {
		ArrayList<ArrayList<String>> answer = new ArrayList<ArrayList<String>>();
		// minimize result to match variables of scheme
		for (ArrayList<String> oneMap : results.getRelation()) {
			ArrayList<String> oneAnswer = new ArrayList<String>();
			for (String variableOfScheme : scheme)
				if (variableOfScheme.startsWith("?"))
					if (results.getScheme().contains(variableOfScheme))
						oneAnswer.add(oneMap.get(results.getScheme().indexOf(
								variableOfScheme)));
					else {
						oneAnswer.add(""); // unlimited variable
					}
				else
					oneAnswer.add(variableOfScheme);
			// test if result already exists in the end result
			boolean alreadyExist = false;
			for (ArrayList<String> iterateAnswer : answer)
				if (oneAnswer.containsAll(iterateAnswer)
						&& iterateAnswer.containsAll(oneAnswer))
					alreadyExist = true;
			// don't store duplicate results
			if (!alreadyExist) {
				answer.add(oneAnswer);
			}
		}
		return answer;
	}

	/**
	 * unify all variables which are in the unificationMap
	 * 
	 * @param childrenRule
	 *            rule that has to be unified
	 * @return unified rule
	 * @see datalog.Predicate
	 * @see datalog.Rule
	 */
	private Rule unifyRule(Rule childrenRule) {
		// first rename all rules according to conditions
		rulesRename(childrenRule);
		Predicate head = childrenRule.getHead();
		RuleBody body = childrenRule.getRuleBody();

		String kind = unificationMap.get("kind");
		int positionValue = Integer.parseInt(unificationMap.get("position"));
		String unificationValue = unificationMap.get("value");
		if (head.getKind().replaceAll("\\d", "").equals(kind)) {
			ArrayList<String> schemeRuleHead = head.getScheme();
			String idValue = schemeRuleHead.get(positionValue);
			// unify head-predicates which match with kind
			unifyPredicate(schemeRuleHead, idValue, unificationValue);

		}
		// unify body-predicates which match with kind
		String valueOfIdVariable = null;
		for (Predicate predicate : body.getPredicates()) {
			if (predicate.getKind().replaceAll("\\d", "").equals(kind)) {
				ArrayList<String> schemePredicate = predicate.getScheme();
				valueOfIdVariable = schemePredicate.get(positionValue);
				unifyPredicate(schemePredicate, valueOfIdVariable,
						unificationValue);
			}
		}
		if (valueOfIdVariable != null) {
			if (!head.getKind().replaceAll("\\d", "").equals(kind)) {
				// unify head-predicates which don't match with kind
				unifyPredicate(head.getScheme(), valueOfIdVariable,
						unificationValue);
			}
			// unify body-predicates which don't match with kind
			for (Predicate predicate : body.getPredicates()) {
				if (!predicate.getKind().replaceAll("\\d", "").equals(kind)) {
					unifyPredicate(predicate.getScheme(),
							valueOfIdVariable, unificationValue);
				}
			}
		}

		return childrenRule;
	}

	/**
	 * unification scheme of Predicate: e.g. A(?x,?y) and method call example:
	 * unifyPredicate(["?x","?y"],"?x","1") --> A(1,?y)
	 * 
	 * @param scheme
	 *            scheme to be unified
	 * @param valueOfIdVariable
	 *            variable that has to be changed
	 * @param unificationValue
	 *            change value
	 */
	private void unifyPredicate(ArrayList<String> scheme,
			String valueOfIdVariable, String unificationValue) {
		if (scheme.contains(valueOfIdVariable)) {
			scheme.set(scheme.indexOf(valueOfIdVariable), unificationValue);
		}

	}

	/**
	 * -rename rules and delete Conditions with two variables, e.g., ?x=?y"
	 * -generate Magic Conditions based on these Conditions
	 * 
	 * this method is, inter alia, important for not operations, e.g.
	 * "A(?x),not B(?y), ?x=?y" will be changed to --> "A(?y),not B(?y)"
	 * 
	 * @param rule
	 *            rule that will be renamed
	 */
	private void rulesRename(Rule rule) {
		if (rule.getConditions() != null)
			for (Iterator<Condition> iterator = rule.getConditions().iterator(); iterator
					.hasNext();) {
				Condition cond = iterator.next();
				// check if it's a condition with two variables, i.e. a Magic
				// Condition!
				if (cond.getOperator().equals("=")
						&& cond.getLeftOperand().startsWith("?")
						&& cond.getRightOperand().startsWith("?")) {

					String left = cond.getLeftOperand();
					String right = cond.getRightOperand();
					// generate Magic Condition
					generateMagicCondition(rule.getRuleBody().getPredicates(),
							left, right);
					// rename variables
					renameVariablesOfAllPredicates(rule, left, right);
					iterator.remove();
				}
			}
	}

	/**
	 * put fact to database (timestamp will be added automatically)
	 * 
	 * @param newFact
	 *            fact that will be added to datastore
	 * @see datalog.Fact
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
	 * @see datalog.Predicate
	 */
	private Predicate join(ArrayList<Predicate> predicates) {
		Predicate tempPredicate = null;
		if (predicates.size() > 0) {
			// start with first predicate
			tempPredicate = predicates.get(0);
			// check if first predicate has results to be joined, otherwise no
			// results
			if (tempPredicate.getRelation() != null) {
				if (!tempPredicate.getRelation().isEmpty()) {
					ArrayList<ArrayList<String>> factsOfNextPredicate;
					// join every following predicate step by step
					for (int i = 1; i < predicates.size(); i++) {
						Predicate nextPredicate = predicates.get(i);
						ArrayList<PairofInteger> equalList = getListOfEqualVariables(
								tempPredicate.getScheme(),
								nextPredicate.getScheme());
						ArrayList<ArrayList<String>> restemp = new ArrayList<ArrayList<String>>();
						factsOfNextPredicate = nextPredicate.getRelation();
						// the join with following predicate is a not operation
						if (predicates.get(i).isNot()) {
							// for every fact of tempPredicate check the not
							// operation with the facts of nextPredicate
							for (ArrayList<String> factOfTempPredicate : tempPredicate
									.getRelation()) {
								if (factsOfNextPredicate == null
										|| getTempNotResult(
												factOfTempPredicate, equalList,
												factsOfNextPredicate)) {
									restemp.add(factOfTempPredicate);
								}
							}
							tempPredicate = new Predicate("temp", tempPredicate
									.getScheme().size(),
									tempPredicate.getScheme(), restemp);
						}
						// the join with following predicate is a normal join
						else {
							if (factsOfNextPredicate != null) {
								// for every fact of tempPredicate do a join
								// with the facts of nextPredicate
								for (ArrayList<String> factOfTempPredicate : tempPredicate
										.getRelation()) {
									ArrayList<ArrayList<String>> results = getTempJoinResult(
											factOfTempPredicate, equalList,
											factsOfNextPredicate);
									if (results != null)
										restemp.addAll(results);
								}
							}
							// new schema after join: consists of variables of
							// both predicates
							ArrayList<Integer> listWithPositions = new ArrayList<Integer>();
							for (PairofInteger pairWithPositions : equalList)
								listWithPositions.add(pairWithPositions
										.getPosition2());
							ArrayList<String> newSchema = new ArrayList<String>();
							newSchema.addAll(tempPredicate.getScheme());
							for (int j = 0; j < nextPredicate.getScheme()
									.size(); j++)
								if (!listWithPositions.contains(j))
									newSchema.add(nextPredicate.getScheme()
											.get(j));
							tempPredicate = new Predicate("temp",
									newSchema.size(), newSchema, restemp);
						}

					}
				}
			}
		}
		return tempPredicate;

	}

	/**
	 * generate temporary results of join, e.g.: C(?y,?z) :- A(?x,?y),B(?x,?z).
	 * join one fact of A with facts of B on variable ?x
	 * 
	 * @param fact1
	 *            one fact that will be joined with facts2
	 * @param equaList
	 *            variable list for joining
	 * @param facts2
	 *            list of facts that will be joined with fact1
	 * @return list of results of join
	 */
	private ArrayList<ArrayList<String>> getTempJoinResult(
			ArrayList<String> fact1, ArrayList<PairofInteger> equalList,
			ArrayList<ArrayList<String>> facts2) {
		ArrayList<ArrayList<String>> result = null;
		ArrayList<Integer> listWithPositions = new ArrayList<Integer>();
		for (PairofInteger pairWithPositions : equalList)
			listWithPositions.add(pairWithPositions.getPosition2());

		for (ArrayList<String> oneOfFact2 : facts2) {
			boolean joinCondition = true;
			for (PairofInteger pairWithPositions : equalList) {
				if (!fact1.get(pairWithPositions.getPosition1()).equals(
						oneOfFact2.get(pairWithPositions.getPosition2())))
					joinCondition = false;
			}
			if (joinCondition == true) {
				ArrayList<String> temp = new ArrayList<String>();
				temp.addAll(fact1);

				for (int i = 0; i < oneOfFact2.size(); i++) {
					if (!listWithPositions.contains(i))
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
	 * check for not operation, e.g.: C(?y,?z) :- A(?x,?y), not B(?x,?z). check
	 * if variable ?x from one fact of A isn't in the ?x values of B
	 * 
	 * @param fact1
	 *            one fact for not operation
	 * @param equaList
	 *            variable list for not operation
	 * @param facts2
	 *            list of facts for not operation
	 * @return return if not condition is true
	 */
	private boolean getTempNotResult(ArrayList<String> fact1,
			ArrayList<PairofInteger> equalList,
			ArrayList<ArrayList<String>> facts2) {
		ArrayList<Integer> listWithPositions = new ArrayList<Integer>();
		for (PairofInteger pairWithPositions : equalList)
			listWithPositions.add(pairWithPositions.getPosition2());

		for (ArrayList<String> oneOfFacts2 : facts2) {
			boolean notCondition = true;
			for (PairofInteger pairWithPositions : equalList) {
				if (!fact1.get(pairWithPositions.getPosition1()).equals(
						oneOfFacts2.get(pairWithPositions.getPosition2())))
					notCondition = false;
			}

			if (notCondition == true) {
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
		// first check if there are already results in the fact list
		for (Fact fact : facts) {
			if (fact.getKind().equals(kind)
					&& fact.getListOfValues().size() == number) {
				found = true;
				int i = 0;
				for (String variable : predicate.getScheme()) {
					if (!variable.startsWith("?"))
						if (!fact.getListOfValues().get(i).equals(variable)) {
							break;
						}
					i++;

				}
				values.add(fact.getListOfValues());
			}
		}

		// if no results in the fact list -> test if entity exists in datastore
		if (values.isEmpty())
			if (!predicate.getScheme().get(0).startsWith("?")) {
				String id = predicate.getScheme().get(0);
				Entity resultEntity = null;
				if (getLatest) {
					resultEntity = db.getLatestEntity(kind,
							Integer.parseInt(id));
				}
				if (resultEntity != null) {

					found = true;

					// get values of entity based on predicate scheme
					ArrayList<String> valueNew = getValuesOfEntity(
							resultEntity, predicate.getScheme(),
							predicate.getKind());

					Fact v = new Fact(kind, valueNew);
					if (!factExists(facts, v))
						facts.add(v);
					values.add(valueNew);

				}
			}
		// set results to predicate
		predicate.setRelation(values);
		if (found && !values.isEmpty()) {
			// generate all Magic Sets based on the kind of this predicate
			generateMagicSet(values, predicate.getKind());
			return true;
		} else
			return false;

	}

	/**
	 * get values of entity based on the variables of a scheme
	 * 
	 * @param entity
	 *            get values of this entity
	 * @param scheme
	 *            scheme with variables
	 * @param kind
	 *            value of kind
	 * @return values of entity
	 */
	private ArrayList<String> getValuesOfEntity(Entity entity,
			ArrayList<String> scheme, String kind) {
		ArrayList<String> valuesOfEntity = new ArrayList<String>();
		for (String property : scheme) {
			if (!property.startsWith("?"))
				valuesOfEntity.add(property);
			else {
				if (mapForRenamedVariables.containsKey(kind)) {
					String[] valuesForRenamedVariable = mapForRenamedVariables
							.get(kind);
					String newValue = valuesForRenamedVariable[0];
					String originalValue = valuesForRenamedVariable[1];
					if (newValue.equals(property))

						property = originalValue;
				}
				property = property.substring(1).replaceAll("\\d", "");
				if (entity.getProperty(property) == null)
					valuesOfEntity.add("null");

				else if (entity.getProperty(property).getClass()
						.equals(String.class))
					valuesOfEntity.add("'"
							+ entity.getProperty(property).toString() + "'");

				else
					valuesOfEntity.add(entity.getProperty(property).toString());

			}
		}
		return valuesOfEntity;
	}

	/**
	 * generate Magic Set id for rules
	 * 
	 * @param values
	 *            contains Magic Set id
	 * @param kind
	 *            value of kind
	 */
	private void generateMagicSet(ArrayList<ArrayList<String>> values,
			String kind) {

		if (magicList != null && !magicList.isEmpty()) {
			// check if a magic condition exists for this kind
			for (MagicCondition magicCondition : magicList) {
				if (magicCondition.getHeadPair().getKind().equals(kind)) {
					if (magicCondition.hasAlreadyResults() == false) {
						// found a magic condition and also found the result/id
						// for this for magic set <=> Magic Set ID!
						magicCondition.setAlreadyFoundResults(true);
						// set the id to corresponding rules based on this magic
						// condition!
						setNewMagicIdToCorrespondingRules(
								magicCondition,
								values.get(0).get(
										magicCondition.getHeadPair()
												.getPositionId()));
					}

				}

			}
		}
	}

	/**
	 * set MagicSet Id value to corresPondingRules
	 * 
	 * @param mCondition
	 *            MagicCondition that holds information on rules that has to be
	 *            changed with idValue
	 * @param idValue
	 *            value of MagicSet Id
	 * @return true if facts exists
	 */
	private void setNewMagicIdToCorrespondingRules(MagicCondition mCondition,
			String idValue) {
		for (Rule rule : rules) {
			for (PairForMagicCondition pairForMCond : mCondition
					.getListOfSubPairs()) {
				if (rule.getHead().getKind().equals(pairForMCond.getKind())) {
					rule.getHead().getScheme()
							.set(pairForMCond.getPositionId(), idValue);
				}
				for (Predicate p : rule.getPredicates()) {
					if (p.getKind().equals(pairForMCond.getKind())) {
						p.getScheme()
								.set(pairForMCond.getPositionId(), idValue);
					}
				}
			}
		}
	}

	/**
	 * if a condition for a magic set exists--> generate new MagicCondition with
	 * all rules which need this magic set
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
					boolean found = false;
					int position = 0;
					if (magicList == null)
						magicList = new ArrayList<MagicCondition>();
					if (!magicList.isEmpty()) {

						for (int x = 0; x < magicList.size(); x++)
							if (magicList.get(x).getHeadPair().equals(leftPair)) {
								found = true;
								position = x;
							}

					}
					if (found) {
						mCondition = magicList.get(position);
						if (!mCondition.contains(rightPair)) {
							mCondition.getListOfSubPairs().add(rightPair);
						}
					} else {
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
	 * insert into MagicCondition: information to corresponding predicates that
	 * depend on this MagicCondition
	 * 
	 * @param mCondition
	 *            MagicCondition that will be investigated
	 * @param kind
	 *            kind of predicate
	 * @param index
	 *            index of id variable
	 * @param number
	 *            size of predicate scheme
	 */
	private void generateSubMagicCondition(MagicCondition mCondition,
			String kind, int index, int number) {
		for (Rule rule : rules)
			if (rule.getHead().getKind().equals(kind)
					&& rule.getHead().getScheme().size() == number) {
				String id = rule.getHead().getScheme().get(index);
				for (Predicate predicate : rule.getPredicates()) {
					ArrayList<String> schema = predicate.getScheme();
					if (predicate.getScheme().contains(id)) {
						PairForMagicCondition rightPair = new PairForMagicCondition(
								predicate.getKind(), schema.indexOf(id));
						if (!mCondition.contains(rightPair)) {
							mCondition.getListOfSubPairs().add(rightPair);
							generateSubMagicCondition(mCondition,
									predicate.getKind(), schema.indexOf(id),
									schema.size());
						}
					}
				}
			}
	}

}
