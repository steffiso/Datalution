package queryTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import parserQueryToDatalogToJava.ParseException;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import datastore.DatalutionDatastoreService;

public class ComplexQuery {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void test() {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		dds.addStartEntities();
		
		List<String> inputList = new ArrayList<String>();
		inputList.add("add Player.score=100");
		inputList
				.add("copy Player.score to Mission where Player.id=Mission.pid");
		inputList
				.add("move Player.name to Mission where Player.id=Mission.pid");
		inputList.add("add Mission.priority=1");

		for (String input : inputList)
			try {
				dds.saveSchemaChange(input);
			} catch (InputMismatchException | ParseException | IOException
					| parserRuletoJava.ParseException | EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		String[] attributes = getAttributesFromGetCommand("get Player.id=1", dds);
		Entity userPlayer;
		try {
			userPlayer = dds.get(attributes[0], attributes[1]);		
			assertEquals("{score=100, ts=0, id=1, points=150}", userPlayer
					.getProperties().toString());
		} catch (InputMismatchException | ParseException | IOException | parserRuletoJava.ParseException
				| parserPutToDatalog.ParseException | URISyntaxException | EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

		
		attributes = getAttributesFromGetCommand("get Mission.id=1", dds);
		Entity userMission;
		try {
			userMission = dds.get(attributes[0], attributes[1]);
			assertEquals(
					"{id=1, title=go to library, ts=0, priority=1, name=Lisa S., score=100, pid=1}",
					userMission.getProperties().toString());
		} catch (InputMismatchException | ParseException | IOException | parserRuletoJava.ParseException
				| parserPutToDatalog.ParseException | URISyntaxException | EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] getAttributesFromGetCommand(String getCommand,
			DatalutionDatastoreService dds) {
		ParserQueryToDatalogToJava parserget = new ParserQueryToDatalogToJava(
				new StringReader(getCommand));
		try {
			parserget.getJavaRules(dds);
		} catch (InputMismatchException | ParseException | IOException
				| parserRuletoJava.ParseException | EntityNotFoundException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new String[] { parserget.getKind(), parserget.getIdStr() };

	}

}
