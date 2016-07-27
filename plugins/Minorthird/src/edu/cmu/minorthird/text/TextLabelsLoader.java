package edu.cmu.minorthird.text;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Loads and saves the contents of a TextLabels into a file.
 *
 * Labels can be loaded from operations (see importOps) or from a serialized TextLabels object.
 * Labels can be serialized or types can be saved as operations, xml, or plain lists.
 *
 * @author William Cohen
 */

public class TextLabelsLoader
{
	private static Logger log = Logger.getLogger(TextLabelsLoader.class);

	/** Spans in labels are a complete list of all spans. */
	static final public int CLOSE_ALL_TYPES=1;

	/** If a document has been labeled for a type, assume all spans of that type are there. */
	static final public int CLOSE_TYPES_IN_LABELED_DOCS=2;

	/** Make no assumptions about closure. */
	static final public int DONT_CLOSE_TYPES=3;

  static final public int CLOSE_BY_OPERATION = 4;

  public static final String[] CLOSURE_NAMES = 
	{"CLOSE_ALL_TYPES", "CLOSE_TYPES_IN_LABELED_DOCS", "DONT_CLOSE_TYPES", "CLOSE_BY_OPERATION"};

	private int closurePolicy = CLOSE_BY_OPERATION;

	private int warnings=0;
	static private final int MAX_WARNINGS=10;

	/** Set the closure policy.
	 * @param policy one of CLOSE_ALL_TYPES, CLOSE_TYPES_IN_LABELED_DOCS, DONT_CLOSE_TYPES
	 */
	public void setClosurePolicy(int policy) { this.closurePolicy = policy; }

	/** Create a new labeling by importing from a file with importOps.
	 */
	public MutableTextLabels loadOps(TextBase base,File file) throws IOException,FileNotFoundException
	{
		MutableTextLabels labels = new BasicTextLabels(base);
		importOps(labels,base,file);
		return labels;
	}

	/**
   * Load lines modifying a TextLabels from a file.
   * There are four allowed operations: addToType, closeType, closeAllTypes, setClosure
   *
   * For addToType:
   *   The lines must be of the form: <code>addToType ID LOW LENGTH TYPE</code> where ID is a
	 * documentID in the given TextBase, LOW is a character
	 * index into that document, and LENGTH is the length in
	 * characters of the span that will be created as given type TYPE.
	 * If LENGTH==-1, then the created span will go to the end of the
	 * document.
   *
   * For closeType:
   *   Lines must be <code>closeType ID TYPE</code> where ID is a documentID in the given TextBase
   * and TYPE is the label type to close over that document.
   *
   * For closeAllTypes:
   *   Lines must be <code>closeAllType ID</code> where ID is a documentID in the given TextBase.
   * The document will be closed for all types present in the TextLabels <em>after all operations</em> are performed.
	 *
   * For setClosure:
   *  Lines must be <code>setClosure POLICY</code> where POLICY is one of the policy types defined in this class.
   * It will immediately change the closure policy for the loader.  This is best used at the beginning of
   * the file to indicate one of the generic policies or the CLOSE_BY_OPERATION (default) policy.
   */
	public void importOps(MutableTextLabels labels,TextBase base,File file) throws IOException,FileNotFoundException
	{
    base = labels.getTextBase();
    if (base == null)
      throw new IllegalStateException("TextBase attached to labels must not be null");

		LineNumberReader in = new LineNumberReader(new FileReader(file));
		String line = null;
    List docList = new ArrayList();
    try
    {
      while ((line = in.readLine())!=null)
      {
        if (line.trim().length() == 0)
          continue;
				if (line.startsWith("#"))
					continue;
        log.debug("read line #" + in.getLineNumber() + ": " + line);
        StringTokenizer tok = new StringTokenizer(line);
        String op;
        try { 
					op = advance(tok, in, file); 
				} catch (IllegalArgumentException e) { 
					throw getNewException(e, ", failed to find operation."); 
				}
        if ("addToType".equals(op)) { 
					addToType(tok, in, file, base, labels); 
				} else if ("setSpanProperty".equals(op)) {
					setSpanProp(tok, in, file, base, labels); 					
				} else if ("closeType".equals(op)) {
					String docId = advance(tok, in, file);
					String type = advance(tok, in, file);
					Span span = base.documentSpan(docId);
					labels.closeTypeInside(type, span);
					log.debug("closed " + type + " on " + docId);
        } else if ("closeAllTypes".equalsIgnoreCase(op)) {
          String docId = advance(tok, in, file);
          docList.add(docId);
        } else {
          throw new IllegalArgumentException("error on line "+in.getLineNumber()+" of "+file.getName());
        }
      }
      //close over the doc list for all types seen
      for (int i = 0; i < docList.size(); i++)
      {
        String docId = (String)docList.get(i);
        Span span = base.documentSpan(docId);
        closeLabels(labels.getTypes(), labels, span);
      }

    }
    catch (IllegalArgumentException e)
    {
      throw getNewException(e, " on line: " +line);
    }
		in.close();
		closeLabels(labels,closurePolicy);
	}

  private void 
	addToType(StringTokenizer tok, LineNumberReader in, File file, TextBase base, MutableTextLabels labels)
  {
    String id = advance(tok,in,file);
    String loStr = advance(tok,in,file);
    String lenStr = advance(tok,in,file);
    String type = advance(tok,in,file);
    int lo,len;
    try {
      lo = Integer.parseInt(loStr);
      len = Integer.parseInt(lenStr);
      Span span = base.documentSpan(id);
      if (span==null) {
				warnings++;
				if (warnings<MAX_WARNINGS) {
					log.warn("unknown id '"+id+"'");
				}	else if (warnings == MAX_WARNINGS) {
					log.warn("there will be no more warnings of this sort given");
				}
      } else {
        if (lo==0 && len<0) labels.addToType( span,type );
        else {
          if (len<0) len = span.asString().length()-lo;
          labels.addToType( span.charIndexSubSpan(lo,lo+len),type );
        }
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("bad number on line "+in.getLineNumber()+" of "+file.getName());
    }
  }

  private void 
	setSpanProp(StringTokenizer tok, LineNumberReader in, File file, TextBase base, MutableTextLabels labels)
  {
    String id = advance(tok,in,file);
    String loStr = advance(tok,in,file);
    String lenStr = advance(tok,in,file);
    String prop = advance(tok,in,file);
    String value = advance(tok,in,file);
    int lo,len;
    try {
      lo = Integer.parseInt(loStr);
      len = Integer.parseInt(lenStr);
      Span span = base.documentSpan(id);
      if (span==null) {
				warnings++;
				if (warnings<MAX_WARNINGS) {
					log.warn("unknown id '"+id+"'");
				}	else if (warnings == MAX_WARNINGS) {
					log.warn("there will be no more warnings of this sort given");
				}
      } else {
        if (lo==0 && len<0) labels.setProperty( span, prop, value );
        else {
          if (len<0) len = span.asString().length()-lo;
					labels.setProperty( span.charIndexSubSpan(lo,lo+len), prop, value );
        }
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("bad number on line "+in.getLineNumber()+" of "+file.getName());
    }
  }

  private static IllegalArgumentException getNewException(IllegalArgumentException e, String addToMsg)
  {
    String msg = e.getMessage() + addToMsg;
    StackTraceElement[] trace = e.getStackTrace();
    IllegalArgumentException exception = new IllegalArgumentException(msg);
    exception.setStackTrace(trace);
    return exception;
  }

	private String advance(StringTokenizer tok,LineNumberReader in,File file) {
		if (!tok.hasMoreTokens())
			throw new IllegalArgumentException("error on line "+in.getLineNumber()+" of "+file.getName() + " failed to find token");
		return tok.nextToken();
	}

  /**
   * Close labels on the labels according to the policy.  This applies the same
   * policy to all documents and types in the labels.  To get finer control of closure
   * use closeLabels(Set, MutableTextLabels, Span) or MutableTextLabels.closeTypeInside(...)
   * @param labels
   * @param policy
   */
	public void closeLabels(MutableTextLabels labels,int policy)
	{
		Set types = labels.getTypes();
		TextBase base = labels.getTextBase();
    switch (policy)
    {
      case CLOSE_ALL_TYPES:
        for (Span.Looper i=base.documentSpanIterator(); i.hasNext(); ) {
          Span document = i.nextSpan();
          closeLabels(types, labels, document);
        }
        break;
      case CLOSE_TYPES_IN_LABELED_DOCS:
				Set labeledDocs = new TreeSet();
        for (Iterator j=types.iterator(); j.hasNext(); ) {
          String type = (String)j.next();
          for (Span.Looper i=labels.instanceIterator(type); i.hasNext(); ) {
            Span span = i.nextSpan();
						labeledDocs.add( span.documentSpan() );
          }
        }
				for (Iterator i=labeledDocs.iterator(); i.hasNext(); ) {
          Span document = (Span)i.next();
          closeLabels(types, labels, document);
				}
        break;
      case DONT_CLOSE_TYPES: //do nothing for this
        break;
      case CLOSE_BY_OPERATION: //already closed in theory
        break;
      default:
        log.warn("closure policy(" + policy + ") not recognized");
		}
	}

  /**
   * Close all types in the typeSet on the given document
   * @param typeSet set of types to close for this document
   * @param labels TextLabels holding the types
   * @param document Span to close types over
   */
  private void closeLabels(Set types, MutableTextLabels labels, Span document)
  {
    for (Iterator j=types.iterator(); j.hasNext(); ) {
      String type = (String)j.next();
      labels.closeTypeInside( type, document );
    }
  }

  /** Read in a serialized TextLabels. */
	public MutableTextLabels loadSerialized(File file,TextBase base) throws IOException,FileNotFoundException
	{
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			MutableTextLabels labels = (MutableTextLabels)in.readObject();
			labels.setTextBase(base);
			in.close();
			return labels;
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("can't read TextLabels from "+file+": "+e);
		}
	}

	/** Serialize a TextLabels. */
	public void saveSerialized(MutableTextLabels labels, File file) throws IOException {
		ObjectOutputStream out =	new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		out.writeObject( labels );
		out.flush();
		out.close();
	}

	/** Save extracted data in a format readable with loadOps. */
	public void saveTypesAsOps(TextLabels labels,File file) throws IOException
	{
		PrintStream out = new PrintStream(new FileOutputStream(file));
		for (Iterator i=labels.getTypes().iterator(); i.hasNext(); ) {
			String type = (String)i.next();
			for (Span.Looper j=labels.instanceIterator(type); j.hasNext(); ) {
				Span s = j.nextSpan();
				if (s.size()>0) {
					int lo = s.getTextToken(0).getLo();
					int hi = s.getTextToken(s.size()-1).getHi();
					out.println("addToType "+s.getDocumentId()+" "+lo+" "+(hi-lo)+" "+type);
				} else {
					log.warn("forgetting label on empty span");
				}
			}
      Span.Looper it = labels.closureIterator(type);
      while (it.hasNext())
      {
        Span s = it.nextSpan();
				Span doc = s.documentSpan();
				if (s.size()!=doc.size()) {
					throw new UnsupportedOperationException("can't save environment with closureSpans!=docSpans");
				}
        out.println("closeType " + s.getDocumentId() + " " + type);
      }
		}
		for (Iterator i=labels.getSpanProperties().iterator(); i.hasNext(); ) {
			String prop = (String)i.next();
			for (Span.Looper j=labels.getSpansWithProperty(prop); j.hasNext(); ) {
				Span s = j.nextSpan();
				String val = labels.getProperty(s,prop);
				int lo = s.getTextToken(0).getLo();
				int hi = s.getTextToken(s.size()-1).getHi();
				out.println("setSpanProp "+s.getDocumentId()+"  "+lo+" "+(hi-lo)+" "+prop+" "+val);
			}
		}
		out.close();
	}

	/** Save spans of given type into the file, one per line.
	 * Linefeeds in strings are replaced with spaces.
	 */
	public void saveTypesAsStrings(TextLabels labels,File file,boolean includeOffset) throws IOException
	{
		PrintStream out = new PrintStream(new FileOutputStream(file));
		for (Iterator j=labels.getTypes().iterator(); j.hasNext(); ) {
			String type = (String)j.next();
			for (Span.Looper i=labels.instanceIterator(type); i.hasNext(); ) {
				Span span = i.nextSpan();
				out.print( type );
				if (includeOffset) { 
					out.print(":"+span.getDocumentId()+":"+span.getTextToken(0).getLo()+":"+span.getTextToken(span.size()-1).getHi());
				}
				out.println( "\t" + span.asString().replace('\n',' '));
			}
		}
		out.close();
	}

	/** Save extracted data in an XML format.  Convert to string
	 * &lt;root>..&lt;type>...&lt;/type>..&lt;/root> nested things &lt;a>A&lt;b>B&lt;/b>C&lt;/a>
	 * are stored as nested things &lt;a>A&lt;set v=a,b>B&lt;/set>C&lt;/a> where
	 * single sets are simplified so mismatches like [A (B C] D)E are
	 * stored as &lt;a>a&lt;set v=a,b>B C&lt;/set>&lt;/a>&lt;b>D&lt;/b>E */

	public String markupDocumentSpan(String documentId,TextLabels labels) {
		TreeMap boundaries = new TreeMap();
		for (Iterator i=labels.getTypes().iterator(); i.hasNext(); ) {
			String type = (String)i.next();
			for (Span.Looper j=labels.instanceIterator(type, documentId); j.hasNext(); ) {
				Span s = j.nextSpan();
				//System.out.println("Left Boundary: " + s.getLeftBoundary());
				//System.out.println("Right Boundary: " + s.getRightBoundary());
				setBoundary(boundaries,"begin",type,s.getLeftBoundary());
				setBoundary(boundaries,"end",type,s.getRightBoundary());
			}
		}
		// now walk thru boundaries and find out which set as
		// associated with each segment - want map from boundaries to
		// type sets
		String source = labels.getTextBase().documentSpan(documentId).asString();
		//System.out.println("source is "+source);
		StringBuffer buf = new StringBuffer("");
		buf.append("<root>");
		int currentPos = 0;
		Set currentTypes = new TreeSet();
		String lastMarkup = null;
		for (Iterator i=boundaries.keySet().iterator(); i.hasNext(); ) {
			Span b = (Span)i.next();
			//System.out.println("b="+b);
			// work out what types are in effect here
			Set ops = (Set)boundaries.get(b);
			for (Iterator j=ops.iterator(); j.hasNext(); ) {
				String[] op = (String[]) j.next();
				System.out.println("op is "+op[0]+","+op[1]);
				if ("begin".equals(op[0])) currentTypes.add(op[1]);
				else currentTypes.remove(op[1]);
			}
			// output next section of document
			int pos;
			if (b.documentSpanStartIndex() < b.documentSpan().size())
				pos = b.documentSpan().subSpan( b.documentSpanStartIndex(), 1).getTextToken(0).getLo();
			else
				pos = b.documentSpan().getTextToken(b.documentSpan().size()-1).getHi();

			//System.out.println("boundary "+pos+" currentTypes="+currentTypes);
			buf.append( source.substring(currentPos, pos) );
			// close off last markup
			if (lastMarkup!=null) buf.append("</"+lastMarkup+">");
			// work out next markup symbol
			String markup = null;
			String value = null;
			if (currentTypes.size()==1) {
				markup = (String) (currentTypes.iterator().next());
			} else if (currentTypes.size()>1) {
				markup = "overlap";
				StringBuffer vBuf = new StringBuffer("");
				for (Iterator j=currentTypes.iterator(); j.hasNext(); ) {
					if (vBuf.length()>0) vBuf.append(",");
					vBuf.append( (String) j.next() );
				}
				value = vBuf.toString();
			}
			if (markup!=null && value!=null) {
				buf.append("<"+markup+" value=\""+value+"\">");
			} else if (markup!=null) {
				buf.append("<"+markup+">");
			}
			// update position, lastMarkup
			currentPos = pos;
			lastMarkup = markup;
			//System.out.println("after update buf='"+buf+"'");
		} // each boundary
		// close it all off
		buf.append(source.substring(currentPos,source.length()));
		buf.append("</root>");
		return buf.toString();
	}

  /** Save extracted data in an XML format.  Convert to string
	 * &lt;root>..&lt;type>...&lt;/type>..&lt;/root> nested things &lt;a>A&lt;b>B&lt;/b>C&lt;/a>
	 * are stored as nested things &lt;a>A&lt;set v=a,b>B&lt;/set>C&lt;/a> where
	 * single sets are simplified so mismatches like [A (B C] D)E are
	 * stored as &lt;a>a&lt;set v=a,b>B C&lt;/set>&lt;/a>&lt;b>D&lt;/b>E */

    public String createXMLmarkup(String documentId,TextLabels labels) {
	// create vector of labels with their start and end positions
	Vector types = new Vector();
	for(Iterator i=labels.getTypes().iterator(); i.hasNext(); ){
	    String type = (String)i.next();
	    for(Span.Looper j=labels.instanceIterator(type, documentId); j.hasNext(); ) {
		Span s = j.nextSpan();
		Vector l = new Vector();
		l.add(type);
		int st = s.documentSpanStartIndex();
		Integer start = new Integer(st);
		int e = st + s.size(); 
		Integer end = new Integer(e);
		l.add(start);
		l.add(end);
		l.add(s);
		types.add(l);
	    }
	}
	//Sort Labels so that there are no overlapping labels and longest spans are on outside
	Vector newTypes = new Vector();
	newTypes.add((Vector)types.get(0));
	boolean flag = true;
	for(int x=1; x<types.size(); x++) {
	    int curStart = ((Integer)(((Vector)types.get(x)).get(1))).intValue();
	    int curEnd = ((Integer)(((Vector)types.get(x)).get(2))).intValue();
	    flag = true;
	    for(int y=0; y<newTypes.size(); y++){
		int prevStart = ((Integer)(((Vector)newTypes.get(y)).get(1))).intValue();
		    int prevEnd = ((Integer)(((Vector)newTypes.get(y)).get(2))).intValue();
		    if(curStart > prevStart && curStart < prevEnd && curEnd > prevEnd){
			flag = false;
			break;
		    }
	    }
	    //add type to array if no conflicts in order
	    if(flag) {
		boolean flag2 = true;
		for(int i=0; i<newTypes.size(); i++){
		    int prevStart = ((Integer)(((Vector)newTypes.get(i)).get(1))).intValue();
		    int prevEnd = ((Integer)(((Vector)newTypes.get(i)).get(2))).intValue();
		    if(curStart < prevStart || (curStart == prevStart && curEnd > prevEnd)) {
			newTypes.insertElementAt((Vector)types.get(x),i);
			flag2 = false;
			break;
		    } 			
		}
		if(flag2)
		    newTypes.add((Vector)types.get(x));
	    }
	}
	Vector orderedMarkers = new Vector();
	Vector previousEnd = new Vector();
	int la = 0;
	Integer high = new Integer(la);
	previousEnd.add(high);
	orderedMarkers = createOrderedMarkers(newTypes, orderedMarkers, previousEnd);
	for(int a=0; a<orderedMarkers.size(); a++) {
	    String t = ((String)(((Vector)orderedMarkers.get(a)).get(0)));
	    int start = ((Integer)(((Vector)orderedMarkers.get(a)).get(1))).intValue();
	    //System.out.println(t + ": " + start);
	}
	//Embed Labels in TextBase
	StringBuffer buf = new StringBuffer("");
	String source = labels.getTextBase().documentSpan(documentId).asString();
	int currentPos = 0;
	for(int i=0; i<orderedMarkers.size(); i++) { 
	    String t = ((String)(((Vector)orderedMarkers.get(i)).get(0)));
	    //int pos = ((Integer)(((Vector)orderedMarkers.get(i)).get(1))).intValue();
	    Span s = ((Span)(((Vector)orderedMarkers.get(i)).get(2)));
	    int pos;
	    if(t.startsWith("</")){
		if (s.documentSpanStartIndex()+s.size() < s.documentSpan().size()){
			pos = s.documentSpan().subSpan( s.documentSpanStartIndex()+s.size(), 1).getTextToken(0).getLo();		
		}else{
		    pos = s.documentSpan().getTextToken(s.documentSpan().size()-1).getHi();
		}
	    } else {
		if (s.documentSpanStartIndex() < s.documentSpan().size()){
			pos = s.documentSpan().subSpan( s.documentSpanStartIndex(), 1).getTextToken(0).getLo();
		}else{
			pos = s.documentSpan().getTextToken(s.documentSpan().size()-1).getHi();
		}
	    }
	    if(pos >= 0 && pos > currentPos) {
		buf.append( source.substring(currentPos, pos) );
		currentPos = pos;
	    }
	    buf.append(t);
	}
	return buf.toString();
	//return "Not Yet";
    }
    
    private Vector createOrderedMarkers(Vector v, Vector ordered, Vector prevEnd) {
	Vector mark = (Vector)v.remove(0);
	String s = "<" + ((String)mark.get(0)) + ">";
	Integer i = (Integer)mark.get(1);
	Integer i2 = (Integer)mark.get(2);
	int curEnd = i2.intValue();
	Vector begin = new Vector();
	begin.add(s);
	begin.add(i);
	begin.add(mark.get(3));
	ordered.add(begin);
	
	int nextEnd = 0;
	if(!v.isEmpty()) {
	    Vector next = (Vector)v.get(0);
	    Integer i3 = (Integer)next.get(2);
	    nextEnd = i3.intValue();
	    if(curEnd >= nextEnd) {
		prevEnd.add(new Integer(curEnd));
		createOrderedMarkers(v, ordered, prevEnd);
	    }
	    
	}
	
	String s2 = "</" + ((String)mark.get(0)) + ">";
	Vector end = new Vector();
	end.add(s2);
	end.add(i2);
	end.add(mark.get(3));
	ordered.add(end);

	Integer temp = (Integer)prevEnd.lastElement();
	int prev = ((Integer)prevEnd.lastElement()).intValue();
	if(!v.isEmpty() && (prev >= nextEnd || prevEnd.size()==1)){
	    //prevEnd.remove(prevEnd.lastIndexOf(temp));
	    createOrderedMarkers(v, ordered, prevEnd);
	} else if(prevEnd.size()>1) {
	    prevEnd.remove(prevEnd.lastIndexOf(temp));
	}

	return ordered;
    }
    
    private void setBoundary(TreeMap boundaries,String beginOrEnd,String type,Span s) {
		Set ops = (Set)boundaries.get(s);
		if (ops==null) boundaries.put(s, (ops = new HashSet()) );
		ops.add( new String[] { beginOrEnd, type } );
	}


	/** Save extracted data in an XML format */
	public String saveTypesAsXML(TextLabels labels) {
		StringBuffer buf = new StringBuffer("<extractions>\n");
		for (Iterator i=labels.getTypes().iterator(); i.hasNext(); ) {
			String type = (String)i.next();
			for (Span.Looper j=labels.instanceIterator(type); j.hasNext(); ) {
				Span s = j.nextSpan();
				int lo = s.getTextToken(0).getLo();
				int hi = s.getTextToken(s.size()-1).getHi();
				buf.append("  <"+type+" lo="+lo+" hi="+hi+">"+s.asString()+"</"+type+">\n");
			}
		}
		buf.append("</extractions>\n");
		return buf.toString();
	}
}
