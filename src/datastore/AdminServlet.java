package datastore;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import parserQueryToDatalogToJava.ParseException;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;
import parserRuletoJava.ParserRuleToJava;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {
	
	String rulesStr = "";
	List<Schema> schema;
	String command;
	Date timestamp;
			
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {				
		String command = req.getParameter("command");
		Database db = new Database();
		rulesStr = "";
		
		if (command != null){
			if (command.equals("start")) {
				db.addStartEntities();	
				resp.sendRedirect("/admin");
			}
			else {
				try {
					rulesStr = new ParserQueryToDatalogToJava(new StringReader(
							command)).getDatalogRules(db);
					resp.sendRedirect("/admin");
				} catch (InputMismatchException e) {
					// TODO Auto-generated catch block
					resp.getWriter().println("Error:");
					resp.getWriter().println(e.getMessage());
					resp.setHeader("Refresh", "5;url=/admin");
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					resp.getWriter().println("Error:");
					resp.getWriter().println(e.getMessage());
					resp.setHeader("Refresh", "5;url=/admin");
				} catch (parserRuletoJava.ParseException e) {
					// TODO Auto-generated catch block
					resp.getWriter().println("Error:");
					resp.getWriter().println(e.getMessage());
					resp.setHeader("Refresh", "5;url=/admin");
				}
				
				if (!rulesStr.equals("")) {
					HashMap<String, String> rulesMap = new HashMap<String, String>();
					List<String> rulesSplit = new ArrayList<String>(Arrays.asList(rulesStr.split("\n")));
					for (String rule: rulesSplit){	
						String head = rule.substring(0, rule.indexOf("("));
						if (rulesMap.containsKey("Rules" + head)) {
							String oldValue = rulesMap.get("Rules" + head);
							rulesMap.put("Rules" + head, oldValue + "\n" + rule);
						}
						else {
							rulesMap.put("Rules" + head, rule);
						}								
					}
					db.addRules(rulesMap);
				}

			}
		}

	}
		
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		resp.setContentType("text/html");
		RequestDispatcher jsp = null;
		if (!rulesStr.equals("")) {
			req.setAttribute("rules", rulesStr);
		}
		
		jsp = req.getRequestDispatcher("/WEB-INF/admin.jsp");
		jsp.forward(req, resp);
	}

}
