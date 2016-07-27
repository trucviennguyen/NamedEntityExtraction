package edu.cmu.minorthird.text.mixup;

import edu.cmu.minorthird.text.*;
import edu.cmu.minorthird.util.ProgressCounter;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A simple pattern-matching and information extraction language.

<pre>
EXAMPLE:
	... in('begin') @number? [ any{2,5} in('end') ] ... && [!in('begin')*] && [!in('end')*]

BNF:
  simplePrim -> [!] simplePrim1
	simplePrim1 -> id | a(DICT) | ai(DICT) | eq(CONST) | eqi(CONST) | re(REGEX) 
	              | any | ... | PROPERTY:VALUE  | PROPERTY:a(foo)  )
	prim -> < simplePrim [,simplePrim]* > | simplePrim
	repeatedPrim -> [L] prim [R] repeat | @type | @type?
	repeat -> {int,int} | {,int} | {int,} | {int} | ? | * | +
	pattern -> | repeatedPrim pattern
	basicExpr -> pattern [ pattern ] pattern 
	basicExpr -> (expr)
	expr -> basicExpr "||" expr 
	expr -> basicExpr "&&" expr

SEMANTICS:
	basicExpr is pattern match - like a regex, but returns all matches, not just the longest one
	token-level tests:
	  eq('foo') check token is exactly foo 
		'foo' is short for eq('foo')
		re('regex') checks if token matches the regex
	  eqi('foo') check lowercase version of token is foo
	  'foo' or eq('foo') checks a token is equal to 'foo'
    a(bar) checks a token is in dictionary 'bar'
		ai(bar) checks that the token is in dictionary 'bar', ignoring case
	  color:red checks that the token has property 'color' set to 'red'
	  color:a(primaryColor) checks that the token's  property 'color' is in the dictionary 'primaryColor'
	  !test is negation of test
	  <test1, test2, ... test3> conjoins token-level tests
	  any is true for any token
  token-sequences:
	  test? is 0 or 1 tokens matching test
	  test+ is 1+ tokens matching test
    test* is 0+ tokens matching test
    test{3,7} is between 3 and 7 tokens matching test		
   	... is equal to any*
		<code>@foo</code> matches a span of type foo
		<code>@foo?</code> matches a span of type foo or the empty sequence
		L means sequence can't be extended to left and still match
		R means sequence can't be extended to right and still match
	expr || expr is union
	expr && expr is piping: generate with expr1, filter with expr2
</pre>

The name's an acronym for My Information eXtraction and Understanding Package.

 *
 * @author William Cohen
*/

public class Mixup 
{
	/** Without constraints, the maximum number of times a mixup
	 * expression can extract something from a document of length N is
	 * O(N*N).  The maxNumberOfMatches... variables below constrain
	 * this behavior, for efficiency.  The variable below is a threshold
	 * after which these constraints kick in.
	 */
	public static int minMatchesToApplyConstraints = 5000;

	/** Without constraints, the maximum number of times a mixup
	 * expression can extract something from a document of length N is
	 * O(N*N), since any token can be the begin or end of an extracted
	 * span.  The maxNumberOfMatchesPerToken value limits this to
	 * maxNumberOfMatchesPerToken*N.
	 */
	public static int maxNumberOfMatchesPerToken = 5;
	/** Without constrains, the maximum number of times a mixup
	 * expression can extract something from a document of length N is
	 * O(N*N), since any token can be the begin or end of an extracted
	 * span.  This limits the number of matches to a fixed number.
	 */
	public static int maxNumberOfMatches = 0;

	private static final boolean DEBUG = false;

	// tokenize: words, single-quoted strings, "&&", "||", "..." or single non-word chars
	static public final Pattern tokenizerPattern = 
	//Pattern.compile("\\s*(\\w+|'([^']|\\\\')*'|\\&\\&|\\|\\||\\.\\.\\.|\\W)\\s*");
	Pattern.compile("\\s*((\\w+)|('(\\\\'|[^\\'])*')|\\&\\&|\\|\\||\\.\\.\\.|\\W)\\s*");

	// legal functions
	private static Set legalFunctions; 
	static { 
		legalFunctions = new HashSet();
		String[] tmp = new String[] { "re", "eq", "eqi", "a", "ai", "any", "prop", "propDict" };
		for (int i=0; i<tmp.length; i++) legalFunctions.add(tmp[i]);
	}

	private Expr expr;

	/** Create a new mixup query. */
	public Mixup(String pattern) throws ParseException {
		expr = new MixupParser(pattern).parseExpr();
	}
	/** Extract subspans from each generated span using the mixup expression.
	 */
	public Span.Looper extract(TextLabels labels, Span.Looper spanLooper) {
		return expr.match(labels, spanLooper);
	}

	public String toString() { return expr.toString(); }

	//
	// recursive descent parser for the BNF above
	//
	private static class MixupParser 
	{
		private String input;
		private Matcher matcher;
		private String token;
		private int cursor;
		public MixupParser(String input) { 
			this.input = input;
			this.matcher = tokenizerPattern.matcher(input); 
			cursor = 0;
			advance(); 
		}
		public Expr parse() throws ParseException {
			return parseExpr();
		}
		private Expr parseExpr() throws ParseException {
			Expr expr1 = null; 
			Expr expr2 = null;
			String op = null; 
			BasicExpr basic = parseBasicExpr();
			if ("&&".equals(token) || "||".equals(token)) {
				op = token;
				advance(); 
				expr2 = parseExpr();
			}
			return new Expr(basic,expr2,op);
		}
		private BasicExpr parseBasicExpr() throws ParseException {
			List list = new ArrayList();
			int left = -1, right = -1;
			if ("(".equals(token)) {
				advance();
				Expr expr = parseExpr();
				if (!")".equals(token)) parseError("expected close paren");
				advance(); // past ')'
				return new BasicExpr(expr);
			} else {
				while (token!=null && !"||".equals(token) && !"&&".equals(token) && !")".equals(token)) {
					if ("[".equals(token)) {
						left = list.size(); advance(); 
					} else if ("]".equals(token)) {
						right = list.size(); advance();
					} else {
						list.add(parseRepeatedPrim());
					} 
				}
				if (left<0) parseError("no left bracket");
				if (right<0) parseError("no right bracket");
				return new BasicExpr( (RepeatedPrim[])list.toArray(new RepeatedPrim[list.size()]), left, right );
			}
		}
		private RepeatedPrim parseRepeatedPrim() throws ParseException {
			RepeatedPrim buf = new RepeatedPrim();
			if ("@".equals(token)) {
				advance();
				buf.type = token;
				advance();
				buf.maxCount = 1;
				if ("?".equals(token)) {
					buf.minCount = 0;
					advance();
				} else {
					buf.minCount = 1;
				}
				return buf;
			} else {
				if ("L".equals(token)) {
					buf.leftMost = true;
					advance();
				}
				parsePrim(buf);
				parseRepeat(buf);
				if ("R".equals(token)) {
					buf.rightMost = true;
					advance();
				}					
				buf.expandShortcuts();
				if (!buf.checkFunction()) parseError("syntax error");
				return buf;
			}
		}
		private void parsePrim(RepeatedPrim buf) throws ParseException {
			if ("<".equals(token)) {
				advance();
				parseSimplePrim(buf);
				while (",".equals(token)) {
					advance();
					parseSimplePrim(buf);
				}
				if (">".equals(token)) advance();
				else parseError("expected '>'");
			} else {
				parseSimplePrim(buf);
			}
		}
		private void parseSimplePrim(RepeatedPrim buf) throws ParseException {
			Prim prim = new Prim();
			if ("!".equals(token)) {
				prim.negated = true;	advance();
			}
			prim.function = token;
			advance();
			if ("(".equals(token)) {
				advance(); // to argument
				prim.argument = token;
				advance(); // to ')' 
				if (!")".equals(token)) parseError("expected close paren");
				advance(); // past prim
			} else if (":".equals(token)) {
				prim.property = prim.function;
				prim.function = "prop";
				advance(); // to property value
				if ("a".equals(token)) {
					advance(); // to '('
					if (!"(".equals(token)) {
						prim.value = "a";
						advance(); // past value
					} else {
						advance(); // to dictionary name
						prim.function = "propDict";
						prim.value = token;
						advance();
						if (!")".equals(token)) parseError("expected close paren");
						advance(); // past close paren
					}					
				} else {
					prim.value = token;
					advance(); // past value
				}
			} 
			prim.expandShortcuts();
			buf.primList.add(prim);
		}
		private void parseRepeat(RepeatedPrim buf) throws ParseException {
			String min = null,max = null;
			if ("{".equals(token)) {
				advance();
				if (!",".equals(token)) {
					min = token;
					advance(); // to "," 
				} else {
					min = "0";
				}
				if ("}".equals(token)) {
					max = min;
					advance();
				} else {
					if (!",".equals(token)) parseError("expected \",\"");
					advance(); 
					if (!"}".equals(token)) { 
						max = token;
						advance(); // to "}"
					} else {
						max = "-1";
					}
					if (!"}".equals(token)) parseError("expected \"}\"");
					advance();
				}
			} else if ("+".equals(token)) {
				min = "1";	max = "-1";	advance();
			} else if ("*".equals(token)) {
				min = "0";	max = "-1";	advance();
			} else if ("?".equals(token)) {
				min = "0";	max = "1"; advance();
			} else {
				min = max = "1";
			}
			try {
				buf.minCount = Integer.parseInt(min);
				buf.maxCount = Integer.parseInt(max);
			} catch (NumberFormatException e) {
				parseError("expected an integer: min = '"+min+"' max='"+max+"'");
			}
		}
		private void parseError(String msg) throws ParseException {
			throw new 
				ParseException(msg+": "
									 +input.substring(0,cursor)+"^^^"+input.substring(cursor,input.length()));
		}
		private boolean advance() { 
			if (matcher.find()) {
				cursor = matcher.start(1);
				token = matcher.group(1);
				return true;
			} else {
				token = null;
				return false;
			}
		}
	}
	
	/** Signals an error in parsing a mixup document. */
	public static class ParseException extends Exception 
	{
		public ParseException(String s) { super(s); }
	}

	//
	// encodes a pattern that matches a single TextToken
	//
	private static class Prim {
		public boolean negated = false;
		public String function = null;
		public String argument = "";
		public String property = "", value="";
		private Pattern pattern = null;
		public Prim() { ; }
		/** See if the predicate for this pattern succeeds for this TextToken.  */
		public boolean matchesPrim(TextLabels labels,Token token) {
			boolean status = matchesUnnegatedPrim(labels,token);
			return negated==!status; 
		}
		private boolean matchesUnnegatedPrim(TextLabels labels,Token token) {
			if ("any".equals(function)) return true;
			else if ("eq".equals(function)) return token.getValue().equals(argument);
			else if ("eqi".equals(function)) return token.getValue().equalsIgnoreCase(argument);
			else if ("a".equals(function)) return labels.inDict(token,argument);
			else if ("ai".equals(function)) {
				final String lc = token.getValue().toLowerCase();
				Token lcToken = new Token() {
						public String toString() { return "[lcToken "+lc+"]"; }
						public String getValue() { return lc; } 
						public int getIndex() { return 0; }};
				return labels.inDict(lcToken,argument);
			}
			else if ("re".equals(function)) {
				return pattern.matcher(token.getValue()).find();
			} else if ("prop".equals(function)) {
				return value.equals( labels.getProperty(token,property) );
			} else if ("propDict".equals(function)) {
				final String propVal = labels.getProperty(token,property);
				if (propVal==null) return false;
				Token propValToken = new Token() {
						public String toString() { return "[token:"+propVal+"]"; }
						public String getValue() { return propVal; } 
						public int getIndex() { return 0; }};
				//System.out.println("testing "+propValToken+" for membership in dict "+value);
				return labels.inDict( propValToken, value );
			} else {
				throw new IllegalStateException("illegal function '"+function+"'");
			}
		}
		/** Expand some syntactic sugar-like abbreviations. */
		public void expandShortcuts() {
			// expand the 'const' abbreviation to eq('const')
			if (function.startsWith("'") && function.endsWith("'")) {
				argument = function;
				function = "eq";
			}
			// unquote a quoted argument
			if (argument.startsWith("'") && argument.endsWith("'")) {
				argument = argument.substring(1,argument.length()-1);
				argument = argument.replaceAll("\\\\'","'");
			}
			// precompile a regex
			if ("re".equals(function)) pattern = Pattern.compile(argument);
			// check for correctness
		}
		/** is this a legal function? */
		public boolean checkFunction()
		{
			return legalFunctions.contains(function);
		}
		public String toString() {
			StringBuffer buf = new StringBuffer("");
			if (negated) buf.append("!");
			if (!"prop".equals(function)) {
				buf.append(function);
				if (argument!=null) buf.append("(" + argument + ")");
			} else {
				buf.append(property+":"+value);
			}
			return buf.toString();
		}
	}

	// encodes a pattern matching a series of Token's
	private static class RepeatedPrim {
		public boolean leftMost=false;
		public boolean rightMost=false;
		public List primList = new ArrayList();
		public int minCount;
		public int maxCount; // -1 indicates infinity
		String type = null;  // non-null for @type and @type?
		/** Expand some syntactic sugar-like abbreviations. */
		public void expandShortcuts() {
			// expand the 'const' abbreviation to eq('const')
			if (primList.size()==1) {
				Prim prim = (Prim)primList.get(0);
				if ("...".equals(prim.function)) {
					prim.function = "any";
					minCount = 0;
					maxCount = -1;
					return;
				}
			}
		}
		public boolean checkFunction() {
			for (Iterator i=primList.iterator(); i.hasNext(); ) {
				Prim prim = (Prim)i.next();
				if ("...".equals(prim.function) && primList.size()!=1) return false;
				if (!prim.checkFunction()) return false;
			}
			return true;
		}
		public String toString() {
			if (type!=null) {
				if (minCount==0) return "@"+type+"?";
				else return "@"+type;
			}
			else {
				StringBuffer buf = new StringBuffer("");
				if (leftMost) buf.append("L ");
				if (primList.size()==1) buf.append((Prim)primList.get(0));
				else if (primList.size()==0) throw new IllegalStateException("empty prim list");
				else {
					buf.append("<" + primList.get(0).toString());
					for (int i=1; i<primList.size(); i++) {
						buf.append(", "+primList.get(i).toString());
					}
					buf.append(">");
				}
				buf.append("{"+minCount+","+maxCount+"}");
				if (rightMost) buf.append("R");
				return buf.toString();
			}
		}
		/** See if this pattern matches span.subSpan(lo,len). */
		public boolean matchesSubspan(TextLabels labels,Span span, int lo, int len) {
			if (type!=null) {
				if (minCount==1) {
					return labels.hasType(span.subSpan(lo,len), type);
				} else {
					return len==0 || labels.hasType(span.subSpan(lo,len), type);
				}
			} else {
				if (len>maxCount && maxCount>=0) return false;
				if (len<minCount) return false;
				for (int i=lo; i<lo+len; i++) {
					if (i>=span.size()) return false;
					if (!matchesPrimList(labels,span.getToken(i))) return false;
				}
				if (leftMost && (len<maxCount || maxCount<0)) {
					if (lo>0 && matchesPrimList(labels,span.getToken(lo-1)))
						return false;
				}
				if (rightMost && (len<maxCount || maxCount<0)) {
					if (lo+len<span.size() && matchesPrimList(labels,span.getToken(lo+len)))
						return false;
				}
				return true;
			}
		}
		private boolean matchesPrimList(TextLabels labels, Token token) {
			for (Iterator i=primList.iterator(); i.hasNext(); ) {
				Prim prim = (Prim)i.next();
				if (!prim.matchesPrim(labels,token)) return false;
			}
			return true;
		}
	}
	
	//
	// encodes a basicExpr in the BNF above
	//
	private static class BasicExpr 
	{
		public final Expr expr;
		public final RepeatedPrim[] repPrim;
		public final int leftBracket, rightBracket;
    //private Logger log = Logger.getLogger(this.getClass());

    public BasicExpr(Expr expr) {
			this.expr = expr;
			this.repPrim = null; this.leftBracket = this.rightBracket = -1;
		}
		public BasicExpr(RepeatedPrim[] repPrim, int leftBracket, int rightBracket) {
			this.expr = null;
			this.repPrim = repPrim; this.leftBracket = leftBracket; this.rightBracket = rightBracket;
		}
		public String toString() {
			if (expr!=null) {
				return "(" + expr.toString() + ")";
			}  else {
				StringBuffer buf = new StringBuffer();
				for (int i=0; i<repPrim.length; i++) {
					if (i==leftBracket) buf.append("[");
					buf.append(" "+repPrim[i].toString() );
					if (i+1==rightBracket) buf.append("]");
				}
				return buf.toString();
			}
		}
		public Span.Looper match(TextLabels labels,Span.Looper spanLooper)
		{
			if (expr!=null) {
				return expr.match(labels,spanLooper);
			} else {
				ProgressCounter pc = new ProgressCounter("mixup","span",spanLooper.estimatedSize());
				Set accum = new TreeSet();
				while (spanLooper.hasNext()) {
					pc.progress();
					Span span = spanLooper.nextSpan();
					// match(labels,accum,span,new int[repPrim.length],new int[repPrim.length],1,0,0);
					fastMatch(labels,span,accum);
				}
				pc.finished();
				return new BasicSpanLooper(accum.iterator());
			}
		}
		private void fastMatch(TextLabels labels,Span span,Set accum)
		{
//      log.debug("span size: " + span.size() + " - " + span.asString());
			// there are at most span.length^2 matches of every repeated primitive
			//log.debug("matching span id/size="+span.getDocumentId()+"/"+span.size());
			//log.debug("before alloc: max/free="+Runtime.getRuntime().maxMemory()+"/"+Runtime.getRuntime().freeMemory());
			int maxRepeatedPrimMatches = span.size() * (span.size()+1);
			if (maxRepeatedPrimMatches>minMatchesToApplyConstraints) {
				// apply constraints
				if (maxNumberOfMatchesPerToken>0) {
					maxRepeatedPrimMatches = Math.min(maxNumberOfMatchesPerToken*span.size(), maxRepeatedPrimMatches);
				}
				if (maxNumberOfMatches>0) {
					maxRepeatedPrimMatches = maxNumberOfMatches;
				}
			}
			int[] loIndexBuffer = new int[ maxRepeatedPrimMatches ];
			int[] lengthBuffer = new int[ maxRepeatedPrimMatches ];
			//log.debug("alloc hi-lo: max/free="+Runtime.getRuntime().maxMemory()+"/"+Runtime.getRuntime().freeMemory()); 
			// store possible places that repPrim[i] can match
			int[][] possibleLos = new int[repPrim.length][];
			int[][] possibleLens = new int[repPrim.length][];
			// also record min/max length 
			int[] minLen = new int[repPrim.length];
			int[] maxLen = new int[repPrim.length];
			boolean[] isAny = new boolean[repPrim.length];
			//log.debug("after alloc: max/free="+Runtime.getRuntime().maxMemory()+"/"+Runtime.getRuntime().freeMemory());
			for (int i=0; i<repPrim.length; i++) {
				// work out possible lengths for repPrim[i]
				RepeatedPrim rp = repPrim[i];
				minLen[i] = rp.minCount;
				maxLen[i] = span.size();
				if (rp.maxCount>=0 && rp.maxCount<maxLen[i]) maxLen[i] = rp.maxCount;
				// see if repPrim[i] is "any"
				if (rp.primList.size()==1) {
					Prim prim = (Prim)rp.primList.get(0);
					isAny[i] = ("any".equals(prim.function) && !prim.negated);
				}
				if (!isAny[i]) {
					// find all places this matches
					int numMatches = 0;
					if (rp.type!=null) {
						// look up matches from the labels
						for (Span.Looper el=labels.instanceIterator(rp.type, span.getDocumentId()); el.hasNext(); ) {
							if (numMatches>=maxRepeatedPrimMatches) {
								overflowWarning(numMatches,maxRepeatedPrimMatches,span,i);
								return;
							}
							Span s = el.nextSpan();
							if (span.contains(s)) {
								if (numMatches>=maxRepeatedPrimMatches) {
									overflowWarning(numMatches,maxRepeatedPrimMatches,span,i);									
									return;
								}
								loIndexBuffer[numMatches] = s.documentSpanStartIndex()-span.documentSpanStartIndex();
								lengthBuffer[numMatches] = s.size();
								numMatches++;
							}
						}
					}
					if (rp.type==null || (rp.type!=null && rp.minCount==0)) {
						// check all possible subspans
						for (int j=0; j<=span.size(); j++) {
							int topLen = Math.min(maxLen[i], span.size()-j);
							for (int k=minLen[i]; k<=topLen; k++) {
								if (numMatches>=maxRepeatedPrimMatches) {
									overflowWarning(numMatches,maxRepeatedPrimMatches,span,i);
									return;
								}
								if (rp.matchesSubspan(labels,span,j,k)) {
									loIndexBuffer[numMatches] = j;
									lengthBuffer[numMatches] = k;
									numMatches++;
								}
							}
						}
					}
					// save matches from buffer into loIndices, lengths
					possibleLos[i] = new int[numMatches];
					possibleLens[i] = new int[numMatches];
					for (int m=0; m<numMatches; m++) {
						possibleLos[i][m] = loIndexBuffer[m];
						possibleLens[i][m] = lengthBuffer[m];
					}
				}
			}
			//
			// now find a good series of loIndex/length pairs
			//
			int[] lows = new int[repPrim.length];
			int[] highs = new int[repPrim.length];
			fastMatch(labels,accum,span,lows,highs,1,0,0,possibleLos,possibleLens,isAny,minLen,maxLen);
		}
		
		private void overflowWarning(int numMatches,int maxRepeatedPrimMatches,Span span,int i)
		{
			/*log.warn("mixup warning at pattern #"+(i+1)+" "+repPrim[i]+") on "+span);
			log.warn("not enough room to store all matches: adjust Mixup.maxNumberOfMatches(PerToken)");
			log.warn("size="+span.size()+" numMatches="+numMatches+" max="+maxRepeatedPrimMatches
							 +" minConstraint="+minMatchesToApplyConstraints);*/
		}

		private void fastMatch(
			TextLabels labels,    // passed along to subroutines
			Set accum,            // accumulate matches
			Span span,            // span being matched
			int[] lows,           // lows[i] is lo index of match to repPrim[i] 
			int[] highs,          // highs[i] is high index of match to repPrim[i] 
			int tab,              // for debugging
			int spanCursor,       // index into the span being matched
			int patternCursor,    // index into the repPrim's being matched
			int[][]possibleLos,   // loIndices[i] is all places repPrim[i] might match
			int[][]possibleLens,  // lengths[i] is parallel-to-loIndices array of lengths 
			boolean[] isAny,      // true if repPrim[i] is "any"
			int[] minLen,         // min lengths of subseq matching an isAny==true repPrim[i]
			int[] maxLen)         // max lengths of subseq matching an isAny==true repPrim[i]
		{
			if (patternCursor==repPrim.length) {
				if (spanCursor==span.size()) {
					// a complete, successful match
					if (DEBUG) showMatch(tab,"complete",span,lows,highs,patternCursor);
					int lo = lows[leftBracket];
					int hi = highs[rightBracket-1];
					accum.add( span.subSpan(lo, hi-lo) );
				} else {
					// a deadend
					if (DEBUG) showMatch(tab,"failed",span,lows,highs,patternCursor);
				}
			} else {
				// continue a partial match
				if (isAny[patternCursor]) {
					if (patternCursor+1<repPrim.length && !isAny[patternCursor+1]) {
						// trick to handle something like '...' followed by a specific pattern 
						for (int i=0; i<possibleLos[patternCursor+1].length; i++) {						
							int nextSpanCursor = possibleLos[patternCursor+1][i];
							int len = nextSpanCursor-spanCursor;
							if (len>=minLen[patternCursor] && len<=maxLen[patternCursor]) {
								lows[patternCursor] = spanCursor;
								highs[patternCursor] = spanCursor+len;
								if (DEBUG) showMatch(tab,"partial",span,lows,highs,patternCursor+1);
								fastMatch(labels,accum,span,lows,highs,tab+1,spanCursor+len,patternCursor+1,
													possibleLos,possibleLens,isAny,minLen,maxLen);
							}
						}
					} else {
						int topLen = Math.min( maxLen[patternCursor], span.size()-spanCursor );
						for (int len=minLen[patternCursor]; len<=topLen; len++) {
								lows[patternCursor] = spanCursor;
								highs[patternCursor] = spanCursor+len;
								if (DEBUG) showMatch(tab,"partial",span,lows,highs,patternCursor+1);
								fastMatch(labels,accum,span,lows,highs,tab+1,spanCursor+len,patternCursor+1,
													possibleLos,possibleLens,isAny,minLen,maxLen);
						}
					}
				} else {
					int topLen = span.size() - spanCursor;
					for (int i=0; i<possibleLos[patternCursor].length; i++) {
						if (possibleLos[patternCursor][i]==spanCursor && possibleLens[patternCursor][i]<=topLen) {
							int len = possibleLens[patternCursor][i];
							lows[patternCursor] = spanCursor;
							highs[patternCursor] = spanCursor+len;
							if (DEBUG) showMatch(tab,"partial",span,lows,highs,patternCursor+1);
							fastMatch(labels,accum,span,lows,highs,tab+1,spanCursor+len,patternCursor+1,
												possibleLos,possibleLens,isAny,minLen,maxLen);
						}
					}
				}
			}
		}

		// 
		// obsolete slower match routine, kept around as a reference implementation for debugging
		// 
		private void 
		match(TextLabels env,Set accum,Span span,int[] lows,int[] highs,int tab,int spanCursor,int patternCursor)
		{
			if (patternCursor==repPrim.length) {
				if (spanCursor==span.size()) {
					// a complete, successful match
					if (DEBUG) showMatch(tab,"complete",span,lows,highs,patternCursor);
					int lo = lows[leftBracket];
					int hi = highs[rightBracket-1];
					accum.add( span.subSpan(lo, hi-lo) );
				} else {
					// a deadend
					if (DEBUG) showMatch(tab,"failed",span,lows,highs,patternCursor);
				}
			} else {
				// continue a partial match
				RepeatedPrim nextPattern = repPrim[patternCursor];
				int maxLen = span.size() - spanCursor; 
				if (nextPattern.maxCount>=0 && nextPattern.maxCount<maxLen) maxLen = nextPattern.maxCount;
				for (int len=nextPattern.minCount; len<=maxLen; len++) {
					boolean lenOk = nextPattern.matchesSubspan(env,span,spanCursor,len);
					if (lenOk) {
						lows[patternCursor] = spanCursor;
						highs[patternCursor] = spanCursor+len;
						if (DEBUG) showMatch(tab,"partial",span,lows,highs,patternCursor+1);
						match(env,accum,span,lows,highs,tab+1,spanCursor+len,patternCursor+1);
					}
				}
			}
		}
		// for debugging
		private void showMatch(int tab,String msg,Span span,int[] lows,int[] highs,int patternCursor) {
			for (int i=0; i<tab; i++) { System.out.print("| "); }
			System.out.print(msg+":");
			for (int i=0; i<patternCursor; i++) {
				System.out.print(" "+repPrim[i].toString()+"["+lows[i]+":"+highs[i]+"]<");
				for (int j=lows[i]; j<highs[i]; j++) {
					if (j>lows[i]) System.out.print(" ");
					System.out.print(span.getToken(j).getValue());
				}
				System.out.print(">");
			}
			System.out.println();
		}
	}

	//
	// encodes an expression in the BNF above
	//
	private static class Expr {
		private BasicExpr expr1;
		private Expr expr2;
		private String op;
		public Expr(BasicExpr expr1,Expr expr2,String op) {
			this.expr1 = expr1; this.expr2 = expr2; this.op = op;
		}
		public Span.Looper match(TextLabels labels,Span.Looper spanLooper) {
			if (expr2==null) {
				return expr1.match(labels,spanLooper);
			} else if ("&&".equals(op)) {
				return expr2.match(labels,expr1.match(labels,spanLooper));
			} else {
				if (!"||".equals(op)) throw new IllegalStateException("illegal operator '"+op+"'");
				// copy the input looper
				Set save = new TreeSet();
				while (spanLooper.hasNext()) save.add( spanLooper.next() );
				// union the outputs of expr1 and expr2
				Span.Looper a = expr1.match(labels,new BasicSpanLooper(save.iterator()));
				Span.Looper b = expr2.match(labels,new BasicSpanLooper(save.iterator()));
				Set union = new TreeSet();
				while (a.hasNext()) union.add( a.next() );
				while (b.hasNext()) union.add( b.next() );
				return new BasicSpanLooper( union.iterator() );
			}
		}
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(expr1.toString());
			if (expr2!=null) buf.append(" "+op+" "+expr2.toString());
			return buf.toString();
		}
	}

	//
	// interactive test routine
	//
	public static void main(String[] args) 
	{
		try {
			Mixup mixup = new Mixup(args[0]);
			System.out.println("normalized expression = "+mixup);
			TextBase b = new BasicTextBase();
			MonotonicTextLabels labels = new BasicTextLabels(b);
			for (int i=1; i<args.length; i++) {
				b.loadDocument("arg_"+i, args[i]);
			}
			new BoneheadStemmer().stem(b,labels);
			//System.out.println("labels="+labels);
			//labels.addWord("the", "det");
			//labels.addWord("thi", "det");
			for (Span.Looper i=mixup.extract(labels, b.documentSpanIterator()); i.hasNext(); ) {
				System.out.println(i.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
