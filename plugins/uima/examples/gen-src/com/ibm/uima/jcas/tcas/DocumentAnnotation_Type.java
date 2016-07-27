
/* First created by JCasGen Fri Jul 13 18:05:50 BST 2007 */
package com.ibm.uima.jcas.tcas;

import com.ibm.uima.jcas.impl.JCas;
import com.ibm.uima.cas.impl.CASImpl;
import com.ibm.uima.cas.impl.FSGenerator;
import com.ibm.uima.cas.FeatureStructure;
import com.ibm.uima.cas.impl.TypeImpl;
import com.ibm.uima.cas.Type;
import com.ibm.uima.cas.impl.FeatureImpl;
import com.ibm.uima.cas.Feature;

/** 
 * Updated by JCasGen Fri Jul 13 18:05:50 BST 2007
 * @generated */
public class DocumentAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;};
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentAnnotation(addr, DocumentAnnotation_Type.this);
  			   DocumentAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentAnnotation(addr, DocumentAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DocumentAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCas.getFeatOkTst("uima.tcas.DocumentAnnotation");
 
  /** @generated */
  final Feature casFeat_language;
  /** @generated */
  final int     casFeatCode_language;
  /** @generated */ 
  public String getLanguage(int addr) {
        if (featOkTst && casFeat_language == null)
      JCas.throwFeatMissing("language", "uima.tcas.DocumentAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_language);
  }
  /** @generated */    
  public void setLanguage(int addr, String v) {
        if (featOkTst && casFeat_language == null)
      JCas.throwFeatMissing("language", "uima.tcas.DocumentAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_language, v);}
    
  


  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_language = jcas.getRequiredFeatureDE(casType, "language", "uima.cas.String", featOkTst);
    casFeatCode_language  = (null == casFeat_language) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_language).getCode();

  }
}



    