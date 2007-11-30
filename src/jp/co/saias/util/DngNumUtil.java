package jp.co.saias.util;

import java.util.*;
import java.io.*;

public class DngNumUtil {

  public String addComma(String num) {
    
    if (! (new Integer(num)).toString().equals(num) ) {
      return num;
    }
    String temp1  = num;
    temp1.replaceAll(",","");
    if(temp1.equals(null)) {
       return num;
    }
    temp1.replaceAll("-","");
    if (temp1.length()<4) {
      return num;
    }
    StringBuffer sb = new StringBuffer(temp1);
    sb.reverse();
    int c=0;
    for (int i=3;i<temp1.length();i+=3) {
      sb.insert(i+c,",");
      c++;
    }
    sb.reverse();
    
    if (Character.toString(num.charAt(0)).equals("-")) sb.insert(0,"-");
    return sb.toString();
  }
  
}
