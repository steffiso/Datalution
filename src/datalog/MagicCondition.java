package datalog;

import java.util.ArrayList;

/**
 * This class generates magic conditions.
 *
 * Definition: A magic condition is a condition consisting of two variables, e.g.
 * ?x=?y. <br>
 * A magic condition is used for magic sets in top down execution.
 * 
 * <br>
 * A magic set is a technique for optimizing query processing.<br>
 * Notes: <br>
 * - conditions with constants , e.g. ?x=1, aren't considered in this
 * implementation -> because they don't occur in our generated datalog rules
 * (except for get commands). But it could be implemented for future use! <br>
 * - the magic set in our scenario just contains one value: the MAGIC SET ID
 * value, therefore as soon as the id value is known it will be set in the
 * rules. <br>
 * <br>
 * Example of magic condition based on these rules:<br>
 * "A(?id1):- A(?id1,?pid), B(?id2,?name), ?id2=?pid" and
 * "B(?id2,?name):-B(?id2), C(?id2,?name)" <br>
 * Each magic condition has a head=(?pid) and a sub=(?id2) variable. The class
 * stores information as follows: the kind and position of the predicate that
 * contains the head variable "?pid", e.g. kind: A - position: 2; the kind and
 * position of predicates with the sub variable "?id2" and also all
 * corresponding predicates with "?id2", e.g. [kind: B - position: 1; kind: C -
 * position: 1] <br>
 * as soon as the value is known for ?pid --> ?id2 will be set to this value to
 * minimize intermediate query results, <br>
 * e.g., ?pid=1 --> rules: "A(?id1):- A(?id1,1), B(1,?name)" and
 * "B(1,?name):-B(1), C(1,?name)"
 */

public class MagicCondition {
	/**
	 * an object that stores kind and position for predicates with head variable
	 * of magic condition
	 */
	private PairForMagicCondition headPair;
	/**
	 * an object that stores a list of kind and position for all corresponding
	 * predicates with sub variable of magic condition
	 */
	private ArrayList<PairForMagicCondition> listOfSubPairs;
	/**
	 * checks whether the result has already been found for left variable of
	 * magic condition
	 */
	private boolean alreadyFoundResults;

	public MagicCondition(PairForMagicCondition headPair,
			PairForMagicCondition subPair) {
		super();
		this.headPair = headPair;
		if (listOfSubPairs == null)
			listOfSubPairs = new ArrayList<PairForMagicCondition>();
		listOfSubPairs.add(subPair);
	}

	public PairForMagicCondition getHeadPair() {
		return headPair;
	}

	public ArrayList<PairForMagicCondition> getListOfSubPairs() {
		return listOfSubPairs;
	}

	public boolean hasAlreadyResults() {
		return alreadyFoundResults;
	}

	public void setHeadPair(PairForMagicCondition kindLeft) {
		this.headPair = kindLeft;
	}

	public void setListOfSubPairs(ArrayList<PairForMagicCondition> listOfSubPairs) {
		this.listOfSubPairs = listOfSubPairs;
	}

	public void setAlreadyFoundResults(boolean results) {
		this.alreadyFoundResults = results;
	}

	@Override
	public String toString() {
		return "MagicCondition:" + headPair.toString() + ":- " + listOfSubPairs.toString();
	}

	public boolean contains(PairForMagicCondition newSubPair) {
		if (listOfSubPairs.contains(newSubPair))
			return true;
		return false;
	}

}
