/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reck;

import java.util.ArrayList;
import util.Charseq;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public interface MentionList extends List {

    /** The size of this set */
    public int size();
    
    /** Returns true if this set contains no elements */
    public  boolean isEmpty();
    
    /** Returns true if this set contains the specified element */
    public  boolean contains(Object o);
    
    /** Get an iterator for this set */
    public Iterator iterator();
    
    /** Returns an array containing all of the elements in this set */
    public Object[] toArray();
    
    public Object[] toArray(Object[] a);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public  void add(Entity entity, String id, String headword, String type, String ldcType, 
            String role, String reference, Charseq extent, Charseq head, Charseq hwPosition);
    
    /** Add an existing entity. Returns true when the set is modified. */
    public  void add(Entity entity, String id, String headword, String type, String ldcType, 
            Charseq extent, Charseq head, Charseq hwPosition);
    
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
    public  boolean containsAll(Collection c);
    
    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation)
     */
    public  boolean addAll(Collection c);
    public  boolean addAll(int index, Collection c);
    
    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation)
     */
    public  boolean removeAll(Collection c);
    
    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation)
     */
    public  boolean retainAll(Collection c);
    
    /** Removes all of the elements from this set (optional operation) */
    public void clear();


    // Comparison and hashing

    /** Compares the specified object with this set for equality */
    public  boolean equals(Object o);

    /** Returns the hash code value for this set */
    public int hashCode();

    public Object get(int index);

    public Object set(int index, Object element);

    public void add(int index, Object element);

    public Object remove(int index);

    public int indexOf(Object o);

    public int lastIndexOf(Object o);

    public ListIterator listIterator();

    public ListIterator listIterator(int index);

    public Corpus subList(int fromIndex, int toIndex);
    
    /** Get an entity by its Id */
    public Mention getMentionById(String id);
    
    /** Get all entities */
    public MentionList get();

    /** Select entities by type */
    public MentionList get(String type);

    /** Select entities by a set of types. Expects a Set of String. */
    public MentionList get(Set types);
    
    /** Select entity mention within start and end */
    public ArrayList getExact(Long start, Long end);

    /** Get the document this set is attached to. */
    public Document getDocument();
}
