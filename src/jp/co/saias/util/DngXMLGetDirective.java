package jp.co.saias.util;

import java.io.*;
import java.util.regex.*;

public class DngXMLGetDirective {

  String strTag="";
  String strValue="";
  public DngXMLGetDirective(String contents) { 
    Pattern pattern = Pattern.compile("<([^!>/?]+)>");
    Matcher matcher = pattern.matcher(contents);
    while(matcher.find()) {
      if (strTag!="") strTag += ":_"; 
      String str=matcher.group(1);
      strTag += str;
      String strTagp[] = str.split("[ \t\n\f\r]+");
      Pattern pattern2 = Pattern.compile("<"+str+">(.*?)</"+strTagp[0]+">",Pattern.DOTALL);
      Matcher matcher2 = pattern2.matcher(contents);
      if (matcher2.find()) {
        if (strValue!="") strValue += ":_"; 
        strValue += matcher2.group(1);
      }
    }
  }
 
  public String[] getTag() {
    return strTag.split(":_");
  }

  public String[] getValue() {
    return strValue.split(":_");
  }
}
