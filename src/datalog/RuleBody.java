package datalog;

import java.util.ArrayList;

/**
 * This class represents the rule body of a datalog rule, a rule body consists
 * of several predicates and their conditions @see Rule
 */

public class RuleBody {
	private ArrayList<Predicate> predicates;
	private ArrayList<Condition> conditions;

	public RuleBody(ArrayList<Predicate> values, ArrayList<Condition> conditons) {
		super();
		this.setPredicates(values);
		this.setConditions(conditons);
	}

	public ArrayList<Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(ArrayList<Predicate> predicates) {
		this.predicates = predicates;
	}

	public ArrayList<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<Condition> conditions) {
		this.conditions = conditions;
	}

	public String toString() {
		return predicates.toString() + "," + conditions.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conditions == null) ? 0 : conditions.hashCode());
		result = prime * result
				+ ((predicates == null) ? 0 : predicates.hashCode());
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
		RuleBody other = (RuleBody) obj;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
		if (predicates == null) {
			if (other.predicates != null)
				return false;
		} else if (!predicates.equals(other.predicates))
			return false;
		return true;
	}

}
