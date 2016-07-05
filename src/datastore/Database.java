package datastore;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.servlet.ServletException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import parserPutToDatalog.ParseException;
import parserPutToDatalog.ParserForPut;
import parserRuletoJava.ParserRuleToJava;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class Database {

	DatastoreService ds;
	
	public Database(){
		OfyService.ofy();
		ds = DatastoreServiceFactory.getDatastoreService();
	}
	public Database(DatastoreService ds) {
		OfyService.ofy();
		this.ds =ds;
	}
	
	public void addStartEntities(){
		
//		Entity schemaPlayer = new Entity("Schema", "Player");
//		Entity schemaMission = new Entity("Schema", "Mission");
//		ds.put(schemaPlayer);
//		ds.put(schemaMission);
		Entity schemaPlayer1 = new Entity("SchemaPlayer",1, KeyFactory.createKey("Schema", "Player"));
		schemaPlayer1.setProperty("value", "?id,?name");
		schemaPlayer1.setProperty("kind", "Player");
		schemaPlayer1.setProperty("version", "1");
		schemaPlayer1.setProperty("ts", "1");
		
		Entity schemaMission1 = new Entity("SchemaMission",1, KeyFactory.createKey("Schema", "Mission"));
		//new Entity(new KeyFactory.Builder("Schema","Mission").addChild("SchemaMission",1).getKey());
		schemaMission1.setProperty("value", "?id,?title,?pid");
		schemaMission1.setProperty("kind", "Mission");
		schemaMission1.setProperty("version", "1");
		schemaMission1.setProperty("ts", "1");
		
		String id = "1";
		//Entity player = new Entity(KeyFactory.createKey("Player",id));
		Entity player1 = new Entity("Player1",id + "1", KeyFactory.createKey("Player", id));
		//		new Entity(new KeyFactory.Builder("Player",id).addChild("Player1",id + "1").getKey());
		player1.setProperty("id", 1);
		player1.setProperty("name", "Lisa");
		player1.setProperty("ts", 1);
		
		Entity player2 = new Entity("Player1",id + "2", KeyFactory.createKey("Player", id));
		player2.setProperty("id", 1);
		player2.setProperty("name", "Lisa S.");
		player2.setProperty("ts", 2);
		
		String id2 = "1";
		Entity mission = new Entity("Mission1",id2 + "1", KeyFactory.createKey("Mission", id2));
		mission.setProperty("id", 1);
		mission.setProperty("title", "go to library");
		mission.setProperty("pid", 1);
		mission.setProperty("ts", 1);
		
		ds.put(schemaPlayer1);
		ds.put(schemaMission1);
		//ds.put(player);
		ds.put(player1);
		ds.put(player2);
		ds.put(mission);
		
	}

	// return all rules
	public ArrayList<datastore.Rule> getRules() {
//		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
//
//        String rules = (String) syncCache.get("Rules");       
		
		//Entity rulesEntity = null;
		List<Entity> rulesEntity = null;
//		try {
			Query q = new Query().setAncestor(KeyFactory.createKey("Rules", 1));
			rulesEntity = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
			//rulesEntity = ds.get(KeyFactory.createKey("Rules",1));
//		} catch (EntityNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if(rulesEntity!=null){
			String rules = "";
			for(Entity e: rulesEntity) 
				rules = rules + (String) e.getProperty("value");
			ArrayList<Rule> rulesList = new ArrayList<Rule>();
			
			if (!rules.equals("")){
        	 ArrayList<String> rulesStringList = new ArrayList<String>(Arrays.asList(rules.split("\n")));
             for (String rule: rulesStringList){
             	Rule r = new Rule();
             	r.setValue(rule);
             	r.setTimestamp(new Date());
             	if (!rule.startsWith("latest") && !rule.startsWith("legacy")){
             		r.setHead(true);
             	}
             	else {
                 	r.setHead(false);        		
             	}
             	rulesList.add(r);
             }
        }
		
        return rulesList;		}
		else return null;
	}
	
	public void addRules(String newRules) throws ServletException{
		//toDO: split rules and save only headrules
		
//		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
//
//        // Write this value to cache using getIdentifiable and putIfUntouched.
//        for (long delayMs = 1; delayMs < 1000; delayMs *= 2) {
//          IdentifiableValue oldRules = syncCache.getIdentifiable("Rules");
//          if (oldRules == null) {
//            // Key doesn't exist. We can safely put it in cache.
//            syncCache.put("Rules", newRules);
//            break;
//          } else if (syncCache.putIfUntouched("Rules", oldRules, oldRules + newRules)) {
//            // newValue has been successfully put into cache.
//            break;
//          } else {
//            // Some other client changed the value since oldValue was retrieved.
//            // Wait a while before trying again, waiting longer on successive loops.
//            try {
//              Thread.sleep(delayMs);
//            } catch (InterruptedException e) {
//              throw new ServletException("Error when sleeping", e);
//            }
//          }
//        }  
		
//		List<Entity> rulesEntity = null;
//		String rules = "";
//		try {
//			Query q = new Query().setAncestor(KeyFactory.createKey("Rules", 1));
//			rulesEntity = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
//		} catch (EntityNotFoundException e) {
//			// TODO Auto-generated catch block
//		}
//		
//
//		ArrayList<String> ruleslist = new ArrayList<String>(Arrays.asList(newRules.split("\n")));
//		newRules = "";
//		for (String s: ruleslist){
//			if (!s.startsWith("legacy") && !s.startsWith("latest")){
//				newRules = newRules + s + "\n";
//			}
//		}
//		rules = rules + newRules;
		Entity e = new Entity("Rule", KeyFactory.createKey("Rules",1));
		e.setProperty("value", newRules);
		ds.put(e);
		
		
	}

	// return the schema for kind and version
	public Schema getSchema(String inputKind, int inputVersion){
		Query q = new Query ("Schema"+inputKind).setAncestor(KeyFactory.createKey("Schema",inputKind))
				.addFilter("version", FilterOperator.EQUAL, inputVersion);
		
		List<Entity> schemaList = ds.prepare(q).asList(FetchOptions.Builder.withDefaults().limit(1));
		Entity schema = schemaList.get(0);
		Schema latestSchema = new Schema();
		latestSchema.setAttributesString(schema.getProperty("value").toString());
		latestSchema.setKind(schema.getProperty("kind").toString());
		latestSchema.setVersion(Integer.parseInt(schema.getProperty("version").toString()));
		latestSchema.setTimestamp(Integer.parseInt(schema.getProperty("ts").toString()));
		return latestSchema;
	}

	// returns the latest schema version number for the given kind
	public int getLatestSchemaVersion(String inputKind){
		Schema latestSchema =  getLatestSchema(inputKind);
		int latestSchemaVersion = 0;
		if (latestSchema != null){
			latestSchemaVersion = latestSchema.getVersion();
		}

		return latestSchemaVersion;
	}

	// returns the latest schema for input kind
	// output: "?name,?score,?points"
	public Schema getLatestSchema(String kind){
		Query q = new Query ("Schema"+kind).setAncestor(KeyFactory.createKey("Schema",
				kind));
		List<Entity> schemaList = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
		Entity schema = schemaList.get(schemaList.size()-1);
		Schema latestSchema = new Schema();
		latestSchema.setAttributesString(schema.getProperty("value").toString());
		latestSchema.setKind(schema.getProperty("kind").toString());
		latestSchema.setVersion(Integer.parseInt(schema.getProperty("version").toString()));
		latestSchema.setTimestamp(Integer.parseInt(schema.getProperty("ts").toString()));
		return latestSchema;
	}

	public Entity getLatestEntity(String kind, String id){		
		Schema latestSchema = getLatestSchema(kind.replaceAll("\\d", ""));
		int version = latestSchema.getVersion();
		Query kindQuery = new Query(kind).setAncestor(KeyFactory.createKey(kind.replaceAll("\\d", ""),
						id));
		List<Entity> latestEntity = ds.prepare(kindQuery).asList(FetchOptions.Builder.withDefaults());
		Entity e = null;
		if (!latestEntity.isEmpty())
			e = latestEntity.get(latestEntity.size()-1);
		
		return e;
	}
	
	public int getLatestTimestamp(String kind, String id){
		int ts = 0;
		Schema latestSchema = getLatestSchema(kind.replaceAll("\\d", ""));
		int version = latestSchema.getVersion();
		String newKey = kind + Integer.toString(version);
		Query kindQuery = new Query(newKey).setAncestor(KeyFactory.createKey(kind.replaceAll("\\d", ""),
				id)).addSort("ts", SortDirection.DESCENDING);
		List<Entity> latestEntity = ds.prepare(kindQuery).asList(FetchOptions.Builder.withDefaults());
		Entity e = null;
		if (!latestEntity.isEmpty())
			e = latestEntity.get(0);
		if (e != null){
			ts = Integer.parseInt((String) e.getProperty("ts"));
		}
		
		return ts;
	}
	
	// write the datalogFact to datastore
	// input: "Player2(4,'Lisa',40)"
	// timestamp will be added automatically
	public void putToDatabase(String datalogFact, String value){ 
		Entity entity = null;
		try {
			entity = new ParserForPut(new StringReader(datalogFact)).getEntity(value);
		} catch (InputMismatchException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (entity != null)
			ds.put(entity);

	}
	
	// write a schema to file "Schema.json"
	// input example: newSchema = "?a,?b,?c"
	public void saveCurrentSchema(String kind, ArrayList<String> newSchemaList){
		String newSchema = "";
		for(String s: newSchemaList){
			newSchema = newSchema + s + "," ;
		}
		newSchema = newSchema.substring(0, newSchema.length()-1);
		long newVersion = getLatestSchemaVersion(kind) + 1;
		Schema latestSchema = getLatestSchema(kind);
		
		Entity schema = new Entity("Schema"+kind, latestSchema.getVersion() + 1, KeyFactory.createKey("Schema", kind));
		schema.setProperty("kind", kind);
		schema.setProperty("version", newVersion);
		schema.setProperty("value", newSchema);
		schema.setProperty("ts", latestSchema.getTimestamp() + 1);
		ds.put(schema);
	}


}
