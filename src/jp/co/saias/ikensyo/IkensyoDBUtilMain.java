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
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

public class IkensyoDBUtilMain {

  public static void main(String[] args) {

    final IkensyoDBUtilMain idm = new IkensyoDBUtilMain();
    final JFrame fr = new JFrame();
    fr.setTitle("�師��Ver2.5 ���ԥǡ����桼�ƥ���ƥ�");
    final Container contentPane = fr.getContentPane();
    contentPane.setLayout(new BorderLayout());
    final JButton imb = new JButton("�����̥ǡ���������");
    imb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerImport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,1);
        it.start();
        //it.restart();
      }
    };
    imb.addActionListener(triggerImport);
    final JButton exb = new JButton("�����̥ǡ����񤭽Ф�");
    exb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerExport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,2);
        it.start();
        //it.restart();
      }
    };
    exb.addActionListener(triggerExport);
    final JButton cb = new JButton("��λ");
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
    JLabel sysTitle = new JLabel("�師��Ver2.5 ���ԥǡ����桼�ƥ���ƥ�");
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

    fr.setSize(320,150);
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
        ipi.statMessage(ipi.STATE_FATAL,"�������ʤ��ǡ����١�������Ǥ����師���ư���ƥǡ����١�����������ǧ���Ƥ���������");
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
    else {
      IkensyoPatientExport ipe = new IkensyoPatientExport();
      if (!ipe.vStat) {
        ipe.statMessage(ipe.STATE_FATAL,"�������ʤ��ǡ����١�������Ǥ����師���ư���ƥǡ����١�����������ǧ���Ƥ���������");
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
  }

  synchronized public void pause() {
    runStat = false;
  }

  synchronized public void restart() {
    runStat = true;
    notifyAll();
  }

}
