package datastore;

import java.io.IOException;
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
import datalog.DatalogRulesGenerator;
import parserQueryToDatalogToJava.ParseException;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {
	
	List<Rule> rules;
	List<Schema> schema;
	String command;
	Date timestamp;
	
	public void doGet(HttpServletRequest req,
             HttpServletResponse resp) throws IOException, ServletException{
		
		resp.setContentType("text/plain");		 
		RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/admin.jsp");
	    jsp.forward(req, resp);
	 }
		
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{			

		String command = req.getParameter("command");

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Database db = new Database(ds);
		
		DatalogRulesGenerator drg = new DatalogRulesGenerator(db);
		if (command.equals("start")) {
			db.addStartEntities();	
			resp.sendRedirect("/admin");
		}
		else {
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
				try {
					db.addRules(rules);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			resp.getWriter().println("Successfully add schema change\n\nGenerated rules:\n"+rules);
		}
	
		
	
	}
		

}
