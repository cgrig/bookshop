package bookshop;

import java.sql.*;
import java.util.*;

/**
 * Validates all the fields for an entry in a table of the database, 
 * if valid save them in the database.
 */
public class Validator extends DoAndReturn {
  @Override public boolean doit() throws SQLException {
    String table = req.getParameter("table");
    StringBuilder errors = new StringBuilder();
    for (EditForm.FD f : EditForm.tables.get(table)) {
      if (!f.v.validate(req.getParameter(f.name)))
        errors.append("<br/>" + f.label + ": " + f.v.explain());
    }
    if (errors.length() > 0) {
      pw.write("<html><head><title>Form validation failed</title>");
      pw.write("<link rel=\"stylesheet\" type=\"text/css\" ");
      pw.write("href=\"bookshop/style.css\"/>");
      pw.write("</head><body><p>Form validation failed.</p>");
      pw.write(errors.toString());
      pw.write("<p><a href=\"javascript:history.go(-1)\">Go back</a></p>");
      pw.write("</body></html>");
      return false;
    }
    HashMap<String, String> fv = new HashMap<String, String>(); 
    for (EditForm.FD f : EditForm.tables.get(table))
      fv.put(f.name, req.getParameter(f.name));
    Database.inst().write(table, req.getParameter("cond"), fv);
    return true;
  }
}
