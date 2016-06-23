package datastore;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.objectify.ObjectifyService.ofy;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class DatabaseTest {
//	Database db;
//	String initialEDB;
//	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
//	
//	@Before
//	public void setUp(){
//		db = new Database(); 	
//		helper.setUp();
//		
//	}
//
//	@Test
//	public void testSchemaChange(){
//		db.saveCurrentSchema("Test", new ArrayList<String>(Arrays.asList("?test1", "?test2")));
//		Schema schema = db.getLatestSchema("Test");
//		assertEquals(schema.getKind(), "Test");
//	}
//	
//	@Test
//	public void testPut() {	
//		Schema schema = new Schema();
//		schema.setAttributesString("?test1,?test2");
//		schema.setKind("Test");
//		schema.setSchemaversion(1);
//		ofy().save().entity(schema).now();
//		
//		Schema loadedSchema = db.getSchema("Test", 1);
//		assertEquals(loadedSchema.getAttributesString(), schema.getAttributesString());
//		assertEquals(loadedSchema.getKind(), schema.getKind());
//		
//		ofy().delete().entity(schema);
//	}
//	
//	@After
//	public void reset(){
//		helper.tearDown();
//	}
	
	
	
}
