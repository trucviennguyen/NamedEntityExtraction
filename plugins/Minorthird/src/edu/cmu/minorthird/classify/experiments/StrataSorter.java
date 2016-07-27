package edu.cmu.minorthird.classify.experiments;



import edu.cmu.minorthird.classify.Example;
import edu.cmu.minorthird.classify.algorithms.random.RandomElement;
import org.apache.log4j.Logger;

import java.util.*;



/**

 * Helper class for splitting up iterators over Instances, by class.

 *

 * @author Edoardo Airoldi

 * Date: Dec 8, 2003

 */



class StrataSorter {



    static private Logger log = Logger.getLogger(StrataSorter.class);



    private Map labelMap;   // map label-Id's to arrayLists of examples

    private List strataList;



    /** Create a StrataSorter. Iterator i must iterate over

     * Instances. */

    public StrataSorter(Iterator i)

    {

        this(new RandomElement(),i);

    }



    /** Create a StrataSorter. Iterator i must iterate over

     * Instances. */

    public StrataSorter(RandomElement random,Iterator i)

    {

        labelMap = new TreeMap();

        while (i.hasNext()) {

            Object o = i.next();

            String id = null;

            //System.out.println( o.getClass() );

            id = ((Example)o).getLabel().bestClassName();

            if (id==null) System.out.println("Error: unlabeled example!");

            ArrayList list = (ArrayList)labelMap.get(id);

            if (list==null) labelMap.put(id, (list=new ArrayList()));

            //System.out.println( o.toString() );

            list.add( o );

        }

        strataList = new ArrayList( labelMap.keySet().size() );

        for (Iterator j=labelMap.keySet().iterator(); j.hasNext(); ) {

            Object key = j.next();

            Collections.shuffle( (ArrayList)labelMap.get(key),new Random(0) );

            //System.out.println( key.toString() );

            strataList.add( key );

        }

        Collections.shuffle( strataList,new Random(0) );

    }



    /** Return an iterator over lists of strata.

     * The strata, and the lists of Instances within

     * each stratus, are randomly ordered.

     */

    public Iterator strataIterator() {

        return new StrataSorter.HisIterator(0); // 'His' as in William's Iterator  =:-)

    }



    private class HisIterator implements Iterator {

        private int i;

        public HisIterator(int i) {this.i = i;}

        public boolean hasNext() { return i<strataList.size(); }

        public Object next() { return labelMap.get( strataList.get(i++) ); }

        public void remove() { throw new UnsupportedOperationException("can't remove"); }

    }

}

