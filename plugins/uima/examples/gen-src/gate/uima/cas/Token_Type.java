
/* First created by JCasGen Fri Jul 13 18:05:50 BST 2007 */
package gate.uima.cas;

import com.ibm.uima.jcas.impl.JCas;
import com.ibm.uima.cas.impl.CASImpl;
import com.ibm.uima.cas.impl.FSGenerator;
import com.ibm.uima.cas.FeatureStructure;
import com.ibm.uima.cas.impl.TypeImpl;
import com.ibm.uima.cas.Type;
import com.ibm.uima.cas.impl.FeatureImpl;
import com.ibm.uima.cas.Feature;
import com.ibm.uima.jcas.tcas.Annotation_Type;

/** A token in text
 * Updated by JCasGen Fri Jul 13 18:05:50 BST 2007
 * @generated */
public class Token_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;};
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Token_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Token_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Token(addr, Token_Type.this);
  			   Token_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Token(addr, Token_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Token.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCas.getFeatOkTst("gate.uima.cas.Token");
 
  /** @generated */
  final Feature casFeat_String;
  /** @generated */
  final int     casFeatCode_String;
  /** @generated */ 
  public String getString(int addr) {
        if (featOkTst && casFeat_String == null)
      JCas.throwFeatMissing("String", "gate.uima.cas.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_String);
  }
  /** @generated */    
  public void setString(int addr, String v) {
        if (featOkTst && casFeat_String == null)
      JCas.throwFeatMissing("String", "gate.uima.cas.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_String, v);}
    
  
 
  /** @generated */
  final Feature casFeat_LowerCaseLetters;
  /** @generated */
  final int     casFeatCode_LowerCaseLetters;
  /** @generated */ 
  public int getLowerCaseLetters(int addr) {
        if (featOkTst && casFeat_LowerCaseLetters == null)
      JCas.throwFeatMissing("LowerCaseLetters", "gate.uima.cas.Token");
    return ll_cas.ll_getIntValue(addr, casFeatCode_LowerCaseLetters);
  }
  /** @generated */    
  public void setLowerCaseLetters(int addr, int v) {
        if (featOkTst && casFeat_LowerCaseLetters == null)
      JCas.throwFeatMissing("LowerCaseLetters", "gate.uima.cas.Token");
    ll_cas.ll_setIntValue(addr, casFeatCode_LowerCaseLetters, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Kind;
  /** @generated */
  final int     casFeatCode_Kind;
  /** @generated */ 
  public String getKind(int addr) {
        if (featOkTst && casFeat_Kind == null)
      JCas.throwFeatMissing("Kind", "gate.uima.cas.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Kind);
  }
  /** @generated */    
  public void setKind(int addr, String v) {
        if (featOkTst && casFeat_Kind == null)
      JCas.throwFeatMissing("Kind", "gate.uima.cas.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_Kind, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Orth;
  /** @generated */
  final int     casFeatCode_Orth;
  /** @generated */ 
  public String getOrth(int addr) {
        if (featOkTst && casFeat_Orth == null)
      JCas.throwFeatMissing("Orth", "gate.uima.cas.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Orth);
  }
  /** @generated */    
  public void setOrth(int addr, String v) {
        if (featOkTst && casFeat_Orth == null)
      JCas.throwFeatMissing("Orth", "gate.uima.cas.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_Orth, v);}
    
  


  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Token_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_String = jcas.getRequiredFeatureDE(casType, "String", "uima.cas.String", featOkTst);
    casFeatCode_String  = (null == casFeat_String) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_String).getCode();

 
    casFeat_LowerCaseLetters = jcas.getRequiredFeatureDE(casType, "LowerCaseLetters", "uima.cas.Integer", featOkTst);
    casFeatCode_LowerCaseLetters  = (null == casFeat_LowerCaseLetters) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_LowerCaseLetters).getCode();

 
    casFeat_Kind = jcas.getRequiredFeatureDE(casType, "Kind", "uima.cas.String", featOkTst);
    casFeatCode_Kind  = (null == casFeat_Kind) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Kind).getCode();

 
    casFeat_Orth = jcas.getRequiredFeatureDE(casType, "Orth", "uima.cas.String", featOkTst);
    casFeatCode_Orth  = (null == casFeat_Orth) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Orth).getCode();

  }
}



    