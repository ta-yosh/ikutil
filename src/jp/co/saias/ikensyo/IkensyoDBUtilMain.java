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
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

public class IkensyoDBUtilMain {

  final Image icon = (new ImageIcon(getClass().getClassLoader().getResource("jp/co/saias/ikensyo/icon/dbutil.png"))).getImage();

  public static void main(String[] args) {

    final IkensyoDBUtilMain idm = new IkensyoDBUtilMain();
    final JFrame fr = new JFrame();
    fr.setTitle("医見書 患者データユーティリティ Ver2.3");
    fr.setIconImage(idm.icon);
    final Container contentPane = fr.getContentPane();
    contentPane.setLayout(new BorderLayout());

    final JButton imb = new JButton("患者別データ取り込み");
    imb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerImport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,1);
        it.start();
        //it.restart();
      }
    };
    imb.addActionListener(triggerImport);

    final JButton exb = new JButton("患者別データ書き出し");
    exb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerExport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,2);
        it.start();
        //it.restart();
      }
    };
    exb.addActionListener(triggerExport);

    final JButton csb = new JButton("患者基本情報CSV書き出し");
    csb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerCsvOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,3);
        it.start();
        //it.restart();
      }
    };
    csb.addActionListener(triggerCsvOut);

    final JButton ssb = new JButton("訪問看護ステーション一覧");
    ssb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerShisetsuOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,4);
        it.start();
        //it.restart();
      }
    };
    ssb.addActionListener(triggerShisetsuOut);

/*
    final JButton web = new JButton("医見書ウェブサイト");
    web.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerWeb = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,4);
        it.start();
        //it.restart();
      }
    };
    web.addActionListener(triggerWeb);
*/
    final JButton cb = new JButton("終了");
    cb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener appExit = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    };
    cb.addActionListener(appExit);
    JPanel pn = new JPanel(new GridLayout(0,1));
    pn.add(exb);
    pn.add(imb);
    pn.add(csb);
    int ysiz=200;
    if ((new IkensyoShisetsuDetect()).stationDetect()==true) {
      pn.add(ssb);
      ysiz=ysiz+50;
    }
    JLabel sysTitle = new JLabel("医見書 患者データユーティリティ");
    sysTitle.setFont(new Font("SanSerif",Font.BOLD,15));
    contentPane.add(sysTitle,BorderLayout.NORTH);
    contentPane.add(pn,BorderLayout.CENTER);
    contentPane.add(cb,BorderLayout.EAST);

    WindowAdapter AppCloser =  new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    fr.addWindowListener(AppCloser);

    fr.setSize(320,ysiz);
    Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension sz = fr.getSize();
    fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
    fr.setVisible(true);
    //try {
    //  it.join();
    //} catch(InterruptedException ex) {
    //  return;
    //}
  }
}

class execThread extends Thread {
 
  JFrame frm;
  int type;
  public boolean runStat = false;

  execThread(JFrame frm,int type) {
     this.frm = frm;
     this.type = type;
  }

  public void run() {
    //frm.setVisible(false);
    //try {
    //   synchronized(this) {
    //     while(!runStat) wait();
    //   }
    //} catch(InterruptedException e) {
    //}
    if (type==1) {
      IkensyoPatientImport ipi = new IkensyoPatientImport();
      if (!ipi.vStat) {
        ipi.statMessage(ipi.STATE_FATAL,"正しくないデータベース設定です。医見書を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipi.setParent(frm);
      try {
        while(ipi.runStat!=ipi.STATE_FATAL) {
          if (ipi.runStat==ipi.STATE_COMPLETE) {
            ipi.destroy();
            break;
          }
          ipi.execImport();
        }
      }
      catch(Exception ex) {
        ipi.statMessage(ipi.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==2) {
      IkensyoPatientExport ipe = new IkensyoPatientExport();
      if (!ipe.vStat) {
        ipe.statMessage(ipe.STATE_FATAL,"正しくないデータベース設定です。医見書を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipe.setParent(frm);
      try {
        while(ipe.runStat!=ipe.STATE_FATAL) {
          //System.out.println("STAT = "+ipe.runStat); 
          if (ipe.runStat==ipe.STATE_COMPLETE) {
            ipe.destroy();
            break;
          }
          ipe.execExport();
        }
      }
      catch(Exception ex) {
        ipe.statMessage(ipe.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==3) {
      IkensyoPatientCsvOut ipc = new IkensyoPatientCsvOut();
      if (!ipc.vStat) {
        ipc.statMessage(ipc.STATE_FATAL,"正しくないデータベース設定です。医見書を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipc.setParent(frm);
      try {
        while(ipc.runStat!=ipc.STATE_FATAL) {
          //System.out.println("STAT = "+ipc.runStat); 
          if (ipc.runStat==ipc.STATE_COMPLETE) {
            ipc.destroy();
            break;
          }
          ipc.execCsvOut();
        }
      }
      catch(Exception ex) {
        ipc.statMessage(ipc.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==4) {
      IkensyoShisetsuOut ipc = new IkensyoShisetsuOut();
      if (!ipc.vStat) {
        ipc.statMessage(ipc.STATE_FATAL,"正しくないデータベース設定です。医見書を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipc.setParent(frm);
      try {
        while(ipc.runStat!=ipc.STATE_FATAL) {
          //System.out.println("STAT = "+ipc.runStat); 
          if (ipc.runStat==ipc.STATE_COMPLETE) {
            ipc.destroy();
            break;
          }
          ipc.execShisetsuOut();
        }
      }
      catch(Exception ex) {
        ipc.statMessage(ipc.STATE_FATAL,ex.getMessage());
      }
    }
    //else  {
    //  IkensyoPatientExport dw = new IkensyoJumpWeb();
    //  dw.setParent(frm);
    //  dw.disp();
   // }
  }

  synchronized public void pause() {
    runStat = false;
  }

  synchronized public void restart() {
    runStat = true;
    notifyAll();
  }

}
