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

import parserQueryToDatalogToJava.ParseException;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {
	
	String rulesStr = "";
	List<Schema> schema;
	String command;
	Date timestamp;
			
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {				
		String command = req.getParameter("command");
		DatalutionDatastoreService dds = new DatalutionDatastoreService();
		rulesStr = "";
		
		
		if (command != null){
			if (command.equals("start")) {
				dds.addStartEntities();	
				resp.sendRedirect("/admin");
			}
			else {
				try {
				rulesStr = dds.changeSchema(command);
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
