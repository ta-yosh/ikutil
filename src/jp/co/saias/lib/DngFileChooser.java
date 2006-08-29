package jp.co.saias.lib;

import java.io.*;
import java.util.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jp.co.saias.lib.DngFileFilter;

public class DngFileChooser {
    private Component parent;
    private String fileDescription;
    private String[] fileExtensions;
    private String title=null;
    private String initialPath=".";
    private boolean mbPath=false;
    public boolean isMbPath=false;

    public DngFileChooser(Component parent) {
        this.parent = parent;
    }

    public DngFileChooser(Component parent,String desc,String ext) {
        String exts[] = {ext};
        this.parent = parent;
        fileDescription = desc;
        fileExtensions = exts;
    }

    public DngFileChooser(Component parent,String desc,String[] exts) {
        this.parent = parent;
        fileDescription = desc;
        fileExtensions = exts;
    }

    public void setTitle(String title) {
      this.title = title;
    }
 
    public void setInitPath(String path) {
      initialPath = path;
    }

    public void setMBPathEnable(boolean mbPath) {
      this.mbPath = mbPath;
    }

    public File getFile() {
        File file=null;
        boolean loopFlg;
        String path = initialPath;
        //parent = this.parent;
        do {
            JFileChooser chooser = new JFileChooser((String)(new File(path).getPath()));
            DngFileFilter filter;
            try {
                //System.out.println("extension = "+fileExtensions[0]);
                filter = new DngFileFilter(fileDescription,fileExtensions);
            } catch(Exception e) {
                //System.out.println("extension auto");
                filter = new DngFileFilter();
            }
            chooser.setFileFilter(filter);
            if (title!=null) chooser.setDialogTitle(title);
            loopFlg = false;

            int returnVal = chooser.showOpenDialog(parent);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }

            if (file == null) {
                return null;
            }

            if (!mbPath) { 
              if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) {
                JOptionPane.showMessageDialog(parent,"取り込み元ファイルは、\n格納場所(フォルダ)やファイル名に日本語や一部の記号文字等を含むものは、使用できません。\n"+file.getPath(),"取り込み元エラー",JOptionPane.ERROR_MESSAGE);
                loopFlg = true;
                //chooser.changeToParentDirectory();
                path = chooser.getCurrentDirectory().getAbsolutePath();
                file=null;
                continue;
              }
            }
            else if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) isMbPath=true;

            if (!file.exists()) {
                loopFlg = true; 
                continue;
            }
        } while (loopFlg);

        return file;
    }

    public File saveFile(String path,String fname) {
        File file= null;
        boolean loopFlg;
        //parent = this.parent;
        do {
            JFileChooser chooser = new JFileChooser((String)(new File(path).getPath()));
            DngFileFilter filter;
            try {
                //System.out.println("extension = "+fileExtensions[0]);
                filter = new DngFileFilter(fileDescription,fileExtensions);
            } catch(Exception e) {
                //System.out.println("extension auto");
                filter = new DngFileFilter();
            }
            chooser.setFileFilter(filter);
            file = new File((String)(chooser.getCurrentDirectory().getPath())+"/"+fname);
            chooser.setSelectedFile(file);
            file = null;
            if (title!=null) chooser.setDialogTitle(title);
            loopFlg = false;

            int returnVal = chooser.showSaveDialog(parent);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }
            else return null;

            if (file == null) {
                return null;
            }
            if (!mbPath) { 
              if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) {
                JOptionPane.showMessageDialog(parent,"保存先に、\n日本語や記号文字等を含むフォルダ名ファイル名は、使用できません。\n"+file.getPath(),"保存先エラー",JOptionPane.ERROR_MESSAGE);
                loopFlg = true;
                //chooser.changeToParentDirectory();
                path = chooser.getCurrentDirectory().getAbsolutePath();
                //file=null;
                continue;
              }
            }
            else if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) isMbPath=true;

            if (file.exists()) {
                if (JOptionPane.showConfirmDialog(parent,file.getName()+"は既に存在します。上書きしてよろしいですか？","ファイル上書き",JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION) {
                  loopFlg = true; 
                  path = chooser.getCurrentDirectory().getAbsolutePath();
                  continue;
                }
            }
        } while (loopFlg);

        return file;
    }
}
