package datastore;

import java.util.ConcurrentModificationException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import datastore.DatalutionDatastoreService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is a unit test class for testing concurrent get and put calls to
 * datastore
 *
 * für die Tests wurden nur die Operationen extrahiert die Aufrufe an Datastore
 * beinhalten:
 * 
 * ein put im Datalution besteht aus den Datastore Operationen
 * getLatestTimestamp(txn,...) und ds.put(txn,Entity), beide nacheinander in
 * einer Transaktion. getLatestTimestamp wurde für diese Tests weggelassen=> für
 * eine besssere Übersicht der elementaren Operationen.
 * 
 * ein get im Datalution besteht aus den Datastore Operationen
 * getLatestEntity(txn,...) am Anfang, einem oder weiteren
 * getLatestEntity(txn,...) im Top Down Verfahren und ds.put(null,Entity) am
 * Ende.
 */

public class ConcurrentTransactionGetPut {

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

	/**
	 * Test von concurrent get und put. 2 Transaktionen: txn1 und txn2; "null"
	 * bedeutet außerhalb von Transaktionen Reihenfolge der Operationen:
	 * txn1.getLatestEntity, txn2.put, txn1.getLatestEntity, null.put,
	 * txn2.commit, txn1.commit
	 * 
	 * immer eine ConcurrentModificationException bei commmit von txn2, egal ob
	 * vor oder nach txn1
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentGetPut1() throws EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey("Player", 1);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);

		putEntities(ds, parent);

		Entity entityOfPutCommand = new Entity("Player2", "1"
				+ Integer.toString(1), parent);
		entityOfPutCommand.setProperty("name", "Lisa2");
		entityOfPutCommand.setProperty("ts", 1);

		Entity entityOfLazyPut = new Entity("Player2", "1"
				+ Integer.toString(0), parent);
		entityOfLazyPut.setProperty("name", "Lisa1");
		entityOfLazyPut.setProperty("ts", 0);

		Transaction txn = ds.beginTransaction();
		Transaction txn2 = ds.beginTransaction();

		// if no entity found for get command - start lazy migration
		dds.getLatestEntity(txn, "Player2", 1);

		// put of put command
		ds.put(txn2, entityOfPutCommand);

		// search Entity in lazy migration
		dds.getLatestEntity(txn, "Player2", 1);

		// ds.put of lazy migration
		ds.put(null, entityOfLazyPut);

		// failed txn2, egal welche Reihenfolge
		txn2.commit();

		txn.commit();

	}

	/**
	 * Test von concurrent get und put. 2 Transaktionen: txn1 und txn2; "null"
	 * bedeutet außerhalb von Transaktionen Reihenfolge der Operationen:
	 * txn1.getLatestEntity, null.put, txn2.put, txn2.commit, txn1.commit
	 * 
	 * immer erfolgreich, egal welche Reihenfolge der commits am Ende
	 */
	@Test
	public void testConcurrentGetPut2() throws EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey("Player", 1);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		putEntities(ds, parent);

		Entity entityOfPutCommand = new Entity("Player2", "1"
				+ Integer.toString(1), parent);
		entityOfPutCommand.setProperty("name", "Lisa2");
		entityOfPutCommand.setProperty("ts", 1);

		Entity entityOfLazyPut = new Entity("Player2", "1"
				+ Integer.toString(0), parent);
		entityOfLazyPut.setProperty("name", "Lisa1");
		entityOfLazyPut.setProperty("ts", 0);

		Transaction txn = ds.beginTransaction();
		Transaction txn2 = ds.beginTransaction();

		// search Entity in lazy migration
		dds.getLatestEntity(txn, "Player2", 1);

		// ds.put of lazy migration
		ds.put(null, entityOfLazyPut);

		// put of put command
		ds.put(txn2, entityOfPutCommand);

		txn2.commit(); // success von beiden, egal welche Reihenfolge
		txn.commit();

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
	public void testConcurrentGetPut3() throws EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key parent = KeyFactory.createKey("Player", 1);
		DatalutionDatastoreService dds = new DatalutionDatastoreService(ds);
		putEntities(ds, parent);

		Entity entityOfPutCommand = new Entity("Player2", "1"
				+ Integer.toString(1), parent);
		entityOfPutCommand.setProperty("name", "Lisa2");
		entityOfPutCommand.setProperty("ts", 1);

		Entity entityOfLazyPut = new Entity("Player2", "1"
				+ Integer.toString(0), parent);
		entityOfLazyPut.setProperty("name", "Lisa1");
		entityOfLazyPut.setProperty("ts", 0);

		Transaction txn = ds.beginTransaction();
		Transaction txn2 = ds.beginTransaction();
		// if no entity found for get command - start lazy migration
		dds.getLatestEntity(txn, "Player2", 1);

		// put of put command
		ds.put(txn2, entityOfPutCommand);

		txn2.commit(); // erfolgreich

		// search Entity in lazy migration => old value
		dds.getLatestEntity(txn, "Player2", 1);

		// search Entity out of transaction => new value
		dds.getLatestEntity(null, "Player2", 1);

		// ds.put of lazy migration
		ds.put(null, entityOfLazyPut);

		// obwohl alten Wert gelesen erfolgreich! Transaktion verhindert nicht,
		// dass der alte Wert ausgegeben wird!
		txn.commit();

	}

	/**
	 * allgemeiner Test von "Optimistic Transactions". 2 Transaktionen: txn1 und
	 * txn2 Reihenfolge der Operationen: txn1.get, txn2.put, txn1.get
	 * 
	 * eine Exception erwartet, war aber erfolgreich1
	 */
	@Test
	public void testConcurrentGetPutGet() throws EntityNotFoundException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity person = new Entity("Person", "tom");
		ds.put(null, person);

		Transaction txn = ds.beginTransaction();
		Transaction txn2 = ds.beginTransaction();

		ds.get(txn, KeyFactory.createKey("Person", "tom"));

		ds.put(txn2, person);

		ds.get(txn, KeyFactory.createKey("Person", "tom")); // reads old value

		txn2.commit(); // success
		txn.commit(); // success, obwohl alten wert gelesen

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
		ds.put(null, player1);
		ds.put(null, player2);
	}
}