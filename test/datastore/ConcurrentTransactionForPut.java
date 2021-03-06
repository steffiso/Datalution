package datastore;

import static org.junit.Assert.*;

import java.util.ConcurrentModificationException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;

import datastore.DatalutionDatastoreService;

/**
 * This is s unit test class for testing concurrent put commands in CloudDatalution.
 */

public class ConcurrentTransactionForPut {

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
		ds.put(player1);
		ds.put(player2);
	}

	/**
	 * test two concurrent callings of the put command
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentPutWithTransaction() throws InterruptedException,
			EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey("Player", 1);

		putEntities(ds, parent);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		int ts1 = 0;
		int ts2 = 0;
		Transaction txn = ds.beginTransaction();
		Transaction txn2 = ds.beginTransaction();
		try {
			ts1 = dds.getLatestTimestamp(txn, "Player1", 1) + 1;
			assertEquals(3, ts1);

			Entity e1 = new Entity("Player1", "1" + Integer.toString(ts1),
					parent);
			e1.setProperty("ts", ts1);
			e1.setProperty("name", "Lisa1");
			ds.put(txn, e1);

			ts2 = dds.getLatestTimestamp(txn2, "Player1", 1) + 1;
			assertEquals(3, ts2);
			txn.commit();

			Entity e2 = new Entity("Player1", "1" + Integer.toString(ts2),
					parent);
			e2.setProperty("name", "Lisa2");
			e1.setProperty("ts", ts2);
			ds.put(txn2, e2);
			txn2.commit(); // fails - ConcurrentModificationException thrown
		} catch (ConcurrentModificationException e) {
			throw e;
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
			if (txn2.isActive()) {
				txn2.rollback();
			}

			Entity latestEntity = dds.getLatestEntity("Player1", 1);
			assertEquals("Lisa1", latestEntity.getProperty("name"));

			// after that you could retry txn2, see testSinglePut
		}

	}

	/**
	 * test a single put in one transaction
	 */
	@Test
	public void testSinglePut()
			throws InterruptedException, EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey("Player", 1);

		putEntities(ds, parent);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		Transaction txn = ds.beginTransaction();
		int ts = 0;
			ts = dds.getLatestTimestamp(txn, "Player1", 1) + 1;
			assertEquals(3, ts);
			Entity e = new Entity("Player1", "1" + Integer.toString(ts),
					parent);
			e.setProperty("name", "Lisa2");
			e.setProperty("ts", ts);
			ds.put(txn, e);
			txn.commit();
			Entity latestEntity = dds.getLatestEntity("Player1", 1);
			assertEquals("Lisa2", latestEntity.getProperty("name"));
	}
	
	/**
	 * test the provided put method of DatalutionDatastoreService Class in an embedded transaction
	 */
	@Test
	public void testSinglePutWithEmbeddedTransaction()
			throws InterruptedException, EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey("Player", 1);

		putEntities(ds, parent);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		Transaction txn = ds.beginTransaction();

		Entity e1 = new Entity("Player1");
		e1.setProperty("name", "Lisa1");
		e1.setProperty("id", 1);
		dds.put(e1);
		txn.commit();
		Entity latestEntity = dds.getLatestEntity("Player1", 1);
		assertEquals("Lisa1", latestEntity.getProperty("name"));
	}
}