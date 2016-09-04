package datalog;

import java.util.ArrayList;

/**
 * class for generating MagicConditions MagicConditon: condition with two
 * variables, e.g. ?x=?y ->it's a magic condition for a magic set in top down
 * execution
 * 
 * (Note: conditions with constants , e.g. ?x=1, aren't considered in this
 * implemantation -> because they don't occur in our generated datalog rules
 * (except for get commands). But it could be implemented for future use!)
 * 
 * @author Stephanie Sombach and Katharina Wiech
 * @version 1.0
 */

public class MagicCondition {
	private PairForMagicCondition left;
	private ArrayList<PairForMagicCondition> right;
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
