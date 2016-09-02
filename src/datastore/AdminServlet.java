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

	private String rulesStr = "";
			
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{	
		
		RequestDispatcher jsp = null;
		String command = req.getParameter("command");
		DatalutionDatastoreService dds = new DatalutionDatastoreService();
		rulesStr = "";		
		
		if (command != null){
			if (command.equals("start")) {
				dds.addStartEntities();	
				req.setAttribute("result", "Start entities added to Datastore!");
			}
			else if (command.startsWith("new")) {
				String [] newEntity = command.split(" ");
				if (newEntity.length == 2)
					try {
						dds.addNewEntity(newEntity[1]);
						req.setAttribute("result", "Adding new entity type " + newEntity[1] + " was successful! "
								+ "Now you can put entities within the user console!");
					} catch (EntityNotFoundException | InputMismatchException e) {
						req.setAttribute("result", e.getMessage());
					}
			}
			else {
				try {
					rulesStr = dds.saveSchemaChange(command);
					req.setAttribute("result", "Schema change was successful! \nGenerated Datalog rules:\n" + rulesStr);
				} catch (InputMismatchException | ParseException | 
						parserRuletoJava.ParseException | EntityNotFoundException e) {
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
