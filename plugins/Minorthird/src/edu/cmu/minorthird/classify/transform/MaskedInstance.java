package edu.cmu.minorthird.classify.transform;


import edu.cmu.minorthird.classify.*;
import edu.cmu.minorthird.util.UnionIterator;
import edu.cmu.minorthird.util.gui.Viewer;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;
import java.util.Iterator;


/**
 * @author Edoardo Airoldi
 * Date: Dec 5, 2003
 */

public class MaskedInstance extends AbstractInstance {

	static private Logger log = Logger.getLogger(MaskedInstance.class);
	private Instance instance;
	private Set available;

	public MaskedInstance(Instance instance, TreeMap availableFeatures) {
		this.instance = instance;
		this.available = availableFeatures.keySet();
	}
	public MaskedInstance(Instance instance, Set availableFeatures) {
		this.instance = instance;
		this.available = availableFeatures;
	}
	
	final public Object getSource() { return instance.getSource(); }
	final public String getSubpopulationId() { return instance.getSubpopulationId(); }
  public Viewer toGUI() { return new GUI.InstanceViewer(this); }


	//
	// extend the binary feature set
	//

	public double getWeight(Feature f)
	{
		if (available.contains(f)) return instance.getWeight(f);
		else return 0.0;
	}

	/** Return an iterator over all available features */
	public Feature.Looper featureIterator() 
	{
		TreeSet set = new TreeSet();
		WeightedSet wset = new WeightedSet();
		for (Feature.Looper i=instance.binaryFeatureIterator(); i.hasNext(); ) {
			Feature f = i.nextFeature();
      if (available.contains(f)) { set.add(f); }
		}
		for (Feature.Looper i=instance.numericFeatureIterator(); i.hasNext(); ) {
			Feature f = i.nextFeature();
      if (available.contains(f)) { wset.add( f,instance.getWeight(f) ); }
		}
		return new Feature.Looper( new UnionIterator(set.iterator(), wset.asSet().iterator()) );
	}

  /** Return an iterator over numeric features */
	final public Feature.Looper numericFeatureIterator() 
	{
		WeightedSet wset = new WeightedSet();
		for (Feature.Looper i=instance.numericFeatureIterator(); i.hasNext(); ) {
			Feature f = i.nextFeature();
      if (available.contains(f)) { wset.add( f,instance.getWeight(f) ); }
		}
		return new Feature.Looper( wset.asSet() );
	}

	/** Return an iterator over binary features */
	final public Feature.Looper binaryFeatureIterator() 
	{
		TreeSet set = new TreeSet();
		for (Feature.Looper i=instance.binaryFeatureIterator(); i.hasNext(); ) {
			Feature f = i.nextFeature();
      if (available.contains(f)) { set.add(f); }
		}
		return new Feature.Looper( set );
	}
	
	public String toString() 
	{
		return "[masked "+instance+" av:"+available+"]";
	}

}

