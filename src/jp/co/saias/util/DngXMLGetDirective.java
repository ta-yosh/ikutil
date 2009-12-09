package jp.co.saias.util;

import java.io.*;
import java.util.regex.*;

public class DngXMLGetDirective {

  String strTag="";
  String strValue="";
  public DngXMLGetDirective(String contents) { 
    tagParse(contents);
  }

  public void tagParse(String contents) {
    contents = Pattern.compile("<!--.*?-->",Pattern.DOTALL).matcher(contents).replaceAll("");
    //System.out.println(contents);
    Pattern pattern = Pattern.compile("<([^!>/?]+?)>");
    Matcher matcher = pattern.matcher(contents);
    while (matcher.find()) {
      String str=matcher.group(1);
      String strTagp[] = str.split("[ \t\n\f\r]+");
      Pattern pattern2 = Pattern.compile("<"+str+">(.*?)</"+strTagp[0]+">",Pattern.DOTALL);
      Matcher matcher2 = pattern2.matcher(contents);
      if (matcher2.find()) {
        if (strTag!="") strTag += ":_"; 
        strTag += str;
        if (strValue!="") strValue += ":_"; 
        String val = matcher2.group(1);
        strValue += val;
        //tagParse(val);
      }
      contents = contents.replaceAll(matcher2.group(0),"");
      System.out.println(contents);
      matcher = matcher.reset();
      matcher = pattern.matcher(contents);
    }
  }
 
  public String[] getTag() {
    return strTag.split(":_");
  }

  public String[] getValue() {
    return strValue.split(":_");
  }
}
