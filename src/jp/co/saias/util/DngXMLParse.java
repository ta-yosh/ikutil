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
      contents = Pattern.compile("\\x5c").matcher(contents).replaceAll("/");
      Hashtable hash0 = new Hashtable();
      hash = xmlParse(contents,hash0,"");
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
    return hash1.get(key).toString();
  } 

  static Hashtable xmlParse(String contents,Hashtable hash,String tree) {
    DngXMLGetDirective xml = new DngXMLGetDirective(contents);
    String[] tag = xml.getTag();
    String[] value = xml.getValue();
    int[] kcnt= new int[tag.length];
    for (int i=0;i<tag.length;i++) {
      kcnt[i]=0;
    }
    if (!tree.equals("")) tree = tree+"->";
    for (int i=0;i<tag.length;i++) {
      String tree0 = tree+tag[i];
      Matcher m = Pattern.compile("(id|name)=\"(.+?)\"",Pattern.DOTALL).matcher(tag[i]);
      Matcher m2 = Pattern.compile("(.+?) +(no)=\"(.+?)\"",Pattern.DOTALL).matcher(tag[i]);
      String key;
      if (m.find()) key = m.group(2);
      else if (m2.find()) key = m2.group(1)+"_"+m2.group(3);
      else if(tag[i] != null) key = tag[i];
      else {
        //System.out.println("----- TAG_"+i+" [ "+tag[i]+" ] "+tree0+"  SKIP-----");
        continue;
      }
      String val = value[i];
      if (Pattern.compile("<[^!>/?]+?>",Pattern.DOTALL).matcher(val).find()) {
        if (i>0) {
         for (int j=0;j<i;j++) {
           if (tag[i].equals(tag[j])) {
             kcnt[j] +=1;
             key = key+(new Integer(kcnt[j])).toString();
             break;
           }
         }
        }
         //System.out.println("----- TAG_"+i+" [ "+key+" ] "+tree0+"  -----");
         Hashtable hash1 = new Hashtable();
         Hashtable hash2 = xmlParse(val,hash1,tree0);
         hash.put(key,hash2);
      }
      else {
        hash.put(key,val);
        //System.out.println("----- TAG_"+i+" [ "+key+" ] "+tree0+" [value: "+val+" ]  -----");
      }
    }
    return hash;
  }
}
