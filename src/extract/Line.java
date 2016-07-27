/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extract;

import util.ExtractorConstants;
import java.util.StringTokenizer;

/**
 *
 * @author tvnguyen
 */
public class Line {
    /***************************************************************
     * Mandatory fields
     ***************************************************************/
    
    /**
     * The token
     */
    private String word = "ND";

    /**
     * The type
     */
    private String type = "Token";
    
    /***************************************************************
     * Common features
     ***************************************************************/
    
    /**
     * The part-of-speech tagger
     */
    private String pos = "ND";
    
    String prefix[] = {"ND", "ND", "ND", "ND"}, suffix[] = {"ND", "ND", "ND", "ND"};
    
    /**
     * Orthographic features
     * 0: upperInitial
     * 1: allCaps
     * 2: lowercase
     * 3: mixedCaps
     */
    private String orth = "ND";
    
    /**
     * Word features
     * 0: word
     * 1: number (arabic_number)
     * 2: roman_numeral
     * 3: control
     * 4: symbol
     * 5: punctuation
     */
    private String kind = "ND";
    
    /**
     * The gazetteer major type
     */
    private String ontoLookup = "O";
    
    /**
     * The collocation
     */
    private String colloc = "O";
    
    /**
     * The named-entity class
     */
    private String ne = "O";
    
    public Line() {
        
    }

    public Line(String word, String type) {
        this.setWord(word);
        this.setType(type);
        if (word.equals(".")) {
            
        }
    }
    
    public Line(String word, String type, String pos, String ne) {
        this.setWord(word);
        this.setType(type);
        this.setPOS(pos);
        this.setNE(ne);
    }
    
    public Line(String word, String type, String pos, String ontoLookup, String ne) {
        this.setWord(word);
        this.setType(type);
        this.setPOS(pos);
        this.setOntoLookup(ontoLookup);
        this.setNE(ne);
    }

    public Line(String st) {
        StringTokenizer tokenizer = new StringTokenizer(st);
        switch (tokenizer.countTokens()) {
            case 2:
                this.setWord(tokenizer.nextToken());
                this.setType(tokenizer.nextToken());
                break;

            case 3:
                this.setWord(tokenizer.nextToken());
                this.setType(tokenizer.nextToken());
                this.setPOS(tokenizer.nextToken());
                break;

            case 4:
                this.setWord(tokenizer.nextToken());
                this.setType(tokenizer.nextToken());
                this.setPOS(tokenizer.nextToken());
                this.setNE(tokenizer.nextToken());

            case 5:
                this.setWord(tokenizer.nextToken());
                this.setType(tokenizer.nextToken());
                this.setPOS(tokenizer.nextToken());
                this.setOntoLookup(tokenizer.nextToken());
                this.setNE(tokenizer.nextToken());
                break;
            default:
                break;
        }

    }
    
    private void getPreAndSuffix() {
        if (word.equals(ExtractorConstants.CARRIAGE_RETURN)) {
            this.prefix = new String[] {"ND", "ND", "ND", "ND"};
            this.suffix = new String[] {"ND", "ND", "ND", "ND"};
        }
        else {
            this.prefix = new String[] {
                word.substring(0, 1),
                (word.length() < 2) ? "ND" : word.substring(0, 2),
                (word.length() < 3) ? "ND" : word.substring(0, 3),
                (word.length() < 4) ? "ND" : word.substring(0, 4)};

            this.suffix = new String[] {
                word.substring(word.length() - 1),
                (word.length() < 2) ? "ND" : word.substring(word.length() - 2),
                (word.length() < 3) ? "ND" : word.substring(word.length() - 3),
                (word.length() < 4) ? "ND" : word.substring(word.length() - 4)};
        }
    }
    
    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
        this.getPreAndSuffix();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getPOS() {
        return pos;
    }
    
    public void setPOS(String pos) {
        this.pos = pos;
    }
    
    public String getontoLookup() {
        return ontoLookup;
    }
    
    public void setOntoLookup(String ontoLookup) {
        this.ontoLookup = ontoLookup;
    }
    
    public String getOrth() {
        return orth;
    }
    
    public void setOrth(String orth) {
        this.orth = orth;
    }
    
    public String getKind() {
        return kind;
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    public String getColloc() {
        return colloc;
    }
    
    public void setColloc(String colloc) {
        this.colloc = colloc;
    }
    
    public String getNE() {
        return ne;
    }
    
    public void setNE(String ne) {
        this.ne = ne;
    }
    
    @Override
    public String toString() {
        String st = "";
        if (word.equals(ExtractorConstants.CARRIAGE_RETURN))
            st = "";
        else
            if (ne.equals(""))
                st = word + " " + word.toLowerCase() 
                        + " " + prefix[0] + " " + prefix[1] + " " + prefix[2] + " " + prefix[3]
                        + " " + suffix[0] + " " + suffix[1] + " " + suffix[2] + " " + suffix[3]
                        + " " + pos + " " + orth + " " + kind
                        + " " + ontoLookup + " " + colloc;
                        // + " " + pos + " " + orth;
            else
                st = word + " " + word.toLowerCase()
                        + " " + prefix[0] + " " + prefix[1] + " " + prefix[2] + " " + prefix[3]
                        + " " + suffix[0] + " " + suffix[1] + " " + suffix[2] + " " + suffix[3]
                        + " " + pos + " " + orth + " " + kind
                        + " " + ontoLookup + " " + colloc + " " + ne;
                        // + " " + pos + " " + orth + " " + ne;
        
        return st;
    }
    
    public String toString_no_Resource() {
        String st = "";
        if (word.equals("\n"))
            st = "";
        else
            if (ne.equals(""))
                st = word + " " + word.toLowerCase() 
                        + " " + prefix[0] + " " + prefix[1] + " " + prefix[2] + " " + prefix[3]
                        + " " + suffix[0] + " " + suffix[1] + " " + suffix[2] + " " + suffix[3]
                        + " " + pos + " " + orth + " " + kind;
            else
                st = word + " " + word.toLowerCase() 
                        + " " + prefix[0] + " " + prefix[1] + " " + prefix[2] + " " + prefix[3] 
                        + " " + suffix[0] + " " + suffix[1] + " " + suffix[2] + " " + suffix[3]
                        + " " + pos + " " + orth + " " + kind
                        + " " + ne;
        
        return st;
    }
}
