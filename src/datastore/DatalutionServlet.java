package datastore;
/**
 * This servlet provides the user console for Datalution with Datastore.
 * Supported commands (with a brief example): 
 * - get (e.g. "get Player.id=1")
 * - put (e.g. "put Player(1,"Lisa S.",200)")
 */
import java.io.IOException;
import java.io.StringReader;
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

import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.Entity;

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
		
		if (req.getParameter("putCommand") != null
				&& !req.getParameter("putCommand").isEmpty()) {
			putCommand = req.getParameter("putCommand");
			try {
				childPlayer = new ParserForPut(new StringReader(putCommand))
						.start();
				dds.put(childPlayer);
			} catch (InputMismatchException e) {
				resp.getWriter().println("Error:");
				resp.getWriter().println(e.getMessage());
				resp.setHeader("Refresh", "5;url=/user");
			} catch (ParseException e) {
				resp.getWriter().println("Error:");
				resp.getWriter().println(e.getMessage());
				resp.setHeader("Refresh", "5;url=/user");
			}
		}		
		resp.sendRedirect("/user");
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException{
		
		dds = new DatalutionDatastoreService();
		Entity userPlayer = null;
		RequestDispatcher jsp = null;
		resp.setContentType("text/html");
		
		if (req.getParameter("getCommand") != null
				&& !req.getParameter("getCommand").isEmpty()) {
			getCommand = req.getParameter("getCommand");
			ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
					new StringReader(getCommand));
			
			try {
				@SuppressWarnings("unused")
				ArrayList<Rule> rules = parserget.getJavaRules(dds);
			} catch (InputMismatchException e) {
				resp.getWriter().println("Error:");
				resp.getWriter().println(e.getMessage());
				resp.setHeader("Refresh", "5;url=/user");
			} catch (parserQueryToDatalogToJava.ParseException e) {
				resp.getWriter().println("Error:");
				resp.getWriter().println(e.getMessage());
				resp.setHeader("Refresh", "5;url=/user");
			} catch (parserRuletoJava.ParseException e) {
				resp.getWriter().println("Error:");
				resp.getWriter().println(e.getMessage());
				resp.setHeader("Refresh", "5;url=/user");
			} catch (BadRequestException e) {
				resp.getWriter().println("Error:");
				resp.getWriter().println(e.getMessage());
				resp.setHeader("Refresh", "5;url=/user");
			}
			
			userId = parserget.getId();
			kind = parserget.getKind();
			
		} else
			getCommand = null;

		if (getCommand != null) {
			userPlayer = dds.get(kind, Integer.toString(userId));

			if (userPlayer != null) {
				String resultEntityStr = formatEntityToOutputString(userPlayer);
				req.setAttribute("values", resultEntityStr);
			}

			req.setAttribute("username", userId);
		}

		jsp = req.getRequestDispatcher("/WEB-INF/user.jsp");
		jsp.forward(req, resp);
	}
	
	/**
   	* Gets a datastore entity to format
   	*
   	* @return result entity in readable form
   	*/
	public String formatEntityToOutputString(Entity entity) {
		Schema latestSchema = dds.getLatestSchema(kind);		
		String outputString = "[";
		for (String attribute : latestSchema.getAttributesAsList())
			outputString = outputString + attribute + "="
					+ entity.getProperty(attribute.substring(1)) + ", ";
		
		outputString = outputString + "ts=" + entity.getProperty("ts") + "]";
		
		return outputString;
	}

}