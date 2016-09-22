package datastore;

import java.util.ArrayList;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import datalog.Predicate;
import datalog.Rule;
import datastore.DatalutionDatastoreService;
import lazyMigration.LazyMigration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This is a unit test class for testing concurrent get and put calls to
 * datastore
 */

public class ConcurrentGetAndPutCalls {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	// Google Datastore Object
	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	
	// Datastore Wrapper
	DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
	
	@Before
	public void setUp() {
		helper.setUp();

		// parent key
		Key parent = KeyFactory.createKey("Player", 1);
		
		// example entities for testing
		putEntities(ds, parent);

	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	/**
	 * Test von concurrent get und put. 2 Transaktionen: txn1 und txn2; "null"
	 * bedeutet außerhalb von Transaktionen Reihenfolge der Operationen:
	 * txn1.getLatestEntity, txn2.put, txn2.commit, txn1.getLatestEntity,
	 * null.getLatestEntity, null.put, txn1.commit
	 * 
	 * erfolgreich, d.h. das Get Kommando gibt einen veralteten Wert zurück
	 */
	@Test
	public void testConcurrentGetPut() throws Exception {
		
		/*---------------------------------preparation-----------------------------------*/
		
		// execute add Command -> generate new schema version "Player2" and datalog rules
		String addCommand = "add Player.score=200";
		dds.saveSchemaChange(addCommand);

		// entity of put command, which will be executed concurrently to get command
		Entity entityOfPutCommand = new Entity("Player2");
		entityOfPutCommand.setProperty("id", 1);
		entityOfPutCommand.setProperty("name", "Lisa S.");
		entityOfPutCommand.setProperty("points", 150);
		entityOfPutCommand.setProperty("score", 300);
		
		// prepare all requirements for lazy migration
		LazyMigration migration = prepareLazyMigration("Player", "1", dds);

		/*---------------------------------start test-----------------------------------*/
		
		// if no entity found for get command - start lazy migration
		Entity entity = dds.getLatestEntity("Player2", 1);
		assertNull(entity);

		// concurrent put command, is executed within transaction in dds.put
		dds.put(entityOfPutCommand);

		// start lazy migration
		ArrayList<String> resultValues = migration.executeLazyMigration();
		
		// put was detected in lazy migration!
		assertEquals("[1, 'Lisa S.', 150, 300, 1]", resultValues.toString());	
		
		// if get command would be transaction safe -
		// it should have returned: [1, 'Lisa S.', 150, 200, 0] -- ToDo!
		
	}

	public void putEntities(DatastoreService ds, Key parent) {
		Entity player1 = new Entity("Player1", "11", parent);
		player1.setProperty("id", 1);
		player1.setProperty("name", "Lisa");
		player1.setProperty("points", 150);
		player1.setProperty("ts", 1);

		Entity player2 = new Entity("Player1", "12", parent);
		player2.setProperty("id", 1);
		player2.setProperty("name", "Lisa S.");
		player2.setProperty("points", 150);
		player2.setProperty("ts", 2);

		Entity schemaPlayer1 = new Entity("SchemaPlayer", 1, KeyFactory.createKey("Schema", "Player"));
		schemaPlayer1.setProperty("value", "?id,?name,?points");
		schemaPlayer1.setProperty("kind", "Player");
		schemaPlayer1.setProperty("version", 1);
		schemaPlayer1.setProperty("ts", 1);

		ds.put(player1);
		ds.put(player2);
		ds.put(schemaPlayer1);
	}

	public LazyMigration prepareLazyMigration(String kind, String id, DatalutionDatastoreService dds) {
		ArrayList<Rule> datalogRules = null;
		try {
			datalogRules = dds.prepareRulesForLazyMigration(kind, id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, String> uniMap = DatalutionDatastoreService.prepareUniMapForLazyMigration(kind, id);

		// get list of schema attributes
		Schema latestSchema = null;
		try {
			latestSchema = dds.getLatestSchema(kind);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		int schemaversion = latestSchema.getVersion();
		ArrayList<String> schemaAttributes = latestSchema.getAttributesAsList();
		schemaAttributes.add("?ts");

		Predicate goal = new Predicate("get" + kind + schemaversion, schemaAttributes.size(), schemaAttributes);

		// execute lazy migration
		LazyMigration migrate = new LazyMigration(datalogRules, goal, uniMap);
		return migrate;
	}

}