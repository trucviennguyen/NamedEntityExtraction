/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package corpora;

import edu.stanford.nlp.ling.HasWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.jdom.JDOMException;

import reck.MentionList;
import reck.EntitySet;
import reck.RelationSet;
import annotation.EntitySetImpl;
import annotation.MentionListImpl;
import annotation.RelationSetImpl;
import extract.FeatureExtractor;
import util.RECKConstants;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public class DocumentImpl implements reck.Document, Cloneable, Serializable {

    public DocumentImpl(String taggedContent, String URI, String source, String type, 
            String version, String author, String encoding, String docId, 
            EntitySet entities, RelationSet relations) {
        this.taggedContent = taggedContent;
        this.URI = URI;
        this.source = source;
        this.type = type;
        this.version = version;
        this.author = author;
        this.encoding = encoding;
        this.docId = docId;
        this.defaultEntities = entities;
        this.defaultMentions = new MentionListImpl(this);
        this.defaultRelations = relations;
    }

    public DocumentImpl(URL u) {
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            Document doc = builder.build(u.openStream());
            Element source_file = doc.getRootElement();
            Element docElement = (org.jdom.Element) source_file.getChild("document");
            
            this.URI = source_file.getAttributeValue("URI");
            this.source = source_file.getAttributeValue("SOURCE");
            this.type = source_file.getAttributeValue("TYPE");
            this.version = source_file.getAttributeValue("VERSION");
            this.author = source_file.getAttributeValue("AUTHOR");
            this.encoding = source_file.getAttributeValue("ENCODING");
            this.docId = docElement.getAttributeValue("DOCID");
            
            String st = u.toString();
            st = st.substring(0, st.lastIndexOf("/") + 1) + source_file.getAttributeValue("URI");
            URI sourceURI = new URI(st);
            this.taggedContent = readTaggedContentFromFile(sourceURI.toURL(), encoding);
            this.noTaggedContent = toNoTaggedContent(taggedContent);
            
            this.defaultEntities = new EntitySetImpl(this, docElement);
            this.defaultMentions = new MentionListImpl(this);
            this.defaultRelations = new RelationSetImpl(this, docElement);            
        }
        catch (JDOMException jdomEx) {
            jdomEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        catch (java.net.URISyntaxException uriEx) {
            uriEx.printStackTrace();
        }
    }
    
    public DocumentImpl(File fs, FeatureExtractor extractor) {
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            Document doc = builder.build(fs);
            Element source_file = doc.getRootElement();
            Element docElement = (org.jdom.Element) source_file.getChild("document");
            
            this.URI = source_file.getAttributeValue("URI");
            this.source = source_file.getAttributeValue("SOURCE");
            this.type = source_file.getAttributeValue("TYPE");
            this.version = source_file.getAttributeValue("VERSION");
            this.author = source_file.getAttributeValue("AUTHOR");
            this.encoding = source_file.getAttributeValue("ENCODING");
            this.docId = docElement.getAttributeValue("DOCID");
            
            String st = fs.getCanonicalPath();
            st = st.substring(0, st.lastIndexOf("\\") + 1);
            
            URI = URI.replace('.', '_').replaceAll("_sgm", ".SGM");
            
            String sourceFilename = st + URI;
            this.taggedContent = readTaggedContentFromFile(new File(sourceFilename).toURI().toURL(), encoding);
            this.noTaggedContent = toNoTaggedContent(taggedContent);
            this.textFilename = 
                    sourceFilename.substring(0, sourceFilename.lastIndexOf(".")) 
                    + ".txt";
            this.exportContentToFile(textFilename, rawContent, encoding);
            
            this.defaultEntities = new EntitySetImpl(this, docElement);
            this.defaultMentions = new MentionListImpl(this);
            this.defaultRelations = new RelationSetImpl(this, docElement);

            ArrayList entityList = new ArrayList(defaultMentions);
            entityList = RECKConstants.sortEntityList(entityList);
            File df = new File(textFilename);
            extractor.extractOneFile(df, entityList);

        }
        catch (gate.util.GateException ge) {
            ge.printStackTrace();
        }
        catch (JDOMException jdomEx) {
            jdomEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
    
    public DocumentImpl(Element source_file) {
        Element docElement = (org.jdom.Element) source_file.getChild("document");
        this.URI = source_file.getAttributeValue("URI");
        this.source = source_file.getAttributeValue("SOURCE");
        this.type = source_file.getAttributeValue("TYPE");
        this.version = source_file.getAttributeValue("VERSION");
        this.author = source_file.getAttributeValue("AUTHOR");
        this.encoding = source_file.getAttributeValue("ENCODING");
        this.docId = docElement.getAttributeValue("DOCID");

        File fs = new File(URI);
        try {
            this.taggedContent = readTaggedContentFromFile(fs.toURI().toURL(), encoding);
            this.noTaggedContent = toNoTaggedContent(taggedContent);
            this.defaultEntities = new EntitySetImpl(this, docElement);
            this.defaultMentions = new MentionListImpl(this);
            this.defaultRelations = new RelationSetImpl(this, docElement);
        }
        catch (MalformedURLException urlEx) {
            urlEx.printStackTrace();
        }
    }
    
    /** Get the content of the document. */
    public String getTaggedContent() {
        return taggedContent;
    } // getContent()
    
    /** Get the content of the document. */
    public String getNoTaggedContent() {
        return noTaggedContent;
    } // getContent()
    
    /** Get the content of the document. */
    public String getRawContent() {
        return rawContent;
    } // getContent()
    
    /** Set the content of the document. */
    public void setTaggedContent(String taggedContent) {
        this.taggedContent = taggedContent;
    } // setContent()
    
    /** Get the text filename of the document. */
    public String getTextFilename() {
        return textFilename;
    } // getTextFilename()
    
    /** Get the URI of the document. */
    public String getURI() {
        return URI;
    } // getURI()
    
    /** Set the URI of the document. */
    public void setURI(String URI) {
        this.URI = URI;
    } // setURI()
    
    /** Get the source of the document. */
    public String getSource() {
        return source;
    } // getSource()
    
    /** Set the content of the document. */
    public void setSource(String source) {
        this.source = source;
    } // setSource()
    
    /** Get the type of the document. */
    public String getType() {
        return type;
    } // getType()
    
    /** Set the type of the document. */
    public void setType(String type) {
        this.type = type;
    } // setType()
    
    /** Get the version of the document. */
    public String getVersion() {
        return version;
    } // getVersion()
    
    /** Set the version of the document. */
    public void setVersion(String version) {
        this.version = version;
    } // setVersion()
    
    /** Get the author of the document. */
    public String getAuthor() {
        return author;
    } // getAuthor()
    
    /** Set the author of the document content source */
    public void setAuthor(String author) {
        this.author = author;
    } // setAuthor()
    
    /** Get the encoding of the document. */
    public String getEncoding() {
        return encoding;
    } // getEncoding()
    
    /** Set the encoding of the document content source */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    } // setEncoding()
    
    /** Get the docId of the document. */
    public String getDocId() {
        return docId;
    } // getDocId()
    
    /** Set the docId of the document content source */
    public void setDocId(String docId) {
        this.docId = docId;
    } // setDocId()
 
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public EntitySet getEntities() {
        if( defaultEntities == null){
            defaultEntities = new EntitySetImpl(this);
        }//if
        return defaultEntities;
    } // getEntities()
    
    /**
     * Set the default set of entities
     */
    public void setDefaultEntities(EntitySetImpl defaultEntities) {
        this.defaultEntities = defaultEntities;
    }
 
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public MentionList getMentions() {
        if( defaultMentions == null){
            defaultMentions = new MentionListImpl(this);
        }//if
        return defaultMentions;
    } // getEntities()
    
    /**
     * Set the default set of entities
     */
    public void setDefaultMentions(MentionListImpl defaultMentions) {
        this.defaultMentions = defaultMentions;
    }
    
    /** Get the default set of entities. The set is created if it
     * doesn't exist yet.
     */
    public ArrayList getTreeList() {
        return defaultTreeList;
    }

    /**
     * Set the default set of entities
     */
    public void setDefaultTreeList(ArrayList defaultTreeList) {
        this.defaultTreeList = defaultTreeList;
    }
    
    /** Get the default set of relations. The set is created if it
     * doesn't exist yet.
     */
    public RelationSet getRelations() {
        if( defaultRelations == null){
            defaultRelations = new RelationSetImpl(this);
        }//if
        return defaultRelations;
    } // getEntities()
    
    /**
     * Set the default set of relations
     */
    public void setDefaultRelations(RelationSetImpl defaultRelations) {
        this.defaultRelations = defaultRelations;
    }
    
    /** Clear all the data members of the object. */
    public void cleanup() {
        defaultEntities = null;
        defaultRelations = null;
    } // cleanup()
    
    public String readTaggedContentFromFile(URL u, String encoding) {
        String docContent = "";

        try {
            int readLength = 0;
            char[] readBuffer = new char[RECKConstants.INTERNAL_BUFFER_SIZE];

            BufferedReader uReader = null;
            StringBuffer buf = new StringBuffer();
            char c;
            long toRead = Long.MAX_VALUE;
            
            if(encoding != null && !encoding.equalsIgnoreCase("")) {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream(), encoding), RECKConstants.INTERNAL_BUFFER_SIZE);
            } 
            else {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream()), RECKConstants.INTERNAL_BUFFER_SIZE);
            }
            
            // read gtom source into buffer
            while (toRead > 0 && (readLength = uReader.read(readBuffer, 0, RECKConstants.INTERNAL_BUFFER_SIZE)) != -1) {
                if (toRead <  readLength) {
                    //well, if toRead(long) is less than readLenght(int)
                    //then there can be no overflow, so the cast is safe
                    readLength = (int)toRead;
                }

                buf.append(readBuffer, 0, readLength);
                toRead -= readLength;
            }

            // 4.close reader
            uReader.close();

            docContent = new String(buf);
        }
        catch (java.net.MalformedURLException urlEx) {
            urlEx.printStackTrace();            
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

        return docContent;
    }
    
    public static String trimUnrealReturns(String st) {
        String ret = st, text = st, pre = "";
        while (text.contains("\n")) {
            int i = text.indexOf("\n");
            
            if ( (i < text.length() - 1) && (text.charAt(i + 1) == '\n') ) {
                pre = pre + text.substring(0, i + 2);
                text = text.substring(i + 2);
            }
            else {

                // char before return is a letter
                if (pre.length() + i >= startSentence) {
                    text = text.substring(0, i) + " " + text.substring(i + 1);
                    ret = pre + text;
                }
                else {
                    pre = pre + text.substring(0, i + 1);
                    text = text.substring(i + 1);
                }

            }

        }
        
        return ret;
    }

    public String toNoTaggedContent(String taggedContent) {
        String noTaggedCT = "";
        int startRawText = -1, endRawText = -1;
        
        int count = 0;
        while (count < taggedContent.length()) {
            int tagIndex = count;
            while (tagIndex < taggedContent.length() && taggedContent.charAt(tagIndex) != '<') tagIndex++;
            if (tagIndex < taggedContent.length()) {
                noTaggedCT = noTaggedCT + taggedContent.substring(count, tagIndex);
                while (tagIndex < taggedContent.length() && taggedContent.charAt(tagIndex) != '>') tagIndex++;
                count = tagIndex + 1;
                if (taggedContent.substring(0, count).endsWith("<BODY>")) {
                    startRawText = noTaggedCT.length();
                }
                if (taggedContent.substring(0, count).endsWith("</BODY>")) {
                    endRawText = noTaggedCT.length();
                } 
            }
            else {
                noTaggedCT = noTaggedCT + taggedContent.substring(count);
                count = tagIndex;
            }
        }
        
        noTaggedCT = trimUnrealReturns(noTaggedCT);
        
        if (startRawText > 0) {
            this.rawContent = noTaggedCT.substring(startRawText, endRawText);
            DocumentImpl.startSentence = startRawText;
        }
        
        return noTaggedCT;
    }
    
    public void exportContentToFile(String outputFilename, String fileContent, String encoding) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename)), encoding));
            writer.write(fileContent);
            writer.close();
        }
        catch (FileNotFoundException fileEx) {
            fileEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        
    }
    
    String taggedContent = null;
    String noTaggedContent = null;
    String rawContent = null;
    public static int startSentence = -1;
    String URI = null;
    String source = null;
    String type = null;
    String version = null;
    String author = null;
    String encoding = null;
    String docId = null;
    
    
    /**
     * Text file containing the content of the source file
     */
    String textFilename = null;
    
    /** The default entity set */
    protected EntitySet defaultEntities = null;
    
    /** The default entity set */
    protected MentionList defaultMentions = null;
    
    /** The default relation set */
    protected RelationSet defaultRelations = null;  
    
    /** list of trees (parse tree combined with matched entities) 
     * produced for sentences in the document 
     */
    protected ArrayList defaultTreeList;   
    List<List<? extends HasWord>> document = null; // initialized in getParseTreeList

}
