package vnkim.database;

import vnkim.util.VNConstants;
import vnkim.creole.gazetteer.VNHashGazetteer;

import java.io.IOException;
import org.openrdf.sesame.server.rmi.*;
import org.openrdf.util.rmirouting.*;

import org.openrdf.model.Value;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.repository.local.*;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.SesameService;
import org.openrdf.sesame.Sesame;
import java.net.*;
import org.openrdf.sesame.config.*;
import org.openrdf.sesame.query.*;

import java.util.HashMap;
import java.util.HashSet;

/**
 * <p>Title: SeRQL</p>
 * <p>Description: Test Vietnamese in SeRQL</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: University of Technology</p>
 * @author Truc Vien
 * @version 1.0
 */

public class VNQueryAPI{


  // Remote connection using RMI
  static FactoryInterface fi = null;
  static SesameService serviceRMI = null;
  static SesameRepository repRMI = null;

  // Remote connection using Http
  static String Url;
  static String UserName;
  static String Passwd;
  static String RepositoryID;


  static String sesameServerURL = null;
  static SesameService serviceHttp = null;
  static SesameRepository repHttp = null;

  // LocalService service = null;
  static LocalService localService = null;
  static LocalRepository localRepository = null;

  protected static HashMap MapClass = new HashMap();

  protected static final String AllClassQuery =
      "SELECT x FROM {x} <rdf:type> {<rdfs:Class>} " +
      "WHERE NOT x LIKE \"*www.w3.org*\" " +
      "AND NOT x LIKE \"*node*\" ";
  protected static final String SubClassQuery =
      "SELECT x FROM {x} <rdfs:subClassOf> {<";

  static
  {
      setURL(VNHashGazetteer.getProperties().getProperty("Url"));
      setUserName(VNHashGazetteer.getProperties().getProperty("UserName"));
      setPasswd(VNHashGazetteer.getProperties().getProperty("PassWord"));
      setRepositoryID(VNHashGazetteer.getProperties().getProperty("RepositoryID"));

      connectHttp();
      readMapClass();
  }


  //Construct the frame
  public VNQueryAPI() {

  }

  // Connection over RMI
  public static void connectRMI() {

    try
    {
      fi = (FactoryInterface)ChannelIfaceInvocation.
           wrapIt(java.rmi.Naming.lookup(VNConstants.RMI_HOST+"/FactoryInterface"));
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }

    try {
      System.out.print("Connecting to sesame ... ");
      SesameService si = fi.getService();
      si.login("testuser", "opensesame");
      repRMI = si.getRepository(VNConstants.MemRepository);
      System.out.println("done");
    }
    catch (Exception ex) {
    }
  }

  public static void setURL(String url) {
      Url = url;
  }

  public static void setUserName(String userName) {
      UserName = userName;
  }

  public static void setPasswd(String passwd) {
      Passwd = passwd;
  }

  public static void setRepositoryID(String repositoryID) {
      RepositoryID = repositoryID;
  }

  // Remote connection using HTTP
  public static void connectHttp() {
      try {
        System.out.print("Connecting to sesame ... ");
        serviceHttp = (SesameService) Sesame.getService(new URL(Url));
        serviceHttp.login(UserName, Passwd);

        repHttp = serviceHttp.getRepository(RepositoryID);

        System.out.println("done");
      }

      catch (Exception ex) {
        System.err.println(ex.toString());
      }
  }

  public static void connectLocal() {

    // Connect using local access
    RepositoryConfig config = new RepositoryConfig(VNConstants.MemRepository);

    SailConfig syncSail =
        new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfSchemaRepository");
    SailConfig memSail =
        new SailConfig("org.openrdf.sesame.sailimpl.memory.RdfSchemaRepository");

    config.addSail(syncSail);
    config.addSail(memSail);
    config.setWorldReadable(true);
    config.setWorldWriteable(true);

    localService = Sesame.getService();
    try
    {
      System.out.print("Connecting to sesame ... ");
      localService.addRepository(config);
      localRepository =
          (LocalRepository)localService.getRepository(VNConstants.MemRepository);
      System.out.println("done");
    }
    catch (Exception ex)
    {
      System.err.println(ex.toString());
    }

  }

  public static QueryResultsTable queryRMI(String query) {
    // String query = "select X from {X} rdf:type {vnkimo_rdfs:Tên} using namespace owl = <http://www.w3.org/2002/07/owl#>, vnkimo_rdfs = <http://www.dit.hcmut.edu.vn/vnkim/vnkimo.rdfs#>, vnkimkb_rdf = <http://www.dit.hcmut.edu.vn/vnkim/vnkimkb.rdf#>";

    QueryResultsTable resultsTable = null;
    try {
      QueryResultsTableBuilder builder = new QueryResultsTableBuilder();
      repRMI.performTableQuery(QueryLanguage.SERQL, query, builder);

      resultsTable = builder.getQueryResultsTable();

      int rowMax = resultsTable.getRowCount();
      int columnMax = resultsTable.getColumnCount();

      for (int row = 0; row<rowMax; row++) {
        for (int column = 0; column<columnMax; column++) {
          Value value = resultsTable.getValue(row, column);
          System.out.print(value.toString());
          System.out.print(" ");
        }
      }

    }
    catch (Exception ex) {
      System.err.println(ex.toString());
      return null;
    }

    return resultsTable;

  }

  public static QueryResultsTable queryHttp(String query) {
    // String query = "select X from {X} rdf:type {vnkimo_rdfs:Tên} using namespace owl = <http://www.w3.org/2002/07/owl#>, vnkimo_rdfs = <http://www.dit.hcmut.edu.vn/vnkim/vnkimo.rdfs#>, vnkimkb_rdf = <http://www.dit.hcmut.edu.vn/vnkim/vnkimkb.rdf#>";

    QueryResultsTable resultsTable = null;

    try {
      resultsTable = repHttp.performTableQuery(QueryLanguage.SERQL, query);
/*
      int rowMax = resultsTable.getRowCount();
      int columnMax = resultsTable.getColumnCount();

      for (int row = 0; row<rowMax; row++) {
        for (int column = 0; column<columnMax; column++) {
          Value value = resultsTable.getValue(row, column);
          System.out.print(value.toString());
          System.out.print(" ");
        }
      }
*/

    }
    catch (Exception ex) {
      System.err.println(ex.toString());
      return null;
    }

    return resultsTable;

  }

  public static void queryLocal(LocalRepository localRepository, String query) {
    // String query = "select X from {X} rdf:type {vnkimo_rdfs:Tên} using namespace owl = <http://www.w3.org/2002/07/owl#>, vnkimo_rdfs = <http://www.dit.hcmut.edu.vn/vnkim/vnkimo.rdfs#>, vnkimkb_rdf = <http://www.dit.hcmut.edu.vn/vnkim/vnkimkb.rdf#>";

    QueryResultsTable resultsTable = null;
    try {
      QueryResultsTableBuilder builder = new QueryResultsTableBuilder();
      localRepository.performTableQuery(QueryLanguage.SERQL, query, builder);

      resultsTable = builder.getQueryResultsTable();

      int rowMax = resultsTable.getRowCount();
      int columnMax = resultsTable.getColumnCount();

      for (int row = 0; row<rowMax; row++) {
        for (int column = 0; column<columnMax; column++) {
          Value value = resultsTable.getValue(row, column);
          System.out.print(value.toString());
          System.out.print(" ");
        }
      }

    }
    catch (Exception ex) {
      System.err.println(ex.toString());
    }

  }

  public static void closeRMI(SesameService si) {
    try {
      si.logout();
    }
    catch (IOException ex) {
      System.err.println(ex.toString());
    }

  }

  public static void closeHttp() {
    try {
      serviceHttp.logout();
    }
    catch (IOException ex) {
      System.err.println(ex.toString());
    }

  }

  public static void readMapClass() {

      System.out.print("Reading ontology hierarchy ... ");

      QueryResultsTable classes = VNQueryAPI.queryHttp(AllClassQuery);
      QueryResultsTable subClasses = null;
      HashSet subClassList = null;

      int rowMax = classes.getRowCount();

      for (int row = 0; row<rowMax; row++) {
          String clas = classes.getValue(row, 0).toString();

          subClasses = VNQueryAPI.queryHttp(SubClassQuery + clas + ">}");
          subClassList = new HashSet();
          int iMax = subClasses.getRowCount();

          for (int i = 0; i < iMax; i++)
              subClassList.add(subClasses.getValue(i, 0).toString());

          MapClass.put(clas, subClassList);
      }

      System.out.println(" done");

  }

  public static boolean isSubClassOf(String s, String s1)
  {
      // System.out.println("s = " + s + "; s1 = " + s1);
      if ((HashSet)MapClass.get(s1) == null)
          return false;
      else
          return ((HashSet)MapClass.get(s1)).contains(s);
  }

}
