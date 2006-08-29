package jp.co.saias.lib;

import java.io.*;
import jp.co.saias.util.DngXMLParse;

public class DngAppProperty {
  private DngXMLParse xml;

  public DngAppProperty(String uri) {
    xml = new DngXMLParse(uri);
  }
  public String getProperty(String key) {
    String[] keys = key.split("/");
    String val = xml.getValue(keys);
    return val;
  }
}
