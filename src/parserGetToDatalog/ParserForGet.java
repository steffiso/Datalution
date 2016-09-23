/* Generated By:JavaCC: Do not edit this line. ParserForGet.java */
package parserGetToDatalog;
import java.util.ArrayList;
import java.io.StringReader;

import datastore.DatalutionDatastoreService;
import datastore.Schema;
import datalog.Rule;
import parserRuletoJava.ParserRuleToJava;

import java.util.InputMismatchException;
import java.io.IOException;

import com.google.appengine.api.datastore.EntityNotFoundException;

public class ParserForGet implements ParserForGetConstants {
  private String kindStr;

  private String idStr;

  private int id;

  private String rulesStr;

  private static DatalutionDatastoreService dds;

  private ArrayList < Rule > rules = new ArrayList < Rule > ();

  private static Schema currentSchemaFrom = null;

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

  public String getRulesAsString()
  {
    return rulesStr;
  }

  public ArrayList < Rule > getRules()
  {
    return rules;
  }

  private static void getSchemaFromDB(String kind) throws InputMismatchException, IOException, EntityNotFoundException
  {
    currentSchemaFrom = getCurrentSchema(kind);
  }

  private static Schema getCurrentSchema(String kind) throws InputMismatchException, IOException, EntityNotFoundException
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

  final public void setAttributesOfGetCommand() throws ParseException, InputMismatchException, EntityNotFoundException, IOException, parserRuletoJava.ParseException {
    get(false);
    jj_consume_token(0);

  }

  @SuppressWarnings("unused")
final public ArrayList < Rule > getJavaRules(DatalutionDatastoreService dds) throws ParseException, InputMismatchException, IOException, parserRuletoJava.ParseException, EntityNotFoundException {
  ParserForGet.dds = dds;
    get(true);
    jj_consume_token(0);
    {if (true) return rules;}
    throw new Error("Missing return statement in function");
  }

  final public void get(Boolean check) throws ParseException, IOException, parserRuletoJava.ParseException, EntityNotFoundException {
  Token kindToken = null;
  Token idToken = null;
  Token propertyToken = null;
    jj_consume_token(get);
    kindToken = jj_consume_token(name);
    jj_consume_token(12);
    propertyToken = jj_consume_token(name);
    jj_consume_token(13);
    idToken = jj_consume_token(number);
    if (!propertyToken.toString().equals("id")) {if (true) throw new IOException("only id for get");}
    String kind = kindToken.toString();
    String idTemp = idToken.toString();
    id = Integer.parseInt(idToken.toString());
    kindStr = kind;
    idStr = idTemp;
    if (check)
    {
      getSchemaFromDB(kind);
      if (currentSchemaFrom == null)
      {
        {if (true) throw new InputMismatchException("no info for schema of " + kind + " found");}
      }
      ArrayList < String > schema = currentSchemaFrom.getAttributesAsList();
      int currentVersion = currentSchemaFrom.getVersion();
      String headRule = "get" + kind + currentVersion + "(" + schemaToString(schema) + ",?ts):-$" + kind + currentVersion + "(" + schemaToString(schema) + ",?ts),?id=" + id + ".\u005cn";
      rules.addAll((new ParserRuleToJava(new StringReader(headRule))).parseHeadRules());
    }
  }

  /** Generated Token Manager. */
  public ParserForGetTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  @SuppressWarnings("unused")
private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[0];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {};
   }

  /** Constructor with InputStream. */
  public ParserForGet(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ParserForGet(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserForGetTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
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
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ParserForGet(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserForGetTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ParserForGet(ParserForGetTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserForGetTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
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

  @SuppressWarnings("unused")
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
    boolean[] la1tokens = new boolean[14];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 0; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 14; i++) {
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
