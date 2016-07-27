/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package annotation;

import java.io.Serializable;
import reck.Entity;
import util.Charseq;
import reck.Mention;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public class MentionImpl implements Mention, Cloneable, Serializable {

    public MentionImpl(Entity entity) {
        this.entity = entity;
    }
    
    public MentionImpl(Entity entity, String id, String headword, String type, String ldcType, 
            String role, String reference, Charseq extent, Charseq head, Charseq hwPosition) {
        this(entity);
        this.id = id;
        this.headword = headword;
        this.type = type;
        this.ldcType = ldcType;
        this.role = role;
        this.reference = reference;
        this.extent = extent;
        this.head = head;
        this.hwPosition = hwPosition;
    }

    public MentionImpl(Entity entity, String id, String headword, String type, String ldcType, 
            Charseq extent, Charseq head, Charseq hwPosition) {
        this(entity);
        this.id = id;
        this.headword = headword;
        this.ldcType = ldcType;
        this.type = type;
        this.extent = extent;
        this.head = head;
        this.hwPosition = hwPosition;
    }
    
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getHeadword() {
        return headword;
    }

    public void setHeadword(String headword) {
        this.headword = headword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getLDCType() {
        return ldcType;
    }

    public void setLDCType(String ldcType) {
        this.ldcType = ldcType;
    }

    public String getRole() {
        return id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Charseq getExtent() {
        return extent;
    }

    public void setExtent(Charseq extent) {
        this.extent = extent;
    }

    public Charseq getHead() {
        return head;
    }

    public void setHead(Charseq head) {
        this.head = head;
    }
    
    public Charseq getHwPosition() {
        return hwPosition;
    }

    public void setHwPosition(Charseq hwPosition) {
        this.hwPosition = hwPosition;
    }
    
    /**
     * Says if the entity mention is matched within range start & end
     * @return
     */
    public boolean matchedRange(Long start, Long end) {
        return ( (head.getStart().longValue() == start.longValue()
                && head.getEnd().longValue() == end.longValue()) 
                || (extent.getStart().longValue() == start.longValue()
                && extent.getEnd().longValue() == end.longValue()) );
/*        return ( (extent.getStart().longValue() == start.longValue()
                && extent.getEnd().longValue() == end.longValue()) );*/
    }

    Entity entity = null;
    String id = null;
    String headword = null;
    String type = null;
    String ldcType = null;
    String role = null;
    String reference = null;
    Charseq extent = new Charseq();
    Charseq head = new Charseq();
    Charseq hwPosition = new Charseq();
}
