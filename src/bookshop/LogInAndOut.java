package bookshop;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * The inner part of the Login/Logout servlet.
 */
public class LogInAndOut extends DoAndReturn  {

  @Override
  public boolean doit() throws SQLException { 
    Database.User user = (Database.User)session.getAttribute("user");
    if (user != null) user = null;
    else {
      String passwd = req.getParameter("password");
      String handle = req.getParameter("user");
      user = Database.inst().login(handle, passwd);
    }
    if (user == null)
      session.removeAttribute("user");
    else
      session.setAttribute("user", user);
    return true;
  }
}

