package jp.co.saias.util;

import java.io.*;
import java.util.regex.*;
import java.util.Hashtable;

import jp.co.saias.util.DngXMLGetDirective;

public class DngXMLParse {
  Hashtable hash;

  public DngXMLParse(String uri) {
    String contents="";
    try {
      BufferedReader in = new BufferedReader(new FileReader(uri));
      String line;
      String LF="\n";
      while ((line = in.readLine()) != null) {
        if (contents!="") contents+=LF;
        contents += line; 
      }
      in.close();
      Hashtable hash0 = new Hashtable();
      hash = xmlParse(contents,hash0);
    } catch(Exception e) {
      hash = null; 
    }
  }

  public Hashtable getHashAll() {
    return hash;
  }

  public String getValue(String keys[]) {
    if (hash==null) return null;
    Hashtable hash1 = hash;
    String key = keys[0];
    for (int i=0;i<keys.length;i++) {
       key = keys[i];
       if (!hash1.containsKey(key)) return "Null";
       if (i<keys.length-1) hash1 = (Hashtable)hash1.get(key);
    }
    return (String)hash1.get(key);
  } 

  static Hashtable xmlParse(String contents,Hashtable hash) {
    DngXMLGetDirective xml = new DngXMLGetDirective(contents);
    String[] tag = xml.getTag();
    String[] value = xml.getValue();
    for (int i=0;i<tag.length;i++) {
      Matcher m = Pattern.compile("(id|name)=\"(.+?)\"").matcher(tag[i]);
      String key;
      if (m.find()) key = m.group(2);
      else if(tag[i] != null) key = tag[i];
      else continue;
      String val = value[i];
      if (Pattern.compile("<.+?>").matcher(val).find()) {
         Hashtable hash1 = new Hashtable();
         Hashtable hash2 = xmlParse(val,hash1);
         hash.put(key,hash2);
      }
      else {
        hash.put(key,val);
      }
    }
    return hash;
  }
}
