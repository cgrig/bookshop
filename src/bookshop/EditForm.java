package bookshop;

import java.sql.*;
import java.util.*;

public class EditForm {

  /**
   * Validates a string.
   */
  static class OkV { 
    public boolean validate(String s) { return true; }
    public String explain() { return ""; }
  }

  /**
   * Validates a string through multiple validators.
   */
  static class AllV extends OkV {
    private OkV[] v;
    public AllV(OkV[] v) { this.v = v; }
    @Override public boolean validate(String s) {
      for (OkV o : v) if (!o.validate(s)) return false;
      return true;
    }
    @Override public String explain() {
      String r = "";
      for (OkV o : v) {
        if (!"".equals(r)) r += " and ";
        r += o.explain();
      }
      return r;
    }
  }

  /**
   * Validates by checking that a  string has the lenght in the right limits.
   */
  static class LenV extends OkV {
    private int m;
    public LenV(int m) { this.m = m; }
    @Override public boolean validate(String s) { return s.length() <= m; }
    @Override public String explain() { return "the lenght must be <=" + m; }
  }

  /**
   * Validates an integer and checks if it's in the right limits.
   */
  static class IntV extends OkV {
    private int min, max;
    public IntV() { this(Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public IntV(int min, int max) { this.min = min; this.max = max; }
    @Override public boolean validate(String s) {
      try {
        int i = Integer.parseInt(s);
        return i >= min && i <= max;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    @Override public String explain() {
      return "should be an integer in the range [" + min + ".." + max + "]";
    }
  }

  /**
   * Validates a string by checking if it contains other characters than the 
   * ones that are permitted.
   */
  static class EscV extends OkV {
    @Override public boolean validate(String s) {
      return "".equals(s.replaceAll("[ 0-9a-zA-Z@,.+'\\-]+",""));
    }
    @Override public String explain() { return "no funny characters"; }
  }

  /**
   * Validate a credit card number, checking that it's a real card number.
   */
  static class CcnV extends OkV {
    @Override public boolean validate(String s) {
      if (s.length() != 16) return false;
      int sum = 0;
      for (int i = 0; i < s.length(); ++i) {
        char c = s.charAt(i);
        if (c < '0' || c > '9') return false;
        int d = c - '0';
        if (i % 2 == 0) d *= 2;
        sum += d % 10; sum += d / 10;
      }
      return sum % 10 == 0;
    }
    @Override public String explain() { 
      return "should be a credit card number"; 
    }
  }

  /**
   * FD class (field description)contains the following data the name of 
   * the field in the database, the label of that field that will be dispalyed 
   * to the user, and the validator associated with it; Creates FD objects.
   */
  static class FD {
    public String name;
    public String label;
    public OkV v;
    public FD(String name, String label, OkV v) {
      this.name = name; this.label = label; this.v = v;
    }
    public FD(String name, String label) {
      this(name, label, new OkV());
    }
  }

  /**
   * Creates a HashMap containing pairs of table name and FDs(field description)
   * associated with that table.
   * For tables Credit Card and Delivery Address.
   */
  static public final HashMap<String, FD[]> tables
    = new HashMap<String, FD[]>();
  static {
    tables.put("CreditCard", new FD[] {
      new FD("ccid", null),
      new FD("card_type", "Card type", 
        new AllV(new OkV[]{new LenV(15), new EscV()})),
      new FD("card_number", "Card number", new CcnV()),
      new FD("cardholder_name", "Cardholder name", 
        new AllV(new OkV[]{new LenV(50), new EscV()})),
      new FD("exp_year", "Expiry year", new IntV()),
      new FD("exp_month", "Expiry month", new IntV(1,12))
    });
    tables.put("DeliveryAddress", new FD[] {
      new FD("aid", null),
      new FD("country", "Country", 
        new AllV(new OkV[]{new LenV(15), new EscV()})),
      new FD("city", "City", 
        new AllV(new OkV[]{new LenV(35), new EscV()})),
      new FD("street", "Street Address", 
        new AllV(new OkV[]{new LenV(30), new EscV()}))
    });
  }

  static private final Database db = Database.inst();

  /**
   * Creates the html form, where you can edit or change the old data 
   * from the database.
   */
  static public String getForm(String table, String cond, String next) {
    StringBuilder sb = new StringBuilder();
    try {
      HashMap<String, String> values = db.read(table, cond);
      sb.append("<form action=\"validator\" method=\"post\"/>");
      sb.append(HtmlUtil.param("orig", next));
      sb.append(HtmlUtil.param("cond", cond));
      sb.append(HtmlUtil.param("table", table));
      sb.append("<table class=\"books\">");
      for (FD f : tables.get(table)) {
        if (f.label != null) {
          sb.append("<tr>");
          sb.append("<td class=\"editLabel\">" + f.label + ":</td>");
          sb.append("<td><input type=\"text\"");
          sb.append(" name=\"" + f.name + "\"");
          sb.append(" value=\"" + values.get(f.name) + "\"");
          sb.append("/></td>");
          sb.append("</tr>");
        } else
          sb.append(HtmlUtil.param(f.name, values.get(f.name)));
      }
      sb.append("<tr><td class=\"editLabel\" colspan=\"2\">");
      sb.append("<input type=\"submit\" value=\"save\">");
      sb.append("</td></tr>");
      sb.append("</table></form>");
    } catch (SQLException e) {
      sb.append("ERROR: <pre>" + e + "</pre>");
    }
    return sb.toString();
  }
}

