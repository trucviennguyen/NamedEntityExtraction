/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package corpora;

import extract.FeatureExtractor;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import reck.Corpus;
import reck.Document;
import reck.Mention;
import util.RECKConstants;
import util.RECKParameters;
import util.RECKConstants.ReckFilenameFilter;
import util.SortedFile;
import util.Statistics;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public class CorpusImpl implements Corpus, Cloneable, Serializable {
    
    RECKParameters reckParams = null;
    FeatureExtractor extractor = null;
    
    public final ArrayList headwords = new ArrayList();

    CorpusImpl corpus = null;
    
    /**
     * Construction method
     */
    public CorpusImpl() {
        corpus = this;
    }
    
    /**
     * Construction method
     */
    public CorpusImpl(ArrayList documentList) {
        this();
        this.documentList = documentList;
    }
    
    /**
     * Construction method
     */
    public CorpusImpl(RECKParameters reckParams) {
        this();
        this.reckParams = reckParams;
        this.extractor = new FeatureExtractor(reckParams.inputFilename, reckParams.outputFilename);
        
        switch (reckParams.mode) {

            /** 
             * read from ACE corpora, process them
             * and export to serialized files
             */
            case 0:
                corpus.readFromDataFile(reckParams.inputFilename);
                corpus.printStatistics();
                extractor.closeInOut();
                break;

            /** 
             * read from ACE corpora, process them
             * and export to serialized files
             */
            case 1:
                corpus.readAndWriteToDataFile(reckParams.inputFilename, reckParams.outputFilename);
                break;

            /** 
             * read documents from serialized files,
             * output file containing all the headword
             * this will be used later for real application on RE
             */
            case 2:
                corpus.readFromSerializedFile(reckParams.inputFilename);
                corpus.constructHeadwordDictionary();
                corpus.writeHeadwordsToFile(reckParams.outputFilename);
                break;

            default:
                // 
        }

    }

    public int size() {
        return documentList.size();
    }

    public boolean isEmpty() {
        return documentList.isEmpty();
    }

    public boolean contains(Object o){
        return documentList.contains(o);
    }

    public Iterator iterator(){
        return documentList.iterator();
    }

    public Object[] toArray(){
        return documentList.toArray();
    }

    public Object[] toArray(Object[] a){
        return documentList.toArray(a);
    }

    public boolean add(Object o){
        return documentList.add(o);
    }

    public boolean remove(Object o){
        return documentList.remove(o);
    }

    public boolean containsAll(Collection c){
        return documentList.containsAll(c);
    }

    public boolean addAll(Collection c){
        return documentList.addAll(c);
    }

    public boolean addAll(int index, Collection c){
        return documentList.addAll(index, c);
    }

    public boolean removeAll(Collection c){
        return documentList.removeAll(c);
    }

    public boolean retainAll(Collection c){
        return documentList.retainAll(c);
    }

    public void clear(){
        documentList.clear();
    }

    @Override
    public boolean equals(Object o){
        if (! (o instanceof CorpusImpl))
            return false;

        return documentList.equals(o);
    }

    public int hashCode(){
        return documentList.hashCode();
    }

    public Object get(int index){
        return documentList.get(index);
    }

    public Object set(int index, Object element){
        return documentList.set(index, element);
    }

    public void add(int index, Object element){
    documentList.add(index, element);
    }

    public Object remove(int index){
        return documentList.remove(index);
    }

    public int indexOf(Object o){
        return documentList.indexOf(o);
    }

    public int lastIndexOf(Object o){
    return lastIndexOf(o);
    }

    public ListIterator listIterator(){
        return documentList.listIterator();
    }

    public ListIterator listIterator(int index){
        return documentList.listIterator(index);
    }

    public Corpus subList(int fromIndex, int toIndex){
        return new CorpusImpl(new ArrayList(documentList.subList(fromIndex, toIndex)));
    }

    public void printStatistics() {
        System.out.println("Statistics: ");
        System.out.println("\t" + Statistics.nbr_documents + " documents");
        System.out.println("\t" + Statistics.nbr_entities + " entities");
        System.out.println("\t" + Statistics.nbr_mentions + " mentions");
        System.out.println("\t" + Statistics.nbr_relations + " relations");
        System.out.println("\t" + Statistics.nbr_out_relations + " relations out");
        System.out.println();
    }
    
    public void readFromDataFile(String inputFilename) {
        File df = new File(inputFilename);
        DocumentImpl doc = null;

        if (df.isDirectory()) {
            ReckFilenameFilter annotated_filter = new ReckFilenameFilter(".XML");
            File[] fs_annotated = df.listFiles(annotated_filter);
            for (int i = 0; i < fs_annotated.length; i++) {
                doc = new DocumentImpl(fs_annotated[i], extractor);
                add(doc);
                relationListACE.addAll(doc.getRelations());
                nbr_entities += doc.getEntities().size();
                nbr_relations += doc.getRelations().size();
                
                Statistics.nbr_documents++;
                
                int news_Category_Index = RECKConstants.newsCategories.indexOf(doc.docId.substring(0, 3));
                Statistics.nbr_newsPerCategory[news_Category_Index]++;
            }
        }
        else {
            doc = new DocumentImpl(df, extractor);
            add(doc);
            relationListACE.addAll(doc.getRelations());
            nbr_entities += doc.getEntities().size();
            nbr_relations += doc.getRelations().size();
                
            Statistics.nbr_documents++;
        }
    }
    
    public void readAndWriteToDataFile(String inputFilename, String outputFilename) {
        File df = new File(inputFilename);
        DocumentImpl doc = null;
        
        File f = new File(outputFilename);
        if (!f.exists())
            f.mkdir();

        if (df.isDirectory()) {
            ReckFilenameFilter annotated_filter = new ReckFilenameFilter(".XML");
            File[] fs_annotated = df.listFiles(annotated_filter);
            for (int i = 0; i < fs_annotated.length; i++) {
                doc = new DocumentImpl(fs_annotated[i], extractor);
                add(doc);
                relationListACE.addAll(doc.getRelations());
                Statistics.nbr_documents++;

                int news_Category_Index = RECKConstants.newsCategories.indexOf(doc.docId.substring(0, 3));
                Statistics.nbr_newsPerCategory[news_Category_Index]++;
            }
        }
        else {
            doc = new DocumentImpl(df, extractor);
            add(doc);
            relationListACE.addAll(doc.getRelations());
            Statistics.nbr_documents++;
        }
    }
    
    public void writeToDataFile(String outputFilename) {
        DocumentImpl doc = null;

        for (int i = 0; i < size(); i++) {
            doc = (DocumentImpl)get(i);
            relationListACE.addAll(doc.getRelations());
            Statistics.nbr_entities += doc.getEntities().size();
            Statistics.nbr_relations += doc.getRelations().size();
        }
    }
    
   public void writeHeadwordsToFile(String outputFilename) {

        BufferedWriter writer_headwords = null;
        
        try {
            writer_headwords = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename + RECKConstants.fileSeparator + "headwords.data"))));
            
            for (int i = 0; i < headwords.size(); i ++) {
                writer_headwords.write((String)headwords.get(i));
                writer_headwords.newLine();
            }
            
            writer_headwords.close();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
   
    public void readFromSerializedFile(String inputFilename) {
        SortedFile df = new SortedFile(inputFilename);
        DocumentImpl doc = null;

        try {
            if (df.isDirectory()) {
                clear();
                ReckFilenameFilter annotated_filter = new ReckFilenameFilter(".document");
                File[] fs_annotated = df.listFiles(annotated_filter);
                for (int i = 0; i < fs_annotated.length; i++) {
                    doc = (DocumentImpl)RECKConstants.readFromFile(fs_annotated[i].getCanonicalPath());
                    add(doc);
                    relationListACE.addAll(doc.getRelations());
                    nbr_entities += doc.getEntities().size();
                    nbr_relations += doc.getRelations().size();
                    Statistics.nbr_entities += doc.getEntities().size();
                    Statistics.nbr_relations += doc.getRelations().size();
                }
            }
            else {
                doc = (DocumentImpl)RECKConstants.readFromFile(inputFilename);
                add(doc);
                relationListACE.addAll(doc.getRelations());
                nbr_entities += doc.getEntities().size();
                nbr_relations += doc.getRelations().size();
                Statistics.nbr_entities += doc.getEntities().size();
                Statistics.nbr_relations += doc.getRelations().size();
            }
        }
        catch (java.lang.ClassNotFoundException classEx) {
            classEx.printStackTrace();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
        System.out.println("nbr_relations = " + Statistics.nbr_relations);
    } // readFromSerializedFile

    public void constructHeadwordDictionary() {
        for (int i = 0; i < size(); i++) {
            Document doc = (Document)get(i);
            Iterator mentionIter = doc.getMentions().iterator();
            while (mentionIter.hasNext()) {
                Mention mention = (Mention)mentionIter.next();
                String st = RECKConstants.trimReturn(mention.getHeadword()).toLowerCase();
                if (headwords.indexOf(st) == -1) 
                    headwords.add(st);
            }
        }
    } // constructHeadwordDictionary

    /**
     * The list that holds the documents in this corpus.
     */
    protected ArrayList documentList = new ArrayList();
    protected ArrayList relationListACE = new ArrayList();
    protected ArrayList possibleRelationList = new ArrayList();
    protected ArrayList<String> markDocumentList = new ArrayList();
    
    // 5-fold cross-validation
    protected ArrayList[] foldRelationList = {new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList()};
    
    // 5-fold cross-validation
    protected ArrayList[] markFoldList = {new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList(),
                                                new ArrayList()};
    
    public int nbr_entities = 0, nbr_relations = 0;    
    public int nbr_none_rels = 0, nbr_avail_rels = 0;
    public int nbr_fold = 0;
    public int avg_tree_size = 0;
    
}
