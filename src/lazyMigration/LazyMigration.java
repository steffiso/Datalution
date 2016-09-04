package lazyMigration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.EntityNotFoundException;

import parserPutToDatalog.ParseException;
import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;

public class LazyMigration {
	private ArrayList<Fact> facts;
	private ArrayList<Rule> rules;
	private Predicate goal;
	private List<Map<String, String>> unificationMap;
	public LazyMigration(ArrayList<Fact> facts, ArrayList<Rule> rules,
			Predicate goal, List<Map<String, String>> unificationMap) {
		this.facts = facts;
		this.rules = rules;
		this.goal = goal;
		this.unificationMap = unificationMap;
	}

	public ArrayList<String> executeLazyMigration() throws ParseException, IOException, URISyntaxException, InputMismatchException, EntityNotFoundException {

		TopDownExecution lazy = new TopDownExecution(facts, rules, goal,
				unificationMap);
		ArrayList<Fact> getAnswers = lazy.getAnswers();

		return getAnswers.get(0).getListOfValues();
	}


	
}
