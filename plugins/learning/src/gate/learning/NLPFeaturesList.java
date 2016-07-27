/*
 *  NLPFeaturesList.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: NLPFeaturesList.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * NLP feature list. Read it from a file, update it using the new documents, and
 * write back into the file.
 */
public class NLPFeaturesList {
  /** the features with ids, can be accessed by multiple threads. */
  public Hashtable featuresList = null;
  /**
   * Document frequence of each term, useful for document or passage
   * classification
   */
  public Hashtable idfFeatures = null;
  /** Total number of documents used for forming the list. */
  int totalNumDocs;
  /** The unique sysmbol used for the N-gram feature. */
  public final static String SYMBOLNGARM = "<>";

  /** Constructor, get the two hashtables. */
  public NLPFeaturesList() {
    featuresList = new Hashtable();
    idfFeatures = new Hashtable();
    totalNumDocs = 1;
  }

  /** Loading the list from a file. */
  public void loadFromFile(File parentDir, String filename) {
    File fileFeaturesList = new File(parentDir, filename);
    if(fileFeaturesList.exists()) {
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream
          (fileFeaturesList), "UTF-8"));
        // featuresList = new Hashtable();
        String line;
        if((line = in.readLine()) != null)
          totalNumDocs = (new Integer(line.substring(line.lastIndexOf("=") + 1)))
            .intValue();
        while((line = in.readLine()) != null) {
          String[] st = line.split(" ");
          featuresList.put(st[0], st[1]);
          idfFeatures.put(st[0], st[2]);
        }
        in.close();
      } catch(IOException e) {
      }
    } else {
      if(LogService.minVerbosityLevel > 0)
        System.out.println("No feature list file in initialisation phrase.");
    }
  }

  /** Write back the list into the file, with updated information. */
  public void writeListIntoFile(File parentDir, String filename) {
    File fileFeaturesList = new File(parentDir, filename);
    if(LogService.minVerbosityLevel > 1)
      System.out.println("Lengh of List = " + featuresList.size());
    try {
      PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
        fileFeaturesList), "UTF-8"));
      // for the total number of docs
      out.println("totalNumDocs=" + new Integer(totalNumDocs));
      List keys = new ArrayList(featuresList.keySet());
      Collections.sort(keys);
      // write the features list into the output file
      Iterator iterator = keys.iterator();
      while(iterator.hasNext()) {
        Object key = iterator.next();
        out.println(key + " " + featuresList.get(key) + " "
          + idfFeatures.get(key));
        // System.out.println(key+ " " + featuresList.get(key));
      }
      out.close();
    } catch(IOException e) {
    }
  }

  /** Update the NLP features from new documents. */
  public void addFeaturesFromDoc(NLPFeaturesOfDoc fd) {
    long size = featuresList.size();
    for(int i = 0; i < fd.numInstances; ++i) {
      String[] features = fd.featuresInLine[i].toString().split(
        ConstantParameters.ITEMSEPARATOR);
      for(int j = 0; j < features.length; ++j) {
        if(features[j] != null && Pattern.matches((".+\\[[-0-9]+\\]$"), features[j])) {
          int ind = features[j].lastIndexOf('[');
          features[j] = features[j].substring(0,ind);
        }
        String feat = features[j];
        if(feat.contains(SYMBOLNGARM))
          feat = feat.substring(0, feat.lastIndexOf(SYMBOLNGARM));
        if(!feat.equals(ConstantParameters.NAMENONFEATURE)) {
          // If the featureName is not in the feature list
          if(size < ConstantParameters.MAXIMUMFEATURES) {
            if(!featuresList.containsKey(feat)) {
              ++size;
              // features is from 1 (not zero), in the SVM-light
              // format
              featuresList.put(feat, new Long(size));
              idfFeatures.put(feat, new Long(1));
            } else {
              idfFeatures.put(feat, new Long((new Long(idfFeatures.get(feat)
                .toString())).longValue() + 1));
            }
          } else {
            System.out
              .println("There are more NLP features from the training docuemnts");
            System.out.println(" than the pre-defined maximal number"
              + new Long(ConstantParameters.MAXIMUMFEATURES));
            return;
          }
        }
      }
    }// end of the loop on the instances
    // update the total number of docs
    totalNumDocs += fd.numInstances;
  }
}
