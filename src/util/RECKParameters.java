/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 *
 * @author Truc Vien
 */
public class RECKParameters implements Cloneable, Serializable
{
    public int mode = 0;

    /** 
     * option "-r"
     * tree to cover two entities 
     * 0: MCT; 1: PT; 2: CPT; 3: FPT; 4: FCPT; 
     * 5: three kinds of tree: 
     *      PT; 
     *      CPT expanded to the left 4 words; 
     *      CPT expanded to the right 4 words
     * 6: three kinds of tree: 
     *      PT; 
     *      CPT expanded to the left extremety of entity mention's extent; 
     *      CPT expanded to the right extremety of entity mention's extent; 
     */
    public int tree_type = 1;
    
    /**
     * option "-w" 
     * windows size for the context-sensitive tree
     */
    public static int windows_size = 1;
    
    /**
     * option "-t"
     * test type
     * 0: training/test set; 1: n-fold cross validation 
     */
    public int test_type = 0;

    /**
     * option "-n"
     * the proportion in seperating training and test set 
     */
    public int nbfolds = 5;
    
    /* the path of input data */
    public String inputFilename = null;
    
    /* the path of output data */
    public String outputFilename = null;
    
    public static RECKParameters reckParameters = null;
    
    public static int nbr_err_in_rel_corpora = 0;
    
    public RECKParameters () {
        reckParameters = this;
    }
    
    public RECKParameters (String[] args) {
        this();
        int k = 4;
        if (args[0].equalsIgnoreCase("-s")) 
            mode = Integer.parseInt(args[1]);
        else
            k = 2;
        this.inputFilename = args[k - 2];
        this.outputFilename = args[k - 1];
        String[] argv = RECKConstants.copyArray(args, k);
        parse(argv);
    }
    
    public RECKParameters(String sParams) {
        this();

        // parse options
        StringTokenizer st = new StringTokenizer(sParams);
        String[] args = new String[st.countTokens()];
        for (int i = 0;i < args.length;i++)
            args[i] = st.nextToken();
        int k = 4;
        if (args[0].equalsIgnoreCase("-s")) 
            mode = Integer.parseInt(args[1]);
        else
            k = 2;
        this.inputFilename = args[k - 2];
        this.outputFilename = args[k - 1];
        String[] argv = RECKConstants.copyArray(args, k);
        parse(argv);
    }
   
    public static RECKParameters getRECKParameters() {
        return reckParameters;
    }
    
    public void parse(String[] argv) {

        // parse options
        for (int i=0;i<argv.length;i++)
        {
            if(argv[i].charAt(0) != '-') break;
            if(++i>=argv.length)
            {
                    System.err.print("unknown option\n");
                    break;
            }
            switch(argv[i-1].charAt(1))
            {                
                case 'r':
                    tree_type = RECKConstants.atoi(argv[i]);
                    break;

                case 'w':
                    windows_size = RECKConstants.atoi(argv[i]);
                    break;
                    
                case 't':
                    test_type = RECKConstants.atoi(argv[i]);
                    break;
                        
                case 'n':
                    nbfolds = RECKConstants.atoi(argv[i]);
                    break;
                    
                default:
                    System.err.print("unknown option\n");
            }
        }  
    }
    
    public String toString() {
        String st = "";
        

        st = "-t " + tree_type + " -k " + " -v " + test_type;
        if (test_type == 1)
            st = st + "-n " + nbfolds;
        
        return st;
    }

}