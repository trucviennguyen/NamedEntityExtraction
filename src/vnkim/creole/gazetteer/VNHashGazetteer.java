package vnkim.creole.gazetteer;

import gate.*;
import gate.creole.*;
import gate.creole.gazetteer.*;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;
import gate.util.LuckyException;

import java.util.*;

import vnkim.util.StringTransformations;
import vnkim.database.VNQueryAPI;

public class VNHashGazetteer extends AbstractGazetteer
{
    protected static final boolean DEBUG = false;
    protected static final String MAJOR_TYPE_STR = "majorType";
    protected static final String MINOR_TYPE_STR = "minorType";
    protected static final String LANGUAGE = "language";
    protected static final String LOOKUP = "SWLookup";
    protected static final String DOING_LOOKUP_IN = "Doing lookup in ";
    protected static final String EMPTY_STR = "";
    protected static final String DOTS = "...";
    protected static final String SLASH_SLASH = "\\";
    protected static final String READING = "Reading ";
    protected static final String DOT = ".";

    protected String HashMode;

    protected Map listsByNode;
    protected ArrayList mapsList;
    protected int mapsListSize;
    protected ArrayList categoryList;

    protected Map LRlistsByNode;
    protected ArrayList LRmapsList;
    protected int LRmapsListSize;
    protected ArrayList LRcategoryList;

    /** the linear definition of the gazetteer */
    protected VNLinearDefinition LRdefinition;

    static HashSet OpenSigns = new HashSet();
    static HashSet ClosingSigns = new HashSet();
    static HashSet PunctuationSigns = new HashSet();
    static HashSet LinkingSigns = new HashSet();
    static HashSet TerminationSigns = new HashSet();
    static HashSet Signs = new HashSet();

/*
    SELECT x FROM {x} <rdf:type> {<rdfs:Class>}
        WHERE NOT x LIKE "*www.w3.org*"
        AND NOT x LIKE "*node*"

    SELECT x FROM {x} <rdfs:subClassOf>
*/

    // remove '.', ''', '(', ')', '~'
    // remove ',', '-', '&', '/',
    static {
      PunctuationSigns.add(new Character('.'));
      PunctuationSigns.add(new Character('~'));
      PunctuationSigns.add(new Character('\''));
      PunctuationSigns.add(new Character('\''));

      OpenSigns.add(new Character('"'));
      OpenSigns.add(new Character('('));

      ClosingSigns.add(new Character('"'));
      ClosingSigns.add(new Character(')'));

      // LinkingSigns.add(new Character(' '));
      LinkingSigns.add(new Character(','));
      LinkingSigns.add(new Character('-'));
      LinkingSigns.add(new Character('&'));
      LinkingSigns.add(new Character('/'));

      TerminationSigns.add(new Character('\r'));
      TerminationSigns.add(new Character('\n'));

      Signs.addAll(PunctuationSigns);
      Signs.addAll(OpenSigns);
      Signs.addAll(ClosingSigns);
      Signs.addAll(LinkingSigns);
      Signs.addAll(TerminationSigns);
      Signs.add(new Character(';'));
      Signs.add(new Character(':'));
      Signs.add(new Character('?'));
    } // static code

    public VNHashGazetteer()
    {
        mapsList = new ArrayList(10);
        mapsListSize = 0;
        categoryList = null;

        LRmapsList = new ArrayList(10);
        LRmapsListSize = 0;
        LRcategoryList = null;
    }

    public Resource init()
        throws ResourceInstantiationException
    {
        try
        {
            KBProps.load(KB.openStream());
            initEntities();
            initLexica();
            VNQueryAPI.closeHttp();
            fireProcessFinished();
        }
        catch(Exception x)
        {
            throw new ResourceInstantiationException(x);
        }
        return this;
    }


    public void initEntities()
        throws ResourceInstantiationException
    {
        try
        {
            super.definition = new VNLinearDefinition(vnkim.util.VNConstants.ENTITY_QUERY);
            // super.definition.setURL(super.listsURL);
            super.definition.load();
            int linesCnt = super.definition.size();
            listsByNode = super.definition.getListsByNode();
            mapsList.add(new HashMap(linesCnt * 10));
            mapsListSize = mapsList.size();
            categoryList = new ArrayList(linesCnt + 1);
            Iterator inodes = super.definition.iterator();
            int nodeIdx = 0;
            VNLinearNode node;
            for(; inodes.hasNext(); readList(node, true))
            {
                node = (VNLinearNode)inodes.next();
                fireStatusChanged("Reading ".concat(String.valueOf(String.valueOf(node.toString()))));
                fireProgressChanged((++nodeIdx * 100) / linesCnt);
            }
        }
        catch(Exception x)
        {
            throw new ResourceInstantiationException(x);
        }
    }

    public void initLexica()
        throws ResourceInstantiationException
    {
        try
        {
            LRdefinition = new VNLinearDefinition(vnkim.util.VNConstants.LEXICA_QUERY);
            LRdefinition.load();
            int linesCnt = LRdefinition.size();
            LRlistsByNode = LRdefinition.getListsByNode();
            LRmapsList.add(new HashMap(linesCnt * 10));
            LRmapsListSize = LRmapsList.size();
            LRcategoryList = new ArrayList(linesCnt + 1);
            Iterator inodes = LRdefinition.iterator();
            int nodeIdx = 0;
            VNLinearNode node;
            for(; inodes.hasNext(); readListLR(node, true))
            {
                node = (VNLinearNode)inodes.next();
                fireStatusChanged("Reading ".concat(String.valueOf(String.valueOf(node.toString()))));
                fireProgressChanged((++nodeIdx * 100) / linesCnt);
            }
        }
        catch(Exception x)
        {
            throw new ResourceInstantiationException(x);
        }
    }
    
    void readList(VNLinearNode node, boolean add)
        throws GazetteerException
    {
        if (node == null)
            throw new GazetteerException(" VNLinearNode node is null ");
        String listName = node.getList();
        String clas = node.getClas();
        String inst = node.getInst();
        String languages = node.getLanguage();
        VNGazetteerList gazList = (VNGazetteerList)listsByNode.get(node);
        if(gazList == null)
            throw new GazetteerException("gazetteer list not found by node");
        SWLookup lookup = new SWLookup(clas, inst, languages);
        if(super.mappingDefinition != null)
        {
            MappingNode mnode = super.mappingDefinition.getNodeByList(listName);
            if(mnode != null)
            {
                lookup.oClass = mnode.getClassID();
                lookup.ontology = mnode.getOntologyID();
            }
        }
        categoryList.add(lookup);

        Iterator iline = gazList.iterator();
        int mapIndex = -1;
        String word = null;
        ArrayList oldKey = null;
        HashMap currentMap = new HashMap();
        int length = 0;
        String line;
        VNLinearInst vnLinearInst;
        if (add)
            while (iline.hasNext())
            {
                vnLinearInst = (VNLinearInst)iline.next();
                SWLookup swLookup = new SWLookup(clas, vnLinearInst.getInst(), null);
                ArrayList key = new ArrayList(1);
                key.add(swLookup);
                // categoryList.add(swLookup);

                line = vnLinearInst.value;
                mapIndex = -1;
                line.trim();
                length = line.length();
                for (int lineIndex = 0; lineIndex < length; lineIndex++)
                {
                    if(lineIndex + 1 != length && !Character.isWhitespace(line.charAt(lineIndex)))
                        continue;
                    if(lineIndex + 1 == length)
                        lineIndex = length;
                    word = line.substring(0, lineIndex).trim();
                    word = StringTransformations.standardized(word);

                    mapIndex++;
                    if(mapsListSize <= mapIndex)
                    {
                        mapsList.add(new HashMap());
                        mapsListSize++;
                    }
                    currentMap = (HashMap)mapsList.get(mapIndex);
                    if (!currentMap.containsKey(word))
                        currentMap.put(word, null);
                }

                oldKey = (ArrayList)currentMap.get(word);
                if(oldKey == null)
                {
                    currentMap.put(word, key);
                } else
                {
                    ArrayList mergedKey = new ArrayList(oldKey);
                    mergedKey.add(key.get(0));
                    currentMap.put(word, mergedKey);
                }
            }
        else
            while(iline.hasNext()) {
                vnLinearInst = (VNLinearInst)iline.next();
                line = vnLinearInst.value;
            }
    }

    void readListLR(VNLinearNode node, boolean add)
        throws GazetteerException
    {
        if (node == null)
            throw new GazetteerException(" VNLinearNode node is null ");
        String listName = node.getList();
        String clas = node.getClas();
        String inst = node.getInst();
        String languages = node.getLanguage();
        VNGazetteerList gazList = (VNGazetteerList)LRlistsByNode.get(node);
        if(gazList == null)
            throw new GazetteerException("gazetteer list not found by node");
        ArrayList key = new ArrayList(1);
        SWLookup lookup = new SWLookup(clas, inst, languages);
        if(super.mappingDefinition != null)
        {
            MappingNode mnode = super.mappingDefinition.getNodeByList(listName);
            if(mnode != null)
            {
                lookup.oClass = mnode.getClassID();
                lookup.ontology = mnode.getOntologyID();
            }
        }
        key.add(lookup);
        LRcategoryList.add(lookup);
        Iterator iline = gazList.iterator();
        int mapIndex = -1;
        String word = null;
        ArrayList oldKey = null;
        HashMap currentMap = new HashMap();
        int length = 0;
        String line;
        VNLinearInst vnLinearInst;
        if (add)
            while (iline.hasNext())
            {
                vnLinearInst = (VNLinearInst)iline.next();
                lookup.inst = vnLinearInst.inst;
                line = vnLinearInst.value;
                mapIndex = -1;
                line.trim();
                length = line.length();
                for (int lineIndex = 0; lineIndex < length; lineIndex++)
                {
                    if(lineIndex + 1 != length && !Character.isWhitespace(line.charAt(lineIndex)))
                        continue;
                    if(lineIndex + 1 == length)
                        lineIndex = length;
                    word = line.substring(0, lineIndex).trim();
                    word = StringTransformations.standardized(word);
                    mapIndex++;
                    if(LRmapsListSize <= mapIndex)
                    {
                        LRmapsList.add(new HashMap());
                        LRmapsListSize++;
                    }
                    currentMap = (HashMap)LRmapsList.get(mapIndex);
                    if (!currentMap.containsKey(word))
                        currentMap.put(word, null);
                }

                oldKey = (ArrayList)currentMap.get(word);
                if(oldKey == null)
                {
                    currentMap.put(word, key);
                } else
                {
                    ArrayList mergedKey = new ArrayList(oldKey);
                    boolean duplicity = false;
                    for(int i = 0; i < oldKey.size() && !duplicity; i++)
                        duplicity = ((SWLookup)mergedKey.get(i)).equals((SWLookup)key.get(0));

                    if (!duplicity)
                        mergedKey.add(key.get(0));
                    currentMap.put(word, mergedKey);
                }
            }
        else
          while(iline.hasNext()) {
              vnLinearInst = (VNLinearInst)iline.next();
              lookup.inst = vnLinearInst.inst;
              line = vnLinearInst.value;
          }
    }

    @SuppressWarnings("empty-statement")
    @Override
    public void execute()
        throws ExecutionException
    {
      if(super.document == null)
          throw new ExecutionException("No document to process!");
      AnnotationSet annotationSet;
      if(super.annotationSetName == null || super.annotationSetName.equals(""))
          annotationSet = super.document.getAnnotations();
      else
          annotationSet = super.document.getAnnotations(super.annotationSetName);
      if(super.document.getSourceUrl() == null)
          fireStatusChanged("Doing lookup in  document created by string...");
      else
          fireStatusChanged(String.valueOf(String.valueOf((new StringBuffer("Doing lookup in ")).append(super.document.getSourceUrl().getFile()).append("..."))));

      String content = super.document.getContent().toString();
      int length = content.length();
      int matchedRegionEnd = 0;
      int matchedRegionStart = 0;
      int iwordStart = 0;
      int iend = 0;
      int secondWordStart = 0;
      String phrase = "";
      int mapIndex = 0;
      ArrayList currentLookup = null;
      ArrayList currentLookupLR = null;
      ArrayList lastValidLookup = null;
      boolean firstWord = true;

      // First punctionation point
      boolean punctuationZone = false;

      // like , và
      boolean termZone = false;
      char currentChar = '\0';
      Character ch = new Character(currentChar);

      do
      {
          if (iwordStart >= length)
              break;
          if (firstWord)
              iwordStart = Math.max(matchedRegionEnd, secondWordStart);
          else
              iwordStart = iend;
          if (iwordStart >= length)
              break;
          for (; iwordStart < length && containsSigns(content.charAt(iwordStart)); iwordStart++);
          iend = iwordStart + 1;
          if (iend >= length)
              break;
          do
          {
              do
              {
                  if (iend >= length)
                    break;
                  currentChar = content.charAt(iend);
                  ch = new Character(currentChar);
                  if (containsSigns(currentChar))
                      break;
                  iend++;
              } while(true);

              if (firstWord)
              {
                  phrase = content.substring(iwordStart, iend);
                  secondWordStart = iend + 1;
                  matchedRegionStart = iwordStart;
                  firstWord = false;
              }
              else
                  phrase = content.substring(matchedRegionStart, iend);

              if (TerminationSigns.contains(ch) ||
                  (PunctuationSigns.contains(ch) && (iend < length - 1 && Character.isWhitespace(content.charAt(++iend)))))
              {
                  termZone = true;
                  firstWord = true;
              }
              else if (PunctuationSigns.contains(ch))
                  punctuationZone = true;
              else
                  punctuationZone = false;

              if (!phrase.equals(""))
              {
                //System.out.println("Before standardized: phrase = " + phrase);
                String st = phrase;
                st = StringTransformations.standardized(st);
                //System.out.println("After standardized: phrase = " + st);

                StringTokenizer tokens = new StringTokenizer(st);
                mapIndex = tokens.countTokens() - 1;

                if (!st.equals("")) {
                  currentLookup = null;
                  currentLookupLR = null;
                  HashMap currentMap = null;
                  HashMap currentMapLR = null;
                  boolean inKB = false;

                  if (Character.isUpperCase(phrase.charAt(0))
                      && mapIndex < mapsListSize)
                  {
                    currentMap = (HashMap) mapsList.get(mapIndex);

                    if (currentMap.containsKey(st)) {
                      inKB = true;
                      if (currentMap.get(st) != null)
                        currentLookup =  new ArrayList((ArrayList)currentMap.get(st));
                      if (currentLookup != null) {
                        matchedRegionEnd = iend;
                        secondWordStart = iend;
                        lastValidLookup = currentLookup;
                      }
                    }
                  }

                  if (mapIndex >= LRmapsListSize) {
                    if (!inKB && !punctuationZone)
                    {
                      for (; secondWordStart < length && containsSigns(content.charAt(secondWordStart)); secondWordStart++);
                      iwordStart = iend = secondWordStart;
                      firstWord = true;
                      punctuationZone = false;
                    }
                    else iend++;
                    continue;
                  }

                  currentMapLR = (HashMap) LRmapsList.get(mapIndex);

                  if (currentMapLR.containsKey(st)) {
                    if (currentMapLR.get(st) != null)
                      currentLookupLR = new ArrayList((ArrayList) currentMapLR.get(st));
                    if (currentLookup == null)
                      currentLookup = currentLookupLR;
                    else if (currentLookupLR != null)
                      currentLookup.addAll(currentLookupLR);

                    if (currentLookup != null) {
                      matchedRegionEnd = iend;
                      secondWordStart = iend;
                      lastValidLookup = currentLookup;
                    }
                  }
                  else if (!inKB) {
                    for (; secondWordStart < length && containsSigns(content.charAt(secondWordStart)); secondWordStart++);
                    iwordStart = iend = secondWordStart;
                    firstWord = true;
                    punctuationZone = false;
                    continue;
                  }
                }
                else
                  if (firstWord)
                  {
                    iwordStart += StringTransformations.getLinkingChars();

                    iwordStart += StringTransformations.getLinkingChars();;
                    for (; iwordStart < length && containsSigns(content.charAt(iwordStart)); iwordStart++);
                    iend = iwordStart;

/*
                    while (iwordStart < length) {
                       if (containsSigns(content.charAt(iwordStart)))
                       {
                         iwordStart++;
                         continue;
                       }
                       if (content.substring(iwordStart, iwordStart + 2).equalsIgnoreCase("VÀ"))
                         iwordStart += 2;
                    }
                    iend = iwordStart;
*/
                    continue;
                  }
                  else
                  {
                    matchedRegionStart += StringTransformations.getLinkingChars();;
                    for (; matchedRegionStart < length && containsSigns(content.charAt(matchedRegionStart)); matchedRegionStart++);
                    iend = matchedRegionStart;
                    continue;
                  }
              }
              else
                if (!punctuationZone) {
                  iend = secondWordStart;
                  firstWord = true;
                  continue;
                }
              iend++;
          } while(!termZone && (!firstWord || lastValidLookup == null) && iend < length);

          if(iend >= length || iwordStart >= length)
          {
              iend = Math.max(secondWordStart, matchedRegionEnd);
              punctuationZone = false;
              firstWord = true;
          }

          if(firstWord && lastValidLookup != null)
          {
              for(Iterator lookupIter = lastValidLookup.iterator(); lookupIter.hasNext();)
              {
                SWLookup lookup = (SWLookup)lookupIter.next();
                FeatureMap fm = Factory.newFeatureMap();
                fm.put("class", lookup.clas);
                if(lookup.oClass != null && lookup.ontology != null)
                {
                    fm.put("class", lookup.oClass);
                    fm.put("ontology", lookup.ontology);
                }
                if(lookup.inst != null)
                {
                    fm.put("inst", lookup.inst);
                    if(lookup.languages != null)
                        fm.put("language", lookup.languages);
                }
                try
                {
                    int l, r;
                    String st = content.substring(matchedRegionStart, matchedRegionEnd);
                    l = 0;
                    r = st.length() - 1;
/*
                    while (l < st.length() - 1)
                    {
                        if (st.substring(l, l + 2).equalsIgnoreCase("và")) l += 2;
                        else
                            if (LinkingSigns.contains(new Character(st.charAt(l)))) l++;
                            else break;
                    }
                    if (LinkingSigns.contains(new Character(st.charAt(l)))) l++;
*/
                    while (r > 0)
                    {
                        if (st.substring(r - 1, r + 1).equalsIgnoreCase("và")) r -= 2;
                        else
                            if (containsSigns(st.charAt(r))) r--;
                            else break;
                    }
                    if (containsSigns(st.charAt(r))) r--;
                    annotationSet.add(new Long(matchedRegionStart + l), new Long(matchedRegionEnd - (st.length() - r - 1)), "Lookup", fm);
                }
                catch (InvalidOffsetException ioe)
                {
                    throw new LuckyException(ioe.toString());
                }
              }

              lastValidLookup = null;
          }

          if (termZone)
          {
              while ((secondWordStart < length) && TerminationSigns.contains(new Character(content.charAt(secondWordStart))))
                 secondWordStart++;
              termZone = false;
              punctuationZone = false;
              firstWord = true;
          }

      } while(true);

      if(lastValidLookup != null)
      {
          for(Iterator lookupIter = lastValidLookup.iterator(); lookupIter.hasNext();)
          {
            SWLookup lookup = (SWLookup)lookupIter.next();
            FeatureMap fm = Factory.newFeatureMap();
            fm.put("class", lookup.clas);
            if(lookup.oClass != null && lookup.ontology != null)
            {
                fm.put("class", lookup.oClass);
                fm.put("ontology", lookup.ontology);
            }
            if(lookup.inst != null)
            {
                fm.put("inst", lookup.inst);
                if(lookup.languages != null)
                    fm.put("language", lookup.languages);
            }
            try
            {
              int l, r;
              String st = content.substring(matchedRegionStart, matchedRegionEnd);
              l = 0;
              r = st.length() - 1;
/*
              while (l < st.length() - 1)
              {
                  if (st.substring(l, l + 2).equalsIgnoreCase("và")) l += 2;
                  else
                      if (LinkingSigns.contains(new Character(st.charAt(l)))) l++;
                      else break;
              }
              if (LinkingSigns.contains(new Character(st.charAt(l)))) l++;
*/
              while (r > 0)
              {
                  if (st.substring(r - 1, r + 1).equalsIgnoreCase("và")) r -= 2;
                  else
                      if (containsSigns(st.charAt(r))) r--;
                      else break;
              }
              if (containsSigns(st.charAt(r))) r--;
              annotationSet.add(new Long(matchedRegionStart + l), new Long(matchedRegionEnd - (st.length() - r - 1)), "Lookup", fm);
            }
            catch(InvalidOffsetException ioe)
            {
                throw new LuckyException(ioe.toString());
            }
          }

          lastValidLookup = null;
      }
      fireProcessFinished();
      fireStatusChanged("Gazetteer processing finished!");
    }

    public boolean containsSigns(char c) {
      return Signs.contains(new Character(c)) || Character.isWhitespace(c);
    }

    public Set lookup(String singleItem)
    {
        Set result = null;
        for(int li = 0; li < mapsListSize; li++)
        {
            HashMap list = (HashMap)mapsList.get(li);
            if (!list.containsKey(singleItem))
                continue;
            ArrayList lookupList = (ArrayList)list.get(singleItem);
            if(lookupList == null || lookupList.size() <= 0)
                continue;
            result = new HashSet(lookupList);
            break;
        }

        return result;
    }

    public boolean remove(String singleItem)
    {
        boolean isRemoved = false;
        int i = 0;
        do
        {
            if(i >= mapsListSize)
                break;
            HashMap map = (HashMap)mapsList.get(i);
            if(map.containsKey(singleItem))
            {
                map.remove(singleItem);
                isRemoved = true;
                break;
            }
            i++;
        } while(true);
        return isRemoved;
    }

    public boolean add(String s, Lookup lookup1)
    {
        throw new GateRuntimeException(String.valueOf(String.valueOf((new StringBuffer("Inapropriate usage of ")).append(getClass()).append("\n Try using ").append(super.getClass()).append("\n for methods with Lookup parameters."))));
    }

    public boolean add(String singleItem, SWLookup lookup)
    {
        boolean isAdded = false;
        ArrayList key = new ArrayList(1);
        key.add(lookup);
        categoryList.add(lookup);
        String line = singleItem;
        int mapIndex = -1;
        String word = null;
        ArrayList oldKey = null;
        HashMap currentMap = null;
        int length = 0;
        line.trim();
        length = line.length();
        for(int lineIndex = 0; lineIndex < length; lineIndex++)
        {
            if(lineIndex + 1 != length && !Character.isWhitespace(line.charAt(lineIndex)))
                continue;
            if(lineIndex + 1 == length)
                lineIndex = length;
            word = line.substring(0, lineIndex).trim();
            if(mapsListSize <= mapIndex)
            {
                mapsList.add(new HashMap());
                mapsListSize++;
            }
            currentMap = (HashMap)mapsList.get(mapIndex);
            if (!currentMap.containsKey(word))
                currentMap.put(word, null);
        }

        oldKey = (ArrayList)currentMap.get(word);
        if(oldKey == null)
        {
            currentMap.put(word, key);
            isAdded = true;
        } else
        {
            ArrayList mergedKey = new ArrayList(oldKey);
            boolean duplicity = false;
            for(int i = 0; i < oldKey.size(); i++)
                duplicity = ((SWLookup)mergedKey.get(i)).equals((SWLookup)key.get(0));

            if (!duplicity)
                mergedKey.add(key.get(0));
            currentMap.put(word, mergedKey);
            isAdded = true;
        }
        return isAdded;
    }

    public void setKB(java.net.URL newKB) {
      KB = newKB;
    }

    public java.net.URL getKB()
    {
        return KB;
    }

    public static Properties getProperties()
    {
        return KBProps;
    }

    private java.net.URL KB;
    protected static Properties KBProps = new Properties();
}
