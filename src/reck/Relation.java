/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reck;

import java.io.Serializable;
import java.util.ArrayList;
import annotation.EntityImpl;
import annotation.MentionImpl;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Relation {

    public String getId();
    public void setId(String id);
    public String getMentionId();
    public void setMentionId(String mention_id);
    public String getType();
    public void setType(String type);
    public String getSubtype();
    public void setSubtype(String subtype);
    public String getTypeSubtype();
    public Entity getEntity(int entityIndex);
    public void setEntity(int entityIndex, EntityImpl e);
    public Mention getMention(int mentionIndex);
    public void setMention(int mentionIndex, MentionImpl mention);
    public RelationMentionTime getMentionTime();
    public void setMentionTime(RelationMentionTime mentionTime);
    
    public class RelationMentionTime implements Cloneable, Serializable{

        public RelationMentionTime(String type, String val, String mod, String dir) {
            this.type = type;
            this.val = val;
            this.mod = mod;
            this.dir = dir;
        }

        String type;
        String val;
        String mod;
        String dir;
    }
}
