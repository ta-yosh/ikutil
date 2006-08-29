/*           Db_Access.java
             Database access class 
             2003/02/24 Coded by Takaaki Yoshida <yoshida@saias.co.jp>
         Saias Co., Ltd.
*/
package jp.co.saias.util;

import java.sql.*;
import java.util.*;
import java.io.*;

public class DngDBAccess {

    public int Cols;
    public int Rows;
    public Vector fieldName;
    public Vector usrValue;

    private Connection con;
    private String drv[] = { 
                             "org.firebirdsql.jdbc.FBDriver", 
                             "org.postgresql.Driver", 
                             "sun.jdbc.odbc.jdbcodbcDriver"}; 
      
    private String dbPrefix[] = {
                   "jdbc:firebirdsql:",
                   "jdbc:postgresql:",
                   "jdbc:odbc:"
    };

    private int drv_no;
    private String url,driver,user,passwd;
    private int fetchCount = 0;

    public DngDBAccess(String rdbms,String uri,String usr,String pass) {
      try {         
	 drv_no = 0;
         if (rdbms=="firebird") drv_no = 0; 
         if (rdbms=="postgresql") drv_no = 1; 
         if (rdbms=="msaccess") drv_no = 2; 

	 driver = drv[drv_no];
         url = dbPrefix[drv_no] + uri;
         this.user = usr;
         this.passwd = pass;
      } catch (Exception e) {
         System.err.println("データベースエラー"+e);
	 System.exit(1);
      }
    }
    
    public boolean connect() {
      try {
        Class.forName(driver);
        con = DriverManager.getConnection(url ,user, passwd);
        return true;
      } catch (Exception e) {
        //System.err.println("データベース接続エラー"+e);
	return false; 
      }
    }
  
    public void begin() {
      try {
        if (con.isClosed()) connect();
        con.setAutoCommit(false);
      } catch (Exception e) {
      }
      return;
    }

    public void commit() {
      try {
        con.commit();
      } catch (Exception e) {
        System.err.println("データベースコミットエラー");
      }
      return;
    }

    public void rollback() {
      try {
        con.rollback();
      } catch (Exception e) {
        System.err.println("データベースロールバックエラー");
      }
      return;
    }

    public Object[] getFieldNames() {
      return fieldName.toArray();
    }

    public Object[] fetchRow() {
      Vector rdat = (Vector) usrValue.elementAt(fetchCount++);
      return rdat.toArray();
    }

    public Object getData(int col,int row) {
      Vector rdat = (Vector) usrValue.elementAt(row);
      return rdat.elementAt(col);
    }

    public Object getData(String colname,int row) {
      int col = fieldName.indexOf(colname);
      Vector rdat = (Vector) usrValue.elementAt(row);
      return rdat.elementAt(col);
    }

    public int execUpdate(String sqlStmt) {
      int num;
      try {
           Statement stmt = con.createStatement();
           String query = sqlStmt;
           num = stmt.executeUpdate(query);
           stmt.close();
      }
      catch(Exception e) {
        System.out.println(e);
        num = -1;
      }
      return num;
    }

    public void execQuery(String sqlStmt) {
      try {
        Statement stmt = con.createStatement();
        String query = sqlStmt;
        ResultSet rs = stmt.executeQuery(query);
        fieldName = new Vector();
        usrValue = new Vector();

        //initialize results
        fieldName.clear(); 
        usrValue.clear(); 
        fetchCount=0;

        ResultSetMetaData result = rs.getMetaData();
        this.Cols = result.getColumnCount();
        int cnt=0;
        while (rs.next()) {
          Vector rdat = new Vector();
          for (int i=1;i<=this.Cols;i++) {
              if (cnt==0) this.fieldName.addElement(result.getColumnName(i));
	      rdat.addElement(rs.getObject(i));
          }
	  this.usrValue.addElement(rdat);
          cnt++;
        }
        this.Rows = cnt;
        rs.close();
        stmt.close();
      } catch (Exception e) {
        System.err.println("データベースクエリエラー");
    }
    return;
  }

    public void Close() {
       try {
	  con.close();
       } catch (Exception e) {
       System.err.println("データベース切断エラー");
       }
       return;
    }
   
}
