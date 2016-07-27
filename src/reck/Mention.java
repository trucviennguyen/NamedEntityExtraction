/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reck;

import util.Charseq;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Mention {
    
    public Entity getEntity();

    public void setEntity(Entity entity);

    public String getId();

    public void setId(String id);
    
    public String getHeadword();

    public void setHeadword(String headword);

    public String getType();

    public void setType(String type);
    
    public String getLDCType();

    public void setLDCType(String ldcType);

    public String getRole();

    public void setRole(String role);

    public String getReference();

    public void setReference(String reference);

    public Charseq getExtent();

    public void setExtent(Charseq extent);

    public Charseq getHead();

    public void setHead(Charseq head);
    
    public Charseq getHwPosition();

    public void setHwPosition(Charseq hwPosition);
    
    /**
     * Says if the entity mention is matched exactly range start & end
     * @return
     */
    public boolean matchedRange(Long start, Long end);
    
}
