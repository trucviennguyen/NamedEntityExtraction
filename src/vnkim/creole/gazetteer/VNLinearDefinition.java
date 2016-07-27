/*
 * VNLinearDefinition.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 02/2002
 *
 */
package vnkim.creole.gazetteer;

import java.io.*;
import java.net.URL;
import java.util.*;

import gate.creole.ResourceInstantiationException;
import gate.creole.gazetteer.GazetteerListInt;

import org.openrdf.sesame.query.QueryResultsTable;

import vnkim.database.VNQueryAPI;
import vnkim.util.VNConstants;

/** Represents a Linear Definition [lists.def] file <br>
 *  The normal usage of the class will be
 *  * construct it
 *  * setURL
 *  * load
 *  * change
 *  * store
 */
public class VNLinearDefinition extends gate.creole.gazetteer.LinearDefinition {

  /** the default encoding of the definition */
  private final static String ENCODING = "UTF-8";

  /** the list of nodes */
  private List nodes = new ArrayList();

  /** the URL of the definition */
  private URL url;

  /** set of lists as strings*/
  private List lists = new ArrayList();

  /**the encoding of the list */
  private String encoding = "UTF-8";

  /** a mapping between a list and a node */
  private Map nodesByList = new HashMap();

  /** a map of gazetteer lists by nodes. this is loaded on loadLists*/
  private Map gazListsByNode = new HashMap();

  /** flag whether the definition has been modified after loading */
  private boolean isModified = false;
  
  /** the separator used to delimit feature name-value pairs in gazetteer lists */
  private String separator;

  private String Query_Mode;

  String ClassQuery = "select DISTINCT x"
       + " from {x} rdfs:subClassOf {protons:Entity}"
       + " using namespace"
       + " protons = <http://proton.semanticweb.org/2006/05/protons#>";

   String Lexica_ClassQuery = "select DISTINCT x"
       + " from {x} rdfs:subClassOf {kimlo:NERLexica}"
       + " using namespace"
       + " kimlo = <http://www.ontotext.com/kim/2006/05/kimlo#>";

  public VNLinearDefinition(String query_Mode) {
    Query_Mode = query_Mode;
  }

  /** Sets the encoding of the linear def
   *  @param encod the encoding to be set */
  public void setEncoding(String encod) {
    encoding = encod;
  }

  /** Gets the encoding of the linear def
   *  @return the encoding of the list*/
  public String getEncoding() {
    return encoding;
  }

  /**
   * Loads the gazetteer lists and maps them to the nodes
   * @return a map of nodes vs VNGazetteerLists
   * @throws ResourceInstantiationException
   */
  public Map loadLists(String Query_Mode) throws ResourceInstantiationException {
    try {
      gazListsByNode = new HashMap();
      Iterator inodes = nodes.iterator();
      while (inodes.hasNext()) {
        VNLinearNode node = (VNLinearNode)inodes.next();

        VNGazetteerList list = new VNGazetteerList(Query_Mode);
        URL lurl = new URL(url, node.getList());
        list.setURL(lurl);
        list.setEncoding(encoding);
        list.load();

        gazListsByNode.put(node,list);
      } // while inodes
    } catch (Exception ex) {
      throw new ResourceInstantiationException(ex);
    }
    return gazListsByNode;
  }  // loadLists()

  /** Loads a single gazetteer list given a name
   *  @param listName the name of the list to be loaded
   *  @return the loaded gazetteer list
   *  @throws ResourceInstantiationException*/
  public GazetteerListInt loadSingleList(String listName, String Query_Mode)
  throws ResourceInstantiationException {
    VNGazetteerList list = new VNGazetteerList(Query_Mode);
    list.setSeparator(separator);
    try {
      URL lurl = new URL(url,listName);
      list.setURL(lurl);
      list.load();
    }
    catch (IOException ioex) {
      throw new ResourceInstantiationException(ioex);
    }
    return list;
  } // loadSingleList

  /**Gets the lists by node map
   * @return a map of nodes vs lists*/
  public Map getListsByNode(){
    return gazListsByNode;
  }

  /** Gets a map of lists names vs nodes
   *  @return a map of lists names vs nodes*/
  public Map getNodesByListNames() {
     return nodesByList;
  }

  /**Gets the value of the isModified flag.
   * @return true if the definition has been modified    */
  public boolean  isModified() {
    return isModified;
  }

  /**Gets the url of this linear definition
   * @return the url of this linear definition   */
  public URL getURL() {
    return url;
  }


  /**Sets the url of this linear definition
   * @param aUrl the url of this linear definition   */
  public void setURL(URL aUrl) {
    url = aUrl;
  }

  /**
   * Loads linear definition if url is set
   */
    public void load() throws ResourceInstantiationException {
        ArrayList lst = new ArrayList();
            if (ClassQuery == null) {
                throw new ResourceInstantiationException("Query not set (null).");
        }

        VNLinearNode node;

        try {
            QueryResultsTable entity = null;
            if (Query_Mode.equals(vnkim.util.VNConstants.ENTITY_QUERY))
            {
                System.out.println("Loading knowledge base ... ");
                entity = VNQueryAPI.queryHttp(ClassQuery);
            }

            else if (Query_Mode.equals(vnkim.util.VNConstants.LEXICA_QUERY))
            {
                System.out.println("Loading lexical resource ... ");
                entity = VNQueryAPI.queryHttp(Lexica_ClassQuery);
            }

            int rowMax = entity.getRowCount();

            for (int row = 0; row < rowMax; row++) {
                String list = entity.getValue(row, 0).toString();
                String clas = list;

                if (Query_Mode.equals(vnkim.util.VNConstants.ENTITY_QUERY)
                    && VNConstants.lexicaClasses.contains(list))
                continue;

                if (Query_Mode.equals(vnkim.util.VNConstants.LEXICA_QUERY)
                    && VNConstants.entityClasses.contains(list))
                    continue;

                node = new VNLinearNode(list, clas, null, null);
                    this.add(node);
            }

            if (Query_Mode.equals(vnkim.util.VNConstants.ENTITY_QUERY)) {
                for (int i = 0; i < VNConstants.entityClasses.size(); i++) {
                    String list = (String)VNConstants.entityClasses.get(i);
                    String clas = list;
                    node = new VNLinearNode(list, clas, null, null);
                    this.add(node);
                }
                System.out.println("Finish loading knowledge base");
            }

            if (Query_Mode.equals(vnkim.util.VNConstants.LEXICA_QUERY)) {
                for (int i = 0; i < VNConstants.lexicaClasses.size(); i++) {
                    String list = (String)VNConstants.lexicaClasses.get(i);
                    String clas = list;
                    node = new VNLinearNode(list, clas, null, null);
                    this.add(node);
                }
                System.out.println("Finish loading lexical resource");
            }

            isModified = false;
        }
        catch (Exception x){
            throw new ResourceInstantiationException(x);
        }
    } // load();

  /**
   * Stores this to a definition file.
   */
  public void store() throws ResourceInstantiationException{
    if (null == url) {
      throw new ResourceInstantiationException("URL not set.(null)");
    }
    try {
      URL tempUrl = url;
      if (-1 != url.getProtocol().indexOf("gate")) {
        tempUrl = gate.util.protocols.gate.Handler.class.getResource(
                      gate.util.Files.getResourcePath() + url.getPath()
                    );
      } // if gate:path url

      File fileo = new File(tempUrl.getFile());
      fileo.delete();
      BufferedWriter defWriter = new BufferedWriter(new FileWriter(fileo));
      Iterator inodes = nodes.iterator();
      while (inodes.hasNext()) {
        defWriter.write(inodes.next().toString());
        defWriter.newLine();
      }
      defWriter.close();
      isModified = false;
    } catch(Exception x) {
      throw new ResourceInstantiationException(x);
    }

  } // store();

  /**
   * Gets gazetteer lists of this definition.
   * note that a new list is created so the adding and removing of lists will
   * not affect the internal members. Also there is no setLists method since the leading
   * member of the class is nodes, and lists cannot be added individually without being
   * associated with a node.
   * @return a list of the gazetteer lists names
   */
  public List getLists() {
    return new ArrayList(lists);
  }

  /** get the nodes of the definition as a list
   *  @return the list of nodes */
  public List getNodes() {
    return new ArrayList(nodes);
  }


  /** Gets the set of all major types in this definition
   * @return the set of all major types present in this definition*/
  public Set getMajors() {
    Set result = new HashSet();
    for ( int i = 0 ; i < nodes.size() ; i++ )
    {
      String maj = ((VNLinearNode)nodes.get(i)).getClas();
      if (null!= maj)
        result.add(maj);
    }
    return result;
  } // getMajors

  /** Gets the set of all minor types in this definition
   * @return the set of all minor types present in this definition*/
  public Set getMinors() {
    Set result = new HashSet();
    for ( int i = 0 ; i < nodes.size() ; i++ ) {
      String min = ((VNLinearNode)nodes.get(i)).getInst();
      if (null!=min)
        result.add(min);
    }
    result.add("");
    return result;
  } // getMinors()

  /** Gets the set of all languages in this definition
   * @return the set of all languages present in this definition*/
  public Set getLanguages() {
    Set result = new HashSet();
    for ( int i = 0 ; i < nodes.size() ; i++ ) {
      String lang = ((VNLinearNode)nodes.get(i)).getLanguage();
      if (null!=lang)
        result.add(lang);
    }
    result.add("");
    return result;
  } // getMinors()


  /*---implementation of interface java.util.List---*/
  public boolean addAll(int index, Collection c) {
    int size = nodes.size();
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      if (o instanceof VNLinearNode)  {
        add(index,o);
      } // instance of VNLinearNode
    } // while

    boolean result = (size != nodes.size());
    isModified |= result;
    return result;
  }

  public Object get(int index) {
    return nodes.get(index);
  }

  public Object set(int index, Object element) {
    throw new UnsupportedOperationException("this method has not been implemented");
  }

  public void add(int index, Object o) {
    if (o instanceof VNLinearNode) {
      String list = ((VNLinearNode)o).getList();
      if (!nodesByList.containsKey(list)) {
        try {
          VNGazetteerList gl = (VNGazetteerList)loadSingleList(list);
          gazListsByNode.put(o,gl);
          nodes.add(index,o);
          nodesByList.put(list,o);
          lists.add(list);
          isModified = true;
        } catch (ResourceInstantiationException x) {
          // do nothing since the list ain't real
        }
      } // if unique
    } // if a linear node
  }

  public Object remove(int index) {
    Object result = null;
    int size = nodes.size();
    result = nodes.remove(index);
    if (null!=result) {
      String list = ((VNLinearNode)result).getList();
      lists.remove(list);
      nodesByList.remove(list);
      gazListsByNode.remove(result);
      isModified |= (size != nodes.size());
    }
    return result;
  }

  public int indexOf(Object o) {
    return nodes.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return nodes.lastIndexOf(o);
  }

  public ListIterator listIterator() {
    throw new UnsupportedOperationException("this method is not implemented");
  }

  public ListIterator listIterator(int index) {
    throw new UnsupportedOperationException("this method is not implemented");
  }

  public List subList(int fromIndex, int toIndex) {
    return nodes.subList(fromIndex,toIndex);
  } // class SafeIterator

  public int size() {
    return nodes.size();
  }

  public boolean isEmpty() {
    return 0 == nodes.size();
  }

  public boolean contains(Object o) {
    return nodes.contains(o);
  }

  public Iterator iterator() {
    return new SafeIterator();
  }

  public Object[] toArray() {
    return nodes.toArray();
  }

  public Object[] toArray(Object[] a) {
    return nodes.toArray(a);
  }

  /**
   * adds a new node, only if its list is new and uniquely mapped to this node.
   * @param o a node
   * @return true if the list of node is not already mapped with another node.
   */
  public boolean add(Object o) {
    boolean result = false;
    if (o instanceof VNLinearNode) {
      String list = ((VNLinearNode)o).getList();
      if (!nodesByList.containsKey(list)) {
        try {
          VNGazetteerList gl = (VNGazetteerList)loadSingleList(list, Query_Mode);
          gazListsByNode.put(o,gl);
          result = nodes.add(o);
          nodesByList.put(list,o);
          lists.add(list);
          isModified=true;
        } catch (ResourceInstantiationException x) {
          result = false;
        }
      } // if unique
    } // if a linear node
    return result;
  } // add()

  public boolean remove(Object o) {
    boolean result = false;
    int size = nodes.size();
    if (o instanceof VNLinearNode) {
      result = nodes.remove(o);
      String list = ((VNLinearNode)o).getList();
      lists.remove(list);
      nodesByList.remove(list);
      gazListsByNode.remove(o);
      isModified |= (size != nodes.size());
    } // if linear node
    return result;
  }// remove

  public boolean containsAll(Collection c) {
    return nodes.containsAll(c);
  }

  public boolean addAll(Collection c) {
    boolean result = false;
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      if (o instanceof VNLinearNode)  {
        result |= add(o);
      } // instance of VNLinearNode
    } // while
    return result;
  } // addAll()


  public boolean removeAll(Collection c) {
    boolean result = false;
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      result |= remove(o);
    }
    return result;
  }// removeAll()


  public boolean retainAll(Collection c) {
    int aprioriSize = nodes.size();
    List scrap = new ArrayList();

    VNLinearNode node;
    Iterator inodes = nodes.iterator();
    while(inodes.hasNext()) {
      node = (VNLinearNode) inodes.next();
      if (c.contains(node)) {
        scrap.add(node);
      }
    } //for

    removeAll(scrap);
    isModified |= (aprioriSize != nodes.size());
    return (aprioriSize != nodes.size());
  }


  public void clear() {
    nodes.clear();
    lists.clear();
    nodesByList.clear();
    gazListsByNode.clear();
    isModified = true;
  }

  public boolean equals(Object o) {
    boolean result = false;
    if ( o instanceof VNLinearDefinition ) {
      VNLinearDefinition def = (VNLinearDefinition) o;
      result &= nodes.equals(def.nodes);
      result &= lists.equals(def.lists);
      result &= nodesByList.equals(def.lists);
    }// if
    return result;
  } // equals()

 /*---end of implementation of interface java.util.List---*/





/*-----------internal classes -------------*/

 /**SafeIterator class provides an iterator which is safe to be iterated and objects removed from it*/
  private class SafeIterator implements Iterator {
    private Iterator iter = VNLinearDefinition.this.nodes.iterator();
    private boolean removeCalled = false;
    private Object last = null;

    public boolean hasNext() {
      return iter.hasNext();
    }

    public Object next() {
      removeCalled = false;
      last = iter.next();
      return last;
    }

    public void remove() {
      if (!removeCalled && null!=last ) {
        VNLinearDefinition.this.remove(last);
      }// if possible remove
      removeCalled = true;
    } // remove

  } // class SafeIterator

  /**
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }
  
  /**
   * @param separator the separator to set
   */
  public void setSeparator(String separator) {
    this.separator = separator;
  }


} // class VNLinearDefinition
