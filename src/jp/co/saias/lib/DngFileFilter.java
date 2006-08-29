package jp.co.saias.lib;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class DngFileFilter extends FileFilter {
    
    private String[] fileExtensions;
    private String fileDescription; 

    public DngFileFilter() {
       System.out.println("extension = AUTO");
       fileDescription= "すべてのファイル";
       fileExtensions = new String[1];
       fileExtensions[0] = "";
    }

    public DngFileFilter(String desc, String exts[]) {
       fileDescription = desc;
       fileExtensions = exts;
    }

    public boolean accept(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                return true;
            }

            String ext = getFileExtension(file);
            if (ext != null) {
                for (int i=0; i<fileExtensions.length; i++) {
                    if ((ext.equals(fileExtensions[i]))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getDescription() {
        StringBuffer sb = new StringBuffer();
        sb.append(fileDescription + "(");
        for(int i=0; i<fileExtensions.length; i++) {
            sb.append("*." + fileExtensions[i]);
        }
        sb.append(")");
        return sb.toString();
    }
     
    public String getFileExtension(File file) {
        if (file == null) {
            return null;
        }

        String fileNm = file.getName();
        int i = fileNm.lastIndexOf('.'); 
        if (i == -1) {
            return null;
        }

        if ( (i > 0) && (i < (fileNm.length() - 1))) {
            return fileNm.substring(i + 1).toLowerCase(); 
        }
        else {
            return null;
        }
    }
}
