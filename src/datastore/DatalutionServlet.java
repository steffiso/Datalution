package datastore;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

@SuppressWarnings("serial")
public class DatalutionServlet extends HttpServlet {
	private String username;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		Player userPlayer = null;
		RequestDispatcher jsp = null;
		if (req.getParameter("username") != null) {
			username = req.getParameter("username");
		}
		if (username != null) {
			OfyService.ofy();
			userPlayer = ofy().load().type(Player.class).id(username).now();
			if (userPlayer != null)
				req.setAttribute("values", userPlayer.toString());
			req.setAttribute("username", username);
		}

		jsp = req.getRequestDispatcher("/WEB-INF/start.jsp");
		jsp.forward(req, resp);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		OfyService.ofy();
		ofy().save()
				.entity(new Player(username, req.getParameter("name"), req
						.getParameter("character"), 100)).now();
		resp.sendRedirect("/datalution");

	}

}
