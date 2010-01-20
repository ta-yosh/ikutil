package jp.co.saias.ikensyo;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

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

    public IkensyoPatientSelect() {
    }
    public IkensyoPatientSelect(String csvFile) {
      String line;
      Rows=0;
      try {
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        while ((line=reader.readLine()) !=null) {
          String[] ritems = new String[] {"","","","","","","","","","",""};
          String[] items = line.split(",");
          int skip=0;
          int col=0;
          Vector rdat = new Vector();
          rdat.addElement(new Integer(Rows+1));
          for (int i=0;i<items.length;i++) {
            if (skip>0) {
              skip--;
              continue;
            }
            col++;
            String item = (items[i]!=null) ? items[i]:"";
            if (item.matches("^\"(([^\"]|[^a-zA-Z_0-9\"])*?(\"\")*?[^\"]*?)*?\"$")) {
              item = item.replaceAll("^\"","").replaceAll("\"$","");
            }
            else if (item.matches("^\"(([^\"]|[^a-zA-Z_0-9\"])*?(\"\")*?[^\"]*?)*$")) {
              while (! item.matches("^\"(([^\"]|[^a-zA-Z_0-9\"])*?(\"\")*?[^\"]*?)*?\"$")) { 
                skip++;
                item = item +","+ items[i+skip];
              }
              item = item.replaceAll("^\"","").replaceAll("\"$","");
            }
            item = item.replaceAll("\"\"","\"");
            if (col==4) item = "   "+item;
            if (col==6) {
              Integer age = new Integer(patientAge(ritems[5]));
              ritems[col] = age.toString();
            }
            if (col==9) {
              if (item.matches(".+-.+")) {
                String wk[] = item.split("-",2);
                if (wk[1].matches(".+-.+")) {
                  ritems[col] = wk[0];
                  ritems[++col] = wk[1];
                } else {
                  ritems[col] = "";
                  ritems[++col] = item;
                }
              }
              else {
                ritems[col] = "";
                ritems[++col] = item;
              }
            }
            else ritems[col] = item;
            System.out.println(col+":"+item);
            if (col==10) break;
          }
          rdat.addElement(ritems[1]);
          rdat.addElement(ritems[2]);
          rdat.addElement(ritems[4]);
          rdat.addElement(new Integer(ritems[6]));
          rdat.addElement("");
          rdat.addElement("");
          rdat.addElement("");
          rdat.addElement(ritems[5]);
          rdat.addElement(ritems[3]);
          rdat.addElement(ritems[7]);
          rdat.addElement(ritems[8]);
          rdat.addElement(ritems[9]);
          rdat.addElement(ritems[10]);
          rdat.addElement("");
          this.data.addElement(rdat); 
          Rows++;
        }
      }
      catch (Exception e) {
         System.out.println(e);
         Rows--;
      }
    }

    public IkensyoPatientSelect(String dbUri,String dbUser,String dbPass) {
      osType = System.getProperty("os.name").substring(0,3);
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PATIENT.PATIENT_NO,PATIENT.CHART_NO,");
      buf.append(" PATIENT.PATIENT_NM,PATIENT.SEX,PATIENT.BIRTHDAY,");
      buf.append(" IKN_ORIGIN.KINYU_DT,SIS_ORIGIN.KINYU_DT,");
      buf.append(" PATIENT.KOUSIN_DT,PATIENT.BIRTHDAY,PATIENT.PATIENT_KN,");
      buf.append(" PATIENT.POST_CD,PATIENT.ADDRESS,PATIENT.TEL1,PATIENT.TEL2,");
      buf.append(" PATIENT.KOUSIN_DT ");
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
        Object data[][] = new Object[15][Rows];
        for (int i=0;i<Rows;i++) {
          for (int j=0;j<15;j++) {
            data[j][i] = dbm.getData(j,i);
          }
        }
        for (int j=0;j<Rows;j++) {
          Vector rdat = new Vector();
          for (int i=0;i<15;i++) {
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
          //rdat.addElement(data[4][j].toString());
          this.data.addElement(rdat);
        }
      }
      else Rows=-1;
    }

    void setFieldName() {
      fieldName.addElement("");
      fieldName.addElement("患者ID");
      fieldName.addElement("氏名");
      fieldName.addElement("性別");
      fieldName.addElement("年齢");
      fieldName.addElement("意見書記入日");
      fieldName.addElement("指示書記入日");
      fieldName.addElement("最終更新日");
      fieldName.addElement("生年月日");
      fieldName.addElement("ふりがな");
      fieldName.addElement("〒");
      fieldName.addElement("住所");
      fieldName.addElement("電話");
      fieldName.addElement("電話2");
      fieldName.addElement("更新日");
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
          for (int j=1;j<15;j++) {
             dat.addElement(usrTbl.getValueAt(i,j));
          }
          return dat;
        }
      }
      return null;
    }

    public String getPatientBasicDataCsv(int pno) {
      StringBuffer csvRecord;
      for (int i=0;i<usrTbl.getRowCount();i++) {
        if (pno==Integer.parseInt((usrTbl.getValueAt(i,0)).toString())) {
             csvRecord = new StringBuffer();
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,1).toString().replaceAll("\"","\"\""));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,2).toString().replaceAll("\"","\"\""));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,9).toString().replaceAll("\"","\"\""));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,3).toString().replaceAll(" +",""));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,8));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,4));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,10));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,11).toString().replaceAll("\"","\"\""));
             csvRecord.append("\"");
             csvRecord.append(",");
             csvRecord.append("\"");
             csvRecord.append(usrTbl.getValueAt(i,12));
             //if (Pattern.compile("[0-9]+").matcher(usrTbl.getValueAt(i,12).toString()).find()) csvRecord.append("-");
             csvRecord.append("-");
             csvRecord.append(usrTbl.getValueAt(i,13));
             csvRecord.append("\"");
          return csvRecord.toString();
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

//  public String getPatientBasicDataSql(int pno) {
//    String sql = "select * from PATIENT where PATIENT_NO="+pno;
//    if (!dbm.connect()) return "CON0";
//    dbm.execQuery(sql);
//    dbm.Close();
//    if (dbm.Rows<1) return null;
//    Object dat[] = dbm.fetchRow();
//    Object fieldName[] = dbm.getFieldNames();
//    StringBuffer sb = new StringBuffer();
//    sb.append("insert into PATIENT (");
//    for (int i=1;i<fieldName.length;i++) {
//      if (i>1) sb.append(",");
//      sb.append(fieldName[i].toString());
//    }
//    sb.append(") values (");  
//    for (int i=1;i<fieldName.length-1;i++) {
//      if (i>1) sb.append(",");
//      if (dat[i]!=null) sb.append("'");
//      sb.append(dat[i]);
//      if (dat[i]!=null) sb.append("'");
//    }
//    sb.append(",CURRENT_TIME)");
//    return sb.toString();
//  }

    public String getPatientBasicDataSql(int pno) {
      Vector dat=getPatientByPno(pno,pno);
      if (dat==null) return null;
      String fieldName[] = {"PATIENT_NO",
                            "CHART_NO",
                            "PATIENT_NM",
                            "SEX",
                            "AGE",
                            "","","",
                            "BIRTHDAY",
                            "PATIENT_KN",
                            "POST_CD",
                            "ADDRESS",
                            "TEL1",
                            "TEL2",
                            "KOUSIN_DT",
                            "LAST_TIME"};
                           
      StringBuffer sb = new StringBuffer();
      sb.append("insert into PATIENT (");
      for (int i=1;i<fieldName.length;i++) {
        if (fieldName[i]=="") continue;
        if (i>1) sb.append(",");
        sb.append(fieldName[i]);
      }
      sb.append(") values (");  
      for (int i=1;i<dat.size();i++) {
        if (fieldName[i]=="") continue;
        String male = new String("男");
        String wk;
        switch (i) {
          case 3: String v=dat.elementAt(i).toString();
                  wk=(v.indexOf(male)>=0) ? "1":"2";
                  break;
          case 8: wk = dat.elementAt(i).toString().replaceAll("/","-");
                  break;
          case 14: if (dat.elementAt(i).toString().equals("")) wk = "CURRENT_TIMESTAMP";
                   else wk = dat.elementAt(i).toString();
                  break;
          default: wk = dat.elementAt(i).toString();
        }
          if (i>1) sb.append(",");
          if (wk!=null && wk.length()>0 && !wk.equals("CURRENT_TIMESTAMP")) 
            sb.append("'");
          sb.append((wk!=null && wk.length()>0) ? wk:"null");
          if (wk!=null && wk.length()>0 && !wk.equals("CURRENT_TIMESTAMP")) 
            sb.append("'");
      }
      sb.append(",CURRENT_TIMESTAMP)");
      //System.out.println(sb);
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
       String bd[] = birthday.split("[-/]");
       if (bd.length<3) return -1;
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
      setFieldName();
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
        DefaultTableCellRenderer ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(SwingConstants.RIGHT);

        usrTbl.getColumnModel().getColumn(0).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(0).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(1).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(2).setPreferredWidth(120);
        usrTbl.getColumnModel().getColumn(3).setPreferredWidth(45);
        usrTbl.getColumnModel().getColumn(4).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(4).setPreferredWidth(45);
        usrTbl.getColumnModel().getColumn(5).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(6).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(7).setPreferredWidth(140);
        usrTbl.getColumnModel().getColumn(8).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(8).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(9).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(9).setMaxWidth(0);
        //usrTbl.getColumnModel().getColumn(10).setPreferredWidth(50);
        usrTbl.getColumnModel().getColumn(10).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(10).setMaxWidth(0);
        //usrTbl.getColumnModel().getColumn(11).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(11).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(11).setMaxWidth(0);
        //usrTbl.getColumnModel().getColumn(12).setPreferredWidth(50);
        usrTbl.getColumnModel().getColumn(12).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(12).setMaxWidth(0);
        //usrTbl.getColumnModel().getColumn(13).setPreferredWidth(50);
        usrTbl.getColumnModel().getColumn(13).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(13).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(14).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(14).setMaxWidth(0);
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

    public JScrollPane getPatientList() {
      setFieldName();
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
        DefaultTableCellRenderer ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(SwingConstants.RIGHT);
        usrTbl.getColumnModel().getColumn(0).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(0).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(1).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(2).setPreferredWidth(120);
        usrTbl.getColumnModel().getColumn(3).setPreferredWidth(45);
        usrTbl.getColumnModel().getColumn(4).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(4).setPreferredWidth(45);
        usrTbl.getColumnModel().getColumn(5).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(5).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(6).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(6).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(7).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(7).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(8).setPreferredWidth(85);
        usrTbl.getColumnModel().getColumn(9).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(9).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(10).setPreferredWidth(75);
        usrTbl.getColumnModel().getColumn(11).setPreferredWidth(205);
        usrTbl.getColumnModel().getColumn(12).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(12).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(13).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(13).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(14).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(14).setMaxWidth(0);

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

    public String PDFout() {
      int cid=0;
      int num=0;
      float width[] = new float[9];
      int ctype[] = new int[9];
      Arrays.fill(ctype,0);
      width[cid++] = 9; //ID
      width[cid++] = 10; //氏名
      width[cid++] = 10; //ふりがな
      ctype[cid] = 7;
      width[cid++] = 3; //性別
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 3; //年齢 
      width[cid++] = 5; //生年月日
      width[cid++] = 4; //郵便番号
      width[cid++] = 23; //住所
      width[cid] = 6; //連絡先(Tel)
      Calendar cal = Calendar.getInstance();
      StringBuffer sb = new StringBuffer();
      sb.append("PATIENT");
      sb.append(cal.get(Calendar.YEAR));
      if (cal.get(Calendar.MONTH)+1<10) sb.append("0");
      sb.append(cal.get(Calendar.MONTH)+1);
      if (cal.get(Calendar.DATE)<10) sb.append("0");
      sb.append(cal.get(Calendar.DATE));
      sb.append(".pdf");
      String fname = sb.toString();

      DngPdfTable pdf = new DngPdfTable(fname,1);
      if (pdf.openPDF("患者基本情報一覧")) {
        sb.delete(0,sb.length());
        sb.append(cal.get(Calendar.YEAR));
        sb.append("年");
        if (cal.get(Calendar.MONTH)+1<10) sb.append("0");
        sb.append(cal.get(Calendar.MONTH)+1);
        sb.append("月");
        if (cal.get(Calendar.DATE)<10) sb.append("0");
        sb.append(cal.get(Calendar.DATE));
        sb.append("日");
        sb.append(" 現在");
        pdf.setSubTitle(sb.toString());
        int[] cnum = new int[] {1,2,9,3,4,8,10,11,12,13};
        Object[][] pdfDat = new Object[usrTbl.getRowCount()][9];
        Object[] pdfColName = new Object[9];
        for (int i=0;i<usrTbl.getRowCount();i++) {
           for (int j=0;j<9;j++) {
             pdfDat[i][j] = usrTbl.getValueAt(i,cnum[j]).toString().replaceAll("^ +","").replaceAll(" +$","");
             if (j==8) pdfDat[i][j] = pdfDat[i][j].toString()+"-"+ usrTbl.getValueAt(i,cnum[j+1]).toString().replaceAll("^ +","").replaceAll(" +$","");
             if (i==0) pdfColName[j] = usrTbl.getColumnName(cnum[j]);
           }
        }
        JTable pdfTbl = new JTable(pdfDat,pdfColName);
        pdf.setTable(pdfTbl,width,ctype,0);
        pdf.flush();
        return fname;
      }
      else {
        return null;
      }
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
        frame.setVisible(true);
    }

}
