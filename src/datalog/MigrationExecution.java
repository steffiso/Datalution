package datalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * superclass for TopDownExecution with similar methods for both migration
 * approaches: bottom up (not implemented yet!) or top down
 * 
 * @author Stephanie Sombach and Katharina Wiech
 */

public class MigrationExecution {

	/** all edb facts and generated idb facts */
	protected ArrayList<Fact> facts;
	/** all generated datalog rules */
	protected ArrayList<Rule> rules;
	/**
	 * map which holds information of renamed variables based on magic
	 * conditions, especially needed for datastore queries
	 * because we need original names of properties
	 */
	protected Map<String, String[]> mapForRenamedVariables;

	/**
	 * constuctor: set edb facts, rules
	 * 
	 * @param facts
	 *            edb facts
	 * @param rules
	 *            datalog rules
	 */
	public MigrationExecution(ArrayList<Fact> facts, ArrayList<Rule> rules) {
		super();
		this.facts = facts;
		this.rules = rules;
		this.mapForRenamedVariables = new HashMap<String, String[]>();
	}

	/** getter method for facts */
	public ArrayList<Fact> getFacts() {
		return facts;
	}

	/** setter method for facts */
	public void setFacts(ArrayList<Fact> values) {
		this.facts = values;
	}

	/**
	 * get all facts of a Kind
	 * 
	 * @param kind
	 *            kind of predicate
	 * @param number
	 *            number of variables of predicate scheme
	 * @return results of all facts for a given kind and scheme
	 */
	public ArrayList<ArrayList<String>> getResultsOfKind(String kind, int number) {
		ArrayList<ArrayList<String>> answer = new ArrayList<ArrayList<String>>();

		for (Fact value : facts) {
			if (value.getKind().equals(kind)
					&& value.getListOfValues().size() == number) {
				answer.add(value.getListOfValues());
			}
		}
		return answer;
	}

	/**
	 * rename predicates of a rule step by step
	 * 
	 * @param rule
	 *            rule to be renamed
	 * @param left
	 *            new value
	 * @param right
	 *            old value
	 */
	protected void renameVariablesOfAllPredicates(Rule rule, String left,
			String right) {
		renameVariablesOfPredicate(rule.getHead(), left, right);
		for (Predicate pred : rule.getPredicates())
			renameVariablesOfPredicate(pred, left, right);
	}

	/**
	 * rename variables of predicate step by step
	 * 
	 * @param predicate
	 *            predicate to be renamed
	 * @param left
	 *            new value
	 * @param right
	 *            old value
	 */
	protected void renameVariablesOfPredicate(Predicate predicate, String left,
			String right) {
		ArrayList<String> scheme = predicate.getScheme();
		if (scheme.contains(right)) {
			scheme.set(scheme.indexOf(right), left);
			if (!mapForRenamedVariables.containsKey(predicate.getKind()))
				mapForRenamedVariables.put(predicate.getKind(), new String[] {
						left, right });
		}
	}

	/**
	 * generate temporary results of all conditions of a rule step by step
	 * 
	 * @param predResult
	 *            predicate to execute all selections on
	 * @param conditions
	 *            all conditions for selection
	 * @return predicate after all selections
	 */
	protected Predicate selection(Predicate predResult,
			ArrayList<Condition> conditions) {
		for (Condition cond : conditions) {
			predResult = getTempCondResult(predResult, cond);
		}
		return predResult;
	}

	/**
	 * generate temporary results of one condition, e.g.: C(?y,?z) :-
	 * A(?x,?y),B(?x,?z),?y=?z. Only put values of A and B to end result which
	 * satisfies condition ?y=?z.
	 * 
	 * @param predicate
	 *            predicate to execute one selection on
	 * @param condition
	 *            one condition for selection
	 * @return predicate after one selection
	 */
	protected Predicate getTempCondResult(Predicate predicate, Condition condition) {
		ArrayList<ArrayList<String>> facts = new ArrayList<ArrayList<String>>();
		String rightOperand = condition.getRightOperand();
		String leftOperand = condition.getLeftOperand();
		String operator = condition.getOperator();
		List<ArrayList<String>> factList = predicate.getRelation();
		for (ArrayList<String> factOfFactList : factList) {
			String left = "";
			String right = "";
			if (leftOperand.startsWith("?"))
				if (predicate.getScheme().contains(leftOperand))
					left = factOfFactList.get(predicate.getScheme()
							.indexOf(leftOperand));
				else {
					facts.add(factOfFactList);
					continue;
				}
			else
				left = leftOperand;
			if (rightOperand.startsWith("?"))
				if (predicate.getScheme().contains(rightOperand))
					right = factOfFactList.get(predicate.getScheme().indexOf(
							rightOperand));
				else {
					facts.add(factOfFactList);
					continue;
				}
			else
				right = rightOperand;
			boolean isNumberFormat = false;
			if ((isInteger(left) && isInteger(right))
					|| (isDouble(left) && isDouble(right)))
				isNumberFormat = true;
			boolean equalsCondition = false;
			switch (operator) {
			case "=":
				if (isNumberFormat) {
					if (Integer.parseInt(left) == Integer.parseInt(right))
						equalsCondition = true;
				} else if (left.equals(right))
					equalsCondition = true;
				break;
			case "!":
				if (isNumberFormat) {
					if (Integer.parseInt(left) != Integer.parseInt(right))
						equalsCondition = true;
				} else if (!left.equals(right))
					equalsCondition = true;
				break;
			case "<":
				if (isNumberFormat) {
					if (Integer.parseInt(left) < Integer.parseInt(right))
						equalsCondition = true;
				} else if (left.compareTo(right) < 0)
					equalsCondition = true;
				break;
			case ">":
				if (isNumberFormat) {
					if (Integer.parseInt(left) > Integer.parseInt(right))
						equalsCondition = true;
				} else if (left.compareTo(right) > 0)
					equalsCondition = true;
				break;
			}
			if (equalsCondition == true) {
				facts.add(factOfFactList);
			}
		}
		return new Predicate("temp", predicate.getScheme().size(), predicate.getScheme(), facts);

	}

	/**
	 * find all equal variables of two predicates for a join, e.g.: C(?y,?z) :-
	 * A(?x,?y),B(?x,?z). --> ?x is a join attribute
	 * 
	 * @param leftList
	 *            variables of left Predicate
	 * @param rightList
	 *            variables of right Predicate
	 * @return list of positions of equal variables in both predicates
	 */
	protected ArrayList<PairofInteger> getEqualList(ArrayList<String> leftList,
			ArrayList<String> rightList) {
		ArrayList<PairofInteger> list = new ArrayList<PairofInteger>();
		for (int i = 0; i < leftList.size(); i++)
			for (int j = 0; j < rightList.size(); j++)
				if (leftList.get(i).startsWith("?")
						&& leftList.get(i).equals(rightList.get(j)))
					list.add(new PairofInteger(i, j));
		return list;
	}

	/**
	 * stratification of the rules datalog has stratification restrictions on
	 * the use of negation and recursion -> so the rules can be executed in a
	 * proper way Note: the mehtod is not used now (but maybe in the future)
	 * because : -the rules in our scenario are already in a proper order -we
	 * haven't implemented bottom up execution yet
	 * 
	 * @param rules
	 *            rules to be stratisfied
	 */
	@SuppressWarnings("unused")
	private void orderStratum(ArrayList<Rule> rules) throws Exception {
		int size = rules.size() - 1;
		Map<String, Integer> mapStratum = new HashMap<String, Integer>();

		for (Rule rule : rules) {
			String kindHead = rule.getHead().getKind();
			mapStratum.put(kindHead, 0);
			for (Predicate predicate : rule.getPredicates()) {
				String kindPredicate = predicate.getKind();
				mapStratum.put(kindPredicate, 0);
			}
		}

		boolean changed;
		do {
			changed = false;
			for (Rule rule : rules)
				for (Predicate predicate : rule.getPredicates()) {
					String head = rule.getHead().getKind();
					if (predicate.isNot()) {
						int headStratum = mapStratum.get(head);
						int predicateStratum = mapStratum.get(predicate
								.getKind());
						int newStratum = Math.max(headStratum,
								predicateStratum + 1);
						if (newStratum > size)
							throw new Exception("no stratification possible");
						if (headStratum < newStratum) {
							mapStratum.put(head, newStratum);
							changed = true;
						}
					} else {
						int headStratum = mapStratum.get(head);
						int predicateStratum = mapStratum.get(predicate
								.getKind());
						int newStratum = Math
								.max(headStratum, predicateStratum);
						if (headStratum < newStratum) {
							mapStratum.put(head, newStratum);
							changed = true;
						}
					}
				}
		} while (changed);

		// save stratum in predicate objects
		for (Rule r : rules) {
			String head = r.getHead().getKind();
			r.getHead().setStratum(mapStratum.get(head));
		}

		sortRules(rules, mapStratum);

	}

	/**
	 * generate dependency map return : e.g.. [Mission2, ( Mission,
	 * latestMission, Player, latestPlayer)]
	 */
	private Map<String, ArrayList<String>> generateDependencyMap(
			ArrayList<Rule> rules) {
		Map<String, ArrayList<String>> dependencyMap = new HashMap<String, ArrayList<String>>();
		for (Rule rule : rules) {
			String headKind = rule.getHead().getKind();
			ArrayList<String> bodyKind = rule.getDependencies();
			if (dependencyMap.get(headKind) == null)
				dependencyMap.put(headKind, bodyKind);
			else {
				ArrayList<String> currentDependencies = new ArrayList<String>();
				currentDependencies = dependencyMap.get(headKind);
				for (String oneKind : bodyKind) {
					if (!currentDependencies.contains(oneKind))
						currentDependencies.add(oneKind);
				}
			}
		}

		return dependencyMap;
	}

	/**
	 * sort rules by stratum values and generated dependencies
	 */

	private void sortRules(ArrayList<Rule> rules,
			Map<String, Integer> mapStratum) {
		Map<String, ArrayList<String>> dependencyMap = generateDependencyMap(rules);
		Map<String, Integer> rankingMap = mapStratum;

		// set default ranking value which is the stratum value
		for (Rule rule : rules) {
			int stratum = rule.getHead().getStratum();
			rule.getHead().setRanking(stratum);
		}

		boolean changed = true;
		do {
			changed = false;
			for (Rule rule : rules) {
				String head = rule.getHead().getKind();
				int headRanking = rule.getHead().getRanking();

				ArrayList<String> dependentPredicates = dependencyMap.get(head);
				for (String predicate : dependentPredicates) {
					int predicateStratum = rankingMap.get(predicate);
					if (headRanking <= predicateStratum) {
						rankingMap.put(rule.getHead().getKind(),
								predicateStratum + 1);
						rule.getHead().setRanking(predicateStratum + 1);
						changed = true;
					}
				}
			}
		} while (changed);

		Collections.sort(rules, new Comparator<Rule>() {
			@Override
			public int compare(Rule rule1, Rule rule2) {
				if (rule1.getHead().getRanking() > rule2.getHead().getRanking())
					return 1;
				if (rule1.getHead().getRanking() < rule2.getHead().getRanking())
					return -1;
				return 0;
			}
		});
	}

	protected static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	protected static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	protected class PairofInteger {
		private int position1;
		private int position2;

		PairofInteger(int p1, int p2) {
			this.setPosition1(p1);
			this.setPosition2(p2);
		}

		public int getPosition1() {
			return position1;
		}

		public void setPosition1(int position1) {
			this.position1 = position1;
		}

		public int getPosition2() {
			return position2;
		}

		public void setPosition2(int position2) {
			this.position2 = position2;
		}
	}
}
