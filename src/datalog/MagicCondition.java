package datalog;

import java.util.ArrayList;

public class MagicCondition {
	private PairForMagicCondition left;
	private ArrayList<PairForMagicCondition> right;
	private boolean alreadyFoundResults;
	private String nameOfMagicView;

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

	public String getNameOfMagicView() {
		return nameOfMagicView;
	}

	public void setNameOfMagicView(String nameOfMagicView) {
		this.nameOfMagicView = nameOfMagicView;
	}

	public boolean contains(PairForMagicCondition newRight) {
		if (right.contains(newRight))
			return true;
		return false;
	}

}
