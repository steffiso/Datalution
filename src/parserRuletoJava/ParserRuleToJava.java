/* Generated By:JavaCC: Do not edit this line. ParserRuleToJava.java */
package parserRuletoJava;
import java.util.ArrayList;
import datalog.Predicate;
import datalog.Rule;
import datalog.Condition;
import datalog.RuleBody;

public class ParserRuleToJava implements ParserRuleToJavaConstants {

  final public ArrayList < Rule > start() throws ParseException {
  ArrayList < Rule > querys = new ArrayList < Rule > ();
  RuleBody p = null;
  Predicate leftRelation = null;
  Rule q = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case not:
    case kindValue:
    case latest:
    case 18:
      leftRelation = getRelation();
      jj_consume_token(15);
      p = getRelationList();
      q = new Rule(leftRelation, p);
      querys.add(q);
      jj_consume_token(16);
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case not:
        case kindValue:
        case latest:
        case 18:
          ;
          break;
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        leftRelation = getRelation();
        jj_consume_token(15);
        p = getRelationList();
          q = new Rule(leftRelation, p);
          querys.add(q);
        jj_consume_token(16);
      }
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    jj_consume_token(0);
    {if (true) return querys;}
    throw new Error("Missing return statement in function");
  }

  final public ArrayList < Rule > parseHeadRules() throws ParseException {
  ArrayList < Rule > querys = new ArrayList < Rule > ();
  RuleBody p = null;
  Predicate leftRelation = null;
  Rule q = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case not:
    case kindValue:
    case latest:
    case 18:
      leftRelation = getRelation();
      jj_consume_token(15);
      p = getRelationList();
      leftRelation.setHead(true);
      q = new Rule(leftRelation, p);
      querys.add(q);
      jj_consume_token(16);
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case not:
        case kindValue:
        case latest:
        case 18:
          ;
          break;
        default:
          jj_la1[2] = jj_gen;
          break label_2;
        }
        leftRelation = getRelation();
        jj_consume_token(15);
        p = getRelationList();
          leftRelation.setHead(true);
          q = new Rule(leftRelation, p);
          querys.add(q);
        jj_consume_token(16);
      }
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    jj_consume_token(0);
    {if (true) return querys;}
    throw new Error("Missing return statement in function");
  }

  final public RuleBody getRelationList() throws ParseException {
  ArrayList < Predicate > values = new ArrayList < Predicate > ();
  ArrayList < Condition > conditions = new ArrayList < Condition > ();
  Predicate predicate = null;
  Condition condition = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case nullValue:
    case variable:
    case string:
    case number:
    case not:
    case kindValue:
    case latest:
    case 18:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case not:
      case kindValue:
      case latest:
      case 18:
        predicate = getRelation();
          values.add(predicate);
        break;
      case nullValue:
      case variable:
      case string:
      case number:
        condition = getCondition();
          conditions.add(condition);
        break;
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 17:
          ;
          break;
        default:
          jj_la1[5] = jj_gen;
          break label_3;
        }
        jj_consume_token(17);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case not:
        case kindValue:
        case latest:
        case 18:
          predicate = getRelation();
            values.add(predicate);
          break;
        case nullValue:
        case variable:
        case string:
        case number:
          condition = getCondition();
            conditions.add(condition);
          break;
        default:
          jj_la1[6] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      break;
    default:
      jj_la1[7] = jj_gen;
      ;
    }
    {if (true) return new RuleBody(values, conditions);}
    throw new Error("Missing return statement in function");
  }

  final public Predicate getRelation() throws ParseException {
  Token kind = null;
  Token schemaToken = null;
  Token latestToken = null;
  String value = null;
  ArrayList < String > scheme = null;
  Predicate predicate = null;
  boolean isNot = false;
  boolean isHead = false;
    scheme = new ArrayList < String > ();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 18:
      jj_consume_token(18);
    isHead = true;
      break;
    default:
      jj_la1[8] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case not:
      jj_consume_token(not);
    isNot = true;
      break;
    default:
      jj_la1[9] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case latest:
      latestToken = jj_consume_token(latest);
      break;
    default:
      jj_la1[10] = jj_gen;
      ;
    }
    kind = jj_consume_token(kindValue);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case number:
      schemaToken = jj_consume_token(number);
      break;
    default:
      jj_la1[11] = jj_gen;
      ;
    }
    jj_consume_token(19);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case nullValue:
    case variable:
    case string:
    case number:
      value = getValue();
      scheme.add(value);
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 17:
          ;
          break;
        default:
          jj_la1[12] = jj_gen;
          break label_4;
        }
        jj_consume_token(17);
        value = getValue();
          scheme.add(value);
      }
      break;
    default:
      jj_la1[13] = jj_gen;
      ;
    }
    jj_consume_token(20);
    predicate = new Predicate(kind.toString() + schemaToken.toString(), scheme.size(), scheme);
    if (isNot) predicate.setNot(true);
    if (latestToken != null) predicate.setLatest(true);
    predicate.setHead(isHead);
    {if (true) return predicate;}
    throw new Error("Missing return statement in function");
  }

  final public Condition getCondition() throws ParseException {
  String right = null;
  String left = null;
  Token operator = null;
    left = getValue();
    operator = jj_consume_token(operators);
    right = getValue();
    Condition con = new Condition(left, right, operator.toString());
    {if (true) return con;}
    throw new Error("Missing return statement in function");
  }

  final public String getValue() throws ParseException {
  Token valueOfToken = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case variable:
      valueOfToken = jj_consume_token(variable);
      break;
    case number:
      valueOfToken = jj_consume_token(number);
      break;
    case string:
      valueOfToken = jj_consume_token(string);
      break;
    case nullValue:
      valueOfToken = jj_consume_token(nullValue);
      break;
    default:
      jj_la1[14] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return valueOfToken.toString();}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public ParserRuleToJavaTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[15];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x45800,0x45800,0x45800,0x45800,0x45ae0,0x20000,0x45ae0,0x45ae0,0x40000,0x800,0x4000,0x200,0x20000,0x2e0,0x2e0,};
   }

  /** Constructor with InputStream. */
  public ParserRuleToJava(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ParserRuleToJava(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserRuleToJavaTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
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
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ParserRuleToJava(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserRuleToJavaTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ParserRuleToJava(ParserRuleToJavaTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserRuleToJavaTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
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
    boolean[] la1tokens = new boolean[21];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 15; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 21; i++) {
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
