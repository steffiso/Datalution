package datalog;

import java.util.ArrayList;

/**
 * This is a class for representing datalog predicates. <br>
 * A datalog rule consists of several predicates. <br>
 * An example of a predicate is: A(?x,?y).
 */

public class Predicate {

	/** kind --> A */
	private String kind;
	/** scheme --> (?x,?y), stores all variables as string */
	private ArrayList<String> scheme;
	/** number --> 2 */
	private int numberSchemeEntries;
	/** is it a "not"/"negated" predicate? */
	private boolean isNot = false;
	/**
	 * is it a "latest" predicate? (marked with: "$", this predicate defines
	 * latest facts/entities in the datastore)
	 */
	private boolean latest = false;
	/** this information is important for ordering rules in a correct sequence */
	private int stratum = 0;
	/** this information is important for ordering rules in a correct sequence */
	private int ranking = 0;
	/** results of predicate are stored here */
	private ArrayList<ArrayList<String>> relation;

	public Predicate(String kind, int number, ArrayList<String> scheme,
			ArrayList<ArrayList<String>> relation) {
		super();
		this.kind = kind;
		this.numberSchemeEntries = number;
		this.scheme = scheme;
		this.relation = relation;
	}

	public Predicate(String kind, int number, ArrayList<String> scheme) {
		super();
		this.kind = kind;
		this.numberSchemeEntries = number;
		this.scheme = scheme;
	}

	public String getKind() {
		return kind;
	}

	public int getNumberSchemeEntries() {
		return numberSchemeEntries;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public void setNumberSchemeEntries(int anz) {
		this.numberSchemeEntries = anz;
	}

	public boolean isNot() {
		return isNot;
	}

	public void setNot(boolean isNot) {
		this.isNot = isNot;
	}

	public int getStratum() {
		return stratum;
	}

	public void setStratum(int stratum) {
		this.stratum = stratum;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public ArrayList<ArrayList<String>> getRelation() {
		return relation;
	}

	public void setRelation(ArrayList<ArrayList<String>> relation) {
		this.relation = relation;
	}

	public ArrayList<String> getScheme() {
		return scheme;
	}

	public void setScheme(ArrayList<String> scheme) {
		this.scheme = scheme;
	}

	@Override
	public String toString() {
		String isNot = isNot() ? "not" : "";
		return isNot + " " + kind + scheme.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isNot ? 1231 : 1237);
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + numberSchemeEntries;
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
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
		Predicate other = (Predicate) obj;
		if (isNot != other.isNot)
			return false;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		if (numberSchemeEntries != other.numberSchemeEntries)
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		return true;
	}

	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
	}

}
