package lazyMigration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Map;

import com.google.appengine.api.datastore.EntityNotFoundException;

import parserPutToDatalog.ParseException;
import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;

/**
 * This class provides method for migrating entities lazily/topdown based on
 * datalog rules.
 */

public class LazyMigration {
	/** the datalog facts*/
	private ArrayList<Fact> facts;
	/** all datalog rules */
	private ArrayList<Rule> rules;
	/** the top goal for the topdown execution */
	private Predicate goal;
	/** this map stores attributes and their values for unification */
	private Map<String, String> unificationMap;

	public LazyMigration(ArrayList<Rule> rules,
			Predicate goal, Map<String, String> unificationMap) {
		this.facts = new ArrayList<Fact>();
		this.rules = rules;
		this.goal = goal;
		this.unificationMap = unificationMap;
	}

	public ArrayList<String> executeLazyMigration() throws ParseException,
			IOException, URISyntaxException, InputMismatchException,
			EntityNotFoundException {

		TopDownExecution lazy = new TopDownExecution(facts, rules, goal,
				unificationMap);
		ArrayList<Fact> getAnswers = lazy.getAnswers();
		if (getAnswers.size() == 1) {
			return getAnswers.get(0).getListOfValues();
		} else
			return null;
	}

}
