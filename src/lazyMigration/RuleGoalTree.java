package lazyMigration;

import java.util.ArrayList;

import datalog.Predicate;
import datalog.Rule;

public class RuleGoalTree {

	private Predicate goal;
	private ArrayList<Rule> children;
	
	public RuleGoalTree(ArrayList<Rule> rules){
		//the rule heads must be equal/unified
		this.goal = rules.get(0).getHead();
		children = rules;
	}	
	public Predicate getGoal() {
		return goal;
	}
	public void setGoal(Predicate goal) {
		this.goal = goal;
	}
	public ArrayList<Rule> getChildren(){
		return children;
	}
	public void setChildren(ArrayList<Rule> children){
		this.children = children;
	}
	
}
