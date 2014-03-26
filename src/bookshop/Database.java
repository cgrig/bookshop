package bookshop;

import java.util.*;
import java.sql.*;


/**
 * Containes (almost) all code that deals with the database.
 */
public class Database {
  private static final String database = "jdbc:mysql://localhost/cgrigore";
  private static final Properties login = new Properties();
  static {
    login.setProperty("user", "cgrigore");
    login.setProperty("password", "6tp4nqr");
  }
  private static final Database inst = new Database();
  private Connection conn;

  /**
   * Establish the connection to the database.
   */
  private Database() {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection(database, login);
    } catch (Exception e) {
      // will break later; that's life
      System.err.println("BOOK: ERR: " + e);
      conn = null;
    }
  }

  /**
   * Instantiate a database object.
   */
  static public Database inst() { return inst; }
  
  /**
   * Creates a Statement object for sending SQL statements to the database
   */
  private Statement getStmt() throws SQLException {
    return conn.createStatement();
  }

  /**
   * Creates a result set obtained by executing a query on the database.
   */
  private ResultSet query(String q) throws SQLException {
    Statement stmt = getStmt();
    return stmt.executeQuery(q);
  }

  /**
   * Returns the generated keys from executing an update (eg INSERT) 
   * in the database.
   */
  private ResultSet update(String u) throws SQLException {
    Statement stmt = getStmt();
    stmt.executeUpdate(u, Statement.RETURN_GENERATED_KEYS);
    return stmt.getGeneratedKeys();
  }

  /**
   * 
   */
  private ResultSet dbg(String op) throws SQLException {
    throw new SQLException("dbg: " + op);
  }


  /**
   * User class designed to contain data from the table User.
   */
  static public class User {
    public String handle;
    public String firstName;
    public String lastName;
    public String studentNumber;
    public boolean isAdmin;
    public String email;
    public String passwd;
  }

  /**
   * Dept class designed to contain data from table Schools.
   * Creates Dept objects.
   */
  static public class Dept {
    public int id;
    public String name;
    public static Dept mk(ResultSet rs) throws SQLException {
      Dept d = new Dept();
      d.id = rs.getInt("sid");
      d.name = rs.getString("school_name");
      return d;
    }
  }

  /**
   * Course class designed to contain data from table Courses.
   * Creates Course objects.
   */
  static public class Course {
    public String code;
    public String name;
    public static Course mk(ResultSet rs) throws SQLException {
      Course c = new Course();
      c.code = rs.getString("course_code");
      c.name = rs.getString("course_name");
      return c;
    }
  }

  /**
   * Book class designed to contain data from tables Books and BooksToCourses.
   * Creates Book objects.
   */
  static public class Book {
    public String isbn;
    public String old_isbn;
    public String title;
    public String author;
    public String publisher;
    public String edition;
    public String image;
    public int year;
    public double price;
    public int importance;
    public String desc;
    public static Book mk(ResultSet rs) throws SQLException {
      Book b = new Book();
      b.isbn = rs.getString("isbn");
      b.old_isbn = rs.getString("old_isbn");
      b.title = rs.getString("title");
      b.author = rs.getString("author");
      b.publisher = rs.getString("publisher");
      b.edition = rs.getString("edition");
      b.image = rs.getString("imageURL");
      b.year = rs.getInt("year");
      b.price = rs.getDouble("price");
      b.importance = rs.getInt("importance");
      b.desc = rs.getString("description");
      return b;
    }
  }

  /**
   * Selects all the courses from the database that are in a given department,
   * and creates a course object for each of them.
   * @return ArrayList<Course>
   */
  public ArrayList<Course> courseByDept(int depid) throws SQLException {
    ResultSet rs = query("SELECT Courses.* FROM Courses,CoursesToSchools"
        + " WHERE Courses.course_code=CoursesToSchools.course_code"
        + " AND sid="+depid);
    ArrayList<Course> courses = new ArrayList<Course>();
    while (rs.next()) courses.add(Course.mk(rs));
    return courses;
  }

  /**
   * Selects all the departemnts from the database, create a dept object for 
   * each of them.
   * @return ArrayList<Dept>
   */
  public ArrayList<Dept> getDepartments() throws SQLException {
    ResultSet rs = query("SELECT * FROM Schools");
    ArrayList<Dept> departments = new ArrayList<Dept>();
    while (rs.next()) departments.add(Dept.mk(rs));
    return departments;
  }

  /**
   * Selects all the books associated with a course and makes a book object 
   * for each of them.
   * @return ArryList<Book>
   */
  public ArrayList<Book> booksByCourse(String courseid) throws SQLException {
    ResultSet rs = query("SELECT * FROM Books,BooksToCourses"
        + " WHERE Books.isbn=BooksToCourses.isbn"
        + " AND course_code=\"" + courseid + "\""); 
    ArrayList<Book> books = new ArrayList<Book>();
    while (rs.next()) books.add(Book.mk(rs));
    return books;
  }

  /**
   * Selects all the books contained by the shopping cart of a user.
   * @return ArrayList<Book>
   */
  public ArrayList<Book> booksByUser(String uid) throws SQLException {
    ResultSet rs = query("SELECT Books.isbn, title, author, price, imageURL"
        + " FROM Books, ShoppingCart WHERE"
        + " uid=\"" + uid + "\""
        + " AND ShoppingCart.isbn=Books.isbn");
    ArrayList<Book> books = new ArrayList<Book>();
    while (rs.next()) {
      Book b = new Book();
      b.isbn = rs.getString("isbn");
      b.title = rs.getString("title");
      b.author = rs.getString("author");
      b.price = rs.getDouble("price");
      b.image = rs.getString("imageURL");
      books.add(b);
    }
    return books;
  }

  /**
   * Finds the first department associated to this course.
   * @return dept object
   */
  public Dept deptByCourse(String courseId) throws SQLException {
    ResultSet rs = query("SELECT Schools.* FROM Schools,CoursesToSchools" 
        + " WHERE Schools.sid=CoursesToSchools.sid" 
        + " AND course_code=\"" + courseId + "\"");
    if (rs.next()) return Dept.mk(rs);
    return null;
  }

  /**
   * Finds the departemnt given its id.
   * @return dept object
   */
  public Dept deptById(int id) throws SQLException {
    ResultSet rs = query("SELECT * FROM Schools WHERE sid=" + id);
    if (rs.next()) return Dept.mk(rs);
    return null;
  }

  /**
   * Finds the course given its id.
   * @return course object
   */
  public Course courseById(String id) throws SQLException {
    ResultSet rs = query("SELECT * FROM Courses WHERE course_code=\"" + id + "\"");
    if (rs.next()) return Course.mk(rs);
    return null;
  }

  /**
   * Checks if a user is lecturer.
   */
  public boolean isProf(String courseId, String userId) throws SQLException {
    ResultSet rs = query("SELECT * FROM UsersToCourses"
        + " WHERE uid=" + userId + "\""
        + "AND course_code=\"" + courseId + "\"");
    if (rs.next()) return rs.getInt("isProf") != 0;
    return false;
  }

  /**
   * Checks if a given course is "my course" for a given user.
   */
  public boolean isMyCourse(String courseId, String userId) throws SQLException{
    ResultSet rs = query("SELECT * FROM UsersToCourses"
        + " WHERE uid=\"" + userId + "\""
        + " AND course_code=\"" + courseId + "\"");
    return rs.next();
  }

  /**
   * Finds "My Courses" for a given user
   */
  public ArrayList<Course> myCourses(String userId) throws SQLException {
     ResultSet rs = query("SELECT * FROM UsersToCourses"
         + " WHERE uid=\"" + userId + "\"");
     // to differentiate between student/lecturer courses
     String courseId;
     ArrayList<Course> cour = new ArrayList<Course>();
     Course c = new Course();
     while (rs.next()){
       courseId = rs.getString("course_code");
       c = courseById(courseId);
       cour.add(c);
     }
     return cour;
  }

  /**
   * Deletes a department from the database.
   */
  public void removeDepartment(int depid) throws SQLException {
    update("DELETE FROM Schools WHERE sid=" + depid);
    
    ResultSet rs = query("SELECT course_code, COUNT(sid)"
        + " FROM CoursesToSchools GROUP BY course_code");
    HashSet<String> multiple = new HashSet<String>();
    while (rs.next()) 
      if (rs.getInt(2) > 1) multiple.add(rs.getString(1));
    rs = query("SELECT * FROM CoursesToSchools WHERE sid=" + depid);
    while (rs.next()) {
      String cid = rs.getString("course_code");
      if (!multiple.contains(cid)) removeCourse(cid);
    }
    update("DELETE FROM CoursesToSchools WHERE sid=" + depid);
  }

  /**
   * Deletes a course from the databse.
   */
  public void removeCourse(String cid) throws SQLException {
    update("DELETE FROM Courses WHERE course_code=\"" + cid + "\"");
    update("DELETE FROM CoursesToSchools WHERE course_code=\"" + cid + "\"");
    update("DELETE FROM UsersToCourses WHERE course_code=\"" + cid + "\"");
    update("DELETE FROM BooksToCourses WHERE course_code=\"" + cid + "\"");
  }


  /**
   * Adds a department to the database.
   */
  public void addDepartment(String name) throws SQLException {
    update("INSERT INTO Schools(school_name) VALUES (\"" + name +"\")");  
  }

  /**
   * Adds a course to the database.
   */
  public void addCourse(Course c, int depid) throws SQLException {
    update("INSERT INTO Courses VALUES (\"" + c.code +"\", \"" + c.name +"\")");
    update("INSERT INTO CoursesToSchools VALUES (\"" + c.code + "\"," + depid +")");
  }

  /**
   * Adds a course to "My Courses" for a given user.
   */
  public void addToMyCourses(String uid, String courseid) throws SQLException {
    update("INSERT INTO UsersToCourses(uid, course_code) VALUES (" 
        + "\"" + uid +"\", \"" + courseid +"\")");
  }

  /**
   * Removes a course from "My Courses" for a given user.
   */
  public void removeFromMyCourses(String uid, String courseid) throws SQLException {
    update("DELETE FROM UsersToCourses WHERE uid=\"" + uid + "\""
        + " AND course_code=\"" + courseid + "\"");
  }

  /**
   * Removes a book the list of books associated to a course.
   */
  public void removeBook(String isbn, String courseid) throws SQLException {
    update("DELETE FROM BooksToCourses WHERE course_code=\"" + courseid 
        + "\" AND isbn=\"" + isbn + "\"");
  }

  /**
   * Adds a book to the list of books associated to a course.
   */
  public void addBook(String isbn, String courseid) throws SQLException {
    try {
      // order is important!
      update("INSERT INTO BooksToCourses(course_code, isbn) VALUES (\"" 
          + courseid + "\", \"" + isbn + "\")");
      update("INSERT INTO Books(isbn) VALUES (\"" + isbn + "\")");
    } catch (SQLException e) {
      /* do nothing if the book was already there */
    }
  }

  /**
   * Adds a book to a shopping cart of a given user.
   */
  public void addToCart(String isbn, String uid) throws SQLException {
    update("INSERT INTO ShoppingCart VALUES(\"" + uid + "\",\"" + isbn + "\")");
  }

  /**
   * Removes a book from the shopping cart of a given user.
   */
  public void removeFromCart(String isbn, String uid) throws SQLException {
    update("DELETE FROM ShoppingCart WHERE isbn=\"" + isbn + "\""
        + " AND uid=\"" + uid + "\"");
  }

  /**
   * Checks if a specific book is in the shopping cart of a given user.
   */
  public boolean inCart(String isbn, String uid) throws SQLException {
    return query("SELECT * FROM ShoppingCart WHERE uid=\"" + uid + "\""
        + " AND isbn=\"" + isbn + "\"").next();
  }

  /**
   * Removes the books from the shoppiing cart and adds them to the 
   * bought books table.
   */
  public void placeOrder(String uid) throws SQLException {
    ResultSet rs = query("SELECT * FROM ShoppingCart WHERE uid=\"" + uid + "\"");
    String isbn;
    while (rs.next()) {
      isbn = rs.getString("isbn");
      update("INSERT INTO BoughtBooks VALUES (\"" + uid + "\", \"" 
          + isbn + "\", NOW(), 0)");
    }
    update("DELETE FROM ShoppingCart WHERE uid=\"" + uid + "\"");
  }

  /**
   * Inserts into the database registration data.
   */
  public void registerUser(User u) throws SQLException {
    ResultSet rs = query("SELECT * FROM Users WHERE uid=\"" + u.handle + "\""); 
    if (rs.next()) return;
    update("INSERT INTO Users"
        + " VALUES(\"" + u.handle 
        + "\",\"" + u.firstName
        + "\",\"" + u.lastName
        + "\",\"" + u.studentNumber
        + "\",\"" + u.email
        + "\",0,\"" + u.passwd + "\")");
    rs = update("INSERT INTO CreditCard (card_type,card_number,cardholder_name,exp_year,exp_month)"
        + " VALUES(\"VISA\",\"0000000000000000\","
        + "\"" + u.firstName + " " + u.lastName + "\","
        + "0,1)");
    rs.next();
    int ccid = rs.getInt(1);
    update("INSERT INTO UsersToCards VALUES("
        + "\"" + u.handle + "\"," + ccid + ")");
    rs = update("INSERT INTO DeliveryAddress (country,city,street) VALUES("
        + "\"Ireland\",\"Dublin\",\"5 NoWhere\")");
    rs.next();
    int aid = rs.getInt(1);
    update("INSERT INTO UsersToAddresses VALUES(\"" + u.handle + "\"," + aid + ")");
  }

  /**
   * Selects from the database the user with a the id given at login, 
   * checks if the password in the database matches the one given at login, and 
   * if it does creates the corresponding User object.
   * @return user object
   */
  public User login(String handle, String passwd) throws SQLException {
    ResultSet rs = query("SELECT * FROM Users WHERE uid =\"" + handle + "\"");
    if (rs.next()) {
      // crash if password not set in db!
      if (!rs.getString("password").equals(passwd))
        return null;
      User u = new User();
      u.handle = handle;
      u.firstName = rs.getString("name");
      u.lastName = rs.getString("surname");
      u.studentNumber = rs.getString("student_number");
      u.isAdmin = rs.getInt("admin") != 0;
      u.email = rs.getString("e_mail");
      return u;
    } else
      return null;
  }

  /**
   * Creates a HashMap with pairs of (name of the field , value in the database)
   * for all the fields of one entry in a table.
   */
  public HashMap<String, String> read(String table, String cond) 
  throws SQLException {
    HashMap<String, String> r = new HashMap<String, String>();
    ResultSet rs = query("SELECT * FROM " + table + " WHERE " + cond);
    if (!rs.next()) throw new SQLException("Nothing found for " + cond);
    for (EditForm.FD f : EditForm.tables.get(table)) 
      r.put(f.name, rs.getString(f.name));
    if (rs.next()) throw new SQLException("Non-unique result for " + cond);
    return r;
  }

  /**
   * Insert into th database new values for all the fields of one entry 
   * in a table.
   */
  public void write(String table, String cond, HashMap<String, String> nv)
  throws SQLException {
    HashMap<String, String> ov = read(table, cond);
    for (Map.Entry<String, String> e : nv.entrySet())
      ov.put(e.getKey(), e.getValue());
    String cols = "";
    String values = "";
    update("DELETE FROM " + table + " WHERE " + cond);
    for (Map.Entry<String, String> e : ov.entrySet()) {
      if (!"".equals(cols)) { cols += ","; values += ","; }
      cols += e.getKey();
      values += "\"" + e.getValue() + "\"";
    }
    update("INSERT INTO " + table + " (" + cols + ") VALUES(" + values + ")"); 
  }

  /**
   * Finds Credit Card Id given an user id.
   */
  public int ccidByUid(String uid) throws SQLException {
    ResultSet rs = query("SELECT * FROM UsersToCards WHERE uid=\"" + uid + "\"");
    rs.next();
    return rs.getInt("ccid");
  }

  /**
   * Finds Address id given an user id.
   */
  public int aidByUid(String uid) throws SQLException {
    ResultSet rs = query("SELECT * FROM UsersToAddresses"
        + " WHERE uid=\"" + uid + "\"");
    rs.next();
    return rs.getInt("aid");
  }
}
