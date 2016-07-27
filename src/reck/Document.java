/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reck;

import java.net.URL;
import annotation.EntitySetImpl;
import annotation.MentionListImpl;
import annotation.RelationSetImpl;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public interface Document {
    
    /** Get the content of the document. */
    public String getNoTaggedContent();
    
    /** Get the content of the document. */
    public String getRawContent();
    
    /** Get the content of the document. */
    public String getTaggedContent();
    
    /** Set the content of the document. */
    public void setTaggedContent(String content);
    
    /** Get the text filename of the document. */
    public String getTextFilename();
    
    /** Get the URI of the document. */
    public String getURI();
    
    /** Set the URI of the document. */
    public void setURI(String URI);
    
    /** Get the source of the document. */
    public String getSource();
    
    /** Set the content of the document. */
    public void setSource(String source);
    
    /** Get the type of the document. */
    public String getType();
    
    /** Set the type of the document. */
    public void setType(String type);
    
    /** Get the version of the document. */
    public String getVersion();
    
    /** Set the version of the document. */
    public void setVersion(String version);
    
    /** Get the author of the document. */
    public String getAuthor();
    
    /** Set the author of the document content source */
    public void setAuthor(String author);
    
    /** Get the encoding of the document. */
    public String getEncoding();
    
    /** Set the encoding of the document content source */
    public void setEncoding(String encoding);
    
    /** Get the docId of the document. */
    public String getDocId();
    
    /** Set the docId of the document content source */
    public void setDocId(String docId);
 
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public EntitySet getEntities();    

    /**
     * Set the default set of entities
     */
    public void setDefaultEntities(EntitySetImpl defaultEntities);
    
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public MentionList getMentions();  

    /**
     * Set the default set of entities
     */
    public void setDefaultMentions(MentionListImpl defaultMentions);
    
    /** Get the default set of relations. The set is created if it
     * doesn't exist yet.
     */
    public RelationSet getRelations();

    /**
     * Set the default set of relations
     */
    public void setDefaultRelations(RelationSetImpl defaultRelations);
    
    /** Clear all the data members of the object. */
    public void cleanup();
    
    public String readTaggedContentFromFile(URL u, String encoding);
    
    public String toNoTaggedContent(String taggedContent);
    
    public void exportContentToFile(String outputFilename, String fileContent, String encoding);

}
