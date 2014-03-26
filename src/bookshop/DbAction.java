package bookshop;

import java.sql.*;

/**
 * Part of the servlets, executes the methods from the Database class 
 * depending on the operation parameter.
 */
public class DbAction extends DoAndReturn {
  private Database db;

  @Override
  public boolean doit() throws Exception {
    db = Database.inst();
    String op = req.getParameter("op");
    Database.User user = (Database.User)session.getAttribute("user");
    if ("rmdep".equals(op)) 
      db.removeDepartment(Integer.parseInt(req.getParameter("depid")));
    else if ("rmcrs".equals(op)) 
      db.removeCourse(req.getParameter("courseid"));
    else if ("rmbook".equals(op)) 
      db.removeBook(req.getParameter("isbn"), req.getParameter("courseid"));
    else if ("adddep".equals(op)) 
      db.addDepartment(req.getParameter("name"));
    else if ("addcrs".equals(op)) {
      Database.Course c = new Database.Course();
      c.code = req.getParameter("courseid");
      c.name = req.getParameter("name");
      db.addCourse(c, Integer.parseInt(req.getParameter("depid")));
    } else if ("addbook".equals(op)) { 
      Database.Book b = new Database.Book();
      b.isbn = req.getParameter("isbn");
      db.addBook(b.isbn, req.getParameter("courseid"));
    } else if ("register".equals(op)) { 
      Database.User u = new Database.User();
      u.handle = req.getParameter("username");
      u.passwd = req.getParameter("password");
      u.firstName = req.getParameter("firstName");
      u.lastName = req.getParameter("lastName");
      u.email = req.getParameter("email");
      u.studentNumber = req.getParameter("snum");
      db.registerUser(u);
    } else if ("addmycrs".equals(op)) {
      db.addToMyCourses(user.handle, req.getParameter("courseid"));
    } else if ("rmvmycrs".equals(op)) {
      db.removeFromMyCourses(user.handle, req.getParameter("courseid"));
    } else if ("addtocart".equals(op)) {
      db.addToCart(req.getParameter("isbn"), user.handle);
    } else if ("rmfromcart".equals(op)) {
      db.removeFromCart(req.getParameter("isbn"), user.handle);
    } else if ("order".equals(op)) {
      db.placeOrder(user.handle);
    } else throw new Exception("Not implemented.");
    return true;
  }
}

