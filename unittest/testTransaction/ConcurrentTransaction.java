package testTransaction;

import static org.junit.Assert.*;

import java.util.ConcurrentModificationException;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.SortDirection;

import datastore.DatalutionDatastoreService;

public class ConcurrentTransaction {

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

	@Test (expected = ConcurrentModificationException.class)
	public void testConcurrentWithTransaction() throws InterruptedException,
			EntityNotFoundException {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey(
				"Player", 1);
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
		ds.put(player1);
		ds.put(player2);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		int ts1 = 0;
		int ts2 = 0;
		// Query kindQuery = new Query("Player1").setAncestor(parent);// ds.prepare(txn2,
			// kindQuery).asList(FetchOptions.Builder.withDefaults().limit(1));
		try {
			Transaction txn = ds.beginTransaction();
			Transaction txn2 = ds.beginTransaction();
			ts1 = dds.getLatestTimestamp(txn, "Player1", 1)+1;
			assertEquals(3, ts1);	
			ds.put(txn,new Entity("Player1", "1"+Integer.toString(ts1), parent));
			ts2 = dds.getLatestTimestamp(txn2, "Player1", 1)+1;	
			assertEquals(3, ts2);
			txn.commit();
			ds.put(txn2,new Entity("Player1", "1"+Integer.toString(ts2), parent));
			txn2.commit();
		} catch (ConcurrentModificationException e) {
			throw e;
		}
	}


}