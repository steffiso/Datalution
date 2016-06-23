package datastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.SortDirection;

import datalog.DatalogRulesGenerator;
import parserQueryToDatalogToJava.ParseException;

import static com.googlecode.objectify.ObjectifyService.ofy;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {
	
	List<Rule> rules;
	List<Schema> schema;
	String command;
	Date timestamp;
	
		public void doGet(HttpServletRequest req,
	             HttpServletResponse resp) throws IOException, ServletException{
			
//			resp.setContentType("text/plain");		    
//			resp.getWriter().println(d.toString());
			RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/admin.jsp");
		    jsp.forward(req, resp);
		 }
		
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{			

			String command = req.getParameter("command");
			
			resp.sendRedirect("/admin");

			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			Database db = new Database(ds);
			
			DatalogRulesGenerator drg = new DatalogRulesGenerator(db);
			String rules = "";
			try {
				rules = drg.getRules(command);
			} catch (InputMismatchException e) {
				// TODO Auto-generated catch block
				resp.getWriter().println(e.getMessage());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				resp.getWriter().println(e.getMessage());
			} catch (parserRuletoJava.ParseException e) {
				// TODO Auto-generated catch block
				resp.getWriter().println(e.getMessage());
			}
			if (!rules.equals("")) {
				ArrayList<String> rulesList = new ArrayList<String>(Arrays.asList(rules.split("\n")));
				for (String s: rulesList){
					
					Entity rule = new Entity("Rule");
					rule.setProperty("value", s);
					rule.setProperty("timestamp", new Date());
					ds.put(rule);
				}
				resp.getWriter().println(rules);
			}
		}
		

}
