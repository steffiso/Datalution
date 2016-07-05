package datastore;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;

@SuppressWarnings("serial")
public class DatalutionServlet extends HttpServlet {
	private String username;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Database db=new Database();
		Entity userPlayer = null;
		RequestDispatcher jsp = null;
		if (req.getParameter("username") != null) {
			username = req.getParameter("username");
		}
		else username=null;
		if (username != null) {

			Query playerQuery = new Query("Player"+ db.getLatestSchemaVersion("Player")).setAncestor(
					KeyFactory.createKey("Player", username)).addSort("ts",
					SortDirection.DESCENDING);
			List<Entity> results = ds.prepare(playerQuery).asList(
					FetchOptions.Builder.withDefaults().limit(1));
			
			if (results.isEmpty()) {
				// if no database entry exist => 
				// top down execution + retry querying
				executeGetCommand("get Player.id="+username);
				playerQuery = new Query("Player"+ db.getLatestSchemaVersion("Player")).setAncestor(
						KeyFactory.createKey("Player", username)).addSort("ts",
						SortDirection.DESCENDING);
				results = ds.prepare(playerQuery).asList(
						FetchOptions.Builder.withDefaults().limit(1));				
			}
			
			if (!results.isEmpty()){
				userPlayer = results.get(0);
				Schema latestSchema =db.getLatestSchema("Player");
				String values = "[";
				for (String s : latestSchema.getAttributes())
					values = values + s + "="
							+ userPlayer.getProperty(s.substring(1)) + ", ";
				values = values + "ts=" + userPlayer.getProperty("ts") + "]";
				req.setAttribute("values", values);
			}
				
			
			req.setAttribute("username", username);
		}
		// saveCurrentSchema("Player", "?id,?name,?character,?score");

		jsp = req.getRequestDispatcher("/WEB-INF/start.jsp");
		jsp.forward(req, resp);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		if (req.getParameter("newPlayer").equals("true")) {
			Entity player = new Entity(KeyFactory.createKey("Player", username));
			ds.put(player);
		}
		Entity childPlayer = null;
		try {
			childPlayer = new ParserForPut(new StringReader(
					req.getParameter("name"))).start(new Database(), username);
		} catch (InputMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ds.put(childPlayer);

		resp.sendRedirect("/datalution");

	}

	private void executeGetCommand(String uiInput) {
		Database databaseTD = new Database();
		String id = "";
		ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
				new StringReader(uiInput));
		ArrayList<Rule> rulesTemp = new ArrayList<Rule>();
		ArrayList<datastore.Rule> rules=databaseTD.getRules();
		for(datastore.Rule r:rules)
			try {
				if (r.isHead())
				rulesTemp.addAll(new ParserRuleToJava(new StringReader(r.getValue())).parseHeadRules());
				else
					rulesTemp.addAll(new ParserRuleToJava(new StringReader(r.getValue())).start());
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
		id = parserget.getId();
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

//	public void saveCurrentSchema(String kind, String newSchema) {
//		int newVersion = 1;
//		Schema schema = new Schema();
//		schema.setAttributesString(newSchema);
//		schema.setKind(kind);
//		schema.setSchemaversion(newVersion);
//		Date d = new Date();
//		schema.setTimestamp(d);
//		OfyService.ofy();
//		ofy().save().entity(schema).now();
//	}
}