/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author Truc Vien
 */
public final class Statistics {

    public static int nbr_documents = 0, nbr_entities = 0, nbr_mentions = 0, nbr_relations = 0;
    
    public static int nbr_newsPerCategory[] = new int[8];
    public static int nbr_entitiesPerType[] = new int[7];
    public static int nbr_entitiesPerSubType[] = new int[42];
    public static int nbr_entitiesPerClass[] = new int[5];
    public static int nbr_relationsPerType[] = new int[8];
    public static int nbr_relationsPerSubType[] = new int[22];
    public static int nbr_relationLDCLexicalConditions[] = new int[6];
    public static int nbr_mentionsPerType[] = new int[4];
    public static int nbr_mentionsPerLDCType[] = new int[18];
    public static int nbr_mentionsPerRole[] = new int[5];
    public static int nbr_mentionsPerReference[] = new int[2];
    
    public static int nbr_out_relations = 0;

    //static {
        
    //}
}
