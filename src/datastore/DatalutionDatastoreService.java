package datastore;
/**
 * This class provides the methods
 * to interact with Google App Engine Datastore.
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

import com.google.api.server.spi.response.BadRequestException;
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

	public DatalutionDatastoreService() {
		ds = DatastoreServiceFactory.getDatastoreService();
	}
	
	public DatalutionDatastoreService(DatastoreService ds) {
		super();
		this.ds = ds;
	}

	/**
	 * Manual put to Datastore (executed from UserServlet).
	 * (each manual put will be executed in an extra transaction)
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
	public Entity get(String kind, String id) {
		Entity goalEntity = getLatestEntity(kind, Integer.parseInt(id));

		if (goalEntity == null) {
			// no entity with latest schemaversion found
			// => execute Lazy Migration
			ArrayList<Rule> rulesTemp = new ArrayList<Rule>();
			ArrayList<String> rules = getAllRulesFromDatastore();
			for (String rule : rules)
				try {
					rulesTemp.addAll(new ParserRuleToJava(new StringReader(rule))
							.parseHeadRules());
				} catch (parserRuletoJava.ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			
			try {
				// generate rules for get command
				rulesTemp.addAll(new ParserQueryToDatalogToJava(
						new StringReader("get " + kind + ".id=" + id))
						.getJavaRules(this));
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
			} catch (BadRequestException e1) {
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
			schema = getLatestSchema(kind).getAttributesAsList();

			schema.add("?ts");

			goal = new Predicate("get" + kind + getLatestSchemaVersion(kind),
					schema.size(), schema);

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
	
	/**
	 * Generates rules and adds them to Datastore 
	 * if a schema change occurs (add, delete, copy, move)
	 * @param command Command which triggers a schema change
	 * @return generated Datalog rules saved in Datastore
	 * @throws BadRequestException 
	 */
	public String saveSchemaChange(String command) throws InputMismatchException,
			parserQueryToDatalogToJava.ParseException, IOException,
			parserRuletoJava.ParseException, BadRequestException {
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
	 * @return List of datalog rules 
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
	 * from Datastore in Memcache
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
	 * add rules from inputMap to datastore and memcache
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
	 * @param inputKind 
	 * @param inputVersion
	 * @return Schema from Datastore
	 */	
	public Schema getSchema(String kind, int version) {
		Key schemaKey = KeyFactory.createKey(
				KeyFactory.createKey("Schema", kind),
				"Schema" + kind, version);

		Entity resultEntity = null;
		try {
			resultEntity = ds.get(schemaKey);
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
	 * @param kind of entity (e.g. Player)
	 * @return latest schema version for specific kind
	 */	
	public int getLatestSchemaVersion(String kind) {
		Schema latestSchema = getLatestSchema(kind);
		int latestSchemaVersion = 0;
		if (latestSchema != null) {
			latestSchemaVersion = latestSchema.getVersion();
		}

		return latestSchemaVersion;
	}

	/**
	 * @param kind of entity (e.g. Player)
	 * @return latest Schema object for specific kind
	 */	
	public Schema getLatestSchema(String kind) {
		Schema latestSchema = new Schema();

		Query q = new Query("Schema" + kind).setAncestor(
				KeyFactory.createKey("Schema", kind)).addSort("ts",
				SortDirection.DESCENDING);
		List<Entity> schema = ds.prepare(q).asList(
				FetchOptions.Builder.withDefaults().limit(1));

		if (schema.isEmpty()) {
			return null;
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
	 * @param kind of entity (e.g. Player)
	 * @param id 
	 * @return latest entity of specific kind with specific id
	 */	
	public Entity getLatestEntity(String kind, int id) {
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
	 * @param kind of entity (e.g. Player)
	 * @param id 
	 * @return highest timestamp for specific kind and id
	 */	
	public int getLatestTimestamp(String kind, int id) {
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
	public void putToDatabase(String datalogFact) {
		Entity entity = null;
		
		try {
			entity = new ParserForPut(new StringReader(datalogFact))
					.getEntity();
		} catch (InputMismatchException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	 */	
	public void saveCurrentSchema(String kind, ArrayList<String> newAttributesList) {
		String newAttributes = "";
		
		for (String s : newAttributesList) {
			newAttributes = newAttributes + s + ",";
		}
		
		newAttributes = newAttributes.substring(0, newAttributes.length() - 1);
		long newVersion = getLatestSchemaVersion(kind) + 1;
		Schema latestSchema = getLatestSchema(kind);
		long newTimestamp;
		if (latestSchema == null){
			newTimestamp = 1;
		}
		else
			newTimestamp = latestSchema.getTimestamp() + 1;
		Entity putSchema = new Entity("Schema" + kind,
//				latestSchema.getVersion() + 1, KeyFactory.createKey("Schema",
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
	public void addNewEntity(String kind) {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("?id");
		saveCurrentSchema(kind, attributes);
	}
	
	/**
	 * This method adds start/test entities to Datastore.
	 * Only for test cases - three Player and three Mission 
	 * entities will be added
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
