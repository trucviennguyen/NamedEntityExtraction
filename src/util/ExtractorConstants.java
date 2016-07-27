/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author doct
 */
public final class ExtractorConstants {
    
    public static final String FILE_TEXT_SUFFIX = ".txt";
    public static final String FILE_INPUT_SUFFIX = ".dat";

    public static String RMI_HOST = "//localhost:";
    public static int RMIPort = 1099;
    public static String MysqlRepository = "rdbms-rdfs-db";
    public static String MemRepository = "mem-rdfs-db";

    public static String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";

    public static String ENTITY_QUERY = "QUERY OVER KB OF ENTITIES";
    public static String LEXICA_QUERY = "QUERY OVER KB OF LEXICAL RESOURCES";

    public static final String CARRIAGE_RETURN = "\n";
    
    /** Local fashion for file separators. */
    public static String fileSeparator = "/";
    // public static String fileSeparator = System.getProperty("file.separator");

    public static String[] initialFilenames = new String[] {"ABC", "APW", "CNN", "MNB", "NBC", "NYT", "PRI", "VOA"};
    public static int NBR_FOLDS = 4;

    public static final String[] PR_CLASS = {
        "gate.creole.annotdelete.AnnotationDeletePR",// Document Reset PR
        "gate.creole.tokeniser.SimpleTokeniser",    // GATE Unicode Tokeniser
        "gate.creole.splitter.SentenceSplitter",    // ANNIE sentence splitter

        // Chau's section here
        "gate.creole.POSTagger",                    // ANNIE POS Tagger

        // Vien's section here
        "vnkim.creole.gazetteer.VNHashGazetteer"   // VN Hash Gazetteer
    };

    public static final float atof(String s)
    {
        return Float.valueOf(s).floatValue();
    }

    public static final double atod(String s)
    {
        return Double.valueOf(s).doubleValue();
    }

    public static final int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    public static String[] copyArray(String[] src, int from) {
        String[] des = new String[0];
        
        if (from < src.length) {
            des = new String[src.length - from];
            for (int i = from; i < src.length; i++) 
                des[i - from] = src[i];
        }
        
        return des;
    }
    
    public static String[] copyArray(String[] src, int from, int to) {
        String[] des = new String[0];
        
        if ( (from <= to) && (to < src.length) ) {
            des = new String[to - from + 1];
            for (int i = from; i < to; i++) 
                des[i - from] = src[i];
        }
        
        return des;
    }

}
