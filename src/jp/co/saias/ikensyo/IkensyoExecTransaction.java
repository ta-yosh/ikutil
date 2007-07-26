package jp.co.saias.ikensyo;

import java.io.*;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JLabel;

import jp.co.saias.lib.*;
import jp.co.saias.util.*;

public class IkensyoExecTransaction extends Thread {

    private static final int STATE_SUCCESS = 0;
    private static final int STATE_CANCEL = -1;
    private static final int STATE_ERROR = -2;
    private static final int STATE_FATAL = -3;

    public int stat=STATE_SUCCESS;
    public String errMessage;
    public String errSql;
    public boolean isStarted = false;
    boolean runStat0 = false;
    boolean runStat = true;
    boolean runStat1 = false;
    String pfile;
    int pNos[][];
    JProgressBar progressBar;
    int count;
    IkensyoPatientSelect iTable;
    IkensyoPatientSelect oTable;
    public String dbOutPath;

    public void setPnos(int pNo[][]) {
      pNos = pNo;
      count = pNos.length;
    }

    public IkensyoExecTransaction(String pFile,int total,JProgressBar bar) {
      count = total;
      progressBar = bar;
      pfile = pFile; 
    }
    public IkensyoExecTransaction(int total,JProgressBar bar) {
      count = total;
      progressBar = bar;
    }

    public void setTable(IkensyoPatientSelect iTable,IkensyoPatientSelect oTable) {
      this.iTable=iTable;
      this.oTable=oTable;
    }
    
    public void run() {
      isStarted = true;
      stat = STATE_SUCCESS;
      DngDBAccess dbm=null; 
      FileWriter fos=null;
      try {
        synchronized(this) {
          while(!runStat0) wait();
        }
      } catch(InterruptedException ie) {
         stat = STATE_CANCEL;
         System.out.println("Interrupted before exec");
         return;
      }
      int lcount = 0;
      if (pfile!=null) {
        DngAppProperty props = new DngAppProperty(pfile); 
        String dbPort = props.getProperty("DBConfig/Port");
        String dbUser = props.getProperty("DBConfig/UserName");
        String dbPass = props.getProperty("DBConfig/Password");
        String dbPath = (oTable==null) ? dbOutPath:props.getProperty("DBConfig/Path");
        String dbServer = props.getProperty("DBConfig/Server");
        String dbUri = dbServer+"/"+dbPort+":"+dbPath;
        dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        if (!dbm.connect()) {
          stat = STATE_FATAL;
          errMessage = "データベースに接続できません。\nDB:"+dbUri;
          interrupt();
          return;
        }
      }
      else {
        try{
          fos = new FileWriter( dbOutPath );
        } catch (IOException e) {
          errMessage = "ファイルをオープンできません。 \nFile:"+dbOutPath;
          interrupt();
          return;
        }
      }
      for (int i=0;i<pNos.length;i++) {
        if (pNos[i]==null) continue;
        try {
          sleep(0);
          synchronized(this) {
            while(!runStat) wait();
          }
        } catch(InterruptedException ie) {
          stat = STATE_CANCEL;
          System.out.println("Interrupted exec");
          break;
        }
        if (stat == STATE_CANCEL) break;
        if (stat==STATE_SUCCESS) {
          String sql;
          String bsql=null;
          do {
            bsql = (pfile!=null) ? iTable.getPatientBasicDataSql(pNos[i][0]) :
                             iTable.getPatientBasicDataCsv(pNos[i][0]);
            if (bsql.equals("CON0")) {
              System.out.println("DB server has been busy. I try to connect again 20sec. after.... please wait.");
              try {sleep(20000);} catch(Exception ie){};
            }
          } while (bsql.equals("CON0"));
          //System.out.println(bsql);
          int dNum[]=null;
          int patientNo=0;
          if (pfile!=null) {
            String type[] = {"IKN_ORIGIN","IKN_BILL","SIS_ORIGIN","COMMON_IKN_SIS","PATIENT"};
            dbm.begin();
            if (pNos[i][1]>0) {
              StringBuffer sb = new StringBuffer();
              dNum = new int[pNos[i].length-1];
              for (int j=1;j<pNos[i].length;j++) {
                if (j>1) sb.append(",");
                sb.append(pNos[i][j]);
                dNum[j-1] = pNos[i][j];
              }

              for (int j=0;j<type.length;j++) {
                sql = "delete from "+type[j]+" where PATIENT_NO in ("+sb.toString()+")";
                //System.out.println(sql);
                dbm.execUpdate(sql);
              }
            }
            int dbstat=dbm.execUpdate(bsql);
            if (dbstat!=-1 && pNos[i][1]>=0 ) {
              sql = "select GEN_ID(GEN_PATIENT,0) from RDB$DATABASE";
              dbm.execQuery(sql);
              patientNo = Integer.parseInt((dbm.getData(0,0)).toString());
              for (int j=0;j<type.length-1;j++) {
                String sqls[] = iTable.getPatientDataSql(type[j],pNos[i][0],patientNo);
                if (sqls==null) continue;
                for (int k=0;k<sqls.length;k++) {
                  sql = sqls[k];
                  //System.out.println(sql);
                  if (dbm.execUpdate(sql)==-1) {
                     stat=STATE_ERROR;
                     break;
                  }
                }
                if (stat==STATE_ERROR) {
                  errSql=sql;
                  break;
                }
              }
            } 
            else if (dbstat==-1) {
              stat=STATE_ERROR;
              errSql=bsql;
            }
          }
          else {
            try {
            fos.write(bsql);
            fos.write("\r\n");
            } catch(IOException ex) {
              stat=STATE_ERROR;
              errMessage = "書き出し用CSVファイルに書き込めません。";
              break;
            }
          }
          if (stat==STATE_SUCCESS) {
            if (oTable!=null) {
              dbm.commit();
              if (dNum!=null) {
                oTable.removeRows(dNum);
              }
              oTable.addRow(iTable.getPatientByPno(pNos[i][0],patientNo));
            }
            progressBar.setValue(++lcount);
            progressBar.setString(String.valueOf(lcount)+"/"+count+"件");
          }
          else {
            errMessage = "取り込みデータに問題があります。";
            if (pfile!=null) { dbm.rollback();}
            else { try{ fos.close();} catch(IOException ex){} }
            break;
          }
        }
      }
      if (pfile!=null && oTable==null && stat>STATE_ERROR) dbm.commit();
      else if (pfile!=null) {dbm.rollback();}
      runStat0 = false;
      runStat1 = true;
      if (pfile!=null) {dbm.Close();}
      else { try{ fos.close();} catch(IOException ex){} }
    }

    synchronized public void pause() {
      if (runStat1) return;
      runStat = false;
    }
    synchronized public void restart() {
      runStat0 = true;
      runStat1 = false;
      runStat = true;
      notifyAll(); 
    }
    synchronized public void interruptExec() {
      if (runStat1) return;
      stat = STATE_CANCEL;
      runStat0 = false;
      interrupt();
      runStat = true;
      notifyAll(); 
    }
}
