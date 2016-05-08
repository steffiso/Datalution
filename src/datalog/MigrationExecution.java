package datalog;

import java.util.ArrayList;
import java.util.List;

//superclass for migration classes: BottomUpExecution and TopDownExecution
public class MigrationExecution {

	// all edb facts and generated idb
	protected ArrayList<Fact> facts;
	// all generated datalog rules
	protected ArrayList<Rule> rules;

	// set edb facts and rules
	public MigrationExecution(ArrayList<Fact> facts, ArrayList<Rule> rules) {
		super();
		this.facts = facts;
		this.rules = rules;
	}
	
	// get all facts
	public ArrayList<Fact> getFacts() {
		return facts;
	}

	// set all facts
	public void setFacts(ArrayList<Fact> values) {
		this.facts = values;
	}
	
	//get all facts of a Kind
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

	// rename redicates step by step
	protected void renameVariablesOfAllPredicates(Rule rule, String left,
			String right) {
		renameVariablesOfPredicate(rule.getHead(), left, right);
		for (Predicate pred : rule.getPredicates())
			renameVariablesOfPredicate(pred, left, right);
	}

	// rename variables of predicate step by step
	protected void renameVariablesOfPredicate(Predicate predicate, String left,
			String right) {
		if (predicate.getScheme().contains(right)) {
			predicate.getScheme().set(predicate.getScheme().indexOf(right),
					left);
		}
	}

	// generate temporary results of all conditions of a rule step by step
	protected Predicate selection(Predicate predResult,
			ArrayList<Condition> conditions) {
		for (Condition cond : conditions) {
			predResult = getTempCondResult(predResult, cond);
		}
		return predResult;
	}

	// generate temporary results of condition, e.g.: C(?y,?z) :-
	// A(?x,?y),B(?x,?z),?y=?z.
	// Only put values of A and B to end result which satisfies condition ?y=?z.
	protected Predicate getTempCondResult(Predicate p, Condition cond) {
		ArrayList<ArrayList<String>> facts = new ArrayList<ArrayList<String>>();
		String rightOperand = cond.getRightOperand();
		String leftOperand = cond.getLeftOperand();
		String operator = cond.getOperator();
		List<ArrayList<String>> factList = p.getRelation();
		for (ArrayList<String> factOfFactList : factList) {
			String left = "";
			String right = "";
			if (leftOperand.startsWith("?"))
				if (p.getScheme().contains(leftOperand))
					left = factOfFactList.get(p.getScheme()
							.indexOf(leftOperand));
				else {
					facts.add(factOfFactList);
					continue;
				}
			else
				left = leftOperand;
			if (rightOperand.startsWith("?"))
				if (p.getScheme().contains(rightOperand))
					right = factOfFactList.get(p.getScheme().indexOf(
							rightOperand));
				else {
					System.out.println(leftOperand + " existiert nicht");
					facts.add(factOfFactList);
					continue;
				}
			else
				right = rightOperand;
			boolean condPredicate = false;
			switch (operator) {
			case "=":
				if ((isInteger(left) && isInteger(right))
						|| (isDouble(left) && isDouble(right))) {
					if (Integer.parseInt(left) == Integer.parseInt(right))
						condPredicate = true;
				} else if (left.equals(right))
					condPredicate = true;
				break;
			case "!":
				if ((isInteger(left) && isInteger(right))
						|| (isDouble(left) && isDouble(right))) {
					if (Integer.parseInt(left) != Integer.parseInt(right))
						condPredicate = true;
				} else if (!left.equals(right))
					condPredicate = true;
				break;
			case "<":
				if ((isInteger(left) && isInteger(right))
						|| (isDouble(left) && isDouble(right))) {
					if (Integer.parseInt(left) < Integer.parseInt(right))
						condPredicate = true;
				} else if (left.compareTo(right) < 0)
					condPredicate = true;
				break;
			case ">":
				if ((isInteger(left) && isInteger(right))
						|| (isDouble(left) && isDouble(right))) {
					if (Integer.parseInt(left) > Integer.parseInt(right))
						condPredicate = true;
				} else if (left.compareTo(right) > 0)
					condPredicate = true;
				break;
			}
			if (condPredicate == true) {
				facts.add(factOfFactList);
			}
		}
		return new Predicate("temp", p.getScheme().size(), p.getScheme(), facts);

	}

	// find all equal attributes of two predicates for a join, e.g.: C(?y,?z) :-
	// A(?x,?y),B(?x,?z). -->
	// ?x
	// is a join attribute
	protected ArrayList<PairofInteger> getEqualList(ArrayList<String> leftList,
			ArrayList<String> rightList) {
		ArrayList<PairofInteger> list = new ArrayList<PairofInteger>();
		for (int i = 0; i < leftList.size(); i++)
			for (int j = 0; j < rightList.size(); j++)
				if (leftList.get(i).startsWith("?"))
					if (leftList.get(i).equals(rightList.get(j)))
						list.add(new PairofInteger(i, j));
		return list;
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
		private int p1;
		private int p2;

		PairofInteger(int p1, int p2) {
			this.setP1(p1);
			this.setP2(p2);
		}

		public int getP1() {
			return p1;
		}

		public void setP1(int p1) {
			this.p1 = p1;
		}

		public int getP2() {
			return p2;
		}

		public void setP2(int p2) {
			this.p2 = p2;
		}
	}
}
