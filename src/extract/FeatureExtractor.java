    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extract;

import corpora.DocumentImpl;
import util.ExtractorConstants;

import util.Intermediate;
import util.SortedFile;
import gate.*;
import gate.annotation.*;
import gate.creole.*;
import gate.util.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import reck.Mention;
import util.FilenameFilterImpl;

/**
 *
 * @author doct
 */
public class FeatureExtractor {

    public FeatureExtractor(String input, String output) {

        try {
            initResource();
            initInOut(output);
        }
        catch (gate.util.GateException ge) {
            ge.printStackTrace();
        }
        catch (java.io.IOException ie) {
            ie.printStackTrace();
        }

    }

    public void extractOneFile(File df, ArrayList entityList) throws GateException, IOException {
        // tell the pipeline about the corpus and run it
        corpus.clear();
        initDocument(df);
        setCorpus(corpus);
        execute();
        exportResults(entityList);
    } // annotate

    /**
     * EXTRACTION (GAZETTEER AND TOKEN) FROM TEXT
     * @input   corpus of raw text
     * @output  corpus of XML files
     */

    /**
     *
     * @throws gate.util.GateException
     * @throws java.io.IOException
     */

    public void initResource() throws GateException, IOException {
        // initialise the GATE library
        // called just only one time
        Out.prln("Initialising GATE...");

        String gateHome = System.getProperty("user.dir");
        System.setProperty("gate.home", gateHome);
        Gate.init();

        //fix for bugs in Solaris
        if(gateHome.startsWith(ExtractorConstants.fileSeparator)) gateHome = new String(gateHome.substring(1));
        if(!gateHome.endsWith(ExtractorConstants.fileSeparator)) gateHome += ExtractorConstants.fileSeparator;
        if(!gateHome.toLowerCase().startsWith("file:" + ExtractorConstants.fileSeparator)) {
            gateHome = "file:" + ExtractorConstants.fileSeparator + gateHome;
        }

        Gate.getCreoleRegister().registerDirectories(new URL(gateHome + "plugins" + ExtractorConstants.fileSeparator + "ANNIE"));
        Gate.getCreoleRegister().registerDirectories(new URL(gateHome + "plugins" + ExtractorConstants.fileSeparator + "VNKIM" + ExtractorConstants.fileSeparator + "VN-NER"));
        Out.prln("...GATE initialised");

        // initialise VNKIM (this may take several minutes)
        this.initExtractor();
    }


    public void initExtractor() {
        Out.prln("Initialising Extractor...");

        try {
            // create a serial analyser controller to run VNKIM with
            controller =
                    (SerialAnalyserController) Factory.createResource(
                    "gate.creole.SerialAnalyserController", Factory.newFeatureMap(),
                    Factory.newFeatureMap(), "Extractor" + Gate.genSym()
            );

            // load each PR as defined in ExtractorConstants
            for (int i = 0; i < ExtractorConstants.PR_CLASS.length; i++) {
                ProcessingResource pr = (ProcessingResource) Factory.createResource(ExtractorConstants.PR_CLASS[i]);

                // add the PR to the pipeline controller
                controller.add(pr);
            } // for each VNKIM PR
            Out.prln("...Extractor loaded");
        }
        catch (gate.creole.ResourceInstantiationException rie) {
            rie.printStackTrace();
        }
    }

    public void initCorpus(String input) throws gate.creole.ResourceInstantiationException {
        SortedFile clean = new SortedFile(input);

        try {
            FilenameFilterImpl filter = new FilenameFilterImpl(".txt");
            File[] cfs = clean.listFiles(filter);

            for (int i = 0; i < cfs.length; i++) {
                initDocument(cfs[i]);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void initDocument(File df) {
        URL u = null;
        try {
            u = df.toURI().toURL();
            FeatureMap params = Factory.newFeatureMap();
            params.put("sourceUrl", u);
            params.put("preserveOriginalContent", true);
            params.put("collectRepositioningInfo", true);
            params.put("encoding", "UTF-8");
            Out.prln("Creating doc for " + u);
            Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
            corpus.add(doc);
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        catch (ResourceInstantiationException rie) {
            rie.printStackTrace();
        }
    }

    /** Tell VNKIM's controller about the annotatedCorpus you want to run on */
    public void setCorpus(Corpus corpus) {
        controller.setCorpus(corpus);
    } // setCorpus

    /** Run VNKIM */
    public void execute() throws GateException {
        Out.prln("Extracting ...");
        controller.execute();
        Out.prln("...Extracting complete");
    } // execute()

    public void initInOut(String out) {
        try {
            corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
            Intermediate.trainWriter = new BufferedWriter[ExtractorConstants.NBR_FOLDS];
            Intermediate.testWriter = new BufferedWriter[ExtractorConstants.NBR_FOLDS];
            for (int i = 0; i < ExtractorConstants.NBR_FOLDS; i++) {
                String trainFilename = out + ExtractorConstants.fileSeparator + "train" + (i + 1) + ".dat";
                String testFilename = out + ExtractorConstants.fileSeparator + "test" + (i + 1) + ".dat";
                Intermediate.trainWriter[i] = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(trainFilename), "UTF-8"));
                Intermediate.testWriter[i] = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(testFilename), "UTF-8"));
            }
        }
        catch (gate.util.GateException ge) {
            ge.printStackTrace();
        }
        catch (java.io.FileNotFoundException fEx) {
            fEx.printStackTrace();
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    public void exportResults(ArrayList entityList)
    {
        // for each document, get an XML document with the
        // person and location names added
        try {

            Document doc = (Document)corpus.get(0);

            // String fileName = (String)doc.getFeatures().get("sourceURL");
            String fileName = doc.getParameterValue("sourceUrl").toString();
            fileName = fileName.substring(fileName.lastIndexOf("/")+1);
            if (fileName.indexOf(".") != -1)
                fileName = fileName.substring(0, fileName.indexOf("."));

            exportAnnotationSet(doc, entityList);
            writeToFeatureFile(fileName);
            lines.clear();
            wordOffsets.clear();

        }
        catch (gate.creole.ResourceInstantiationException rie) {
            rie.printStackTrace();
        }
    }

    public void closeInOut() {
        try {
            for (int i = 0; i < ExtractorConstants.NBR_FOLDS; i++) {
                Intermediate.trainWriter[i].close();
                Intermediate.testWriter[i].close();
            }
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    public void exportAnnotationSet(Document doc, ArrayList entityList) {

        createWordList(doc);
        createGoldAnnots(doc, entityList);
        // correctIndexAnnots(doc);
        markupDocumentWithWordFeatures(doc);
        markupDocumentWithLookups(doc);
        markupDocumentWithGoldAnnots(doc);

        /**
         * END TRAINING FILE
         */
    }

    public void createWordList(Document doc) {
        Annotation currAnnot;

        /**
         * BEGIN TRAINING FILE
         */

        tokenAnnots = new SortedAnnotationList();
        lookupAnnots = new SortedAnnotationList();
        Iterator it = null;

        /** Get Lookup annotations
         * Generate second column with clas
         */
        AnnotationSet annotSet = doc.getAnnotations();

        /** Get Token annotations
         * Generate third column with orth
     */
        if (annotSet != null)
        {
            it = annotSet.iterator();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                String type = currAnnot.getType();
                if (type.equals("Lookup")) {
                    //
                    lookupAnnots.addSortedExclusive(currAnnot);
                }
                else if(type.equals("Token") || type.equals("Split") || type.equals("DEFAULT_TOKEN")) {
                    //
                    int start = currAnnot.getStartNode().getOffset().intValue();
                    int end = currAnnot.getEndNode().getOffset().intValue();

                    String annotSt = doc.getContent().toString().substring(start, end);
                    if (annotSt.indexOf(" ") == -1)
                        tokenAnnots.addSortedExclusive(currAnnot);
                }
            } // while
        }
    }

    public void createGoldAnnots(Document doc, ArrayList entityList) {
        AnnotationSetImpl annotSet = new AnnotationSetImpl(doc);
        Annotation currAnnot;

        goldAnnots = new SortedAnnotationList();
        /**
         * BEGIN TRAINING FILE
         */
        int s = 1;
        for (int i = 0; i < entityList.size(); i++) {
            Mention m = (Mention)entityList.get(i);
            int start = m.getHead().getStart().intValue(), end = m.getHead().getEnd().intValue() + 1;
            Node startNode = new NodeImpl(new Integer(s++), (long)(start - DocumentImpl.startSentence)),
                   endNode = new NodeImpl(new Integer(s++), (long)(end - DocumentImpl.startSentence));
            String type = m.getEntity().getType();
            // String annotSt = doc.getContent().toString().substring(start, end);
            String annotSt2 = doc.getContent().toString().substring(start - DocumentImpl.startSentence,
                    end - DocumentImpl.startSentence);
            annotSet.add(startNode, endNode, type, null);
        }

        Iterator it = null;

        /** Get Token annotations
         * Generate third column with orth
         */
        if (annotSet != null)
        {
            it = annotSet.iterator();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                goldAnnots.addSortedExclusive(currAnnot);
            } // while
        }
    }

    public int countCarriages(String content, int start) {
        int n = 0;

        for (int i = 0; i < start; i++) {
            if (content.charAt(i) == '\r') {
                start++;
                n++;
            }
        }

        return n;
    }

    public void correctIndexAnnots(Document textDoc) {
        //
        Annotation currAnnot;

        for (int i = 0; i < goldAnnots.size(); i++) {
            currAnnot = (Annotation) goldAnnots.get(i);
            int start = currAnnot.getStartNode().getOffset().intValue();
            int end = currAnnot.getEndNode().getOffset().intValue();

            int diff = countCarriages(textDoc.getContent().toString(), start);

            String annotSt1 = textDoc.getContent().toString().substring(start + diff, end + diff);

            String clas = currAnnot.getType();
            NodeImpl newStart = new NodeImpl(currAnnot.getStartNode().getId(), new Long(start + diff)),
                       newEnd = new NodeImpl(currAnnot.getEndNode().getId(), new Long(end + diff));
            currAnnot.setStartNode(newStart);
            currAnnot.setEndNode(newEnd);
        }

    }

    public void markupDocumentWithWordFeatures(Document doc) {
        Annotation currAnnot;

        for (int i = 0; i < tokenAnnots.size(); i++) {
            currAnnot = (Annotation) tokenAnnots.get(i);

            int start = currAnnot.getStartNode().getOffset().intValue(),
                  end = currAnnot.getEndNode().getOffset().intValue();
            String word = doc.getContent().toString().substring(start, end);
            String type = currAnnot.getType();

            Line line = new Line(word, type);
            lines.add(line);
            wordOffsets.add(start);

            String posTag = (String)currAnnot.getFeatures().get("category");
            if (posTag != null)
                line.setPOS(posTag);

            String orth = (String)currAnnot.getFeatures().get("orth");
            if (orth != null) {
                if (orth.equalsIgnoreCase("upperInitial")
                        || orth.equalsIgnoreCase("allCaps")
                        || orth.equalsIgnoreCase("lowercase"))
                    line.setOrth(orth);
                else if (orth.equals("mixedCaps"))
                    if (Character.isUpperCase(line.getWord().charAt(0)))
                        line.setOrth(orth);
                    else
                        line.setOrth("upperInitial");
            }

            String kind = (String)currAnnot.getFeatures().get("kind");
            if (kind != null)
                if (kind.equalsIgnoreCase("word")) {
                    line.setKind(kind);
                    if ((orth != null) && (orth.equalsIgnoreCase("allCaps"))) {
                        int k = 0;
                        while ( (k < word.length()) && ((word.charAt(k) == 'X') || (word.charAt(k) == 'I')) )
                            k++;
                        if (k == word.length())
                            line.setKind("roman_numeral");
                    }
                }
                else if (kind.equals("number") || kind.equals("control")
                        || kind.equals("symbol") || kind.equals("punctuation"))
                    line.setKind(kind);

        } // for
    }

    public void markupDocumentWithLookups(Document doc) {
        Annotation currAnnot;

        int j = 0;
        for (int i = 0; i < lookupAnnots.size(); i++) {
            currAnnot = (Annotation) lookupAnnots.get(i);
            int start = currAnnot.getStartNode().getOffset().intValue();
            int end = currAnnot.getEndNode().getOffset().intValue();

            String annotSt = doc.getContent().toString().substring(start, end);
            String clas = (String)currAnnot.getFeatures().get("class");
            int index = clas.lastIndexOf("#");
            clas = clas.substring(index + 1);

            while ( (j < wordOffsets.size()) && (((Integer)wordOffsets.get(j)).intValue() < start) ) j++;
            if (((Integer)wordOffsets.get(j)).intValue() == start) {
                Line line = (Line)lines.get(j);
                String word = line.getWord();

                // consider only strings with initial uppercased character

                // annotation type has only one word
                line.setOntoLookup("B-" + clas);

                // annotation type has more than one word
                if (((Integer)wordOffsets.get(j)).intValue() + word.length() < end) {
                    j = j + 1;
                    Line ln = null;
                    while (j < wordOffsets.size()) {
                        ln = (Line)lines.get(j);
                        word = ln.getWord();
                        if (((Integer)wordOffsets.get(j)).intValue() + word.length() <= end) {
                            ln.setOntoLookup("I-" + clas);
                            j++;
                        }
                        else
                            break;
                    }
                }
            }

        } // for
    }

    public void markupDocumentWithGoldAnnots(Document doc) {
        Annotation currAnnot;

        int j = 0;
        for (int i = 0; i < goldAnnots.size(); i++) {
            currAnnot = (Annotation) goldAnnots.get(i);
            int start = currAnnot.getStartNode().getOffset().intValue();
            int end = currAnnot.getEndNode().getOffset().intValue();

            String annotSt = doc.getContent().toString().substring(start, end);
            String clas = currAnnot.getType();

            while ( (j < wordOffsets.size()) && (((Integer)wordOffsets.get(j)).intValue() < start) ) j++;
            if (((Integer)wordOffsets.get(j)).intValue() == start) {
                Line line = (Line)lines.get(j);
                String word = line.getWord();

                // consider only strings with initial uppercased character

                // annotation type has only one word
                line.setNE("B-" + clas);

                // annotation type has more than one word
                if (((Integer)wordOffsets.get(j)).intValue() + word.length() < end) {
                    j = j + 1;
                    Line ln = null;
                    while (j < wordOffsets.size()) {
                        ln = (Line)lines.get(j);
                        word = ln.getWord();
                        if (((Integer)wordOffsets.get(j)).intValue() + word.length() <= end) {
                            ln.setNE("I-" + clas);
                            j++;
                        }
                        else
                            break;
                    }
                }
            }

        } // for
    }

    public void writeToFeatureFile(String filename) {
        int i;
        String st = null;
        Line line = null;
        int ifold = 0;
        while (ifold < ExtractorConstants.NBR_FOLDS)
            if (filename.startsWith(ExtractorConstants.initialFilenames[2*ifold])
                || filename.startsWith(ExtractorConstants.initialFilenames[2*ifold + 1]))
                break;
            else
                ifold++;
        try {
            for (i = 0; i < lines.size(); i++) {
                line = (Line)lines.get(i);

                if (line.getWord().equals("simpson"))
                    System.out.print("");

                if (line.getType().equals("Split")) {
                    splt = true;
                    i++;
                }

                if (splt) 
                    if (!prev_splt) {
                        for (int j = 0; j < ExtractorConstants.NBR_FOLDS; j++)
                            if (j != ifold)
                                Intermediate.trainWriter[j].write("\n");
                        Intermediate.testWriter[ifold].write("\n");
                        iline++;
                        prev_splt = splt;
                        splt = false;
                    }
                    else {
                        prev_splt = splt = false;
                    }
                else {
                    st = line.toString();
                    for (int j = 0; j < ExtractorConstants.NBR_FOLDS; j++)
                        if (j != ifold) {
                            Intermediate.trainWriter[j].write(st);
                            Intermediate.trainWriter[j].write("\n");
                        }
                    Intermediate.testWriter[ifold].write(st);
                    iline++;
                    Intermediate.testWriter[ifold].write("\n");
                    iline++;
                    prev_splt = splt;
                }

            }
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

    }

    public static final class SuffixFilenameFilter implements FilenameFilter {

        /**
        * Construction method
        */
        public SuffixFilenameFilter(String suffix) {
            this.suffix = suffix;
        }

        /**
         * Tests if a specified file should be included in a file list.
         *
         * @param   dir    the directory in which the file was found.
         * @param   name   the name of the file.
         * @return  <code>true</code> if and only if the name should be
         * included in the file list; <code>false</code> otherwise.
         */
        public boolean accept(File dir, String name) {
            if (name.endsWith(suffix))
                return true;
            return false;
        }

        // The expected suffix
        String suffix;

    }

    /**
     *
     */
    public static class SortedAnnotationList extends Vector {
        public SortedAnnotationList() {
            super();
        } // SortedAnnotationList

        public boolean addSortedExclusive(Annotation annot) {
            Annotation currAnnot = null;

            long annotStart = annot.getStartNode().getOffset().longValue(),
                 annotEnd = annot.getEndNode().getOffset().longValue();
            long currStart, currEnd;
            // insert
            for (int i = 0; i < size(); ++i) {
                currAnnot = (Annotation) get(i);
                currStart = currAnnot.getStartNode().getOffset().longValue();
                currEnd = currAnnot.getEndNode().getOffset().longValue();
                if ((annotStart == currStart) && (annotEnd == currEnd))
                    return true;
                if (annotStart < currStart) {
                    insertElementAt(annot, i);
                    return true;
                } // if
            } // for

            int size = size();
            insertElementAt(annot, size);

            return true;
        } // addSorted
    } // SortedAnnotationList

    /** The Corpus Pipeline application to contain VNKIM */
    private SerialAnalyserController controller;

    // create a GATE annotatedCorpus and add a document for each command-line
    // argument
    private static Corpus corpus;

    private SortedAnnotationList tokenAnnots, lookupAnnots, goldAnnots;

    private ArrayList wordOffsets = new ArrayList();
    private ArrayList lines = new ArrayList();

    private Hashtable collocs = new Hashtable();

    private int iline = 0;
    private boolean prev_splt, splt = false;
}
