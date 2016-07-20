package datastore;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lazyMigration.LazyMigration;
import parserPutToDatalog.ParseException;
import parserPutToDatalog.ParserForPut;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;
import parserRuletoJava.ParserRuleToJava;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;

@SuppressWarnings("serial")
public class DatalutionServlet extends HttpServlet {
	private int userid;
	private String kind;
	private String getCommand;
	private String putCommand;
	private Database db;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		db = new Database();
		Entity userPlayer = null;
		RequestDispatcher jsp = null;
		if (req.getParameter("getCommand") != null) {
			getCommand = req.getParameter("getCommand");
			ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
			new StringReader(getCommand));
			try {
				ArrayList<Rule> rules = parserget.getJavaRules(db);
			} catch (InputMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (parserQueryToDatalogToJava.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (parserRuletoJava.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			userid = parserget.getId();
			kind = parserget.getKind();
		}
		else getCommand=null;
		if (getCommand != null) {			
			userPlayer = db.getLatestEntity(kind, userid);
			if (userPlayer == null) {
				// if no database entry exist => 
				// top down execution + retry querying
				executeGetCommand(getCommand);
				db=new Database();
				userPlayer = db.getLatestEntity(kind, userid);		
			}
			
			if (userPlayer != null){
				Schema latestSchema =db.getLatestSchema(kind);
				String values = "[";
				for (String s : latestSchema.getAttributes())
					values = values + s + "="
							+ userPlayer.getProperty(s.substring(1)) + ", ";
				values = values + "ts=" + userPlayer.getProperty("ts") + "]";
				req.setAttribute("values", values);
			}			
			
			req.setAttribute("username", userid);
		}

		jsp = req.getRequestDispatcher("/WEB-INF/start.jsp");
		jsp.forward(req, resp);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity childPlayer = null;
		if (req.getParameter("putCommand") != null){
			putCommand = req.getParameter("putCommand");
			try {
				childPlayer = new ParserForPut(new StringReader(
						putCommand)).start(new Database());

				ds.put(childPlayer);
			} catch (InputMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		resp.sendRedirect("/user");

	}

	private void executeGetCommand(String uiInput) {
		Database databaseTD = new Database();
		String id = "";
		ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
				new StringReader(uiInput));
		ArrayList<Rule> rulesTemp = new ArrayList<Rule>();
		ArrayList<datastore.Rule> rules=databaseTD.getAllRulesFromDatastore();
		for(datastore.Rule r:rules)
			try {
				//if (r.isHead())
				rulesTemp.addAll(new ParserRuleToJava(new StringReader(r.getValue())).parseHeadRules());
//				else
//					rulesTemp.addAll(new ParserRuleToJava(new StringReader(r.getValue())).start());
			} catch (parserRuletoJava.ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		/*
		 * if (rulesForTD != null) rulesTemp = copyRules();
		 */

		try {
			rulesTemp.addAll(parserget.getJavaRules(new Database()));
		} catch (InputMismatchException
				| parserQueryToDatalogToJava.ParseException | IOException
				| parserRuletoJava.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String kind = parserget.getKind();
		id = parserget.getIdStr();
		// start lazy migration
		Map<String, String> attributeMap = new TreeMap<String, String>();
		attributeMap.put("kind", kind);
		attributeMap.put("position", "0");
		attributeMap.put("value", id);
		ArrayList<Map<String, String>> uniMap = new ArrayList<Map<String, String>>();
		uniMap.add(attributeMap);
		ArrayList<String> schema;
		Predicate goal;
		schema = databaseTD.getLatestSchema(kind).getAttributes();

		schema.add("?ts");

		goal = new Predicate("get" + kind
				+ databaseTD.getLatestSchemaVersion(kind), schema.size(),
				schema);

		executeQueryTD(rulesTemp, goal, uniMap);
		System.out.println(rulesTemp);
	}

	private void executeQueryTD(ArrayList<Rule> rulesTemp, Predicate goal,
			ArrayList<Map<String, String>> uniMap) {

		ArrayList<Fact> facts = new ArrayList<Fact>();

		LazyMigration migrate = new LazyMigration(facts, rulesTemp, goal,
				uniMap);
		ArrayList<String> answerString = null;
		try {
			answerString = migrate.writeAnswersInDatabase();
		} catch (ParseException | IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(answerString);
	}
}