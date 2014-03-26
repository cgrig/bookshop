package bookshop;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * The Category parameter is the base for the main menu.
 */
enum Category {
  BROWSE("Browse"),
  MY_COURSES("My courses"),
  MY_DETAILS("My details"),
  CART("Shopping cart"),
  ORDERS("Orders"),
  ADVANCED("Advanced");

  private final String descr;
  public String descr() { return descr; }
  Category(String descr) {
    this.descr = descr;
  }
}

public class Main extends HttpServlet {
  private PrintWriter pw;
  private Category categ;
  private HttpServletRequest req;
  private HttpSession session;
  private Database db;
  private Database.User user;

  /**
   * Puts together all the pieces to form 
   * the main html page of the web site.
   */
  public void doGet(HttpServletRequest req, HttpServletResponse ans) 
  throws IOException {
    this.req = req;
    pw = ans.getWriter();
    ans.setContentType("text/html");
    session = req.getSession(true);
    db = Database.inst();

    sendPrelude();
    sendLoginBox();
    readCategParam();
    sendCategLinks();
    switch (categ) {
    case BROWSE: sendBrowseCateg(); break;
    case MY_COURSES: sendMyCoursesCateg(); break;
    case MY_DETAILS: sendMyDetailsCateg(); break;
    case CART: sendCartCateg(); break;
    case ORDERS: sendOrdersCateg(); break;
    case ADVANCED: sendAdvancedCateg(); break;
    default: assert false;
    }
    sendPostlude();
  }

  /**
   * Read the category parameter.
   * Category parameter has Browse as default.
   */
  private void readCategParam() {
    categ = Category.BROWSE;
    if (user == null) return;
    String t = req.getParameter("categ");
    if (t != null) {
      try {
        categ = Category.valueOf(t);
      } catch (IllegalArgumentException e) {
        // let BROWSE be the default
      }
    }
  }

  /**
   * The header of the html page.
   */
  private void sendPrelude() {
    pw.write("<html><head><title>Students' Bookshop</title>");
    pw.write("<link rel=\"stylesheet\" type=\"text/css\" ");
    pw.write("href=\"bookshop/style.css\"/>");
    pw.write("</head><body>");
  }

  /**
   * The footer of the html page.
   */
  private void sendPostlude() {
    pw.write("</body></html>");
    pw.flush();
  }

  /**
   * The login/logout and register portion of the html page.
   */
  private void sendLoginBox() {
    user = (Database.User)session.getAttribute("user");
    pw.write("<form action=\"loginout\" method=\"post\">");
    pw.write("<input name=\"orig\" type=\"hidden\" value=\"");
    String q = req.getQueryString();
    if (q == null) q = "";
    pw.write("bookshop?" + q);
    pw.write("\"/>");
    if (user == null) {
      // login
      // TODO: put here a javascript that hashes the password
      pw.write("user:");
      pw.write("<input name=\"user\" type=\"text\"/>");
      pw.write("password:");
      pw.write("<input name=\"password\" type=\"password\" />");
      pw.write("<input type=\"submit\" value=\"login\"/>");
      pw.write("<a href=\"bookshop/register.html\">Register</a>");
    } else {
      // logout
      pw.write("<input type=\"submit\" value=\"logout\"/>");
      pw.write(" Hello, " + user.firstName + "! ");
    }
    pw.write("</form>");
  }

  /**
   * Display the menu according to the type of user.
   */
  private void sendCategLinks() {
    pw.write("<div class=\"categ\">");
    if (categ == Category.BROWSE) pw.write("<b>");
    pw.write(" <a class=\"categ\" href=\"bookshop?categ=" 
        + Category.BROWSE.name() + "\">"
        + Category.BROWSE.descr() + "</a> ");
    if (categ == Category.BROWSE) pw.write("</b>");
    if (user != null) {
      for (Category c : EnumSet.range(Category.MY_COURSES, Category.CART)) {
        if (categ == c) pw.write("<b>");
        pw.write(" <a class=\"categ\" href=\"bookshop?categ="
            + c.name() + "\">" + c.descr() + "</a> ");
        if (categ == c) pw.write("</b>");
      }
      if (user.isAdmin){
        if (categ == Category.ORDERS) pw.write("<b>");
        pw.write(" <a class=\"categ\" href=\"bookshop?categ="
            +  Category.ORDERS.name() + "\">" +  Category.ORDERS.descr() 
            + "</a> ");
        if (categ == Category.ORDERS) pw.write("</b>");
        if (categ == Category.ADVANCED) pw.write("<b>");
        pw.write(" <a class=\"categ\" href=\"bookshop?categ="
            +  Category.ADVANCED.name() + "\">" +  Category.ADVANCED.descr() 
            + "</a> ");
        if (categ == Category.ADVANCED) pw.write("</b>");
      }
    }
    pw.write("</div><br/>");
  }


  /**
   * Displays the content for the Browse category.
   */
  private void sendBrowseCateg() {
    String depid = req.getParameter("depid");
    String courseid = req.getParameter("courseid");
    if (courseid != null) sendBooks(depid, courseid);
    else if (depid != null) sendCourses(depid);
    else sendDepartments();
  }


  /**
   * Inside the Browse category displays the Departments sorted in 
   * alphabetical order.
   * For admin only it offers the possibility of adding a new department.
   */
  private void sendDepartments() {
    pw.write("<div class=\"path\">");
    pw.write("<b>Departments</b>");
    pw.write("</div>");

    ArrayList<Database.Dept> dep = new ArrayList<Database.Dept>();
    try {
      dep = db.getDepartments();
    } catch (SQLException e) {
      par("Can't read departments: " + e);
    }

    Collections.sort(dep, new Comparator<Database.Dept>() { 
          public int compare(Database.Dept a, Database.Dept b) {
            return a.name.toLowerCase().compareTo(b.name.toLowerCase());
          }
        });
    
      
    if (user != null && user.isAdmin) {
      String q = req.getQueryString();
      if (q == null) q = "";
      pw.write("<p>");
      pw.write("<form action=\"dbaction\" method=\"post\">");
      hide("orig", "bookshop?" + q);
      hide("op", "adddep");
      pw.write("Name of new department: ");
      pw.write("<input type=\"text\" name=\"name\" value=\"\">");
      pw.write("<input type=\"submit\" value=\"add\"/>");
      pw.write("</form></p>");
    }
    
    pw.write("<p>");
      for (Database.Dept d : dep) {
        pw.write(" <a href=\"bookshop?categ=" + Category.BROWSE.name() 
        + "&" + "depid=" + d.id + "\">" + d.name + "</a></br>");
      }
    pw.write("</p>");
  }

  /**
   * Inside a department from the Browse category displays the list of 
   * Courses ordered alphabetically by the course code.
   * For admin it offers the possibility of adding a course 
   * and of removing the department from the departments list.
   */
  private void sendCourses(String depidS) {
    int depid = -1;
    Database.Dept dept = null;
    try { depid = Integer.parseInt(depidS); }
    catch (NumberFormatException e) {
      par("I can't understand the department id " + depidS);
      return;
    }
    try { dept = db.deptById(depid); }
    catch (SQLException e) { /* dept remains null */ }
    if (dept == null) {
      par("I don't recognize the department " + depidS);
      return;
    }
    pw.write("<div class=\"path\">");
    pw.write("<a href=\"bookshop?categ=" + Category.BROWSE.name());
    pw.write("\">Departments</a>");
    pw.write(" &raquo; ");
    pw.write("<b>" + dept.name + "</b>");
    pw.write("</div>");

    if (user != null && user.isAdmin) {
      String q = req.getQueryString();
      if (q == null) q = "";

      pw.write("<br/>");
      pw.write("<form action=\"dbaction\" method=\"post\">");
      hide("orig", "bookshop?" + q.replaceAll("depid=[0-9]+&?", ""));
      hide("op", "rmdep");
      hide("depid", "" + depid);
      pw.write("<input type=\"submit\" value=\"remove department\"/>");
      pw.write("</form>");

      pw.write("<form action=\"dbaction\" method=\"post\">");
      hide("orig", "bookshop?" + q);
      hide("op", "addcrs");
      hide("depid", req.getParameter("depid"));
      pw.write("Code of new course: ");
      pw.write("<input type=\"text\" name=\"courseid\" value=\"\">");
      pw.write("Name of new course: ");
      pw.write("<input type=\"text\" name=\"name\" value=\"\">");
      pw.write("<input type=\"submit\" value=\"add course\"/>");
      pw.write("</form>");

    }

    ArrayList<Database.Course> courses = new ArrayList<Database.Course>();
    try { courses = db.courseByDept(depid); }
    catch (SQLException e) {
      par("I coudn't retrieve the courses from the database.");
      return;
    }
    Collections.sort(courses, new Comparator<Database.Course>() {
          public int compare(Database.Course a, Database.Course b) {
            return a.code.compareTo(b.code);
          }
        });
    pw.write("<p>");
      for (Database.Course c : courses) {
        pw.write("<a href=\"bookshop?categ=" + Category.BROWSE.name());
        pw.write("&depid=" + depid + "&courseid=" + c.code + "\">");
        pw.write(c.code + "</a> &mdash; ");
        pw.write(c.name + "<br/>");
      }
    pw.write("</p>");
  }

  /**
   * Inside a Course (from the Browse >> Department) displays the list of 
   * Books, ordered by the importance and then alphabetically.
   * If the user is logged in it offers the possibility to add a book to the 
   * Shopping Cart and to  add/ remove the course to/from the My Courses list.
   * For admin it offers: add/remove a book and remove course from the 
   * Courses list. There is also the option to edit a book.
   * Edit book is not completely implemented yet.
   */
  private void sendBooks(String depidS, String cid) {
    int depid = -1;
    Database.Dept dept = null;
    Database.Course course = null;
    String q = req.getQueryString();
    if (q == null) q = "";

    try { depid = Integer.parseInt(depidS); }
    catch (NumberFormatException e) {
      par("I can't understand the department id " + depidS);
      return;
    }
    try { dept = db.deptById(depid); }
    catch (SQLException e) { /* dept remains null */ }
    if (dept == null) {
      par("I don't recognize the department " + depidS);
      return;
    }

    try { course = db.courseById(cid);}
    catch (SQLException e) {/* course remains null */ }
    if (course == null) {
      par("I don't recognize the course " + cid);
      return;
    }
    pw.write("<div class=\"path\">");
    pw.write("<a href=\"bookshop?categ=" + Category.BROWSE.name());
    pw.write("\">Departments</a>");
    pw.write(" &raquo; ");
    pw.write("<a href=\"bookshop?categ=" + Category.BROWSE.name());
    pw.write("&depid=" + depid + "\">" + dept.name + "</a>");
    pw.write(" &raquo; ");
    pw.write("<b>" + course.code + "</b>");
    pw.write("</div>");
    pw.write("<br/>");
    pw.write("<div class=\"secTitle\">");
    pw.write(course.name);
    pw.write("</div>");
    pw.write("<br/>");
    boolean isP = false;
    boolean isMyCourse = false;
    if (user != null) {
      try { isP = db.isProf(cid, user.handle);}
      catch (SQLException e) {}
      pw.write("<table class=\"simple\">");
      if (user.isAdmin || isP ) {
        pw.write("<tr><td>");
        pw.write("<form action=\"dbaction\" method=\"post\">");
        hide("orig", "bookshop?" + q.replaceAll("courseid=[a-zA-Z0-9]+&?", ""));
        hide("op", "rmcrs");
        hide("courseid", req.getParameter("courseid"));
        pw.write("<input type=\"submit\" value=\"remove course\"/>");
        pw.write("</form></td>");

        pw.write("<td>");
        pw.write("<form action=\"dbaction\" method=\"post\">");
        hide("orig", "bookshop?" + q);
        hide("op", "addbook");
        hide("courseid", req.getParameter("courseid"));
        pw.write("ISBN: ");
        pw.write("<input type=\"text\" name=\"isbn\" value=\"\">");
        pw.write("<input type=\"submit\" value=\"add book\"/>");
        pw.write("</form></td>");
        
        pw.write("<td><form action=\"dbaction\" method=\"post\">");
        hide("orig", "bookshop?" + q);
        hide("op", "editbooks");
        pw.write("<input type=\"submit\" value=\"edit books\"/>");
        pw.write("</form></td></tr>");
      }
      try { isMyCourse = db.isMyCourse(cid, user.handle);}
      catch (SQLException e) { par("error: " + e); }
      if (!isMyCourse) {
        pw.write("<tr><td colspan=\"2\">");
        pw.write("<form action=\"dbaction\" method=\"post\">");
        hide("orig", "bookshop?" + q);
        hide("op", "addmycrs");
        hide("courseid", req.getParameter("courseid"));
        pw.write("<input type=\"submit\" value=\"add to  My courses\"/>");
        pw.write("</form></td></tr>");
      } else {
        pw.write("<tr><td colspan=\"2\">");
        pw.write("<form action=\"dbaction\" method=\"post\">");
        hide("orig", "bookshop?" + q);
        hide("op", "rmvmycrs");
        hide("courseid", req.getParameter("courseid"));
        pw.write("<input type=\"submit\" value=\"remove from My courses\"/>");
        pw.write("</form></td></tr>");
      }
      pw.write("</table>");
    }
    ArrayList<Database.Book> books = new ArrayList<Database.Book>();
    try { books = db.booksByCourse(cid); }
    catch (SQLException e) {
      par("I coudn't retrieve the books from the database.");
      return;
    }
    Collections.sort(books, new Comparator<Database.Book>() {
          public int compare(Database.Book a, Database.Book b) {
            int di = a.importance - b.importance;
            if (di != 0) return di;
            return a.title.compareTo(b.title);
          }
        });
    pw.write("<div class=\"books\">");
    for (Database.Book b : books) {
      pw.write("<table class=\"books\">");
      pw.write("<tr>");
      pw.write("<th>"+b.title+"</th>");
      pw.write("<th style=\"width:20%;text-align:right\">");
      if (user != null && (user.isAdmin || isP) ) {
        pw.write("<form action=\"dbaction\" method=\"post\">");
        hide("orig", "bookshop?" + q);
        hide("op", "rmbook");
        hide("isbn", b.isbn);
        hide("courseid", req.getParameter("courseid"));
        pw.write("<input type=\"submit\" value=\"remove\"/>");
        pw.write("</form>");
      }
      pw.write("</th>");
      pw.write("</tr>");

      pw.write("<tr>");
      pw.write("<td><b>"+b.author+"</b></td>");
      pw.write("<td rowspan=\"3\" style=\"text-align:right\">");
      pw.write("<img src=\""+b.image+"\"/></td>");
      pw.write("</tr>");
      pw.write("<tr><td>Publisher: "+b.publisher+"</td></tr>");
      pw.write("<tr><td>Edition: "+b.edition+"</td></tr>");
      pw.write("<tr><td>Year: "+b.year+"</td>");
      pw.write("<td rowspan=\"3\" style=\"text-align:right\">");
      try {
        if (user != null && !db.inCart(b.isbn, user.handle)) {
          pw.write("<form action=\"dbaction\" method=\"post\">");
          hide("orig", "bookshop?" + q);
          hide("op", "addtocart");
          hide("isbn", b.isbn);
          pw.write("<input type=\"submit\" value=\"add to cart\"/>");
          pw.write("</form>");
        }
      } catch (SQLException e) {
        pw.write("Internal error: " + e);
      }
      pw.write("</td>");
      pw.write("</tr>");
      pw.write("<tr><td>ISBN: "+b.isbn+"</td></tr>");
      pw.write("<tr><td>USD "+b.price+"</td></tr>");
      pw.write("<tr><td colspan=\"2\">"+b.desc+"</td></tr>");
      pw.write("</table>");
      pw.write("<br />");
    }
    pw.write("</div>");
  }

  /**
   * Displays the content for the My Courses category only if the user 
   * is logged in, the list with the courses that the user selected as 
   * his courses, ordered alphabetically by the course code.
   */
  private void sendMyCoursesCateg() {
    if (user == null) return;
    ArrayList<Database.Course> courses = new ArrayList<Database.Course>();
    try { courses = db.myCourses(user.handle); }
    catch (SQLException e) {
      par("I coudn't retrieve the courses from the database.");
      return;
    }
    Collections.sort(courses, new Comparator<Database.Course>() {
          public int compare(Database.Course a, Database.Course b) {
            return a.code.compareTo(b.code);
          }
        });
    pw.write("<p>");
    Database. Dept dept = null;
    for (Database.Course c : courses) {
      try { dept = db.deptByCourse(c.code); }
      catch (SQLException e) {
        par("can't find dept for this course " + e );
        /* dept remains null */ }
      if (dept == null) {
        par("I don't recognize the department for the course " + c.code);
      return;
      }
      pw.write("<a href=\"bookshop?categ=" + Category.BROWSE.name());
      pw.write("&depid=" + dept.id + "&courseid=" + c.code + "\">");
      pw.write(c.code + "</a> &mdash; ");
      pw.write(c.name + "<br/>");
    }
    pw.write("</p>");
  }

  /**
   * Displays the content of My Details category only if the user is logged in, 
   * he can edit new details which are validated and saved in the database.
   */
  private void sendMyDetailsCateg() {
    if (user == null) return;
    int ccid = 0, aid = 0;
    try {
      aid = db.aidByUid(user.handle);
      ccid = db.ccidByUid(user.handle);
    } catch (SQLException e) {
      par("Can't get CCID: " + e);
      return;
    }
    pw.write("<br />");
    pw.write(EditForm.getForm(
          "CreditCard", "ccid=" + ccid, 
          "bookshop?categ=MY_DETAILS"));
    pw.write("</br>");
    pw.write(EditForm.getForm(
          "DeliveryAddress", "aid=" + aid,
          "bookshop?categ=MY_DETAILS"));
  }

  /**
   * Displays the content of Shopping Cart category only if the usser is logged
   * in, it offers the possibility of removing books from the shopping cart and
   * of placing order.
   * It also displays the total price for the books in the shopping cart.
   */
  private void sendCartCateg() {
    if (user == null) return;
    ArrayList<Database.Book> books = new ArrayList<Database.Book>();
    try { books = db.booksByUser(user.handle); }
    catch (SQLException e) { par("error: " + e); }

    if (books.isEmpty()) {
      par("Your shopping cart is empty.");
      return;
    }

    pw.write("<br/><form action=\"dbaction\" method=\"post\">");
    hide("op", "order");
    hide("orig", "bookshop?categ=CART");
    pw.write("<input type=\"submit\" value=\"place order\"/>");
    pw.write("</form>");

    double totalPrice = 0.0;
    for (Database.Book b : books) totalPrice += b.price;
    par("Total: USD " + totalPrice);
    for (Database.Book b : books) {
      pw.write("<form action=\"dbaction\" method=\"post\">");
      hide("orig", "bookshop?categ=CART");
      hide("op", "rmfromcart");
      hide("isbn", b.isbn);
      pw.write("<table class=\"discreet\">");
      pw.write("<tr><td rowspan=\"2\"><img src=\"" + b.image + "\"/></td>");
      pw.write("<td><b>" + b.title + "</b>,<br/> ");
      pw.write("<i>" + b.author + "</i></td></tr>");
      pw.write("<tr><td>USD " + b.price);
      pw.write(" <input type=\"submit\" value=\"remove\"/>");
      pw.write("</td></tr>");
      pw.write("</table></form>");
    }
  }

  /**
   * Only for admins, displays the content of Orders category.
   * Not implemented yet.
   */
  private void sendOrdersCateg() {
    if (user == null || !user.isAdmin) return;
    par("todo: orders");
  }

  /**
   * Only for admins, displays the content of Advanced category.
   * Not implemented yet.
   */
  private void sendAdvancedCateg() {
    if (user == null || !user.isAdmin) return;
    par("todo: advanced");
  }

  /**
   * Writes a paragraph in html.
   */
  private void par(String s) {
    pw.write("<p>");
    pw.write(s);
    pw.write("</p>");
  }

  /**
   * Writes a hidden field in a form.
   */
  private void hide(String param, String value) {
    pw.write(HtmlUtil.param(param, value));
  }
}
