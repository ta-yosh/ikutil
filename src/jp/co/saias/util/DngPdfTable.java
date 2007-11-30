package jp.co.saias.util;

import java.io.*;

import javax.swing.JTable;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import java.awt.Color;

public class DngPdfTable { 
    Document document;
    BaseFont mincho,gothic;
    Font mincho10,mincho12,mincho14,mincho16,mincho8;
    Font mincho10b,mincho12b,mincho14b,mincho16b;
    Font gothic10,gothic12,gothic14,gothic16;
    Font gothic10b,gothic12b,gothic14b,gothic16b,gothic8b;
    Table ptable;
    String fname;
    String ctype[];
    int ptype;

    public DngPdfTable(String fname, int type) {
        this.fname = fname; 
        this.ptype = type;
        document = new Document(((type>0) ? PageSize.A4.rotate():PageSize.A4) ,20,20,35,20);
        try {
          mincho = BaseFont.createFont("HeiseiMin-W3",
                                     "UniJIS-UCS2-HW-H",false);
          gothic = BaseFont.createFont("HeiseiKakuGo-W5",
                                     "UniJIS-UCS2-HW-H",false);
          mincho8 = new Font(mincho,8);
          mincho10 = new Font(mincho,10);
          mincho12 = new Font(mincho,12);
          mincho14 = new Font(mincho,14);
          mincho16 = new Font(mincho,16);
          mincho10b = new Font(mincho,10,Font.BOLD);
          mincho12b = new Font(mincho,12,Font.BOLD);
          mincho14b = new Font(mincho,14,Font.BOLD);
          mincho16b = new Font(mincho,16,Font.BOLD);
          gothic10 = new Font(gothic,10);
          gothic12 = new Font(gothic,12);
          gothic14 = new Font(gothic,14);
          gothic16 = new Font(gothic,16);
          gothic8b = new Font(gothic,8,Font.BOLD);
          gothic10b = new Font(gothic,10,Font.BOLD);
          gothic12b = new Font(gothic,12,Font.BOLD);
          gothic14b = new Font(gothic,14,Font.BOLD);
          gothic16b = new Font(gothic,16,Font.BOLD);
        } catch (DocumentException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public boolean openPDF(String title) {
      try {
        PdfWriter.getInstance(document,new FileOutputStream(fname));
        document.open();
        Paragraph para1 = new Paragraph(title, mincho14b);
        para1.setAlignment(Element.ALIGN_CENTER);
        document.add(para1);
      } catch (FileNotFoundException e) {
          e.printStackTrace();
          return false;
      } catch (DocumentException e) {
          e.printStackTrace();
          return false;
      } catch(Exception e) {
          e.printStackTrace();
          return false;
      }
      return true;
    }

    public void setParagraph(int lead,String phrase) {
      Paragraph para1;
      if (lead>0) {
        para1 = new Paragraph(lead,phrase, gothic10);
      } else if (lead<0) {
        para1 = new Paragraph(phrase, gothic10);
        para1.setAlignment(Element.ALIGN_RIGHT);
      } else {
        para1 = new Paragraph(phrase, gothic10);
      }
      try {
        document.add(para1);
      } catch (DocumentException e) {
          e.printStackTrace();
      }
    }

    public void setTable(JTable table,float[] width,int[] ctype,int colCount) {
      try {
      ptable = (colCount==0) ? new Table(table.getColumnCount()):
                               new Table(colCount);
      ptable.setDefaultHorizontalAlignment(Element.ALIGN_LEFT);
      ptable.setDefaultVerticalAlignment(Element.ALIGN_MIDDLE);
      ptable.setWidth(100);
      ptable.setWidths(width);
      ptable.setBorderWidth(1);
      ptable.setBorderColor(new Color(0, 0, 0));
      ptable.setPadding(2);
      ptable.setSpacing(0);
      ptable.setCellsFitPage(true);

      for (int j=0;j<table.getColumnCount();j++) {
        Cell cell = new Cell(new Phrase(table.getColumnName(j), 
              (ctype[j]==6)? gothic8b:(colCount>0)? gothic10b:gothic12b));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (colCount>0) {
          if (ctype[j]<3) cell.setRowspan(2);
          if (ctype[j]==5) cell.setColspan(2);
          cell.setBorderColor(new Color(185,185,185));
          if (ctype[j]<3 || j>=colCount) 
            cell.setBorderColorBottom(new Color(0,0,0));
        }
        ptable.addCell(cell);
      }
      int rcount = 0;
      int rpp = (colCount>0) ? ((ptype>0)? 9:14) : ((ptype>0)? 14:18);
      int rpp2 = (colCount>0) ? ((ptype>0)? 10:15) : ((ptype>0)? 16:20);
      int pcount = 0;
      int ptotal = (table.getRowCount()-rpp)/rpp2 + 1 ;
      //HeaderFooter footer = new HeaderFooter(new Phrase("-"),new Phrase("/ "+ptotal+" -"));
      //footer.setAlignment(Element.ALIGN_CENTER);
      //footer.setBorder(Rectangle.NO_BORDER);
      //document.setFooter(footer);
      if ((table.getRowCount()-rpp)%rpp2>0) ptotal++;
      for (int i=0;i<table.getRowCount();i++) {
        if (rcount==rpp) {
          document.add(ptable);
          pcount++;
          if (ptotal>1) {
            Paragraph para1 = new Paragraph("\n- "+pcount+"/"+ptotal+" -", gothic10);
            para1.setAlignment(Element.ALIGN_CENTER);
            document.add(para1);
          }
          while (rcount>0) {
            ptable.deleteLastRow();
            if (colCount>0) ptable.deleteLastRow();
            rcount--;
          }
          rpp=rpp2;
          document.newPage();
        }
        for (int j=0;j<table.getColumnCount();j++) {
          Object value;
          value=table.getValueAt(i,j);
          Cell cell;
          if (value!=null) {
            String cval = value.toString().replaceAll("^ +","").replaceAll(" +$","");
            if (cval.matches("[0-9]+")) {
              if (ctype[j]==1) {
                 cval = (new DngNumUtil()).addComma(cval);
              }
            }
            cell = new Cell(new Phrase(cval,
                   (ctype[j]==6)? mincho8:(colCount>0)? mincho10:mincho12));
            if (ctype[j]>0 && ctype[j]<4) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
          }
          else {
            cell = new Cell(new Phrase(" ", mincho12));
          }
          if (colCount>0) {
            if (ctype[j]<3) cell.setRowspan(2);
            if (ctype[j]==5) cell.setColspan(2);
            cell.setBorderColor(new Color(185,185,185));
            if (ctype[j]<3 || j>=colCount) 
              cell.setBorderColorBottom(new Color(0,0,0));
          }
          //else  cell.setBorderColor(new Color(100,100,100));
          ptable.addCell(cell);
        }
        rcount++;
      }
      document.add(ptable);
      pcount++;
      if (ptotal>1) {
       String br = "\n";
       for (int i=rcount;i<rpp;i++) {
         br = br+"\n\n\n";
       }
       Paragraph para = new Paragraph(br+"- "+pcount+"/"+ptotal+" -", gothic10);
       para.setAlignment(Element.ALIGN_CENTER);
       document.add(para);
      }
      } catch (DocumentException e) {
          e.printStackTrace();
      } catch(Exception e) {
          e.printStackTrace();
      }
    }

    public void flush() {
      document.close();
    }
}
