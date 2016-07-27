/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.Serializable;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public class Charseq implements Cloneable, Serializable {
    
    public Charseq() {
        this.start = new Long(-1);
        this.end = new Long(-1);
    }
    
    public Charseq(Long start, Long end) {
        this.start = start;
        this.end = end;        
    }
    
    public Charseq(String start, String end) {
        this.start = new Long(start);
        this.end = new Long(end);
    }
    
    public Charseq(int start, int end) {
        this.start = new Long((long)start);
        this.end = new Long((long)end);
    }
    
    public Long getStart() {
        return start;
    }
    
    public Long getEnd() {
        return end;
    }
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } 
        else if (!(o instanceof Charseq)) {
            return false;
        }
        
        Charseq pos = (Charseq) o;
        if (!(start.equals(pos.getStart()))) {
            return false;
        }
        
        if (!(end.equals(pos.getEnd()))) {
            return false;
        }
        
        return true;
    }
    
    public Charseq clone() {
        return new Charseq(start, end);
    }
    
    public int length() {
        return (int) (end - start + 1);
    }
    
    /**
     * start position
     *
     */
    Long start = null;
    
    /**
     * end position
     *
     */
    Long end = null;
}
