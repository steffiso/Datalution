package datastore;
/**
 * This servlet provides the user console for Datalution with Datastore.
 * Supported commands (with a brief example): 
 * - get (e.g. "get Player.id=1")
 * - put (e.g. "put Player(1,"Lisa S.",200)")
 */
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import parserPutToDatalog.ParseException;
import parserPutToDatalog.ParserForPut;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import datalog.Rule;

@SuppressWarnings("serial")
public class DatalutionServlet extends HttpServlet {
	
	private int userId;
	private String kind;
	private String getCommand;
	private String putCommand;
	private DatalutionDatastoreService dds;

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		dds = new DatalutionDatastoreService();
		Entity childPlayer = null;
		RequestDispatcher jsp = null;
		putCommand = req.getParameter("putCommand");
		
		if (putCommand != null && !putCommand.isEmpty()) {
			try {
				childPlayer = new ParserForPut(new StringReader(putCommand))
						.start();
				dds.put(childPlayer);
				req.setAttribute("result", putCommand + " was successful!");
			} catch (InputMismatchException | ParseException | EntityNotFoundException e) {
				req.setAttribute("result", e.getMessage());
			}
		}		
		
		jsp = req.getRequestDispatcher("/WEB-INF/user.jsp");
		jsp.forward(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException{
		
		dds = new DatalutionDatastoreService();
		Entity userPlayer = null;
		RequestDispatcher jsp = null;
		kind = null;
		userId = 0;
		resp.setContentType("text/html");
		getCommand = req.getParameter("getCommand");
		
		if (getCommand != null && !getCommand.isEmpty()) {
			ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
					new StringReader(getCommand));
			
			try {
				@SuppressWarnings("unused")
				ArrayList<Rule> rules = parserget.getJavaRules(dds);				
				userId = parserget.getId();
				kind = parserget.getKind();
				req.setAttribute("username", userId);
			} catch (InputMismatchException | parserQueryToDatalogToJava.ParseException |
					parserRuletoJava.ParseException | EntityNotFoundException e) {
				req.setAttribute("result", e.getMessage());
			} 			
		}

		if (getCommand != null && kind != null) {
			try {
				userPlayer = dds.get(kind, Integer.toString(userId));
			} catch (InputMismatchException | parserQueryToDatalogToJava.ParseException
					| parserRuletoJava.ParseException | ParseException | 
					URISyntaxException | EntityNotFoundException e) {
				req.setAttribute("result", e.toString());
			}

			if (userPlayer != null) {
				String resultEntityStr = "";
				try {
					resultEntityStr = formatEntityToOutputString(userPlayer);
					req.setAttribute("result", resultEntityStr);
				} catch (EntityNotFoundException e) {
					req.setAttribute("result", e.getMessage());
				}
			}
		}

		jsp = req.getRequestDispatcher("/WEB-INF/user.jsp");
		jsp.forward(req, resp);
	}
	
	/**
   	* Format a datastore entity to an output String
   	*
   	* @return result entity in readable form
	* @throws EntityNotFoundException 
   	*/
	public String formatEntityToOutputString(Entity entity) throws EntityNotFoundException {
		Schema latestSchema = dds.getLatestSchema(kind);		
		String outputString = "[";
		for (String attribute : latestSchema.getAttributesAsList())
			outputString = outputString + attribute + "="
					+ entity.getProperty(attribute.substring(1)) + ", ";
		
		outputString = outputString + "ts=" + entity.getProperty("ts") + "]";
		
		return outputString;
	}

}