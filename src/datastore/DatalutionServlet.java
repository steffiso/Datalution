package datastore;

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

import com.google.appengine.api.datastore.Entity;

import datalog.Rule;

@SuppressWarnings("serial")
public class DatalutionServlet extends HttpServlet {
	private int userid;
	private String kind;
	private String userIdStr;
	private String getCommand;
	private String putCommand;
	private DatalutionDatastoreService dds;

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		dds = new DatalutionDatastoreService();
		Entity childPlayer = null;
		if (req.getParameter("putCommand") != null){
			putCommand = req.getParameter("putCommand");
			try {
				childPlayer = new ParserForPut(new StringReader(
						putCommand)).start();
				dds.put(childPlayer);
			} catch (InputMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		resp.sendRedirect("/user");

	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		dds = new DatalutionDatastoreService();
		Entity userPlayer = null;
		RequestDispatcher jsp = null;
		if (req.getParameter("getCommand") != null) {
			getCommand = req.getParameter("getCommand");
			ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
						new StringReader(getCommand));
			try {
				@SuppressWarnings("unused")
				ArrayList<Rule> rules = parserget.getJavaRules(dds);
			} catch (InputMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (parserQueryToDatalogToJava.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (parserRuletoJava.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			userid = parserget.getId();
			kind = parserget.getKind();
			userIdStr = parserget.getIdStr();
		}
		else getCommand = null;
		
		if (getCommand != null) {			
			userPlayer = dds.get(kind, userIdStr);
			
			if (userPlayer != null){
				Schema latestSchema = dds.getLatestSchema(kind);
				String values = "[";
				for (String s : latestSchema.getAttributes())
					values = values + s + "="
							+ userPlayer.getProperty(s.substring(1)) + ", ";
				values = values + "ts=" + userPlayer.getProperty("ts") + "]";
				req.setAttribute("values", values);
			}			
			
			req.setAttribute("username", userid);
		}

		jsp = req.getRequestDispatcher("/WEB-INF/start.jsp");
		jsp.forward(req, resp);
	}

}