/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import corpora.CorpusImpl;
import util.RECKParameters;

/**
 *
 * @author Truc-Vien T. Nguyen
 */
public class RNExtractor {
    public static RNExtractor RNExtractor = null;
    public RECKParameters reckParams = null;

    public RNExtractor(String[] args) {
        reckParams = new RECKParameters(args);
        CorpusImpl corpus = new CorpusImpl(reckParams);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (args.length < 1) {
            System.out.println("inputFileName ...");
            return;
        }

        RNExtractor = new RNExtractor(args);
    }

    public static RNExtractor getRNExtractor() {
        return RNExtractor;
    }
}
