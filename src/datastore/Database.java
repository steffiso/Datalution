package datastore;

import java.util.ArrayList;
import java.util.Date;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class Database {

	public Database() {
		OfyService.ofy();
	}

	// return all database entries as edb facts in one string
	// e.g.
	// "Player1(1,'Lisa',20,1).\nPlayer1(2,'Bart',20,2).\nPlayer1(3,'Homer',20,3)."
	public String getEDB(){
		String edb = "";
		return edb;
	}

	// return all database entries from database in a json-like string
	// e.g. "Player1{"id":1,"name":"Lisa","score":20,"ts":1}.\n" +
	// "Player1{"id":2,"name":"Bart","score":20,"ts":2}.\n" +
	// "Player1{"id":3,"name":"Homer","score":20,"ts":3}."
	public String getJson() {
		String edb = "";

		return edb;
	}

	// return the schema for kind and version
	public Schema getSchema(String inputKind, int inputVersion){
		Schema schema = null;
		schema = ofy().load().type(Schema.class).filter("kind", inputKind).filter("schemaversion", inputVersion).first().now();
		return schema;
	}

	// returns the latest schema version number for the given kind
	public int getLatestSchemaVersion(String inputKind){
		Schema latestSchema =  getLatestSchema(inputKind);
		int latestSchemaVersion = 0;
		if (latestSchema != null){
			latestSchemaVersion = latestSchema.getSchemaversion();
		}

		return latestSchemaVersion;
	}

	// returns the latest schema for input kind
	// output: "?name,?score,?points"
	public Schema getLatestSchema(String kind){
		Schema latestSchema =  ofy().load().type(Schema.class).filter("kind", kind).order("-schemaversion").first().now();
		return latestSchema;
	}

	// write the datalogFact to datastore
	// input: "Player2(4,'Lisa',40)"
	// timestamp will be added automatically
	public void putToDatabase(String datalogFact){


	}
	
	// write a schema to file "Schema.json"
	// input example: newSchema = "?a,?b,?c"
	public void saveCurrentSchema(String kind, ArrayList<String> newSchemaList){
		String newSchema = "";
		for(String s: newSchemaList){
			newSchema = newSchema + s + "," ;
		}
		newSchema = newSchema.substring(0, newSchema.length()-1);
		int newVersion = getLatestSchemaVersion(kind) + 1;
		Schema schema = new Schema();
		schema.setAttributesString(newSchema);
		schema.setKind(kind);
		schema.setSchemaversion(newVersion);
		Date d = new Date();
		schema.setTimestamp(d);
		
		ofy().save().entity(schema).now();
	}


}
