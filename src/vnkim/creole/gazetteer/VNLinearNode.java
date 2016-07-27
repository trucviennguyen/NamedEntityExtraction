/*
 * LinearNode.java
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

/**Linear node specifies an entry of the type :
 * list:clas:inst:language */
public class VNLinearNode {

  /** the gazetteer list from the node */
  private String list;

  /** the inst type from the node */
  private String inst;

  /** the class type from the node */
  private String clas;

  /** the languages member from the node */
  private String language;

  /**
   * Constructs a linear node given its elements
   * @param aList the gazetteer list file name
   * @param aMajor the clas type
   * @param aMinor the inst type
   * @param aLanguage the language(s)
   */
  public VNLinearNode(String aList, String aClas, String aInst, String aLanguage) {
    list = aList;
    inst = aInst;
    clas = aClas;
    language = aLanguage;
  } // LinearNode construct

  /**Get the gazetteer list filename from the node
   * @return the gazetteer list filename */
  public String getList() {
    return list;
  }

  /**Sets the gazetteer list filename for the node
   * @param aList  the gazetteer list filename*/
  public void setList(String aList) {
    list = aList;
  }

  /** Gets the language of the node (the language is optional)
   *  @return the language of the node */
  public String getLanguage() {
    return language;
  }

  /** Sets the language of the node
   *  @param aLanguage the language of the node */
  public void setLanguage(String aLanguage) {
    language = aLanguage;
  }

  /** Gets the inst type
   *  @return the inst type  */
  public String getInst() {
    return inst;
  }

  /** Sets the inst type
   *  @return the inst type */
  public void setInst(String theInst) {
    inst = theInst;
  }

  /** Gets the clas type
   *  @return the clas type*/
  public String getClas() {
    return clas;
  }

  /** Sets the clas type
   *  @param theClas the clas type */
  public void setClas(String theClas) {
    clas = theClas;
  }

  /**
   * Gets the string representation of this node
   * @return the string representation of this node
   */
  public String toString() {
    String result = list+':'+clas;

    if ( (null!=inst)  && (0 != inst.length()))
      result += ':'+inst;

    if ( (null!=language) && (0 != language.length())) {
      if ((null==inst) || (0 == inst.length()) )
        result +=':';
      result += ':'+language;
    }
    return result;
  }

  /**Checks this node vs another one for equality.
   * @param o another node
   * @return true if languages,list,clas type and inst type match.*/
  public boolean equals(Object o) {
     boolean result = false;
     if ( o instanceof VNLinearNode ) {
      VNLinearNode node = (VNLinearNode) o;
      result = true;

      if (null != this.getLanguage())
        result &= this.getLanguage().equals(node.getLanguage());

      if ( null != this.getList())
        result &= this.getList().equals(node.getList());

      if ( null!=this.getClas())
        result &= this.getClas().equals(node.getClas());

      if ( null!= this.getInst())
        result &= this.getInst().equals(node.getInst());
     }
     return result;
  }

} // class LinearNode
