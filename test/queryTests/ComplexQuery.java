package queryTests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import datastore.DatalutionDatastoreService;

/**
 * This is a unit test class for testing a sequence of commands (like adding, copying...).
 */

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
	public void testComplexQuery() {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		dds.addStartEntities();

		List<String> inputList = new ArrayList<String>();
		
		String addScoreCommand = "add Player.score=100";
		inputList.add(addScoreCommand);
		
		String copyCommand = "copy Player.score to Mission where Player.id=Mission.pid";
		inputList.add(copyCommand); 
				
		String moveCommand = "move Player.name to Mission where Player.id=Mission.pid";
		inputList.add(moveCommand);
							
		String addPriorityCommand  = "add Mission.priority=1";
		inputList.add(addPriorityCommand);

		for (String input : inputList)
			try {
				dds.saveSchemaChange(input);
			} catch (Exception e) {
				e.printStackTrace();
			}

		Entity userPlayer;
		try {
			Key keyPlayer = KeyFactory.createKey("Player", "1");
			userPlayer = dds.get(keyPlayer);
			assertEquals("100", userPlayer
					.getProperty("score"));
			assertNull(userPlayer
					.getProperty("name"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Entity userMission;
		try {
			Key keyMission = KeyFactory.createKey("Mission", "3");
			userMission = dds.get(keyMission);
			assertEquals("100", userMission
					.getProperty("score"));
			assertEquals("'Homer'",userMission
					.getProperty("name"));
			assertEquals("1", userMission
					.getProperty("priority"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
