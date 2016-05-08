package datalog;

import java.util.ArrayList;

public class Fact {
	
	// Speichere die Werte eines Faktes hier ab. Bsp. A(1,2)

	private String kind; // Wert für kind --> A
	private ArrayList<String> listOfValues; // alle Werte innerhalb eines Faktes
											// --> (1,2)

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
	
	public String toString(){
		String fact = kind + "(";
		for (String s: listOfValues){
			fact = fact + s + ",";
		}
		fact = fact.substring(0, fact.length()-1);
		fact = fact + ").";
		return fact;
		//return kind + "(" + listOfValues.toString() + ").";
	}

}
