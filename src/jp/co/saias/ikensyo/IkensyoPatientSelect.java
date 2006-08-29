package jp.co.saias.ikensyo;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import jp.co.saias.util.*;
import jp.co.saias.lib.*;

public class IkensyoPatientSelect {

    private DngDBAccess dbm;
    private Vector fieldName = new Vector(); 
    private Vector data = new Vector();
    private JTable usrTbl;
    private boolean isSelectable=true;
    public int Rows;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;
    String osType;

    public IkensyoPatientSelect(String dbUri,String dbUser,String dbPass) {
      osType = System.getProperty("os.name").substring(0,3);
      fieldName.addElement("");
      fieldName.addElement("患者ID");
      fieldName.addElement("氏名");
      fieldName.addElement("性別");
      fieldName.addElement("年齢");
      fieldName.addElement("意見書記入日");
      fieldName.addElement("指示書記入日");
      fieldName.addElement("最終更新日");
      fieldName.addElement("");
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PATIENT.PATIENT_NO,PATIENT.CHART_NO,");
      buf.append("  PATIENT.PATIENT_NM,PATIENT.SEX,PATIENT.BIRTHDAY,");
      buf.append("  IKN_ORIGIN.KINYU_DT,SIS_ORIGIN.KINYU_DT,");
      buf.append("  PATIENT.KOUSIN_DT,PATIENT.BIRTHDAY,PATIENT.PATIENT_KN ");
      buf.append("from PATIENT ");
      buf.append("left outer join IKN_ORIGIN ");
      buf.append("     on (PATIENT.PATIENT_NO=IKN_ORIGIN.PATIENT_NO");
      buf.append("          and IKN_ORIGIN.EDA_NO=(");
      buf.append("              select max(EDA_NO) from IKN_ORIGIN ");
      buf.append("              where PATIENT_NO=PATIENT.PATIENT_NO)) ");
      buf.append("left outer join SIS_ORIGIN ");
      buf.append("     on ( PATIENT.PATIENT_NO=SIS_ORIGIN.PATIENT_NO ");
      buf.append("          and SIS_ORIGIN.EDA_NO=(");
      buf.append("              select max(EDA_NO) from SIS_ORIGIN ");
      buf.append("              where PATIENT_NO=PATIENT.PATIENT_NO)) ");
      buf.append("order by PATIENT.PATIENT_KN");
      String sql = buf.toString(); 
      if (dbm.connect()) {
        dbm.execQuery(sql);
        dbm.Close();
        Rows = dbm.Rows;
        Object data[][] = new Object[9][Rows];
        for (int i=0;i<Rows;i++) {
          for (int j=0;j<9;j++) {
            data[j][i] = dbm.getData(j,i);
          }
        }
        for (int j=0;j<Rows;j++) {
          Vector rdat = new Vector();
          for (int i=0;i<9;i++) {
            if (i==4) {
              String str;
              Integer age;
              try {
                str = data[i][j].toString();
                age = new Integer(patientAge(str));
              } catch(Exception e) {
                age = new Integer(0);
              }
              rdat.addElement(age);
            }
            else {
              try {
                String str = data[i][j].toString();
                if (i==3) str = "   "+((str.equals("1")) ? "男":"女");
                if (i>4 && i<7) str = str.substring(0,10);
                if (i==7) str = str.substring(0,16);
                rdat.addElement(str);
              }catch(Exception e) {
                rdat.addElement("");
              }
            }
          }
          rdat.addElement(data[4][j].toString());
          this.data.addElement(rdat);
        }
      }
      else Rows=-1;
    }

    public boolean isSelected() {
      int sel = usrTbl.getSelectedRow();
      return (sel!=-1) ? true:false;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }

    public Object[][] getSelectedPatients() {
      int rows[] = usrTbl.getSelectedRows();
      Object pdat[][] = new Object[rows.length][5];
      for (int i=0;i<rows.length;i++) {
        pdat[i][0] = usrTbl.getValueAt(rows[i],0);
        pdat[i][1] = usrTbl.getValueAt(rows[i],1);
        pdat[i][2] = usrTbl.getValueAt(rows[i],2);
        pdat[i][3] = usrTbl.getValueAt(rows[i],3);
        pdat[i][4] = usrTbl.getValueAt(rows[i],8);
      }
      return pdat;
    }

    public Vector getPatientByPno(int pno,int nno) {
      Vector dat = new Vector();
      for (int i=0;i<usrTbl.getRowCount();i++) {
        if (pno==Integer.parseInt((usrTbl.getValueAt(i,0)).toString())) {
          dat.addElement(new Integer(nno));
          for (int j=1;j<9;j++) {
             dat.addElement(usrTbl.getValueAt(i,j));
          }
          return dat;
        }
      }
      return null;
    }

    public String[] getPatientDataSql(String type,int pno,int newpno) {
      String sql = "select * from "+type+" where PATIENT_NO="+pno+" order by EDA_NO";
      
      if (!dbm.connect()) return null;
      dbm.execQuery(sql);
      dbm.Close();
      if (dbm.Rows<1) return null;
      Object fieldName[] = dbm.getFieldNames();
      int ii=0;
      String dsql[]= new String[dbm.Rows];
      Object dat[] = new Object[fieldName.length];
      for (int j=0;j<dbm.Rows;j++) {
        dat = dbm.fetchRow();
        StringBuffer sb = new StringBuffer();
        sb.append("insert into ");
        sb.append(type);
        sb.append(" (");
        for (int i=0;i<fieldName.length;i++) {
          if (i>0) sb.append(",");
          sb.append(fieldName[i].toString());
        }
        sb.append(") values (");  
        sb.append(newpno);  
        for (int i=1;i<fieldName.length;i++) {
          sb.append(",");
          if (fieldName[i].toString().equals("LAST_TIME")) {
            sb.append("CURRENT_TIMESTAMP");
          }
          else {
            if (dat[i]!=null) {
              sb.append("'");
              String str = dat[i].toString();
              str = str.replaceAll("\'","''");
              sb.append(str);
              sb.append("'");
            }
            else sb.append(dat[i]);
          }
        }
        sb.append(")");
        dsql[ii++] = sb.toString();
        //System.out.println(sb);
      }
      return dsql;
    }

    public String getPatientBasicDataSql(int pno) {
      String sql = "select * from PATIENT where PATIENT_NO="+pno;
      if (!dbm.connect()) return "CON0";
      dbm.execQuery(sql);
      dbm.Close();
      if (dbm.Rows<1) return null;
      Object dat[] = dbm.fetchRow();
      Object fieldName[] = dbm.getFieldNames();
      StringBuffer sb = new StringBuffer();
      sb.append("insert into PATIENT (");
      for (int i=1;i<fieldName.length;i++) {
        if (i>1) sb.append(",");
        sb.append(fieldName[i].toString());
      }
      sb.append(") values (");  
      for (int i=1;i<fieldName.length-1;i++) {
        if (i>1) sb.append(",");
        if (dat[i]!=null) sb.append("'");
        sb.append(dat[i]);
        if (dat[i]!=null) sb.append("'");
      }
      sb.append(",CURRENT_TIME)");
      return sb.toString();
    }

    public int[] checkDuplicate(Object dat[]) {
      ArrayList nl = new ArrayList();
      for (int i=0;i<usrTbl.getRowCount();i++) {
        if (dat[0].equals(usrTbl.getValueAt(i,1)) &&
            dat[1].equals(usrTbl.getValueAt(i,2)) &&
            dat[2].equals(usrTbl.getValueAt(i,3)) &&
            dat[3].equals(usrTbl.getValueAt(i,8)) ) {
          nl.add(usrTbl.getValueAt(i,0));

          //String sql = "select DOC_KBN,max(EDA_NO) from COMMON_IKN_SIS where PATIENT_NO="+pno+" group by DOC_KBN";
          //dbm.execQuery(sql);
          //int ikn = 0;
          //int sis = 0;
          //for (int j=0;j<dbm.Rows;j++) {
          //  if ((dbm.getData(0,j)).toString()=="1") {
          //    ikn = Integer.parseInt((dbm.getData(1,j)).toString());
          //  }
          //  if ((dbm.getData(0,j)).toString()=="2") {
          //    sis = Integer.parseInt((dbm.getData(1,j)).toString());
          //  }
          //}
        }
      }
      if (nl.isEmpty()) return null;
      int pno[] = new int[nl.size()];
      for (int i=0;i<nl.size();i++) {
        pno[i] = Integer.parseInt((nl.get(i)).toString());
      }
      return pno;
    }

    int patientAge(String birthday) {
       String bd[] = birthday.split("-");
       int yy = Integer.parseInt(bd[0]);
       int mm = Integer.parseInt(bd[1]);
       int dd = Integer.parseInt(bd[2]);
       Calendar c = Calendar.getInstance();
       int age = c.get(c.YEAR)-yy; 
       int mon = c.get(c.MONTH);
       if ((mm-mon) > 0) age--;
       else if (mm==mon && dd - c.get(c.DATE) > 0) age--;
       return age;
    }
    public JScrollPane getScrollList() {
        dtm = new DefaultTableModel(data, fieldName);
        sorter = new TableSorter2(dtm);
        usrTbl = new JTable(sorter);
        sorter.setTableHeader(usrTbl.getTableHeader());
        sorter.setCellEditableAll(false);
        sorter.setColumnClass(4,Integer.class);
        //sorter.setPrimaryKeyCol(0);
        //sorter.addMouseListenerToHeaderInTable(usrTbl);
	usrTbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	usrTbl.setRowSelectionAllowed(true);
	usrTbl.setDefaultEditor(Object.class, null);
        usrTbl.setShowGrid(false);
	if (!isSelectable) usrTbl.setCellSelectionEnabled(isSelectable);
        usrTbl.getColumnModel().getColumn(0).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(0).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(1).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(2).setPreferredWidth(120);
        usrTbl.getColumnModel().getColumn(3).setPreferredWidth(50);
        usrTbl.getColumnModel().getColumn(4).setPreferredWidth(50);
        usrTbl.getColumnModel().getColumn(5).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(6).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(7).setPreferredWidth(150);
        usrTbl.getColumnModel().getColumn(8).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(8).setMaxWidth(0);
        usrTbl.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrPane = new JScrollPane();
        scrPane.getViewport().setView(usrTbl);
        scrPane.setFont(new Font("san-serif",Font.PLAIN,14));
	scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scrPane.getHorizontalScrollBar();
	scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	scrPane.getVerticalScrollBar();
	return scrPane;
    }

    public TableSorter2 getSorter() {
        return sorter;
    }

    public void removeRows(int pno[]) {
      for (int j=0;j<pno.length;j++) {
        for (int i=0;i<usrTbl.getRowCount();i++) {
          if (pno[j]==Integer.parseInt((usrTbl.getValueAt(i,0)).toString())) {
             dtm.removeRow(sorter.modelIndex(i));
             usrTbl.repaint();
             break;
          }
        }
      }
      usrTbl.repaint();
    }

    public void addRow(Vector dat) {
      dtm.insertRow(0,dat);
      usrTbl.repaint();
    }

    public static void main(String args[]){
       JFrame frame = new JFrame();
       String uri="localhost/3050:/home/deuce/ikenj/cur/data/IKENSYO.FDB";
       String user = "sysdba";
       String pass = "masterkey";
       IkensyoPatientSelect cont = new IkensyoPatientSelect(uri,user,pass);
        frame.setTitle("DB TEST");
        frame.setBackground(Color.lightGray);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add("Center", cont.getScrollList());
        frame.pack();
        frame.setSize(650, 600);
        frame.show();
    }

}
