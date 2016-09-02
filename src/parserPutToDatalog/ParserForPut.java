/* Generated By:JavaCC: Do not edit this line. ParserForPut.java */
package parserPutToDatalog;
import datastore.DatalutionDatastoreService;
import java.util.ArrayList;
import java.util.InputMismatchException;
import com.google.appengine.api.datastore.Entity;
import datastore.Schema;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

public class ParserForPut implements ParserForPutConstants {
  private static int schemaVersion = 0;

  private static ArrayList < String > attributes = null;

  private static int counter = 0;

  private static int length = 0;

  private static DatalutionDatastoreService db = new DatalutionDatastoreService();

  private static void getSchema(String kind, int version) throws InputMismatchException, EntityNotFoundException
  {
    Schema schema = null;
    if (version == 0)
    {
      schema = db.getLatestSchema(kind);
    }
    else schema = db.getSchema(kind, version);
    if (schema != null)
    {
      attributes = schema.getAttributesAsList();
      length = attributes.size();
      schemaVersion = schema.getVersion();
    }
    else
        throw new EntityNotFoundException(KeyFactory.createKey("Schema",kind));
  }

  final public Entity start() throws ParseException, InputMismatchException, EntityNotFoundException {
  Entity value = null;
  schemaVersion = 0;
  attributes = null;
  length = 0;
    value = getEntity();
    jj_consume_token(0);
    {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public Entity getEntity() throws ParseException, InputMismatchException, EntityNotFoundException {
  Token kindToken = null;
  Token schemaToken = null;
  Token idToken = null;
  Entity putEntity = null;
  boolean isManualPut = false;
  int id = 0;
  counter = 1;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case put:
      jj_consume_token(put);
    isManualPut = true;
      break;
    default:
      jj_la1[0] = jj_gen;
      ;
    }
    kindToken = jj_consume_token(kindValue);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case number:
      schemaToken = jj_consume_token(number);
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    jj_consume_token(16);
    idToken = jj_consume_token(number);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 17:
      jj_consume_token(17);
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    if (idToken != null)
    {
      id = Integer.parseInt(idToken.toString());
    }
    else if (idToken == null)
    {
      {if (true) throw new InputMismatchException("no id found");}
    }
    if (schemaToken != null && isManualPut == false)
    {
      //put from lazy migration
      schemaVersion = Integer.parseInt(schemaToken.toString());
      getSchema(kindToken.toString(), schemaVersion);
    }
    else if (schemaToken != null && isManualPut == true)
    {
      {if (true) throw new InputMismatchException("no numbers for value of kind allowed");}
    }
    else
    {
      // manual put
      getSchema(kindToken.toString(), 0);
    }
    if (attributes == null)
    {
      {if (true) throw new InputMismatchException("no info for schema of " + kindToken.toString() + " found");}
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case nullValue:
    case string:
    case number:
      putEntity = listOfValues(new Entity
            (
              kindToken.toString()+ schemaVersion
            ));
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    jj_consume_token(18);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 19:
      jj_consume_token(19);
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    if (putEntity == null) putEntity = new Entity(kindToken.toString() + schemaVersion);
    //throw new InputMismatchException("no attributes for " + kindToken.toString());
    putEntity.setProperty("id", id);
    {if (true) return putEntity;}
    throw new Error("Missing return statement in function");
  }

  final public Entity listOfValues(Entity putEntity) throws ParseException {
  Token valueOfToken = null;
  Entity valueOfOtherToken = null;
  String valueOne = "";
  String name = null;
  boolean nullvalue = false;
  int numbers = 0;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case string:
      valueOfToken = jj_consume_token(string);
      break;
    case number:
      valueOfToken = jj_consume_token(number);
      break;
    case nullValue:
      valueOfToken = jj_consume_token(nullValue);
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    if (counter < length)
    {
      if (valueOfToken.kind == string)
      {
        name = valueOfToken.toString();
        name = name.substring(1, name.length() - 1);
      }
      else if (valueOfToken.kind == nullValue)
      {
        nullvalue = true;
      }
      else numbers = Integer.parseInt(valueOfToken.toString());
      String attributename = attributes.get(counter);
      valueOne = attributename.substring(1, attributename.length());
      counter++;
    }
    else if (counter == length)
    {
      counter++;
    }
    else
    {
      counter++;
      {if (true) return null;}
    }
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 17:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_1;
      }
      jj_consume_token(17);
      valueOfOtherToken = listOfValues(putEntity);
    }
    if (valueOfOtherToken != null)
    {
      putEntity = valueOfOtherToken;
      if (name != null) putEntity.setProperty(valueOne, name);
      else if (nullvalue) putEntity.setProperty(valueOne, null);
      else if (valueOne == "")
      {}
      else putEntity.setProperty(valueOne, numbers);
      {if (true) return putEntity;}
    }
    else
    {
      if (name != null) putEntity.setProperty(valueOne, name);
      else if (nullvalue) putEntity.setProperty(valueOne, null);
      else if (valueOne == "")
      {}
      else putEntity.setProperty(valueOne, numbers);
      {if (true) return putEntity;}
    }
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public ParserForPutTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[7];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x40,0x1000,0x20000,0x1c00,0x80000,0x1c00,0x20000,};
   }

  /** Constructor with InputStream. */
  public ParserForPut(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ParserForPut(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserForPutTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
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
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ParserForPut(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserForPutTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ParserForPut(ParserForPutTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserForPutTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
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
    for (int i = 0; i < 7; i++) {
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
