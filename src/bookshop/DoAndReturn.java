package bookshop;

import java.io.*;
import javax.servlet.http.*;

/**
 * The skeleton of all the servlets.
 */
public class DoAndReturn extends HttpServlet {
  protected HttpServletRequest req;
  protected HttpServletResponse ans;
  protected HttpSession session;
  protected PrintWriter pw;
  protected String orig;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse ans) 
  throws IOException {
    this.req = req;
    this.ans = ans;
    ans.setContentType("text/html");
    pw = ans.getWriter();
    session = req.getSession(true);
    orig = req.getParameter("orig");
    if (orig == null) {
      pw.write("<html><head><title>Oops</title></head>");
      pw.write("<body><p>Address where to return is missing</p>");
      pw.write("</body></html>");
      return;
    }
    try {
      if (doit())
        ans.sendRedirect(ans.encodeRedirectURL(orig));
        //Workaround for firefox and ~
        //pw.write("<html><head><meta http-equiv=\"REFRESH\" ");
        //pw.write("content=\"0;url=http://csserver.ucd.ie/~cgrigore/servlet/");
        //pw.write(orig);
        //pw.write("\"/></head></html>");
    } catch (Exception e) {
      pw.write("<html>");
      pw.write("<head><title>Oops</title></head>");
      pw.write("<body>Operation failed! " + e + "</body>");
      pw.write("</html>");
      return;
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse ans) 
  throws IOException {
    pw = ans.getWriter();
    pw.write("You shouldn't come here uninvited!");
  }

  public boolean doit() throws Exception {
    // to be overwritten
    return true;
  }
}

