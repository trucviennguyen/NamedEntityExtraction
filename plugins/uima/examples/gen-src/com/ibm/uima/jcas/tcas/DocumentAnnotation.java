

/* First created by JCasGen Fri Jul 13 18:05:50 BST 2007 */
package com.ibm.uima.jcas.tcas;

import com.ibm.uima.jcas.impl.JCas; 
import com.ibm.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Fri Jul 13 18:05:50 BST 2007
 * XML source: /data/raid3/gate/ian/4.0/gate/plugins/uima/examples/conf/uima_descriptors/AllTokenRelatedAnnotators.xml
 * @generated */
public class DocumentAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCas.getNextIndex();
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 
  
  public DocumentAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: language

  /** getter for language - gets 
   * @generated */
  public String getLanguage() {
    if (DocumentAnnotation_Type.featOkTst && ((DocumentAnnotation_Type)jcasType).casFeat_language == null)
      JCas.throwFeatMissing("language", "uima.tcas.DocumentAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentAnnotation_Type)jcasType).casFeatCode_language);}
    
  /** setter for language - sets  
   * @generated */
  public void setLanguage(String v) {
    if (DocumentAnnotation_Type.featOkTst && ((DocumentAnnotation_Type)jcasType).casFeat_language == null)
      JCas.throwFeatMissing("language", "uima.tcas.DocumentAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentAnnotation_Type)jcasType).casFeatCode_language, v);}    
  }

    