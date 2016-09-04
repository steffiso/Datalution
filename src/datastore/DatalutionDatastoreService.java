package datastore;
/**
 * This class provides the methods
 * to interact with Google Datastore.
 * It can be used similar to the DatastoreService with
 * core methods put(Entity e) and get(String kind, int id)
 */
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
import parserGetToDatalog.ParserForGet;

import com.google.appengine.api.memcache.MemcacheServiceFactory;

import parserPutToDatalog.ParseException;
import parserPutToDatalog.ParserForPut;
import parserQueryToDatalogToJava.ParserQueryToDatalogToJava;
import parserRuletoJava.ParserRuleToJava;

public class DatalutionDatastoreService {

	DatastoreService ds;

	public DatalutionDatastoreService() {
		ds = DatastoreServiceFactory.getDatastoreService();
	}
	
	public DatalutionDatastoreService(DatastoreService ds) {
		super();
		this.ds = ds;
	}

	/**
	 * Manual put to Datastore (executed from UserServlet).
	 * (each manual put will be executed in an individual transaction)
	 * @param entity Entity object to put to Datastore
	 */
	public void put(Entity entity) {
		String kind = entity.getKind();
		int id = (int) entity.getProperty("id");

		int retries = 3;
		while (true) {
			Transaction txn = ds.beginTransaction();
			try {
				int newTS = getLatestTimestamp(txn, kind,
						id) + 1;
				Entity newEntity = new Entity(kind,
						id + Integer.toString(newTS),
						KeyFactory.createKey(
								kind.replaceAll("\\d", ""),
								id));
				newEntity.setPropertiesFrom(entity);
				newEntity.setProperty("ts", newTS);
				
				// put entity inside open/current transaction
				ds.put(txn, newEntity);
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

	/**
	 * Get an entity from Datastore with current kind and id
	 * (lazy migration with Datalog will be started if necessary)
	 * @param kind Kind of needed entity (e.g. Player) 
	 * @param id Id of needed entity (e.g. 1)
	 * @return latest entity (even after lazy migration)
	 */
	public Entity get(String kind, String id) throws InputMismatchException, 
		parserQueryToDatalogToJava.ParseException, IOException, 
		parserRuletoJava.ParseException, ParseException, URISyntaxException, 
		EntityNotFoundException, parserGetToDatalog.ParseException {
		
		Entity goalEntity = getLatestEntity(kind, Integer.parseInt(id));

		if (goalEntity == null) {
			// no entity with latest schemaversion found
			// prepare lazy migration
			ArrayList<Rule> datalogRules = prepareRulesForLazyMigration(kind, id);
			
			ArrayList<Map<String, String>> uniMap = prepareUniMapForLazyMigration(kind, id);			

			// get list of schema attributes
			Schema latestSchema  = getLatestSchema(kind);
			int schemaversion = latestSchema.getVersion();
			ArrayList<String> schemaAttributes = latestSchema.getAttributesAsList();
			schemaAttributes.add("?ts");
			
			Predicate goal = new Predicate("get" + kind + schemaversion,
					schemaAttributes.size(), schemaAttributes);
			ArrayList<Fact> facts = new ArrayList<Fact>();			

			// execute lazy migration
			LazyMigration migrate = new LazyMigration(facts, datalogRules, goal,
					uniMap);
			ArrayList<String> resultValues = migrate.executeLazyMigration();
			
			// in the next step the result entity will be taken from the answer string of lazy migration;
			// as an alternative approach another Datastore query can be performed:
			// goalEntity = getLatestEntity(kind,Integer.parseInt(id));			
			if (resultValues != null) {
				goalEntity = new Entity(kind + schemaversion, id + "0", KeyFactory.createKey(
					kind, id));
				for (int i = 0; i < schemaAttributes.size() - 1; i++) {
					goalEntity.setProperty(schemaAttributes.get(i).substring(1), resultValues.get(i));
				}	
				goalEntity.setProperty("ts", 0);
			}
			else 
				throw new EntityNotFoundException(KeyFactory.createKey(kind, id));
		}

		return goalEntity;
	}
	
	/**
	 * Prepare unification map for lazy migration
	 * @param kind Kind of needed entity (e.g. Player) 
	 * @param id Id of needed entity (e.g. 1)
	 * @return list of unification maps
	 */
	public ArrayList<Map<String, String>> prepareUniMapForLazyMigration(String kind, String id){
		Map<String, String> attributeMap = new TreeMap<String, String>();
		attributeMap.put("kind", kind);
		attributeMap.put("position", "0");
		attributeMap.put("value", id);
		ArrayList<Map<String, String>> uniMap = new ArrayList<Map<String, String>>();
		uniMap.add(attributeMap);
		
		return uniMap;
	}
	
	/**
	 * Prepare Datalog rules for lazy migration
	 * @param kind Kind of needed entity (e.g. Player) 
	 * @param id Id of needed entity (e.g. 1)
	 * @return list of Datalog rules
	 */
	public ArrayList<Rule> prepareRulesForLazyMigration(String kind, String id) throws parserRuletoJava.ParseException, 
		InputMismatchException, parserGetToDatalog.ParseException, IOException, EntityNotFoundException{
		
		ArrayList<Rule> datalogRules = new ArrayList<Rule>();
		
		// load saved rules from Datastore
		ArrayList<String> rulesFromDatastore = getAllRulesFromDatastore();
		for (String rule : rulesFromDatastore)
			datalogRules.addAll(new ParserRuleToJava(new StringReader(rule))
						.parseHeadRules());

		// generate additional rule for get command
		datalogRules.addAll(new ParserForGet(
					new StringReader("get " + kind + ".id=" + id)).getJavaRules(this));
		
		return datalogRules;
	}
	
	/**
	 * Generates rules and adds them to Datastore 
	 * if a schema change occurs (add, delete, copy, move)
	 * @param command Command which triggers a schema change
	 * @return generated Datalog rules saved in Datastore
	 */
	public String saveSchemaChange(String command) throws InputMismatchException,
			parserQueryToDatalogToJava.ParseException, IOException,
			parserRuletoJava.ParseException, EntityNotFoundException{
		String rulesStr = new ParserQueryToDatalogToJava(new StringReader(
				command)).getDatalogRules(this);

		if (!rulesStr.equals("")) {
			HashMap<String, String> rulesMap = new HashMap<String, String>();
			List<String> rulesSplit = new ArrayList<String>(
					Arrays.asList(rulesStr.split("\n")));
			for (String rule : rulesSplit) {
				String head = rule.substring(0, rule.indexOf("("));
				// generate a hash map with Key for Datastore rule entities
				// and the corresponding rules as String
				if (rulesMap.containsKey("Rules" + head)) {
					String oldValue = rulesMap.get("Rules" + head);
					rulesMap.put("Rules" + head, oldValue + "\n" + rule);
				} else {
					rulesMap.put("Rules" + head, rule);
				}
			}
			addRulesToDatastore(rulesMap);
		}
		return rulesStr;
	}
	
	/**
	 * @return List of datalog rules saved in Datastore
	 */
	public ArrayList<String> getAllRulesFromDatastore() {
		List<Entity> rulesEntity = null;
		Query q = new Query().setAncestor(KeyFactory.createKey("Rules", 1));
		rulesEntity = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());

		if (rulesEntity != null) {
			String rules = "";
			for (Entity e : rulesEntity)
				rules = rules + (String) e.getProperty("value");

			ArrayList<String> rulesList = new ArrayList<String>();

			if (!rules.equals("")) {
				rulesList = new ArrayList<String>(
						Arrays.asList(rules.split("\n")));
			}

			return rulesList;
		} else
			return null;
	}
	
	/**
	 * Returns all rules with input key from Memcache.
	 * If no matching rules exist in Memcache, load matching rules 
	 * from Datastore to Memcache
	 * @param key Kind with Schemaversion to load all rules starting with this key
	 * @return Specific rules as a List (e.g. all rules starting with "Player2")
	 */
	public ArrayList<String> getRulesFromMemcache(String key) {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		ArrayList<String> rulesList = new ArrayList<String>();
		String rules = "";
		rules = (String) syncCache.get("Rules" + key);
		
		if (!rules.equals("")) {
			// rule with given key exists in memcache
			rulesList = new ArrayList<String>(
					Arrays.asList(rules.split("\n")));
		} else {
			// copy rule from datastore to memcache
			Map<String, String> map = new HashMap<String, String>();
			String value = "";
			Query q = new Query("Rules" + key).setAncestor(KeyFactory
					.createKey("Rules", 1));
			List<Entity> rulesEntity = ds.prepare(q).asList(
					FetchOptions.Builder.withDefaults());
			for (Entity e : rulesEntity) {
				value = value + e.getProperty("value") + "\n";
			}
			map.put("Rules" + key, value);
			addRulesToDatastore(map);
		}
		return rulesList;
	}

	/**
	 * Add rules from inputMap to datastore and memcache
	 * @param newRules as a Map<String,String> (e.g. key="RulesPlayer2",
	 * value="Player2(?id,?name,?score,?ts):-$Player1(?id,?name,?ts)."
	 */	
	public void addRulesToDatastore(Map<String, String> newRules) {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();

		for (Map.Entry<String, String> entry : newRules.entrySet()) {
			String rulesKey = entry.getKey();
			String rulesValue = entry.getValue();

			Entity ruleEntity = new Entity(rulesKey, KeyFactory.createKey(
					"Rules", 1));
			ruleEntity.setProperty("value", rulesValue);
			ds.put(ruleEntity);

			IdentifiableValue oldRules = syncCache.getIdentifiable(rulesKey);
			if (oldRules == null) {
				// key doesn't exist yet => put it in cache
				syncCache.put(rulesKey, rulesValue);
			}
		}
	}

	/**
	 * Gets specific schema with current kind and version
	 * @param inputKind 
	 * @param inputVersion
	 * @return Schema from Datastore
	 */	
	public Schema getSchema(String kind, int version) throws EntityNotFoundException {
		Key schemaKey = KeyFactory.createKey(
				KeyFactory.createKey("Schema", kind),
				"Schema" + kind, version);

		Entity resultEntity = ds.get(schemaKey);
		
		if (resultEntity != null) {
			Schema schema = new Schema();
			schema.setAttributes(resultEntity.getProperty("value").toString());
			schema.setKind(resultEntity.getProperty("kind").toString());
			schema.setVersion(Integer.parseInt(resultEntity.getProperty("version")
					.toString()));
			schema.setTimestamp(Integer
					.parseInt(resultEntity.getProperty("ts").toString()));
			return schema;
		} else
			return null;
	}

	/**
	 * Returns the highest schema version for specific kind
	 * @param kind of entity (e.g. Player)
	 * @return latest schema version for specific kind
	 */	
	public int getLatestSchemaVersion(String kind) throws EntityNotFoundException {
		Schema latestSchema = getLatestSchema(kind);
		int latestSchemaVersion = 0;
		if (latestSchema != null) {
			latestSchemaVersion = latestSchema.getVersion();
		}

		return latestSchemaVersion;
	}

	/**
	 * Returns the latest schema of a specific kind
	 * @param kind of entity (e.g. Player)
	 * @return latest Schema object for specific kind
	 */	
	public Schema getLatestSchema(String kind) throws EntityNotFoundException {
		Schema latestSchema = new Schema();

		Query q = new Query("Schema" + kind).setAncestor(
				KeyFactory.createKey("Schema", kind)).addSort("ts",
				SortDirection.DESCENDING);
		List<Entity> schema = ds.prepare(q).asList(
				FetchOptions.Builder.withDefaults().limit(1));

		if (schema.isEmpty()) {
			throw new EntityNotFoundException(KeyFactory.createKey("Schema",kind));
		} else {
			latestSchema.setAttributes(schema.get(0).getProperty("value")
					.toString());
			latestSchema.setKind(schema.get(0).getProperty("kind").toString());
			latestSchema.setVersion(Integer.parseInt(schema.get(0)
					.getProperty("version").toString()));
			latestSchema.setTimestamp(Integer.parseInt(schema.get(0)
					.getProperty("ts").toString()));
			return latestSchema;
		}
	}

	/**
	 * Returns the latest entitiy saved in Datastore for specific kind and id
	 * @param kind of entity (e.g. Player)
	 * @param id 
	 * @return latest entity of specific kind with specific id
	 */	
	public Entity getLatestEntity(String kind, int id) throws EntityNotFoundException {
		Schema schema = null;
		int version = 0;
		String kindWithoutVersionNr = kind.replaceAll("\\d", "");
		
		if (!kindWithoutVersionNr.equals(kind)) {
			version = Integer.parseInt(kind.replaceAll("[^0-9]", ""));
		}
		if (version == 0) {
			schema = getLatestSchema(kindWithoutVersionNr);
			version = schema.getVersion();
		}		
		
		Query kindQuery = new Query(kindWithoutVersionNr
				+ Integer.toString(version)).setAncestor(
				KeyFactory.createKey(kindWithoutVersionNr, id)).addSort("ts",
				SortDirection.DESCENDING);
		List<Entity> latestEntity = ds.prepare(kindQuery).asList(
				FetchOptions.Builder.withDefaults().limit(1));
		
		if (latestEntity.isEmpty())
			return null;
		else
			return latestEntity.get(0);
	}

	/**
	 * Returns the highest timestamp for specific kind and id
	 * @param kind of entity (e.g. Player)
	 * @param id 
	 * @return highest timestamp for specific kind and id
	 * @throws EntityNotFoundException 
	 */	
	public int getLatestTimestamp(String kind, int id) throws EntityNotFoundException {
		int ts = 0;
		Schema latestSchema = getLatestSchema(kind.replaceAll("\\d", ""));
		int version = latestSchema.getVersion();

		Query kindQuery = new Query(kind + Integer.toString(version))
				.setAncestor(
						KeyFactory.createKey(kind.replaceAll("\\d", ""), id))
				.addSort("ts", SortDirection.DESCENDING);
		List<Entity> latestEntity = ds.prepare(kindQuery).asList(
				FetchOptions.Builder.withDefaults().limit(1));

		if (latestEntity.isEmpty())
			return 0;
		else {
			ts = ((Long) latestEntity.get(0).getProperty("ts")).intValue();
			return ts;
		}
	}

	/**
	 * Get highest timestamp inside a specific transaction
	 * @param kind of entity (e.g. Player)
	 * @param id 
	 * @return highest timestamp for specific kind and id
	 */	
	public int getLatestTimestamp(Transaction txn, String kind, int id) {
		int ts = 0;

		Query kindQuery = new Query(kind).setAncestor(
				KeyFactory.createKey(kind.replaceAll("\\d", ""), id)).addSort(
				"ts", SortDirection.DESCENDING);
		List<Entity> latestEntity = ds.prepare(txn, kindQuery).asList(
				FetchOptions.Builder.withDefaults().limit(1));

		if (latestEntity.isEmpty())
			return 0;
		else {
			ts = ((Long) latestEntity.get(0).getProperty("ts")).intValue();
			return ts;
		}
	}

	/**
	 * Write a datalog fact to Datastore; 
	 * timestamp will be added automatically
	 * @param datalog fact, e.g. "Player2(4,'Lisa',40)"
	 */	
	public void putToDatabase(String datalogFact) throws InputMismatchException, 
		ParseException, EntityNotFoundException {
		
		Entity entity = null;
		
		entity = new ParserForPut(new StringReader(datalogFact)).getEntity();
		
		if (entity != null) {
			Entity putEntity = new Entity(entity.getKind(), entity.getProperty("id")
					+ "0", KeyFactory.createKey(
					entity.getKind().replaceAll("\\d", ""),
					(int) entity.getProperty("id")));
			putEntity.setPropertiesFrom(entity);
			putEntity.setProperty("ts", (int) 0);
			ds.put(putEntity);
		}
	}

	/**
	 * Write a schema to Datastore; 
	 * @param kind of entity (e.g. Player)
	 * @param newAttributesList list of attribute names (with prefix "?")
	 * @throws EntityNotFoundException 
	 */	
	public void saveCurrentSchema(String kind, ArrayList<String> newAttributesList) {
		String newAttributes = "";
		
		for (String s : newAttributesList) {
			newAttributes = newAttributes + s + ",";
		}
		
		newAttributes = newAttributes.substring(0, newAttributes.length() - 1);
		long newVersion;
		Schema latestSchema;
		try {
			newVersion = getLatestSchemaVersion(kind) + 1;
			latestSchema = getLatestSchema(kind);
		} catch (EntityNotFoundException e) {
			// no schema version for this kind exists
			// => new entity type
			latestSchema = null;
			newVersion = 1;
		}
		
		long newTimestamp;
		if (latestSchema == null){
			newTimestamp = 1;
		}
		else
			newTimestamp = latestSchema.getTimestamp() + 1;
		Entity putSchema = new Entity("Schema" + kind,
				newVersion, KeyFactory.createKey("Schema",
						kind));
		putSchema.setProperty("kind", kind);
		putSchema.setProperty("version", newVersion);
		putSchema.setProperty("value", newAttributes);
		putSchema.setProperty("ts", newTimestamp);
		ds.put(putSchema);
	}
	
	/**
	 * This method adds a new entity kind to Datastore with default attribute "id"
	 * @param kind New entity type
	 */
	public void addNewEntity(String kind) throws EntityNotFoundException, InputMismatchException {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("?id");
		if (!kind.replaceAll("\\d", "").equals(kind)) {
			throw new InputMismatchException("Invalid kind name! (numbers are not allowed)");
		}
		else 				
			saveCurrentSchema(kind, attributes);
	}
	
	/**
	 * This method adds start/test entities to Datastore.
	 * Only for test cases - three Player and three Mission 
	 * entities will be added.
	 */
	public void addStartEntities() {
		Entity schemaPlayer1 = new Entity("SchemaPlayer", 1,
				KeyFactory.createKey("Schema", "Player"));
		schemaPlayer1.setProperty("value", "?id,?name,?points");
		schemaPlayer1.setProperty("kind", "Player");
		schemaPlayer1.setProperty("version", 1);
		schemaPlayer1.setProperty("ts", 1);

		Entity schemaMission1 = new Entity("SchemaMission", 1,
				KeyFactory.createKey("Schema", "Mission"));
		schemaMission1.setProperty("value", "?id,?title,?pid");
		schemaMission1.setProperty("kind", "Mission");
		schemaMission1.setProperty("version", 1);
		schemaMission1.setProperty("ts", 1);

		Entity player1 = new Entity("Player1", "11", KeyFactory.createKey(
				"Player", 1));
		player1.setProperty("id", 1);
		player1.setProperty("name", "Lisa");
		player1.setProperty("points", 150);
		player1.setProperty("ts", 1);

		Entity player2 = new Entity("Player1", "12", KeyFactory.createKey(
				"Player", 1));
		player2.setProperty("id", 1);
		player2.setProperty("name", "Lisa S.");
		player2.setProperty("points", 150);
		player2.setProperty("ts", 2);

		Entity player3 = new Entity("Player1", "21", KeyFactory.createKey(
				"Player", 2));
		player3.setProperty("id", 2);
		player3.setProperty("name", "Bart");
		player3.setProperty("points", 100);
		player3.setProperty("ts", 1);

		Entity player4 = new Entity("Player1", "31", KeyFactory.createKey(
				"Player", 3));
		player4.setProperty("id", 3);
		player4.setProperty("name", "Homer");
		player4.setProperty("points", 100);
		player4.setProperty("ts", 1);

		Entity mission1 = new Entity("Mission1", "11", KeyFactory.createKey(
				"Mission", 1));
		mission1.setProperty("id", 1);
		mission1.setProperty("title", "go to library");
		mission1.setProperty("pid", 1);
		mission1.setProperty("ts", 1);

		Entity mission2 = new Entity("Mission1", "21", KeyFactory.createKey(
				"Mission", 2));
		mission2.setProperty("id", 2);
		mission2.setProperty("title", "go to work");
		mission2.setProperty("pid", 20);
		mission2.setProperty("ts", 1);

		Entity mission3 = new Entity("Mission1", "31", KeyFactory.createKey(
				"Mission", 3));
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

}
