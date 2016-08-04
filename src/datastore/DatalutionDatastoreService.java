package datastore;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;

import datalog.Fact;
import datalog.Predicate;
import datalog.Rule;
import lazyMigration.LazyMigration;

import com.google.appengine.api.memcache.MemcacheServiceFactory;

import parserPutToDatalog.ParseException;
import parserPutToDatalog.ParserForPut;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;
import parserRuletoJava.ParserRuleToJava;

public class DatalutionDatastoreService {

	DatastoreService ds;
	
	public DatalutionDatastoreService(){
		ds = DatastoreServiceFactory.getDatastoreService();
	}
	
	public void addStartEntities(){
		Entity schemaPlayer1 = new Entity("SchemaPlayer",1, KeyFactory.createKey("Schema", "Player"));
		schemaPlayer1.setProperty("value", "?id,?name,?points");
		schemaPlayer1.setProperty("kind", "Player");
		schemaPlayer1.setProperty("version", 1);
		schemaPlayer1.setProperty("ts", 1);
		
		Entity schemaMission1 = new Entity("SchemaMission",1, KeyFactory.createKey("Schema", "Mission"));
		schemaMission1.setProperty("value", "?id,?title,?pid");
		schemaMission1.setProperty("kind", "Mission");
		schemaMission1.setProperty("version", 1);
		schemaMission1.setProperty("ts", 1);
		
		Entity player1 = new Entity("Player1","11", KeyFactory.createKey("Player", 1));
		player1.setProperty("id", 1);
		player1.setProperty("name", "Lisa");
		player1.setProperty("points", 150);
		player1.setProperty("ts", 1);
		
		Entity player2 = new Entity("Player1","12", KeyFactory.createKey("Player", 1));
		player2.setProperty("id", 1);
		player2.setProperty("name", "Lisa S.");
		player2.setProperty("points", 150);
		player2.setProperty("ts", 2);
		
		Entity player3 = new Entity("Player1","21", KeyFactory.createKey("Player", 2));
		player3.setProperty("id", 2);
		player3.setProperty("name", "Bart");
		player3.setProperty("points", 100);
		player3.setProperty("ts", 1);
		
		Entity player4 = new Entity("Player1","31", KeyFactory.createKey("Player", 3));
		player4.setProperty("id", 3);
		player4.setProperty("name", "Homer");
		player4.setProperty("points", 100);
		player4.setProperty("ts", 1);
		
		Entity mission1 = new Entity("Mission1","11", KeyFactory.createKey("Mission", 1));
		mission1.setProperty("id", 1);
		mission1.setProperty("title", "go to library");
		mission1.setProperty("pid", 1);
		mission1.setProperty("ts", 1);
		
		Entity mission2 = new Entity("Mission1","21", KeyFactory.createKey("Mission", 2));
		mission2.setProperty("id", 2);
		mission2.setProperty("title", "go to work");
		mission2.setProperty("pid", 20);
		mission2.setProperty("ts", 1);
		
		Entity mission3 = new Entity("Mission1","31", KeyFactory.createKey("Mission", 3));
		mission3.setProperty("id", 3);
		mission3.setProperty("title", "visit Moe");
		mission3.setProperty("pid", 3);
		mission3.setProperty("ts", 1);
		
		ds.put(schemaPlayer1);
		ds.put(schemaMission1);
		ds.put(player1);
		ds.put(player2);
		ds.put(player3);
		ds.put(player4);
		ds.put(mission1);
		ds.put(mission2);
		ds.put(mission3);
	}
	
	public void put(Entity entity){
		int retries = 3;
		while (true) {
		  Transaction txn = ds.beginTransaction();
		  try {
			  ds.put(entity);
			  txn.commit();
			  break;
		  } catch (ConcurrentModificationException e) {
		    if (retries == 0) {
		      throw e;
		    }
		    // Allow retry to occur
		    --retries;
		  } finally {
		    if (txn.isActive()) {
		      txn.rollback();
		    }
		  }
		}
	}
	
	public Entity get(String kind, String id){
		Entity goalEntity = getLatestEntity(kind,Integer.parseInt(id));
		
		if (goalEntity == null){		
			ArrayList<Rule> rulesTemp = new ArrayList<Rule>();
			ArrayList<datastore.Rule> rules= getAllRulesFromDatastore();
			for(datastore.Rule r:rules)
				try {
					rulesTemp.addAll(new ParserRuleToJava(new StringReader(r.getValue())).parseHeadRules());
				} catch (parserRuletoJava.ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			// start lazy migration
			try {
				rulesTemp.addAll(new ParserQueryToDatalogToJava(
						new StringReader("get " + kind + ".id="+id)).getJavaRules(this));
			} catch (InputMismatchException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (parserQueryToDatalogToJava.ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (parserRuletoJava.ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Map<String, String> attributeMap = new TreeMap<String, String>();
			attributeMap.put("kind", kind);
			attributeMap.put("position", "0");
			attributeMap.put("value", id);
			ArrayList<Map<String, String>> uniMap = new ArrayList<Map<String, String>>();
			uniMap.add(attributeMap);
			ArrayList<String> schema;
			Predicate goal;
			schema = getLatestSchema(kind).getAttributes();
	
			schema.add("?ts");
	
			goal = new Predicate("get" + kind
					+ getLatestSchemaVersion(kind), schema.size(),
					schema);
			
			ArrayList<Fact> facts = new ArrayList<Fact>();
	
			LazyMigration migrate = new LazyMigration(facts, rulesTemp, goal,
					uniMap);
			
			@SuppressWarnings("unused")
			ArrayList<String> answerString = null;
			try {
				answerString = migrate.writeAnswersInDatabase();
			} catch (ParseException | IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// besser : nicht nochmal aus datastore abfragen, 
			// sondern direkt aus answerString?
			goalEntity = getLatestEntity(kind, Integer.parseInt(id));
			
		}
		
		return goalEntity;
	}
	
	public String changeSchema(String command) throws InputMismatchException, parserQueryToDatalogToJava.ParseException, IOException, parserRuletoJava.ParseException{
		String rulesStr = new ParserQueryToDatalogToJava(new StringReader(
					command)).getDatalogRules(this);

		if (!rulesStr.equals("")) {
			HashMap<String, String> rulesMap = new HashMap<String, String>();
			List<String> rulesSplit = new ArrayList<String>(Arrays.asList(rulesStr.split("\n")));
			for (String rule: rulesSplit){	
				String head = rule.substring(0, rule.indexOf("("));
				if (rulesMap.containsKey("Rules" + head)) {
					String oldValue = rulesMap.get("Rules" + head);
					rulesMap.put("Rules" + head, oldValue + "\n" + rule);
				}
				else {
					rulesMap.put("Rules" + head, rule);
				}								
			}
			addRules(rulesMap);
		}
		return rulesStr;
	}

	public ArrayList<datastore.Rule> getAllRulesFromDatastore(){

		List<Entity> rulesEntity = null;
		Query q = new Query().setAncestor(KeyFactory.createKey("Rules", 1));
		rulesEntity = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
			
		if (rulesEntity != null) {
			String rules = "";
			for(Entity e: rulesEntity) 
				rules = rules + (String) e.getProperty("value");
			
			ArrayList<datastore.Rule> rulesList = new ArrayList<datastore.Rule>();
			
			if (!rules.equals("")) {
				ArrayList<String> rulesStringList = new ArrayList<String>(Arrays.asList(rules.split("\n")));
				for (String rule: rulesStringList){
					datastore.Rule r = new datastore.Rule();
					r.setValue(rule);
					rulesList.add(r);
				}
			}
		
			return rulesList;		
        }
		else 
			return null;
	}
	
	// input key, e.g. "Player2"
	// return specific rules, e.g. all rules starting with "Player2"
	public ArrayList<datastore.Rule> getRulesFromMemcache(String key) {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		ArrayList<datastore.Rule> rulesList = new ArrayList<datastore.Rule>();
        String rules = "";
        
        rules = (String) syncCache.get("Rules" + key);
		if (!rules.equals("")){
			// rule with given key exists in memcache
			ArrayList<String> rulesStringList = new ArrayList<String>(Arrays.asList(rules.split("\n")));
			for (String rule: rulesStringList){
				datastore.Rule r = new datastore.Rule();
				r.setValue(rule);
				rulesList.add(r);
			}
		}
		else {
			// put rule from datastore in memcache
			Map<String,String> map = new HashMap<String,String>();
			String value = "";
			Query q = new Query("Rules" + key).setAncestor(KeyFactory.createKey("Rules", 1));
			List<Entity> rulesEntity = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
			for (Entity e : rulesEntity){
				value = value + e.getProperty("value") + "\n";
			}
			map.put("Rules" + key, value);
			addRules(map);
		}
		return rulesList;
	}
	
	// input map example: key="RulesPlayer2", value="Player2(?id,?name,?score,?ts):-$Player1(?id,?name,?ts)."
	// add rules from inputMap to datastore and memcache
	public void addRules(Map<String, String> newRules){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();

		for (Map.Entry<String, String> entry : newRules.entrySet()) {
			String rulesKey = entry.getKey();
			String rulesValue = entry.getValue();
			
			Entity ruleEntity = new Entity(rulesKey, KeyFactory.createKey("Rules",1));
			ruleEntity.setProperty("value", rulesValue);
			ds.put(ruleEntity);	
			
			IdentifiableValue oldRules = syncCache.getIdentifiable(rulesKey);
	        if (oldRules == null) {
		        // Key doesn't exist. We can safely put it in cache.
		        syncCache.put(rulesKey, rulesValue);
	        }       
		}			
	}

	// return the schema for kind and version
	public Schema getSchema(String inputKind, int inputVersion){
		Key schemaKey = KeyFactory.createKey(KeyFactory.createKey("Schema", inputKind), "Schema" + inputKind, inputVersion);
		
		Entity e = null;
		try {
			e = ds.get(schemaKey);
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (e != null){
			Schema schema = new Schema();
			schema.setAttributesString(e.getProperty("value").toString());
			schema.setKind(e.getProperty("kind").toString());
			schema.setVersion(Integer.parseInt(e.getProperty("version").toString()));
			schema.setTimestamp(Integer.parseInt(e.getProperty("ts").toString()));
			return schema;			
		}
		else return null;
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
	public Schema getLatestSchema(String kind){
		Schema latestSchema = new Schema();
		
		Query q = new Query ("Schema"+kind).setAncestor(KeyFactory.createKey("Schema", kind))
			.addSort("ts", SortDirection.DESCENDING);	
		List<Entity> schema = ds.prepare(q).asList(FetchOptions.Builder.withDefaults().limit(1));
		
		if (schema.isEmpty()) {
			return null;
		}
		else {
			latestSchema.setAttributesString(schema.get(0).getProperty("value").toString());
			latestSchema.setKind(schema.get(0).getProperty("kind").toString());
			latestSchema.setVersion(Integer.parseInt(schema.get(0).getProperty("version").toString()));
			latestSchema.setTimestamp(Integer.parseInt(schema.get(0).getProperty("ts").toString()));
			return latestSchema;
		}
	}
	
	// returns the entity with the highest timestamp for input kind and specific id
	public Entity getLatestEntity(String kind, int id){		
		Schema schema = null;
		int version = 0;
		String kindWithoutVersionNr = kind.replaceAll("\\d", "");
		if (!kindWithoutVersionNr.equals(kind)) {
			version = Integer.parseInt( kind.replaceAll("[^0-9]", ""));
		}
		if (version == 0) {
			schema = getLatestSchema(kindWithoutVersionNr);
			version = schema.getVersion();
		}
		
		Query kindQuery = new Query(kindWithoutVersionNr + Integer.toString(version))
				.setAncestor(KeyFactory.createKey(kindWithoutVersionNr, id))
				.addSort("ts", SortDirection.DESCENDING);
		List<Entity> latestEntity = ds.prepare(kindQuery).asList(FetchOptions.Builder.withDefaults().limit(1));
		
		if (latestEntity.isEmpty()) 
			return null;
		else 
			return latestEntity.get(0);
	}
	
	// returns the highest timestamp for a specific kind and id
	public int getLatestTimestamp(String kind, int id){		
		int ts = 0;
		Schema latestSchema = getLatestSchema(kind.replaceAll("\\d", ""));
		int version = latestSchema.getVersion();

		Query kindQuery = new Query(kind + Integer.toString(version))
				.setAncestor(KeyFactory.createKey(kind.replaceAll("\\d", ""), id))
				.addSort("ts", SortDirection.DESCENDING);
		List<Entity> latestEntity = ds.prepare(kindQuery).asList(FetchOptions.Builder.withDefaults().limit(1));
		
		if (latestEntity.isEmpty()) 
			return 0;
		else {
			ts = ((Long)latestEntity.get(0).getProperty("ts")).intValue();
			return ts;
		}
	}
	
	// write the datalogFact to datastore
	// input: "Player2(4,'Lisa',40)"
	// timestamp will be added automatically
	public void putToDatabase(String datalogFact){
		Entity entity = null;
		try {
			entity = new ParserForPut(new StringReader(datalogFact)).getEntity();
		} catch (InputMismatchException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (entity != null)
			ds.put(null, entity);

	}
	
	// put a schema entity to datastore
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
