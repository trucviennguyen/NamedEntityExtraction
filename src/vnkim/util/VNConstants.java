package vnkim.util;

import java.util.ArrayList;

/**
 * <p>Title: VNKIM</p>
 * <p>Description: Knowledge Information Extraction for Vietnamese</p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Technology</p>
 * @author Truc Vien
 * @version 1.0
 */

public final class VNConstants
{

    public static String RMI_HOST = "//localhost:";
    public static int RMIPort = 1099;
    public static String MysqlRepository = "rdbms-rdfs-db";
    public static String MemRepository = "mem-rdfs-db";

    public static String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";

    public static String ENTITY_QUERY = "QUERY OVER KB OF ENTITIES";
    public static String LEXICA_QUERY = "QUERY OVER KB OF LEXICAL RESOURCES";

    public static ArrayList entityClasses = new ArrayList();
    public static ArrayList lexicaClasses = new ArrayList();

    static {
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonLexica");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonEnding");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonFirst");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonFirstAmbig");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonFirstFemale");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonFirstMale");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#PersonSpur");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#Surname");
        entityClasses.add("http://www.ontotext.com/kim/2006/05/kimlo#SurnamePrefix");

        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protont#Abstract");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Art");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#BusinessAbstraction");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#CalendarMonth");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protont#ContactInformation");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#DayOfMonth");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#DayOfWeek");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#DayTime");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Disease");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#EMail");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Festival");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#HomePage");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#IndustrySector");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#InternetAddress");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#InternetDomain");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#IPAddress");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Language");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Market");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Money");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#NaturalPhenomenon");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Number");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Percent");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#PhoneNumber");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#PostalAddress");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Profession");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#ResearchArea");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Science");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Season");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#SocialAbstraction");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#Sport");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#StreetKey");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#TemporalAbstraction");
        lexicaClasses.add("http://proton.semanticweb.org/2006/05/protonu#WebPage");
    }

}
