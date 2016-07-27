/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reck;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Corpus extends List {

    public int size();

    public boolean isEmpty();

    public boolean contains(Object o);

    public Iterator iterator();

    public Object[] toArray();

    public Object[] toArray(Object[] a);

    public boolean add(Object o);

    public boolean remove(Object o);

    public boolean containsAll(Collection c);

    public boolean addAll(Collection c);

    public boolean addAll(int index, Collection c);

    public boolean removeAll(Collection c);

    public boolean retainAll(Collection c);

    public void clear();

    public boolean equals(Object o);

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
}
