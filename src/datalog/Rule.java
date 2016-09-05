package datalog;

import java.util.ArrayList;

/**
 * A class for representing a datalog rule Explanation of Rule based on this
 * example: C(?y):- A(?x,?y),B(?z,?y),?y=2
 */

public class Rule {

	/** ruleHead --> C(?y) */
	private Predicate ruleHead;
	/** ruleBody --> A(?x,?y),B(?z,?y),?y=2 */
	private RuleBody ruleBody;
	/** this information is important for ordering rules in a correct sequence */
	private ArrayList<String> dependencies;

	public Rule(Predicate ruleHead, RuleBody ruleBody) {
		this.ruleHead = ruleHead;
		this.setRuleBody(ruleBody);
		setDependencies();
	}

	public Predicate getHead() {
		return ruleHead;
	}

	public void setHead(Predicate ruleHead) {
		this.ruleHead = ruleHead;
	}

	public RuleBody getRuleBody() {
		return ruleBody;
	}

	public void setRuleBody(RuleBody ruleBody) {
		this.ruleBody = ruleBody;
	}

	public ArrayList<Predicate> getPredicates() {
		return ruleBody.getPredicates();
	}

	public void setPredicates(ArrayList<Predicate> predicates) {
		ruleBody.setPredicates(predicates);
	}

	public ArrayList<Condition> getConditions() {
		return ruleBody.getConditions();
	}

	public void setConditions(ArrayList<Condition> conditions) {
		ruleBody.setConditions(conditions);
	}

	public void setDependencies() {
		dependencies = new ArrayList<String>();
		for (Predicate predicate : ruleBody.getPredicates()) {
			String kind = predicate.getKind();
			if (!dependencies.contains(kind))
				dependencies.add(kind);
		}
	}

	public ArrayList<String> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		String rule = ruleHead.toString() + ":-";
		ArrayList<Predicate> predicates = ruleBody.getPredicates();
		ArrayList<Condition> conditions = ruleBody.getConditions();
		if (predicates != null)
			rule = rule + predicates.toString();
		if (conditions != null)
			rule = rule + conditions.toString();

		return rule + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ruleBody == null) ? 0 : ruleBody.hashCode());
		result = prime * result
				+ ((ruleHead == null) ? 0 : ruleHead.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (ruleBody == null) {
			if (other.ruleBody != null)
				return false;
		} else if (!ruleBody.equals(other.ruleBody))
			return false;
		if (ruleHead == null) {
			if (other.ruleHead != null)
				return false;
		} else if (!ruleHead.equals(other.ruleHead))
			return false;
		return true;
	}

}
