package datalog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import datastore.Database;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;
import datalog.Rule;

public class DatalogRulesGenerator {

	private Database db;
	
	public DatalogRulesGenerator(Database db){
		this.db = db;
	}
	// return the generated Datalog rules in one String for 
	// get, add, delete, copy or move
	// e.g. String input = "add Player.points=20"
	public String getRules(String input) throws parserQueryToDatalogToJava.ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
		String rules = "";

		String[] splitString = input.split("\n");
		for (int i = 0; i < splitString.length; i++) {
				rules = rules
						+ new ParserQueryToDatalogToJava(new StringReader(
								splitString[i])).getDatalogRules(db);
			

		}

		return rules;
	}

	// return the generated Datalog rules in ArrayList<Rule> for 
	// get, add, delete, copy or move
	// e.g. String input = "add Player.points=20"
	public ArrayList<Rule> getJavaRules(String input) throws parserQueryToDatalogToJava.ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
		ArrayList<Rule> rules = new ArrayList<Rule>();

		String[] splitString = input.split("\n");
		for (int i = 0; i < splitString.length; i++) {
			
				rules.addAll((new ParserQueryToDatalogToJava(
						new StringReader(splitString[i]))).getJavaRules(db));
		}

		return rules;
	}

	// writes a fact to database
	// input e.g. "put Player(4,'Maggie',30)
	public String putFact(String input) {
		String fact = "";
		String jsonString = "";
	
//			jsonString = new ParserForPut(new StringReader(input))
//					.getJSONString();
//
//
//		// put to database
//		db.writeInJsonFile(db.getFilenameEDB(), jsonString);
//		if (input.startsWith("put")) {
//			fact = input.substring(4, input.length()) + ".";
//		} else
//			fact = input + ".";
		return fact;
	}

	// returns all edb facts from database in one string
	public String getEDBFacts(){
		String edbFacts = "";
		edbFacts = db.getEDB();
		return edbFacts;

	}

	// returns all edb facts from database in one json-like string
	public String getJsonFacts(){
		String edbFacts = "";
		edbFacts = db.getJson();
		return edbFacts;

	}
}
