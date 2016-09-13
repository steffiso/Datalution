package lazyMigration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Map;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Transaction;

import parserPutToDatalog.ParseException;
import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;

/**
 * this class provides method for migrating entities lazily/topdown based on
 * datalog rules
 */

public class LazyMigration {
	private ArrayList<Fact> facts;
	private ArrayList<Rule> rules;
	private Predicate goal;
	private Map<String, String> unificationMap;
	private Transaction txn;

	public LazyMigration(Transaction txn, ArrayList<Rule> rules,
			Predicate goal, Map<String, String> unificationMap) {
		this.txn=txn;
		this.facts = new ArrayList<Fact>();
		this.rules = rules;
		this.goal = goal;
		this.unificationMap = unificationMap;
	}

	public ArrayList<String> executeLazyMigration() throws ParseException,
			IOException, URISyntaxException, InputMismatchException,
			EntityNotFoundException {

		TopDownExecution lazy = new TopDownExecution(txn,facts, rules, goal,
				unificationMap);
		ArrayList<Fact> getAnswers = lazy.getAnswers();
		if (getAnswers.size() == 1) {
			return getAnswers.get(0).getListOfValues();
		} else
			return null;
	}

}
