package jp.co.saias.ikensyo;

import java.io.*;
import java.util.Calendar;
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

public class IkensyoPatientExport extends IkensyoPatientImport {

    String dbOutPath=null;
    String realOutPath=null;
    String realInPath=null;
    boolean isMbOutPath;

    public IkensyoPatientExport() {
        propertyFile = getPropertyFile(); 
        dbServer = getProperty("doc/DBConfig/Server");
        dbPath = getProperty("doc/DBConfig/Path");
        dbPort = getProperty("doc/DBConfig/Port");
    }

    public JDialog  dbUpdate(JButton execBtn,final IkensyoExecTransaction dbexec) throws Exception {

      realInPath = dbPath;
      if (dbOutPath==null) {
        fr = (parent!=null) ? new JDialog(parent) : new JDialog();
        fr.setTitle("医見書 患者データユーティリティ");

        if (!checkLocalHost(dbServer)) {
           cancel(); 
        }
        contentPane = fr.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        boolean kstat = false;
        while (!kstat) {
          if (!checkDBPath(dbPath)) {
            cancel(); 
          }
          String uri = dbServer + "/" + dbPort + ":" + dbPath;
          iTable = new IkensyoPatientSelect(uri,getProperty("doc/DBConfig/UserName"),getProperty("doc/DBConfig/Password"));
          if (iTable.Rows<0) {
            statMessage(STATE_ERROR,"データベースに接続できません。\n医見書が問題なく起動する状態かどうかご確認ください。");
            if (isMbInPath) new File(dbPath).delete();
            return null;
          }
          if (iTable.Rows==0) {
            if ( JOptionPane.showConfirmDialog(
                  fr,
                  "現在設定されているデータベースには患者データが存在しません。\n別のデータベースを選択しますか？",
                  "患者データ書き出し",JOptionPane.YES_NO_OPTION
                 )==JOptionPane.NO_OPTION) {
                if (isMbInPath) new File(dbPath).delete();
                runStat = STATE_COMPLETE;
                fr.dispose();
                dbexec.interrupt();
                return null;
            }
            String origPath = dbPath;
            if (isMbInPath) new File(dbPath).delete();
            dbPath = getImportDBPath(1); 
            realInPath = dbPath;
            if (dbPath==null) return null;
            if (isMbInPath) {
              dbPath = new File(origPath).getParent()+"/exportwork.fdb";
              try {
                new DngFileUtil().fileCopy(realInPath,dbPath);
              } catch(Exception err) {
                 statMessage(STATE_ERROR,"作業領域の確保ができません。\n書き出し元ファイルを日本語文字を含まないパスに置いて実行しなおしてみて下さい。");
                 return null;
              }
            }
          }
          else kstat = true;
        }
      } 
      else contentPane.removeAll();

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

      final JButton exitBtn = new JButton("終了");
      exitBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
      final ActionListener exitNow = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          runStat = STATE_COMPLETE;
          if (isMbInPath) new File(dbPath).delete();
          if (isCalled) {
            fr.dispose();
            parent.setEnabled(true);
            parent.setVisible(true);
          }
          else System.exit(0);
          return; 
        }
      };
      exitBtn.addActionListener(exitNow);

        JLabel title = new JLabel(" 医見書 患者別データの書き出し");
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        JPanel northP = new JPanel(new BorderLayout());
        northP.add(title,BorderLayout.NORTH);
        contentPane.add(northP);
        center0P = new JPanel();
        center0P.add(execBtn);
        center0P.add(exitBtn);
        JPanel centerP = new JPanel();
        JLabel dispPath = new JLabel("  現在のデータベース："+realInPath);
        dispPath.setFont(new Font("Serif",Font.PLAIN,12));
        dispPath.setForeground(Color.darkGray);
        northP.add(dispPath,BorderLayout.CENTER);
        JLabel chinf= new JLabel(" ※データベース変更は、医見書本体の\"データベース設定\"で行って下さい。");
        chinf.setFont(new Font("Dialog",Font.ITALIC,10));
        chinf.setForeground(Color.blue);
        northP.add(chinf,BorderLayout.SOUTH);
        JLabel lab1 = new JLabel("患者一覧   Ctrl(Shift) + マウスクリックで複数選択可能、全選択はCtrl + A");
        lab1.setFont(new Font("Dialog",Font.PLAIN,12));
        centerP.add(lab1);
        contentPane.add(centerP);
        contentPane.add(iTable.getScrollList());
        contentPane.add(center0P);
        WindowAdapter AppCloser =  new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            runStat = STATE_COMPLETE;
            if (isMbInPath) new File(dbPath).delete();
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
        fr.setSize(680,610);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension sz = fr.getSize();
        fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
        return fr;
    }

    public void execExport() {
    
      final JProgressBar pb = new JProgressBar();
      final JLabel tit1 = new JLabel("【患者データ書き出し】");
      final JLabel tit = new JLabel("患者データを書き出しています。");
      tit.setHorizontalAlignment(JLabel.LEFT);
      int stat = STATE_SUCCESS;

      final IkensyoExecTransaction dbexec= new IkensyoExecTransaction(propertyFile,0,pb);
      pb.setStringPainted(true);
      pb.setMinimum(0);

      final JButton sb = new JButton("書き出し");
      sb.setFont(new Font("SanSerif",Font.PLAIN,14));
      final JPanel pn0 = new JPanel();
      final ActionListener actionListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
          dbexec.pause();
          if (dbexec.runStat1) return;
          if ( 
            JOptionPane.showConfirmDialog(
              fr,
              "書き出しを中止しますか？\n「はい」を押すと以降の書き出しを中止し、\n書き出し済みの患者のみのファイルが作成されます。",
              "患者データ書き出し",JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION
              ) {
            if (dbexec.runStat1) {
              statMessage(STATE_INFO,"既に全ての書き出しが完了しています。");
              return;
            }
            dbexec.interruptExec();
          }
          else  {
            if (dbexec.runStat1) return;
            dbexec.restart();
          }
        }
      };
      ActionListener actionStart = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int pNos[][] = prepareExec();
          if (pNos!=null && pNos.length>0) {
              pb.setValue(0);
              pb.setString("0/"+String.valueOf(pNos.length)+"件");
              dbOutPath = getExportDBPath(dbOutPath);
              if (dbOutPath==null) return;
              if (dbOutPath.compareToIgnoreCase(realInPath)==0) {
                 statMessage(STATE_ERROR,"書き出し元と同一ファイルに書き出す事はできません。\n処理を中止します。");
                dbOutPath=null;
                return;
              }
            dbexec.setPnos(pNos);
            pb.setMaximum(pNos.length);
            JLabel tit0 = new JLabel(pNos.length+"人分の患者データ書き出し処理を開始します。");
            tit0.setFont(new Font("Dialog",Font.BOLD,12));
            JPanel pn = new JPanel();
            pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
            pn.add(tit0);
            pn.add(dupName);
            if (JOptionPane.showConfirmDialog(fr,pn,"患者データ書き出し",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {

              File ofp = new File(dbOutPath);
              //if (ofp.exists()) new DngFileUtil().moveFile(dbOutPath,dbOutPath+".old");
              if (ofp.exists()) ofp.renameTo(new File(dbOutPath+".old"));

              if (isMbOutPath) {
                realOutPath = dbOutPath;
                dbOutPath = new File(dbPath).getParent()+"/ikenwork.fdb";
              }

              try {
                new DngFileUtil().fileCopy(dbPath,dbOutPath);
              } catch(IOException er) {
                er.printStackTrace();
                statMessage(STATE_ERROR,"書き出し用データベースの作成に失敗しました。");
                return;
              }
              if (!initExportDB(dbOutPath)) {
                return;
              } 
              center0P.setVisible(false);
              dbexec.dbOutPath = dbOutPath;
              pn0.setVisible(true);
              dbexec.restart();
            }
          }
          else {
            statMessage(STATE_ERROR,  "患者が選択されていません。");
          }
        }
      };
      sb.addActionListener(actionStart);

      try {
        if (dbUpdate(sb,dbexec)==null){
          runStat=STATE_COMPLETE;
          return;
        }
        dbexec.setTable(iTable,null);
      } catch (Exception e) {
        statMessage(STATE_ERROR,"患者データ一覧の取得失敗");
        return;
      }

      final JButton cb = new JButton("キャンセル");
      cb.setFont(new Font("SanSerif",Font.PLAIN,14));
      cb.addActionListener(actionListener);
      pn0.setBackground(Color.white);
      pn0.add(new JLabel("患者データの書き出し中......."));
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
          if (dbexec.stat==STATE_SUCCESS || dbexec.stat==STATE_CANCEL) {
            pn0.removeAll();
            pn0.add(new JLabel("書き出し先ファイルの最適化中......."));
            pn0.setVisible(true);
            finalizeExportDB();
            if (isMbOutPath) {
              try {
                new DngFileUtil().fileCopy(dbOutPath,realOutPath);
              } catch(IOException er) {
                er.printStackTrace();
                statMessage(STATE_ERROR,"書き出し先ファイルの保存に失敗しました。");
                return;
              }
              finally {
                new File(dbOutPath).delete();
                dbOutPath = realOutPath;
              }
            }
            pn0.setVisible(false);
            pn0.removeAll();
            pn0.add(new JLabel("患者データの書き出し中......."));
            pn0.add(cb);
          }
          //System.out.println("runStat = "+runStat);
          if (runStat==STATE_COMPLETE) { 
            if (isMbInPath) new File(dbPath).delete();
            return;
          }
          statMessage(dbexec.stat,dbexec.errMessage);
          //if (dbexec.errSql!=null) System.out.println(dbexec.errSql);
          runStat = dbexec.stat;
          //System.out.println("dbexec.stat = "+runStat+" dbexec.runStat0= "+dbexec.runStat0);
        }
        catch (InterruptedException er) {
          fr.setVisible(false);
        }
        center0P.setVisible(true);
      }
      if (isMbInPath) new File(dbPath).delete();
      return;
    }

  public void statMessage(int stat,String err) {
    String title = "患者データ書き出し";
    switch (stat) {
      case STATE_INFO:
        JOptionPane.showMessageDialog(
           fr, err, title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

      case STATE_SUCCESS:
        JOptionPane.showMessageDialog(
           fr, "書き出し完了しました。", title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

       case STATE_CANCEL:
         JOptionPane.showMessageDialog(
           fr,"書き出しを途中で中断しました。以降の処理は中止します。",title,
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
         pn1.add(new JLabel("実行継続不能エラー"),BorderLayout.NORTH);
         pn1.add(new JLabel(err),BorderLayout.SOUTH);
         JOptionPane.showMessageDialog(
           fr,pn1, title,
           JOptionPane.ERROR_MESSAGE
         );
         break;
    }
  }

  private String getExportDBPath(String outPath) {
    String path = null;
    String ext[] = {"fdb"};
    String fname = null; 
    if (outPath==null) outPath=realInPath;
    else fname = (new File(outPath)).getName();

    try {
      if (fname==null) {
        Calendar c = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append("PATIENT");
        sb.append(c.get(c.YEAR));
        String mm = (new Integer(c.get(c.MONTH)+1)).toString();
        if ((c.get(c.MONTH)+1)<10) sb.append("0");
        sb.append(mm);
        String dd = (new Integer(c.get(c.DATE))).toString();
        if (c.get(c.DATE)<10) sb.append("0");
        sb.append(dd);
        sb.append(".fdb");
        fname = sb.toString();
      }
      DngFileChooser chooser = new DngFileChooser(fr,"FDB file for Ikensyo2.5",ext);
      chooser.setTitle("書き出し用FDBファイルの保存場所を指定して下さい。");
      chooser.setMBPathEnable(true);
      File file = chooser.saveFile(outPath,fname);
      path = file.getPath();
      isMbOutPath = chooser.isMbPath;
    } catch(Exception e) {
      if (!isCalled) {
        statMessage(STATE_CANCEL,e.getMessage());
        System.exit(1);
      }
      else return null;
    }
    return path;
  }


    public static void main(String[] args) {
        IkensyoPatientExport ipi = new IkensyoPatientExport();
        try {
           //while(ipi.runStat!=STATE_FATAL) {
           //  if (ipi.runStat==STATE_COMPLETE) System.exit(0);
             ipi.execExport();
           //}
           System.exit(0);
        }
        catch(Exception e) {
          ipi.statMessage(STATE_FATAL,e.getMessage());
          System.exit(1);
        }
    }

    public boolean initExportDB(String path) {
        String tables[] = { "DOCTOR" ,"GRAPHICS_COMMAND" ,"IKENSYO_VERSION"
                         ,"INSURER" ,"JIGYOUSHA" ,"M_DISEASE"
                         ,"M_HELP_TEIKEIBUN" ,"M_INSTITUTION" ,"M_KINGAKU_TENSU"
                         ,"M_POST" ,"M_SINRYOUKA" ,"RENKEII" ,"STATION" ,"TAX"
                         ,"TEIKEIBUN"};

        String pTable[] = { "PATIENT" ,"COMMON_IKN_SIS" ,"IKN_BILL" 
                           ,"IKN_ORIGIN" ,"SIS_ORIGIN"};
        String dbUser = getProperty("doc/DBConfig/UserName");
        String dbPass = getProperty("doc/DBConfig/Password");
        String dbUri = dbServer+"/"+dbPort+":"+path;
        DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        if (!dbm.connect()) {
          statMessage(STATE_ERROR,"書き出し用データベースに接続できません。\nDB:"+dbUri);
          return false;
        }
        String sql;
        for(int i=0;i<tables.length;i++) {
          sql = "drop table "+tables[i];        
          dbm.execUpdate(sql);
        }
        for(int i=0;i<pTable.length;i++) {
          sql = "delete from "+pTable[i];        
          dbm.execUpdate(sql);
        }
        dbm.Close();
        dbOutPath = path;
        
        return true;
    }
 
    public void finalizeExportDB() {

      boolean is20 = false;
      boolean is21 = false;
      String dbUser = getProperty("doc/DBConfig/UserName");
      String dbPass = getProperty("doc/DBConfig/Password");
      String dbTmpPath = dbOutPath+".fbak";
      String[] envp= new String[1];
      String gbak;

      String cmd[] = new String[8];
      String quot = "";
      String rmc = null;   
      String osn = System.getProperty("os.name").substring(0,3);

      if (osn.equals("Mac")) {
         cmd[0] = "/Library/Frameworks/Firebird.framework/Versions/A/Resources/bin/gbak";
      }
      else { 
        Process process;
        try {
          if (osn.equals("Win")) process = Runtime.getRuntime().exec("cmd.exe /c ECHO %ProgramFiles%");
          else process = Runtime.getRuntime().exec("which gbak");
          InputStream is = process.getInputStream();
          BufferedReader br = new BufferedReader(new InputStreamReader(is));
          cmd[0] = br.readLine();
        } catch (Exception e) {
          return;
        }
        if (cmd[0].equals(null)) return;
      }
      if (osn.equals("Win")) {
        quot = "\"";
        gbak = cmd[0]+"\\Firebird\\Firebird_1_5\\bin\\gbak.exe";
        if (! (new File(gbak)).exists()) {
          gbak = cmd[0]+"\\Firebird\\Firebird_2_0\\bin\\gbak.exe";
          if (! (new File(gbak)).exists()) {
            cmd[0] = quot+cmd[0]+"\\Firebird\\Firebird_2_1\\bin\\gbak.exe"+quot;
            is21 = true;
          }
          cmd[0] = quot+cmd[0]+"\\Firebird\\Firebird_2_0\\bin\\gbak.exe"+quot;
          is20 = true;
        }
        else {
          cmd[0] = quot+gbak+quot;
        }
      //  rmc = "cmd.exe /c del "+quot+dbTmpPath+quot;
      }
     // else rmc = "rm "+dbTmpPath;
      
      cmd[1] = "-b";
      cmd[2] = "-user";
      cmd[3] = dbUser;
      cmd[4] = "-pass";
      cmd[5] = dbPass;
      cmd[6] = quot+dbOutPath+quot;
      cmd[7] = quot+dbTmpPath+quot;

      try {
          Runtime runtime = Runtime.getRuntime();
          Process process = runtime.exec(cmd,null);
          //InputStream is = process.getInputStream();
          //BufferedReader br = new BufferedReader(new InputStreamReader(is));
          //String line;
          //while((line=br.readLine())!=null) {
          //  System.out.println(line);
          //}
          int tmpI = process.waitFor();
          if (tmpI==0) {
             //if (is20) {
             //  gbak = cmd[0];
             //  cmd = new String[9];
             //  cmd[0] = gbak;
             //  cmd[1] = "-r";
             //  cmd[2] = "-REP";
             //  cmd[3] = "-user";
             //  cmd[4] = dbUser;
             //  cmd[5] = "-pass";
             //  cmd[6] = dbPass;
             //  cmd[7] = quot+dbTmpPath+quot;
             //  cmd[8] = quot+dbOutPath+quot;
             //} else {
               cmd[1] = "-rep";
               cmd[6] = quot+dbTmpPath+quot;
               cmd[7] = quot+dbOutPath+quot;
            // }
             process = runtime.exec(cmd,null);
             tmpI = process.waitFor();
             if (!osn.equals("Win") && !osn.equals("Mac")) new DngFileUtil().chMod("666",cmd[7]);
             //new File(dbTmpPath).delete();
             //process = runtime.exec(rmc);
             //tmpI = process.waitFor();
          }
      } catch (Exception e) {
          System.out.println(e.toString());
      }
    }
}

