package bookshop;

/** Produces snippets of HTML */
public class HtmlUtil {
 
  public static String param(String name, String value) {
    value = value.replaceAll("\"", "\\\"");
    return
      "<input name=\"" + name + "\" type=\"hidden\" value=\""
      + value + "\"/>"
    ;
  }
}
