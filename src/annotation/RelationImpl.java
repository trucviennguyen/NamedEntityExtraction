/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.jdom.Element;
import reck.Document;
import reck.Entity;
import reck.Mention;
import reck.Relation;
import reck.Relation.RelationMentionTime;
import util.RECKConstants;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public class RelationImpl implements Relation, Cloneable, Serializable {
    
    public RelationImpl(String id, String type, String subtype) {
        this.id = id;
        this.type = type;
        this.subtype = subtype;
    }
    
    public RelationImpl(String type, Mention mention1, Mention mention2) {
        this.type = type;
        this.mention1 = mention1;
        this.mention2 = mention2;

        if (!RECKConstants.mentionOrder(mention1, mention2)) {
            Mention tm = mention1;
            mention1 = mention2;
            mention2 = tm;
        }
        
        this.entity1 = mention1.getEntity();
        this.entity2 = mention2.getEntity();
    }
    
    public RelationImpl(String id, String type, String subtype,
            Entity entity1, Entity entity2, Mention mention1, Mention mention2) {

        this.id = id;
        this.type = type;
        this.subtype = subtype;
        
        if (!RECKConstants.mentionOrder(mention1, mention2)) {
            Mention tm = mention1;
            mention1 = mention2;
            mention2 = tm;
            
            Entity te = entity1;
            entity1 = entity2;
            entity2 = te;
        }

        this.mention1 = mention1;
        this.mention2 = mention2;
        this.entity1 = entity1;
        this.entity2 = entity2;
    }
    
    public RelationImpl(Document doc, Element relation) {
        this.id = relation.getAttributeValue("ID");
        this.type = relation.getAttributeValue("TYPE");
        this.subtype = relation.getAttributeValue("SUBTYPE");
        
        Iterator entityIter = relation.getChildren("rel_entity_arg").iterator();
        String entityId1 = ((Element)entityIter.next()).getAttributeValue("ENTITYID");
        String entityId2 = ((Element)entityIter.next()).getAttributeValue("ENTITYID");
        
        this.entity1 = doc.getEntities().getEntityById(entityId1);
        this.entity2 = doc.getEntities().getEntityById(entityId2);
        
        // Consider only explicit relations
        if (relation.getChild("relation_mention") != null) {
        
            Iterator mentionIter = relation.getChild("relation_mention").getChildren("rel_mention_arg").iterator();
            String mentionId1 = ((Element)mentionIter.next()).getAttributeValue("ENTITYMENTIONID");
            String mentionId2 = ((Element)mentionIter.next()).getAttributeValue("ENTITYMENTIONID");

            this.mention1 = doc.getMentions().getMentionById(mentionId1);
            this.mention2 = doc.getMentions().getMentionById(mentionId2);
            
            if (!RECKConstants.mentionOrder(mention1, mention2)) {
                Mention tm = mention1;
                mention1 = mention2;
                mention2 = tm;
                
                Entity te = entity1;
                entity1 = entity2;
                entity2 = te;
            }

        }
        
    }
    
    public RelationImpl(Document doc, String id, String type, String subtype, Entity entity1, Entity entity2, Element relMention) {
        this.id = id;
        this.type = type;
        this.subtype = subtype;
        
        this.entity1 = entity1;
        this.entity2 = entity2;
        
        this.mention_id = relMention.getAttributeValue("ID");
        this.lexical_condition = relMention.getAttributeValue("LDCLEXICALCONDITION");
        
        // Consider only explicit relations
        Iterator mentionIter = relMention.getChildren("rel_mention_arg").iterator();
        String mentionId1 = ((Element)mentionIter.next()).getAttributeValue("ENTITYMENTIONID");
        String mentionId2 = ((Element)mentionIter.next()).getAttributeValue("ENTITYMENTIONID");
        
        this.mention1 = doc.getMentions().getMentionById(mentionId1);
        this.mention2 = doc.getMentions().getMentionById(mentionId2);
        
        if (!RECKConstants.mentionOrder(mention1, mention2)) {
            Mention tm = mention1;
            mention1 = mention2;
            mention2 = tm;
            
            Entity te = entity1;
            entity1 = entity2;
            entity2 = te;
        }
        
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getMentionId() {
        return mention_id;
    }
    
    public void setMentionId(String mention_id) {
        this.mention_id = mention_id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSubtype() {
        return subtype;
    }
    
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    
    public String getTypeSubtype() {
        return type + " " + subtype;
    }
    
    public Entity getEntity(int entityIndex) {
        if (entityIndex == 0)
            return entity1;
        return entity2;
    }
    
    public void setEntity(int entityIndex, EntityImpl e) {
        if (entityIndex == 0)
            this.entity1 = e;
        else
            this.entity2 = e;
    }    

    public Mention getMention(int mentionIndex) {        
        if (mentionIndex == 0)
            return mention1;
        return mention2;
    }
    
    public void setMention(int mentionIndex, MentionImpl mention) {
        if (mentionIndex == 0)
            this.mention1 = mention;
        else
            this.mention2 = mention;        
    }
    
    public RelationMentionTime getMentionTime() {
        return mentionTime;
    }
    
    public void setMentionTime(RelationMentionTime mentionTime){
        this.mentionTime = mentionTime;
    }

    String id = null;
    String mention_id = null;
    String lexical_condition = null;
    String type = null;
    String subtype = null;
    Entity entity1 = null;
    Entity entity2 = null;
    Mention mention1 = null;
    Mention mention2 = null;
    RelationMentionTime mentionTime = null;

}
