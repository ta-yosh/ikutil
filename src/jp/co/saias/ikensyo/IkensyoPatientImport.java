package jp.co.saias.ikensyo; 

import java.io.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

import jp.co.saias.lib.*;
import jp.co.saias.util.*;

public class IkensyoPatientImport {

    public static final int STATE_INFO = 2;
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_CANCEL = -1;
    public static final int STATE_ERROR = -2;
    public static final int STATE_FATAL = -3;
    public static final int STATE_COMPLETE = 1;

    public String propertyFile;
    public boolean propGeted = false;
    public boolean replaceAll = false;
    public DngAppProperty Props;
    public String dbServer;
    public String dbPath0;
    public String dbPath;
    public String dbPort;
    public IkensyoPatientSelect iTable,oTable;
    public JFrame parent=null;
    public JDialog fr;
    public JPanel dupName,center0P;
    public int dupNum;
    public Container contentPane;
    public boolean isCalled=false;
    public int runStat=STATE_SUCCESS;
    public boolean vStat = true;
    boolean isMbInPath;

    public IkensyoPatientImport() {
        propertyFile = getPropertyFile(); 
        dbServer = getProperty("DBConfig/Server");
        dbPath = getProperty("DBConfig/Path");
        dbPort = getProperty("DBConfig/Port");
    }

    public void destroy() {
        fr.dispose();
    }

    public void setParent(JFrame frm) {
        this.parent = frm;
        isCalled = true;
    }

    public IkensyoPatientSelect getTable(int num) {
      switch(num) {
        case 0: return iTable;
        case 1: return oTable;
        default: return null;
      }
    }

    public Container getPane() {
      return contentPane;
    }

    public JDialog  dbUpdate(JButton execBtn,final IkensyoExecTransaction dbexec) throws Exception {

      String realInPath=null;
      if (dbPath0==null) {
        fr = (parent!=null) ? new JDialog(parent) : new JDialog();
        fr.setTitle("�師��Ver2.5 ���ԥǡ����桼�ƥ���ƥ�");

        if (!checkDBPath(dbPath)) {
           cancel(); 
        }
        if (!checkLocalHost(dbServer)) {
           cancel(); 
        }
        contentPane = fr.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        boolean kstat = false;
        String uri;
        uri = dbServer + "/" + dbPort + ":" + dbPath;
        oTable = new IkensyoPatientSelect(uri,getProperty("DBConfig/UserName"),getProperty("DBConfig/Password"));
        oTable.setSelectable(false);
        if (oTable.Rows<0) {
          statMessage(STATE_ERROR,"�師��ǡ����١�������³�Ǥ��ޤ���\n�師��Ver2.5 ������˵�ư������֤��ɤ�������ǧ����������");
          return null;
        }

        while(!kstat) {
          dbPath0 = getImportDBPath(0);
          realInPath = dbPath0;
          if (dbPath0==null) return null;
          if (dbPath0.compareToIgnoreCase(dbPath)==0) {
          statMessage(STATE_ERROR,"�����߸��ȼ������褬Ʊ��ե�����Ǥ���");
            continue;
          }

          if (isMbInPath) {
            dbPath0 = new File(dbPath).getParent()+"/importwork.fdb";
            try {
            new DngFileUtil().fileCopy(realInPath,dbPath0);
            } catch(Exception err) {
               statMessage(STATE_ERROR,"����ΰ�γ��ݤ��Ǥ��ޤ���\n�����߸��ե�������������ե������Ʊ�����ؤ��֤��Ƽ¹Ԥ��ʤ����ƤߤƲ�������");
               return null;
            }
          }
          uri = dbServer + "/" + dbPort + ":" + dbPath0;
          iTable = new IkensyoPatientSelect(uri,getProperty("DBConfig/UserName"),getProperty("DBConfig/Password"));

        if (iTable.Rows<0) {
          statMessage(STATE_ERROR,"�����߸��ǡ����١�������³�Ǥ��ޤ���\n�ǡ����١����ե�����Υ����������򤴳�ǧ����������");
          return null;
        }
          if (iTable.Rows==0) {
            statMessage(STATE_ERROR,"���򤷤��ե�����ϴ��ԥǡ�����¸�ߤ��ʤ������ޤ��ϡ��師����������ǡ����١����ǤϤ���ޤ���");
            continue;
          }
          kstat = true;
        }

      } 
      else contentPane.removeAll();

        ActionListener exitNow = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            runStat = STATE_COMPLETE;
            if (isMbInPath) new File(dbPath0).delete();
            if (isCalled) {
              fr.dispose();
              parent.setEnabled(true);
              parent.setVisible(true);
            }
            else System.exit(0);
            return;
          }
        };

        ActionListener append = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             replaceAll = false;
          }
        };

        ActionListener replace = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             replaceAll = true;
          }
        };

        JButton exitBtn = new JButton("��λ");
        exitBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
        exitBtn.addActionListener(exitNow);
        JRadioButton appendBtn = new JRadioButton("�ɲ�",((replaceAll)? false:true));
        appendBtn.setFont(new Font("Dialog",Font.PLAIN,12));
        appendBtn.addActionListener(append);
        JRadioButton replaceBtn = new JRadioButton("�֤�����",replaceAll);
        replaceBtn.setFont(new Font("Dialog",Font.PLAIN,12));
        replaceBtn.addActionListener(replace);
        ButtonGroup bg = new ButtonGroup();
        bg.add(replaceBtn);
        bg.add(appendBtn);
        JPanel rbp = new JPanel(); //new GridLayout(0,1));
        rbp.setBorder(BorderFactory.createLineBorder(Color.black));
        //rbp.setBorder(
        //  BorderFactory.createTitledBorder(
        //  BorderFactory.createLineBorder(Color.black),
        //    "��������ˡ������"
        //  )
        //);
        JLabel title = new JLabel(" �師��Ver2.5 �����̥ǡ����μ�����");
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        JLabel choi = new JLabel("��������ˡ������");
        choi.setFont(new Font("Dialog",Font.PLAIN,12));
        rbp.add(choi);
        rbp.add(replaceBtn);
        rbp.add(appendBtn);
        JPanel northP = new JPanel(new BorderLayout());
        northP.add(title,BorderLayout.NORTH);
        contentPane.add(northP);
        center0P = new JPanel();
        center0P.add(rbp);
        center0P.add(execBtn);
        center0P.add(exitBtn);
        JPanel centerP = new JPanel();
        JLabel lab0 = new JLabel("  �����߸��ǡ����١�����"+realInPath);
        lab0.setFont(new Font("Serif",Font.PLAIN,12));
        lab0.setForeground(Color.darkGray);
        northP.add(lab0,BorderLayout.CENTER);
        JLabel lab1 = new JLabel("�����߸����԰���   Ctrl(Shift) + �ޥ�������å���ʣ�������ǽ���������Ctrl + A");
        lab1.setFont(new Font("Dialog",Font.PLAIN,12));
        centerP.add(lab1);
        contentPane.add(centerP);
        contentPane.add(iTable.getScrollList());
        contentPane.add(center0P);
        JPanel southP = new JPanel(new GridLayout(0,1));
        JLabel lab2 = new JLabel("  ��������(���ߤΰ師��)�ǡ����١�����"+dbPath);
        lab2.setFont(new Font("Serif",Font.PLAIN,12));
        lab2.setForeground(Color.darkGray);
        southP.add(lab2);
        JLabel lab21 = new JLabel("    ����������ǡ����١������ѹ��ϰ師�����Τ�\"�ǡ����١�������\"�ǹԤäƲ�������");
        lab21.setFont(new Font("Dialog",Font.ITALIC,10));
        lab21.setForeground(Color.blue);
        southP.add(lab21);
        contentPane.add(southP);
        JLabel lab3 = new JLabel("�������贵�԰���");
        lab3.setFont(new Font("Dialog",Font.PLAIN,12));
        JPanel pnA = new JPanel();
        pnA.add(lab3);
        contentPane.add(pnA);
        contentPane.add(oTable.getScrollList());
        TableSorter2 iS = iTable.getSorter();
        TableSorter2 oS = oTable.getSorter();
        iS.setSynchroTableSorter(oS);
        oS.setSynchroTableSorter(iS);
        WindowAdapter AppCloser =  new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            runStat = STATE_COMPLETE;
            if (isMbInPath) new File(dbPath0).delete();
            if (isCalled) {
              fr.dispose();
              parent.setEnabled(true);
              parent.setVisible(true);
            }
            else System.exit(0);
            return;
          }
        };
        fr.addWindowListener(AppCloser);
        //fr.pack();
        fr.setSize(655,610);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension sz = fr.getSize();
        fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
        //fr.setVisible(true);
        return fr;
    }

    public boolean checkDBPath(String path) {
      File dbf = new File(path);
      if (!dbf.exists()) {
          statMessage(STATE_ERROR,"�ǡ����١��������Ĥ���ޤ���\n�師��Ver2.5��ư�����������ǡ����١����ե����뤬���ꤵ��Ƥ��뤫����ǧ��������");
        return false;
      }
      return true;
    }

    public boolean checkLocalHost(String server) {
        if (!server.equals("localhost") &&
            !server.equals("127.0.0.1")) {
            statMessage(STATE_ERROR,"�������ϥǡ����١��������ФȤʤäƤ��륳��ԥ塼���ǹԤäƲ�������");
            return false;
        }
        return true;
    }

    public String getImportDBPath(int type) {
      String path = "";
      String ext[] = {"FDB","fdb","old"};
      String moto = (type==0) ? "�����߸�":"�񤭽Ф���";
      try {
        DngFileChooser chooser = new DngFileChooser(fr,"FDB or old file for Ikensyo2.5",ext);
        chooser.setTitle(moto+"���ԥǡ����ե�����(IKENSYO.FDB����ӥХå����åפ��)����ꤷ�Ƥ���������");
        chooser.setMBPathEnable(true);
        chooser.setInitPath((dbPath0!=null) ? dbPath0:dbPath); 
        File file = chooser.getFile();
        path = file.getPath();
        isMbInPath = chooser.isMbPath;
      } catch(Exception e) {
        if (!isCalled) {
          statMessage(STATE_CANCEL,e.getMessage());
          System.exit(1);
        }
        else return null;
      }
      return path;
    }
   
    public String getProperty(String key)
    {
        String value = "";
        if (!propGeted) {
            Props = new DngAppProperty(propertyFile);
            propGeted = true;
        }
        try
        {
            value = Props.getProperty(key);
            if (value.equals("Null")) vStat = false;
        }
        catch(Exception ex)
        {
            statMessage(STATE_FATAL,ex.getMessage());
            System.exit(1);
        }
        return value;
    }

    public String getPropertyFile()  {
      String ppath;
      File pf = new File("IkensyoProperityXML.xml");
      if (!pf.exists()) {
        statMessage(STATE_ERROR, "�師��Ver2.5������ե�����(IkensyoProperityXML.xml)�����Ĥ���ޤ���\n��λ��(OK)�פ򲡤��ƽ�λ���������Υץ�����師��Ver2.5�Υ��󥹥ȡ���ǥ��쥯�ȥ�(Ikensyo2.5�ե����)\n�����֤��Ƥ���¹Ԥ��Ʋ�������");
        System.exit(1);
      }
      if (pf==null) System.exit(1);
      return pf.getAbsolutePath();
    }

    public void complete() {
      System.exit(0);
    }

    public void cancel() {
      JOptionPane.showMessageDialog(
        fr,
        "��������ߤ��ޤ���",
        "���ԥǡ����桼�ƥ���ƥ�",JOptionPane.INFORMATION_MESSAGE
      );
      System.exit(0);
    }

    public int[][] prepareExec() {
      if (!iTable.isSelected()) return null;
      Object pdat[][] = iTable.getSelectedPatients();
      if (pdat.length<1) return null;
      //String tranSql[] = new String[pdat.length];
      int pNos[][] = new int[pdat.length][];
      dupName = new JPanel(new GridLayout(0,1));
      int dupNum=0;
      for (int i=0;i<pdat.length;i++) {
        Object dat[] = new Object[4];
        dat[0] = pdat[i][1];
        dat[1] = pdat[i][2];
        dat[2] = pdat[i][3];
        dat[3] = pdat[i][4];
        int patientNo = Integer.parseInt(pdat[i][0].toString());
        //String[] iSql = iTable.getPatientDataSql("IKN_ORIGIN",patientNo);
        //String[] sSql = iTable.getPatientDataSql("SIS_ORIGIN",patientNo);
        //String[] cSql = iTable.getPatientDataSql("COMMON_IKN_SIS",patientNo);
        //tranSql[i] = new String[iSql.length+sSql.length+cSql.length+1];
        int[] pinfo = (replaceAll) ? oTable.checkDuplicate(dat): null;
        if (pinfo==null) {
           pNos[i] = new int[2];
           pNos[i][0] = patientNo;
           pNos[i][1] = 0;
           //tranSql[i][0] = iTable.getPatientBasicDataSql(patientNo);
        } else {
           pNos[i] = new int[pinfo.length+1];
           pNos[i][0] = patientNo;
           for (int j=0;j<pinfo.length;j++) {
             pNos[i][j+1] = pinfo[j];
           }
           if (dupNum==0) {
             JLabel l1 = new JLabel("�ʤ�����������ˡ�Ȥ���\"�֤�����\"�����򤵤�Ƥ��ޤ�");
             JLabel l2 = new JLabel("��������ΰʲ��δ��Ծ���ϼ����߸��Ƚ�ʣ���Ƥ��뤿�������졢");
             JLabel l3 = new JLabel("�����߸���Ʊ�촵�Ԥξ�����֤��������ޤ���");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             l2.setFont(new Font("Dialog",Font.PLAIN,12));
             l3.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
             dupName.add(l2);
             dupName.add(l3);
           }
           if (++dupNum < 10) { 
             JLabel l1 = new JLabel(dat[1].toString());
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
           else if(dupNum==10) {
             JLabel l1 = new JLabel("¾ ¿��");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
           //int iknEdaNo = pinfo[1]+1;
           //int qknEdaNo = pinfo[2]+1;
           //tranSql[i][0] = "UPDATE "+patientNo + ":" +iknEdaNo+":"+qknEdaNo;
        }
      }
      return pNos;
    }

    public void execImport() {
    
      final JProgressBar pb = new JProgressBar();
      final JLabel tit1 = new JLabel("�ڴ��ԥǡ��������ߡ�");
      final JLabel tit = new JLabel("........DB�򹹿����Ƥ��ޤ����֥���󥻥�פ򲡤��Ȥ��λ����ʹߤμ����ߤ���ߤ��ޤ���");
      tit.setHorizontalAlignment(JLabel.LEFT);
      int stat = STATE_SUCCESS;

      final IkensyoExecTransaction dbexec= new IkensyoExecTransaction(propertyFile,0,pb);
      pb.setStringPainted(true);
      pb.setMinimum(0);

      final JButton sb = new JButton("������");
      sb.setFont(new Font("SanSerif",Font.PLAIN,14));
      final JPanel pn0 = new JPanel();
      final ActionListener actionListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
          dbexec.pause();
          if (dbexec.runStat1) return;
          if ( 
            JOptionPane.showConfirmDialog(
              fr,
              "�����ߤ���ߤ��ޤ�����\n�֤Ϥ�(Yes)�פ򲡤��ȸ��߽����Ѥߤδ��԰ʹߤμ����ߤ���ߤ��ޤ���",
              "���ԥǡ���������",JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION
              ) {
            if (dbexec.runStat1) {
              statMessage(STATE_INFO,"���˼����߽��������ƴ�λ���Ƥ��ޤ���");
              return;
            }
            dbexec.interruptExec();
          }
          else {
            if (dbexec.runStat1) return;
            dbexec.restart();
          }
        }
      };
      ActionListener actionStart = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int pNos[][] = prepareExec();
          if (pNos!=null && pNos.length>0) {
            dbexec.setPnos(pNos);
            pb.setValue(0);
            pb.setMaximum(pNos.length);
            pb.setString("0/"+String.valueOf(pNos.length)+"��");
            JLabel tit0 = new JLabel(pNos.length+"�ͤδ��Ԥ����򤵤�Ƥ��ޤ�����λ��פ򲡤��Ƚ����򳫻Ϥ��ޤ���");
            //tit0.setFont(new Font("Dialog",Font.PLAIN,12));
            JPanel pn1 = new JPanel();
            pn1.add(tit0);
            JPanel pn = new JPanel();
            pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
            pn.add(pn1);
            pn.add(dupName);
            if (JOptionPane.showConfirmDialog(fr,pn,"���ԥǡ���������",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {
              center0P.setVisible(false);
              pn0.setVisible(true);
              dbexec.restart();
            }
          }
          else {
            statMessage(STATE_ERROR, "���Ԥ����򤵤�Ƥ��ޤ���");
          }
        }
      };
      sb.addActionListener(actionStart);

      try {
        if (dbUpdate(sb,dbexec)==null) {
          runStat=STATE_COMPLETE;
          return;
        }
        dbexec.setTable(iTable,oTable);
      } catch (Exception e) {
        statMessage(STATE_ERROR,"���ԥǡ��������μ�������");
        return;
      }

      final JButton cb = new JButton("����󥻥�");
      cb.setFont(new Font("SanSerif",Font.PLAIN,14));
      cb.addActionListener(actionListener);
      pn0.setBackground(Color.white);
      pn0.add(new JLabel("���ԥǡ����μ�������......."));
      pn0.add(cb);
      contentPane.add(pb);
      contentPane.add(pn0);
      pn0.setVisible(false);
      parent.setEnabled(false);
      fr.setVisible(true);
      while (runStat!=STATE_FATAL) {
        dbexec.pause();
        dbexec.run();
        try {
          dbexec.join();
          pn0.setVisible(false);
          if (runStat==STATE_COMPLETE) {
            if (isMbInPath) new File(dbPath0).delete();
            return;
          }
          statMessage(dbexec.stat,dbexec.errMessage);
          runStat = dbexec.stat;
          if (runStat==STATE_ERROR) 
          System.out.println("Error caused by SQL Statements:\n"+dbexec.errSql);
        }
        catch (InterruptedException er) {
          fr.setVisible(false);
        }
        center0P.setVisible(true);
      }
      if (isMbInPath) new File(dbPath0).delete();
      return;
    }

  public void statMessage(int stat,String err) {
    String title = "���ԥǡ���������";
    switch (stat) {
      case STATE_INFO:
        JOptionPane.showMessageDialog(
           fr, err, title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

      case STATE_SUCCESS:
        JOptionPane.showMessageDialog(
           fr, "�����ߴ�λ���ޤ�����", title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

       case STATE_CANCEL:
         JOptionPane.showMessageDialog(
           fr,"�����ߤ����Ǥ��ޤ�����",title,
           JOptionPane.INFORMATION_MESSAGE
         );
         break;

       case STATE_ERROR:
         JOptionPane.showMessageDialog(
           fr,err, title,
           JOptionPane.ERROR_MESSAGE
         );
         break;

       case STATE_FATAL:
         JPanel pn1 = new JPanel(new BorderLayout());
         pn1.add(new JLabel("�¹Է�³��ǽ���顼"),BorderLayout.NORTH);
         pn1.add(new JLabel(err),BorderLayout.SOUTH);
         JOptionPane.showMessageDialog(
           fr,pn1, title,
           JOptionPane.ERROR_MESSAGE
         );
         break;
    }
  }
/*
    public static void main(String[] args) {
        IkensyoPatientImport ipi = new IkensyoPatientImport();
        ipi.setParent(null);
        try {
           while(ipi.runStat!=STATE_FATAL) {
             if (ipi.runStat==STATE_COMPLETE) System.exit(0);
             ipi.execImport();
           }
           //System.exit(0);
        }
        catch(Exception e) {
          ipi.statMessage(STATE_FATAL,e.getMessage());
          System.exit(1);
        }
    }
*/
}
