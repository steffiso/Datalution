package usecases;

import java.io.IOException;
import java.io.StringReader;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import parserPutToDatalog.ParserForPut;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import datastore.DatalutionDatastoreService;
import datastore.Schema;
import parserGetToDatalog.ParserForGet;
/**
 * This servlet provides the user console for CloudDatalution with Datastore.
 * Supported commands (with a brief example): 
 * - get (e.g. "get Player.id=1")
 * - put (e.g. "put Player(1,"Lisa S.",200)")
 */
@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {
	
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
				req.setAttribute("result", putCommand + " was successful! Added entity to Datastore!");
			} catch (Exception e) {
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
		getCommand = req.getParameter("getCommand");
		
		if (getCommand != null && !getCommand.isEmpty()) {
			ParserForGet parserget = new ParserForGet(
					new StringReader(getCommand));
			
			try {
				parserget.setAttributesOfGetCommand();				
				userId = parserget.getId();
				kind = parserget.getKind();
				req.setAttribute("username", userId);
			} catch (Exception  e) {
				req.setAttribute("result", e.getMessage());
			} 			
		}

		if (getCommand != null && kind != null) {
			// parsing of get command was successful => try to find an entity in Datastore
			try {
				Key key = KeyFactory.createKey(kind, Integer.toString(userId));
				userPlayer = dds.get(key);
			} catch (Exception e) {
				req.setAttribute("result", e.toString());
			}

			if (userPlayer != null) {
				// entity with current schema was found
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
		String outputString = kind + ": [";
		for (String attribute : latestSchema.getAttributesAsList())
			outputString = outputString + attribute + "="
					+ entity.getProperty(attribute.substring(1)) + ", ";
		
		outputString = outputString + "ts=" + entity.getProperty("ts") + "]";
		
		return outputString;
	}

}