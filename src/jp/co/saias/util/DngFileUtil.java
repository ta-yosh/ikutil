package jp.co.saias.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class DngFileUtil {

  public void fileCopy(String src, String dest) throws IOException {
    String ost = System.getProperty("os.name").substring(0,3);
    try {
    if (ost.equals("Win"))  fileCopy2(src,dest);
    else if (ost.equals("Fre")) fileCopy3(src,dest);
    else if (ost.equals("Lin")) fileCopy3(src,dest);
    else if (ost.equals("Mac")) fileCopy3(src,dest);
    else fileCopy2(src,dest);
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  public void fileCopy1(String src, String dest) throws IOException {
    int buffer_size = 2048;
    byte[] buffer = new byte[buffer_size];

    InputStream in = null;
    OutputStream out = null;
    try {
      in = new BufferedInputStream(new FileInputStream(src), buffer_size);
      out = new BufferedOutputStream(new FileOutputStream(dest), buffer_size);

      int readsize;
      while((readsize = in.read(buffer)) != -1) {
        out.write(buffer, 0, readsize);
      }
    }
    finally {
      try {
        if(in != null) {
          in.close();
        }
      }
      catch(IOException e) {
      }
      try {
        if(out != null) {
          out.close();
        }
      }
      catch(IOException e) {
      }
    }
  }

  public void fileCopy2(String src, String dest) throws IOException {
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      in = new FileInputStream(src);
      out = new FileOutputStream(dest);

      FileChannel cIn = in.getChannel();
      FileChannel cOut = out.getChannel();

      cIn.transferTo(0, cIn.size(), cOut);
    }
    finally {
      try {
        if(in != null) {
          in.close();
        }
      }
      catch(IOException e) {
      }
      try {
        if(out != null) {
          out.close();
        }
      }
      catch(IOException e) {
      }
    }
  }
  
  private void fileCopy3(String src, String dest) throws IOException {
    try {
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec("cp -p "+src+" "+dest);
      int tmpI = process.waitFor();
      //System.out.println(tmpI);
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  public void moveFile(String src, String dest) {
    String execStr;
    String ost = System.getProperty("os.name").substring(0,3);
    if (ost.equals("Win")) {
      execStr = "cmd.exe /c ren \""+src+"\" \""+(new File(dest)).getName()+"\"";
    }
    else execStr = "mv "+src+" "+dest;
    //System.out.println(execStr);
    try {
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec(execStr);
      int tmpI = process.waitFor();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  public void remove(String src) {
    String execStr;
    String ost = System.getProperty("os.name").substring(0,3);
    if (ost.equals("Win")) {
      execStr = "cmd.exe /c del \""+src+"\"";
    }
    else execStr = "rm "+src;
    //System.out.println(execStr);
    try {
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec(execStr);
      int tmpI = process.waitFor();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  public void chMod(String modStr,String fPath) {
    String ost = System.getProperty("os.name").substring(0,3);
    //if (ost.equals("Win") || ost.equals("Mac")) return;
    if (ost.equals("Win")) return;

    String execStr = "chmod "+modStr+" "+fPath;
    try {
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec(execStr);
      int tmpI = process.waitFor();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
  public void printOSType() {
    System.out.println(System.getProperty("os.name").substring(0,3)); 
  }
}
