/* Generated By:JavaCC: Do not edit this line. ParserQueryToDatalogToJava.java */
package parserQueryToDatalogToJava;
import java.util.ArrayList;
import java.io.StringReader;
import datastore.DatalutionDatastoreService;
import datastore.Schema;
import datalog.Rule;
import parserRuletoJava.ParserRuleToJava;
import java.util.InputMismatchException;
import java.io.IOException;
import com.google.appengine.api.datastore.EntityNotFoundException;

public class ParserQueryToDatalogToJava implements ParserQueryToDatalogToJavaConstants {
  private String kindStr;

  private String idStr;

  private int id;

  private String rulesStr;  private static DatalutionDatastoreService dds;

  private static ArrayList < Rule > rules = new ArrayList < Rule > ();

  private static Schema currentSchemaFrom = null;

  private static Schema currentSchemaTo = null;

  public String getKind()
  {
    return kindStr;
  }

  public String getIdStr()
  {
    return idStr;
  }

  public int getId()
  {
    return id;
  }

  public String getRules()
  {
    return rulesStr;
  }

  private static void getSchemaFromDB(String kindFrom, String kindTo) throws InputMismatchException, IOException
  {
    if (!kindFrom.equals("")) currentSchemaFrom = getCurrentSchema(kindFrom);
    if (!kindTo.equals("")) currentSchemaTo = getCurrentSchema(kindTo);
  }

  private static Schema getCurrentSchema(String kind) throws InputMismatchException, IOException
  {
    Schema currentSchema = dds.getLatestSchema(kind);
    if (currentSchema != null) return currentSchema;
    else return null;
  }

  private static String schemaToString(ArrayList < String > schema)
  {
    String schemaStr = "";
    for (String s : schema)
    {
      schemaStr = schemaStr + s + ",";
    }
    schemaStr = schemaStr.substring(0, schemaStr.length() - 1);
    return schemaStr;
  }

  private static ArrayList < String > addAttributeNr(ArrayList < String > schema, int nr, String copiedAttr)
  {
    ArrayList < String > changedSchema = new ArrayList < String > ();
    for (String s : schema)
    {
      if (!s.equals("null"))
      {
        if (s.equals(copiedAttr)) changedSchema.add(s + "2");
        else changedSchema.add(s + Integer.toString(nr));
      }
      else changedSchema.add(s);
    }
    return changedSchema;
  }

  private static ArrayList < String > getNewSchemaDelete(String kind, String value) throws InputMismatchException, IOException
  {
    ArrayList < String > currentSchemaAttributes = null;
    if (currentSchemaFrom.getKind().equals(kind))
        currentSchemaAttributes = currentSchemaFrom.getAttributesAsList();
    else if (currentSchemaTo.getKind().equals(kind))
        currentSchemaAttributes = currentSchemaTo.getAttributesAsList();

    if (currentSchemaAttributes == null)
    {
      throw new InputMismatchException("no info for schema of " + kind + " found");
    }
    else
    {
      ArrayList < String > tempNewSchema = new ArrayList < String > ();
      for (String attribute : currentSchemaAttributes)
      {
        if (!attribute.equals("?" + value)) tempNewSchema.add(attribute);
      }
      return tempNewSchema;
    }
  }

  private static ArrayList < String > getNewSchemaAdd(String kind, String value) throws InputMismatchException, IOException
  {
    ArrayList < String > currentSchemaAttributes = null;
    if (currentSchemaFrom.getKind().equals(kind)) currentSchemaAttributes = currentSchemaFrom.getAttributesAsList();
    else if (currentSchemaTo.getKind().equals(kind)) currentSchemaAttributes = currentSchemaTo.getAttributesAsList();
    if (currentSchemaAttributes == null)
    {
      throw new InputMismatchException("no info for schema of " + kind + " found");
    }
    else
    {
      currentSchemaAttributes.add(value);
      return currentSchemaAttributes;
    }
  }

  private static void saveCurrentSchema(String kind, ArrayList < String > newSchema) throws InputMismatchException, IOException
  {
    ArrayList < String > currentSchemaAttributes = null;
    if (currentSchemaFrom.getKind().equals(kind)) currentSchemaAttributes = currentSchemaFrom.getAttributesAsList();
    else if (currentSchemaTo.getKind().equals(kind)) currentSchemaAttributes = currentSchemaTo.getAttributesAsList();
    if (currentSchemaAttributes == null)
    {
      throw new InputMismatchException("no info for schema of " + kind + " found");
    }
    else
    {
      dds.saveCurrentSchema(kind, newSchema);
    }
  }

  public String getAttributeName(String kind, int schemaNumber, int pos) throws InputMismatchException, IOException, EntityNotFoundException
  {
    Schema currentSchema = dds.getSchema(kind, schemaNumber);
    if (currentSchema != null)
    {
      ArrayList < String > attributes = currentSchema.getAttributesAsList();
      String value = attributes.get(pos);
      return value;
    }
    else
    {
      throw new InputMismatchException("no info for schema of " + kind + " found");
    }
  }

  private static boolean propertyExists(Schema schema, String value) throws InputMismatchException, IOException
  {
    return schema.getAttributesAsList().contains("?" + value);
  }

  final public String getDatalogRules(DatalutionDatastoreService dds) throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  String value = null;
  this.dds = dds;
    value = start();
    jj_consume_token(0);
    {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public ArrayList < Rule > getJavaRules(DatalutionDatastoreService dds) throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  String value = null;
  this.dds = dds;
    value = start();
    jj_consume_token(0);
    rulesStr = value;
    {if (true) return rules;}
    throw new Error("Missing return statement in function");
  }

  final public String start() throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  String value = null;
  rules = new ArrayList < Rule > ();
  currentSchemaFrom = null;
  currentSchemaTo = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case get:
      value = get();
      break;
    case add:
      value = add();
      break;
    case delete:
      value = delete();
      break;
    case copy:
      value = copy();
      break;
    case move:
      value = move();
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public String get() throws ParseException, IOException, parserRuletoJava.ParseException {
  Token kindToken = null;
  Token idToken = null;
  Token propertyToken = null;
    jj_consume_token(get);
    kindToken = jj_consume_token(name);
    jj_consume_token(16);
    propertyToken = jj_consume_token(name);
    jj_consume_token(17);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case string:
      idToken = jj_consume_token(string);
      break;
    case number:
      idToken = jj_consume_token(number);
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    if (!propertyToken.toString().equals("id")) {if (true) throw new IOException("only id for get");}
    String kind = kindToken.toString();
    String idTemp;
    if (idToken.kind == string)
    {
      idTemp = idToken.toString();
      idTemp = idTemp.substring(1, idTemp.length() - 1);
      idTemp = "'" + idTemp + "'";
    }
    else
    {
      idTemp = idToken.toString();
      id = Integer.parseInt(idToken.toString());
    }
    kindStr = kind;
    idStr = idTemp;
    getSchemaFromDB(kind, "");
    if (currentSchemaFrom == null)
    {
      {if (true) throw new IOException("no info for schema of " + kind + " found");}
    }
    ArrayList < String > schema = currentSchemaFrom.getAttributesAsList();
    int currentVersion = currentSchemaFrom.getVersion();
    String headRule = "get" + kind + currentVersion + "(" + schemaToString(schema) + ",?ts):-$"
        + kind + currentVersion + "(" + schemaToString(schema) + ",?ts),?id=" + id + ".\u005cn";
    rules.addAll((new ParserRuleToJava(new StringReader(headRule))).parseHeadRules());
    {if (true) return headRule;}
    throw new Error("Missing return statement in function");
  }

  final public String add() throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  Token kindToken = null;
  Token propertyToken = null;
  Token valueToken = null;
    jj_consume_token(add);
    kindToken = jj_consume_token(name);
    jj_consume_token(16);
    propertyToken = jj_consume_token(name);
    jj_consume_token(17);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case string:
      valueToken = jj_consume_token(string);
      break;
    case number:
      valueToken = jj_consume_token(number);
      break;
    case nullValue:
      valueToken = jj_consume_token(nullValue);
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    String propertyName = propertyToken.toString();
    String propertyValue;
    if (valueToken.kind == string)
    {
      propertyValue = valueToken.toString();
      propertyValue = propertyValue.substring(1, propertyValue.length() - 1);
      propertyValue = "'" + propertyValue + "'";
    }
    else propertyValue = valueToken.toString();
    String kind = kindToken.toString();
    getSchemaFromDB(kind, "");
    if (currentSchemaFrom == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kind + " found");}
    }
    if (propertyExists(currentSchemaFrom, propertyName))
    {
      {if (true) throw new InputMismatchException("attribute for " + kind + " already exists");}
    }
    ArrayList < String > currentSchema = currentSchemaFrom.getAttributesAsList();
    ArrayList < String > newSchema = getNewSchemaAdd(kind, "?" + propertyName);
    int currentSchemaVersion = currentSchemaFrom.getVersion();
    int newSchemaVersion = currentSchemaVersion + 1;
    String headRules = kind + newSchemaVersion + "(" + schemaToString(getNewSchemaAdd(kind, propertyValue)) + ",?ts):-$"
        + kind + currentSchemaVersion + "(" + schemaToString(currentSchema) + ",?ts).\u005cn";
    saveCurrentSchema(kind, newSchema);
    rules.addAll((new ParserRuleToJava(new StringReader(headRules))).parseHeadRules());
    {if (true) return headRules;}
    throw new Error("Missing return statement in function");
  }

  final public String delete() throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  Token kindToken = null;
  Token propertyToken = null;
    jj_consume_token(delete);
    kindToken = jj_consume_token(name);
    jj_consume_token(16);
    propertyToken = jj_consume_token(name);
    String propertyName = propertyToken.toString();
    String kind = kindToken.toString();
    getSchemaFromDB(kind, "");
    if (currentSchemaFrom == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kind + " found");}
    }
    if (!propertyExists(currentSchemaFrom, propertyName))
    {
      {if (true) throw new InputMismatchException("attribute for " + kind + " does not exist");}
    }
    ArrayList < String > newSchema = getNewSchemaDelete(kind, propertyName);
    int currentVersion = currentSchemaFrom.getVersion();
    int newVersion = currentVersion + 1;
    String headRules = kind + newVersion + "(" + schemaToString(newSchema) + ",?ts):-$"
        + kind + currentVersion + "(" + schemaToString(currentSchemaFrom.getAttributesAsList()) + ",?ts).\u005cn";
    saveCurrentSchema(kind, newSchema);
    rules.addAll((new ParserRuleToJava(new StringReader(headRules))).parseHeadRules());
    {if (true) return headRules;}
    throw new Error("Missing return statement in function");
  }

  final public String copy() throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  Token kindFromToken = null;
  Token kindToToken = null;
  Token propertyToken = null;
  Token condKind1Token = null;
  Token condKind2Token = null;
  Token condProp1Token = null;
  Token condProp2Token = null;
    jj_consume_token(copy);
    kindFromToken = jj_consume_token(name);
    jj_consume_token(16);
    propertyToken = jj_consume_token(name);
    jj_consume_token(18);
    kindToToken = jj_consume_token(name);
    jj_consume_token(19);
    condKind1Token = jj_consume_token(name);
    jj_consume_token(16);
    condProp1Token = jj_consume_token(name);
    jj_consume_token(17);
    condKind2Token = jj_consume_token(name);
    jj_consume_token(16);
    condProp2Token = jj_consume_token(name);
    String kindFrom = kindFromToken.toString();
    String kindTo = kindToToken.toString();
    String attribute = propertyToken.toString();
    getSchemaFromDB(kindFrom, kindTo);
    if (currentSchemaFrom == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kindFrom + " found");}
    }
    if (currentSchemaTo == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kindTo + " found");}
    }
    if (!propertyExists(currentSchemaFrom, attribute))
    {
      {if (true) throw new InputMismatchException("attribute: " + attribute + " for " + kindFrom + " does not exist");}
    }
    String condKind1 = condKind1Token.toString();
    String condKind2 = condKind2Token.toString();
    String condProp1 = condProp1Token.toString();
    String condProp2 = condProp2Token.toString();
    String conditionFrom = "";
    String conditionTo = "";
    if (condKind1.equals(kindTo)) conditionTo = condProp1;
    else if (condKind1.equals(kindFrom)) conditionFrom = condProp1;
    else
    {
      {if (true) throw new InputMismatchException("No matching source for condition " + condKind1 + "." + condProp1);}
    }
    if (condKind2.equals(kindFrom)) conditionFrom = condProp2;
    else if (condKind2.equals(kindTo)) conditionTo = condProp2;
    else
    {
      {if (true) throw new InputMismatchException("No matching source for condition " + condKind2 + "." + condProp2);}
    }
    if (!propertyExists(currentSchemaFrom, conditionFrom))
    {
      {if (true) throw new InputMismatchException("attribute: " + conditionFrom + " for " + kindFrom + " does not exist");}
    }
    if (!propertyExists(currentSchemaTo, conditionTo))
    {
      {if (true) throw new InputMismatchException("attribute: " + conditionTo + " for " + kindTo + " does not exist");}
    }
    if (propertyExists(currentSchemaTo, attribute))
    {
      {if (true) throw new InputMismatchException("attribute: " + attribute + " for " + kindTo + " already exists");}
    }
    ArrayList < String > schemaToNew = getNewSchemaAdd(kindTo, "?" + attribute);
    ArrayList < String > schemaToNew2 = getNewSchemaAdd(kindTo, "null");
    saveCurrentSchema(kindTo, schemaToNew);
    saveCurrentSchema(kindFrom, currentSchemaFrom.getAttributesAsList());
    int currentSchemaVersionTo = currentSchemaTo.getVersion();
    int currentSchemaVersionFrom = currentSchemaFrom.getVersion();
    int newSchemaVersionTo = currentSchemaVersionTo + 1;
    int newSchemaVersionFrom = currentSchemaVersionFrom + 1;
    schemaToNew = addAttributeNr(schemaToNew, 1, "?" + attribute);
    schemaToNew2 = addAttributeNr(schemaToNew2, 1, "");
    ArrayList<String > schemaTo = addAttributeNr(currentSchemaTo.getAttributesAsList(), 1, "");
   ArrayList<String > schemaFrom = addAttributeNr(currentSchemaFrom.getAttributesAsList(), 2, "");
    String condition = "?" + conditionFrom + "2 = " + "?" + conditionTo + "1";
    String headRules = kindTo + newSchemaVersionTo + "(" + schemaToString(schemaToNew) + ",?ts1):-$"
        + kindTo + currentSchemaVersionTo + "(" + schemaToString(schemaTo) + ",?ts1),$"
                + kindFrom + currentSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2)," + condition + ".\u005cn";
    headRules = headRules + kindTo + newSchemaVersionTo + "(" + schemaToString(schemaToNew2) + ",?ts1):-$"
        + kindTo + currentSchemaVersionTo + "(" + schemaToString(schemaTo) + ",?ts1)," +
        " not $" + kindFrom + currentSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2)," + condition + ".\u005cn";
    headRules = headRules + kindFrom + newSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2):-$"
        + kindFrom + currentSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2).\u005cn";
    rules.addAll((new ParserRuleToJava(new StringReader(headRules))).parseHeadRules());
    {if (true) return headRules;}
    throw new Error("Missing return statement in function");
  }

  final public String move() throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException {
  Token kindFromToken = null;
  Token kindToToken = null;
  Token propertyToken = null;
  Token condKind1Token = null;
  Token condKind2Token = null;
  Token condProp1Token = null;
  Token condProp2Token = null;
    jj_consume_token(move);
    kindFromToken = jj_consume_token(name);
    jj_consume_token(16);
    propertyToken = jj_consume_token(name);
    jj_consume_token(18);
    kindToToken = jj_consume_token(name);
    jj_consume_token(19);
    condKind1Token = jj_consume_token(name);
    jj_consume_token(16);
    condProp1Token = jj_consume_token(name);
    jj_consume_token(17);
    condKind2Token = jj_consume_token(name);
    jj_consume_token(16);
    condProp2Token = jj_consume_token(name);
    String kindFrom = kindFromToken.toString();
    String kindTo = kindToToken.toString();
    String attribute = propertyToken.toString();
    getSchemaFromDB(kindFrom, kindTo);

    if (currentSchemaFrom == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kindFrom + " found");}
    }
    if (currentSchemaTo == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kindTo + " found");}
    }
    if (!propertyExists(currentSchemaFrom, attribute))
    {
      {if (true) throw new InputMismatchException("attribute: " + attribute + " for " + kindFrom + " does not exist");}
    }
    String condKind1 = condKind1Token.toString();
    String condKind2 = condKind2Token.toString();
    String condProp1 = condProp1Token.toString();
    String condProp2 = condProp2Token.toString();
    String conditionFrom = "";
    String conditionTo = "";
    if (condKind1.equals(kindTo)) conditionTo = condProp1;
    else if (condKind1.equals(kindFrom)) conditionFrom = condProp1;
    else
    {
      {if (true) throw new InputMismatchException("No matching source for condition " + condKind1 + "." + condProp1);}
    }
    if (condKind2.equals(kindFrom))
        conditionFrom = condProp2;
    else if (condKind2.equals(kindTo))
        conditionTo = condProp2;
    else
    {
      {if (true) throw new InputMismatchException("No matching source for condition " + condKind2 + "." + condProp2);}
    }

    if (!propertyExists(currentSchemaFrom, conditionFrom))
    {
      {if (true) throw new InputMismatchException("attribute: " + conditionFrom + " for " + kindFrom + " does not exist");}
    }
    if (!propertyExists(currentSchemaTo, conditionTo))
    {
      {if (true) throw new InputMismatchException("attribute: " + conditionTo + " for " + kindTo + " does not exist");}
    }
    if (propertyExists(currentSchemaTo, attribute))
    {
      {if (true) throw new InputMismatchException("attribute: " + attribute + " for " + kindTo + " already exists");}
    }
    ArrayList < String > schemaFromNew = getNewSchemaDelete(kindFrom, attribute);
    ArrayList < String > schemaToNew = getNewSchemaAdd(kindTo, "?" + attribute);
    ArrayList < String > schemaToNew2 = getNewSchemaAdd(kindTo, "null");

    saveCurrentSchema(kindFrom, schemaFromNew);
    saveCurrentSchema(kindTo, schemaToNew);
    int currentSchemaVersionFrom = currentSchemaFrom.getVersion();
    int currentSchemaVersionTo =currentSchemaTo.getVersion();
    int newSchemaVersionFrom = currentSchemaVersionFrom + 1;
    int newSchemaVersionTo = currentSchemaVersionTo + 1;
    schemaToNew = addAttributeNr(schemaToNew, 1, "?" + attribute);
    schemaToNew2 = addAttributeNr(schemaToNew2, 1, "");
    ArrayList<String > schemaTo = addAttributeNr(currentSchemaTo.getAttributesAsList(), 1, "");
    ArrayList<String > schemaFrom = addAttributeNr(currentSchemaFrom.getAttributesAsList(), 2, "");
    schemaFromNew = addAttributeNr(schemaFromNew, 2, "");
    String condition = "?" + conditionFrom + "2 = " + "?" + conditionTo + "1";
    String headRules = kindTo + newSchemaVersionTo + "(" + schemaToString(schemaToNew) + ",?ts1):-$"
        + kindTo + currentSchemaVersionTo + "(" + schemaToString(schemaTo) + ",?ts1),$"
        + kindFrom + currentSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2)," + condition + ".\u005cn";
    headRules = headRules + kindTo + newSchemaVersionTo + "(" + schemaToString(schemaToNew2) + ",?ts1):-$"
        + kindTo + currentSchemaVersionTo + "(" + schemaToString(schemaTo) + ",?ts1)," +
        " not $" + kindFrom + currentSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2)," + condition + ".\u005cn";
    headRules = headRules + kindFrom + newSchemaVersionFrom + "(" + schemaToString(schemaFromNew) + ",?ts2):-$"
        + kindFrom + currentSchemaVersionFrom + "(" + schemaToString(schemaFrom) + ",?ts2).\u005cn";
    rules.addAll((new ParserRuleToJava(new StringReader(headRules))).parseHeadRules());
    {if (true) return headRules;}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public ParserQueryToDatalogToJavaTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[3];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x3e0,0x1400,0x1c00,};
   }

  /** Constructor with InputStream. */
  public ParserQueryToDatalogToJava(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ParserQueryToDatalogToJava(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserQueryToDatalogToJavaTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ParserQueryToDatalogToJava(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserQueryToDatalogToJavaTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ParserQueryToDatalogToJava(ParserQueryToDatalogToJavaTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserQueryToDatalogToJavaTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[20];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 3; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 20; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
