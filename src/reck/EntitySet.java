/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reck;

import util.Charseq;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public interface EntitySet extends Set {

    /** The size of this set */
    public int size();
    
    /** Returns true if this set contains no elements */
    public  boolean isEmpty();
    
    /** Returns true if this set contains the specified element */
    public  boolean contains(Object o);
    
    /** Get an iterator for this set */
    public Iterator iterator();
    
    /** Returns an array containing all of the elements in this set */
    //public Object[] toArray();
    
    //public Object[] toArray(Object[] a);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public  void add(String id, String type, String subtype, String classAB, 
            Mention[] mentions, Charseq[] attributes, String content);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public  void add(String id, String type, String subtype, String classAB, 
            List<Mention> mentions, List<Charseq> attributes, String content);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public  boolean add(Object o);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public  boolean remove(String id);
    
    /** Remove an element from this set. */
    public  boolean remove(Object o);
    
    /** 
     * Returns true if this set contains all of the elements of the 
     *  specified collection
     */
    // public  boolean containsAll(Collection c);
    
    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation)
     */
    public  boolean addAll(Collection c);
    
    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation)
     */
    // public  boolean retainAll(Collection c);
    
    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation)
     */
    // public  boolean removeAll(Collection c);
    
    /** Removes all of the elements from this set (optional operation) */
    // public void clear();


    // Comparison and hashing

    /** Compares the specified object with this set for equality */
    // public  boolean equals(Object o);

    /** Returns the hash code value for this set */
    // public int hashCode();
    
    /** Get an entity by its Id */
    public Entity getEntityById(String id);
    
    /** Get all entities */
    public EntitySet get();

    /** Select entities by type */
    public EntitySet get(String type);

    /** Select entities by a set of types. Expects a Set of String. */
    public EntitySet get(Set types);

    /** Get the document this set is attached to. */
    public Document getDocument();
}
