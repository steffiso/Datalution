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

import com.google.api.server.spi.response.BadRequestException;

import parserQueryToDatalogToJava.ParseException;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {

	private String rulesStr = "";
			
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {			
		String command = req.getParameter("command");
		DatalutionDatastoreService dds = new DatalutionDatastoreService();
		rulesStr = "";		
		
		if (command != null){
			if (command.equals("start")) {
				dds.addStartEntities();	
				resp.sendRedirect("/admin");
			}
			else if (command.startsWith("new")) {
				String [] newEntity = command.split(" ");
				if (newEntity.length == 2)
					dds.addNewEntity(newEntity[1]);
			}
			else {
				try {
					rulesStr = dds.saveSchemaChange(command);
					resp.sendRedirect("/admin");
				} catch (InputMismatchException e) {
					resp.getWriter().println("Error:");
					resp.getWriter().println(e.getMessage());
					resp.setHeader("Refresh", "5;url=/admin");
				} catch (ParseException e) {
					resp.getWriter().println("Error:");
					resp.getWriter().println(e.getMessage());
					resp.setHeader("Refresh", "5;url=/admin");
				} catch (parserRuletoJava.ParseException e) {
					resp.getWriter().println("Error:");
					resp.getWriter().println(e.getMessage());
					resp.setHeader("Refresh", "5;url=/admin");
				} catch (BadRequestException e) {
					// TODO Auto-generated catch block
					resp.getWriter().println(e.getMessage());
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
