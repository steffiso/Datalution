package datastore;
/**
 * This servlet provides the admin console for Datalution with Datastore.
 * Supported commands within the admin command line: 
 * - add (e.g. "add Player.points=100")
 * - delete (e.g. "delete Player.points")
 * - copy (e.g. "copy Player.points to Mission where Player.id=Mission.pid")
 * - move (e.g. "move Player.points to Mission where Player.id=Mission.pid"
 * - start (adds Player and Mission start entities for test cases)
 */
import java.io.IOException;
import java.util.InputMismatchException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EntityNotFoundException;

import parserQueryToDatalogToJava.ParseException;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {

	private String datalogRules = "";
			
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{	
		
		RequestDispatcher jsp = null;
		String command = req.getParameter("command");
		DatalutionDatastoreService dds = new DatalutionDatastoreService();
		datalogRules = "";		
		
		if (command != null){
			if (command.equals("start")) {
				// command for adding start/test entities to Datastore
				dds.addStartEntities();	
				req.setAttribute("result", "Start entities successful added to Datastore!");
			}
			else if (command.startsWith("new")) {
				// command for adding new entitiy type
				String [] newEntity = command.split(" ");
				if (newEntity.length == 2)
					try {
						dds.addNewEntity(newEntity[1]);
						req.setAttribute("result", "Adding new entity type " + newEntity[1] + " was successful!\n"
								+ "Now you can put entities within the user console!");
					} catch (EntityNotFoundException | InputMismatchException e) {
						req.setAttribute("result", e.getMessage());
					}
			}
			else {
				try {
					// if command is valid, new schema and generated datalog rules will be saved in datastore
					datalogRules = dds.saveSchemaChange(command);
					req.setAttribute("result", "Schema change was successful! " +
							"\n\nGenerated Datalog rules:\n" + datalogRules);
				} catch (InputMismatchException | ParseException | 
						parserRuletoJava.ParseException | parserQueryToDatalogToJava.TokenMgrError 
						| EntityNotFoundException e) {
					// if an error occurs, show error message in result text box
					req.setAttribute("result", e.getMessage());
				}
			}
		}		

		jsp = req.getRequestDispatcher("/WEB-INF/admin.jsp");
		jsp.forward(req, resp);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException{
		
		RequestDispatcher jsp = null;
		jsp = req.getRequestDispatcher("/WEB-INF/admin.jsp");
		jsp.forward(req, resp);
	}
}
