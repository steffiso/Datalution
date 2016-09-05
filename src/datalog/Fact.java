package datalog;

import java.util.ArrayList;

/**
 * This class stores all information of datalog edb and idb facts - edb facts
 * are values which are stored in the database and known at the beginning - idb
 * facts are derived values from datalog rules for example, one fact will be
 * represented as follows: A(1,2)
 */

public class Fact {

	/** kind --> A */
	private String kind;
	/** all values of fact --> (1,2) */
	private ArrayList<String> listOfValues;

	public Fact(String kind, ArrayList<String> values) {
		this.kind = kind;
		listOfValues = values;
	}

	public ArrayList<String> getListOfValues() {
		return listOfValues;
	}

	public void setListOfValues(ArrayList<String> listOfValues) {
		this.listOfValues = listOfValues;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String toString() {
		String fact = kind + "(";
		for (String s : listOfValues) {
			fact = fact + s + ",";
		}
		fact = fact.substring(0, fact.length() - 1);
		fact = fact + ").";
		return fact;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result
				+ ((listOfValues == null) ? 0 : listOfValues.hashCode());
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
		Fact other = (Fact) obj;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		if (listOfValues == null) {
			if (other.listOfValues != null)
				return false;
		} else if (!listOfValues.equals(other.listOfValues))
			return false;
		return true;
	}

}
