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
 * value:inst:language */
public class VNLinearInst {

  /** the value from the node */
  public String value;

  /** the inst type from the node */
  public String inst;

  /** the languages member from the node */
  private String language;

  /**
   * Constructs a linear node given its elements
   * @param aList the gazetteer list file name
   * @param aMajor the value type
   * @param aMinor the inst type
   * @param aLanguage the language(s)
   */
  public VNLinearInst(String aValue, String aInst, String aLanguage) {
    inst = aInst;
    value = aValue;
    language = aLanguage;
  } // LinearNode construct

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

  /** Gets the value type
   *  @return the value type*/
  public String getClas() {
    return value;
  }

  /** Sets the value type
   *  @param theClas the value type */
  public void setClas(String theClas) {
    value = theClas;
  }

  /**
   * Gets the string representation of this node
   * @return the string representation of this node
   */
  public String toString() {
    String result = value;

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
   * @return true if languages,list,value type and inst type match.*/
  public boolean equals(Object o) {
     boolean result = false;
     if ( o instanceof VNLinearInst ) {
      VNLinearInst node = (VNLinearInst) o;
      result = true;

      if (null != this.getLanguage())
        result &= this.getLanguage().equals(node.getLanguage());

      if ( null!=this.getClas())
        result &= this.getClas().equals(node.getClas());

      if ( null!= this.getInst())
        result &= this.getInst().equals(node.getInst());
     }
     return result;
  }

} // class LinearNode
