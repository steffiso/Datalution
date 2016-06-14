package datastore;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class DatalutionServlet extends HttpServlet {
	private String username;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity userPlayer = null;
		RequestDispatcher jsp = null;
		if (req.getParameter("username") != null) {
			username = req.getParameter("username");
		}
		if (username != null) {
			try {
				userPlayer = ds.get(KeyFactory.createKey("Player", username));
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (userPlayer != null)
				req.setAttribute(
						"values",
						"[name="
								+ userPlayer.getProperty("name").toString()
								+ ", character="
								+ userPlayer.getProperty("character")
										.toString() + ", score="
								+ userPlayer.getProperty("score").toString()
								+ "]");
			req.setAttribute("username", username);
		}
		//saveCurrentSchema("Player", "?id,?name,?character,?score");

		jsp = req.getRequestDispatcher("/WEB-INF/start.jsp");
		jsp.forward(req, resp);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity book = new Entity(KeyFactory.createKey("Player", username));
		book.setProperty("name", req.getParameter("name"));
		book.setProperty("character", req.getParameter("character"));
		book.setProperty("score", 100);
		ds.put(book);
		resp.sendRedirect("/datalution");

	}

	public void saveCurrentSchema(String kind, String newSchema){
		int newVersion = 1;
		Schema schema = new Schema();
		schema.setAttributesString(newSchema);
		schema.setKind(kind);
		schema.setSchemaversion(newVersion);
		Date d = new Date();
		schema.setTimestamp(d);	
		OfyService.ofy();
		ofy().save().entity(schema).now();
	}
}
