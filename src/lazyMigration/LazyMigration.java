package lazyMigration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import parserPutToDatalog.ParseException;
import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;

public class LazyMigration {
	private ArrayList<Fact> facts;
	private ArrayList<Rule> rules;
	private Predicate goal;
	private List<Map<String, String>> unificationMap;
	private int numberOfPuts;

	public LazyMigration(ArrayList<Fact> facts, ArrayList<Rule> rules,
			Predicate goal, List<Map<String, String>> unificationMap) {
		this.facts = facts;
		this.rules = rules;
		this.goal = goal;
		this.unificationMap = unificationMap;
	}

	public ArrayList<String> writeAnswersInDatabase() throws ParseException, IOException, URISyntaxException {

		TopDownExecution lazy = new TopDownExecution(facts, rules, goal,
				unificationMap);
		ArrayList<Fact> getAnswers = lazy.getAnswers();

		ArrayList<String> list = null;
		for (Fact f : getAnswers) {
			if (list == null)
				list = new ArrayList<String>();
			list.add(f.toString());
		}
		setNumberOfPuts(lazy.getNumberOfPuts());
		return list;
	}
	public int getNumberOfPuts() {
		return numberOfPuts;
	}

	public void setNumberOfPuts(int numberOfPuts) {
		this.numberOfPuts = numberOfPuts;
	}

	
}
