package datalog;

import java.util.ArrayList;

/**
 * class for generating Magic Conditions
 *
 * Def. MagicConditon: a condition consisting of two variables, e.g. ?x=?y --> a
 * a magic condition is used for magic sets in top down execution
 * 
 * a magic set is a technique for optimizing query processing
 * 
 * Notes:
 * 
 * - conditions with constants , e.g. ?x=1, aren't considered in this
 * implementation -> because they don't occur in our generated datalog rules
 * (except for get commands). But it could be implemented for future use!
 * 
 * - the magic set in our scenario just contains one value: the MAGIC SET ID
 * value, therefore as soon as the id value is known it will be set in the rules
 * 
 *
 * Example of magic condition based on these rules:
 * "A(?id1):- A(?id1,?pid), B(?id2,?name), ?pid=?id2" and
 * "B(?id2,?name):-B(?id2), C(?id2,?name)" the magic condition has a left(?pid)
 * and a right(?id2) variable The class stores information as follows: the kind
 * and position of predicate with left variable ?pid, e.g. kind: A - position: 2
 * the kind and position of predicates with right variable ?id2 and all
 * corresponding predicates with ?id2, e.g. [kind: B - position: 1; kind: C -
 * position: 1] as soon as the value is known for ?pid --> ?id2 will be set to
 * this value to minimize intermediate query results e.g., ?pid=1 --> rules:
 * "A(?id1):- A(?id1,1), B(1,?name)" and "B(1,?name):-B(1), C(1,?name)"
 */

public class MagicCondition {
	/**
	 * an object that stores kind and position for predicates with left variable
	 * of magic condition
	 */
	private PairForMagicCondition left;
	/**
	 * an object that stores a list of kind and position for all corresponding
	 * predicates with right variable of magic condition
	 */
	private ArrayList<PairForMagicCondition> right;
	/**
	 * checks whether the result has already been found for left variable of
	 * magic condition
	 */
	private boolean alreadyFoundResults;

	public MagicCondition(PairForMagicCondition kindLeft,
			PairForMagicCondition kindRight) {
		super();
		this.left = kindLeft;
		if (right == null)
			right = new ArrayList<PairForMagicCondition>();
		right.add(kindRight);
	}

	public PairForMagicCondition getLeft() {
		return left;
	}

	public ArrayList<PairForMagicCondition> getRight() {
		return right;
	}

	public boolean hasAlreadyResults() {
		return alreadyFoundResults;
	}

	public void setLeft(PairForMagicCondition kindLeft) {
		this.left = kindLeft;
	}

	public void setKindRight(ArrayList<PairForMagicCondition> kindRight) {
		this.right = kindRight;
	}

	public void setAlreadyFoundResults(boolean results) {
		this.alreadyFoundResults = results;
	}

	@Override
	public String toString() {
		return "MagicCondition:" + left.toString() + ":- " + right.toString();
	}

	public boolean contains(PairForMagicCondition newRight) {
		if (right.contains(newRight))
			return true;
		return false;
	}

}
