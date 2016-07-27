package vnkim.util;

/**
 * <p>Title: VNKIM</p>
 * <p>Description: Knowledge Information Extraction for Vietnamese</p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Technology</p>
 * @author Truc Vien
 * @version 1.0
 */

public class StringTransformations {

  private static int LinkingChars = 0;

  public StringTransformations() {
  }

  public static int getLinkingChars() {
    return LinkingChars;
  }

  public static String any2Letters(String s) {
    StringBuffer stringbuffer = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isLetter(c)) {
        stringbuffer.append(c);
        continue;
      }
      if (Character.isDigit(c))
        stringbuffer.append( (char) ( (c - 48) + 65));
    }

    return stringbuffer.toString();
  }

  public static String stripPunctuationAndUnifyCase(String s) {
    StringBuffer stringbuffer = new StringBuffer();
    s = s.trim();
    char ac[] = s.toCharArray();
    int i = s.length();
    boolean flag = true;
    char c1 = '~';
    for (int j = 0; j < i; j++) {
      char c = ac[j];
      if (Character.isLetterOrDigit(c)) {
        if (!flag)
          c = Character.toLowerCase(c);
        stringbuffer.append(c);
        continue;
      }
      if (!Character.isWhitespace(c))
        continue;
      flag = false;
      if (!Character.isWhitespace(c1))
        stringbuffer.append(' ');
    }

    return stringbuffer.toString().trim();
  }

  public static String strip(String s, boolean flag) {
    StringBuffer stringbuffer = new StringBuffer();
    boolean flag1 = true;
    s = s.trim();
    char ac[] = s.toCharArray();
    int i = s.length();
    if (i <= 1)
      return s;
    char c = ac[0];
    stringbuffer.append(c);
    boolean flag2 = true;
    boolean flag4 = Character.isLetter(c);
    boolean flag7 = !Character.isLetterOrDigit(c);
    boolean flag10 = true;
    char c2 = 'p';
    boolean flag11 = false;
    for (int j = 1; j < i; j++) {
      char c1 = ac[j];
      boolean flag9 = !Character.isLetterOrDigit(c1);
      boolean flag12 = Character.isWhitespace(c1);
      boolean flag3 = Character.isLetter(c1);
      boolean flag6 = Character.isDigit(c1);
      if (j > 0 && flag2) {
        if (flag7 && flag12)
          continue;
        flag2 = flag4 && flag9 || flag7 && flag3;
        if (flag9) {
          if (c2 == 'p')
            c2 = c1;
          flag2 = flag10 = c2 == c1;
        }
        if (j > 2 && !flag2 && stringbuffer.length() > 0) {
          char c3 = stringbuffer.charAt(stringbuffer.length() - 1);
          stringbuffer.deleteCharAt(stringbuffer.length() - 1);
          stringbuffer.append(Character.toLowerCase(c3));
        }
      }
      if (flag3 || flag6) {
        if (flag && flag3)
          flag1 &= Character.isUpperCase(c1);
        if (!flag10)
          c1 = Character.toLowerCase(c1);
        stringbuffer.append(c1);
      }
      else
      if (!flag2)
        flag10 = false;
      flag4 = flag3;
      flag7 = flag9;
    }

    String s1 = stringbuffer.toString();
    if (flag && flag1)
      s1 = s1.toUpperCase();
    return s1;
  }

  public static String stripPunctAtEnd(String s) {
    for (int i = s.length() - 1; i >= 0; i--) {
      char c = s.charAt(i);
      if (!Character.isLetter(c) && !Character.isDigit(c) &&
          !Character.isWhitespace(c))
        s = s.substring(0, i);
      else
        return s;
    }

    return s;
  }

  public static int wsCount(String s) {
    if (s == null)
      return 0;
    int i = s.length();
    int j = 0;
    for (int k = 0; k < i; k++)
      if (Character.isWhitespace(s.charAt(k)))
        j++;

    return j;
  }

  public static String stripMultiWS(String s) {
    StringBuffer stringbuffer = new StringBuffer();
    s = s.trim();
    char ac[] = s.toCharArray();
    int i = s.length();
    boolean flag1 = false;
    for (int j = 0; j < i; j++) {
      char c = ac[j];
      boolean flag = Character.isWhitespace(c);
      if (flag && !flag1)
        stringbuffer.append(' ');
      else
      if (!flag)
        stringbuffer.append(c);
      flag1 = flag;
    }

    return stringbuffer.toString();
  }

  public static boolean isUpperCaseWord(String word) {
    if (word.toUpperCase().compareTo(word) == 0)
      return true;
    else return false;
  }

  /**
   * The objective of this method is to standardize the string by
   * removing some special characters likes '.', ',', '-', '_', '&'
   * some stop words likes "VÀ".
   * @param original String
   * @return String
   */
  public static String standardized(String original) {
    if (original == null)return null;
    String standardizedString = original;
    // remove '.', ''', '~'
    standardizedString = standardizedString.replaceAll("\\.|'|~", "");
    // replace ',', '-', '&', '/' with whitespace
    standardizedString = standardizedString.replaceAll("\\s*(,|-|&|/)\\s*", " ");
    // uppercase
    standardizedString = standardizedString.toUpperCase();
    standardizedString = " " + standardizedString + " ";
    standardizedString = standardizedString.replaceAll(" VÀ ", " ");
    // chuẩn hóa chính tả
    // chuẩn hóa cách viết chữ i
    standardizedString = standardizedString.replaceAll("\\sDY\\s", " DI ");
    standardizedString = standardizedString.replaceAll("\\sDÝ\\s", " DÍ ");
    standardizedString = standardizedString.replaceAll("\\sDỲ\\s", " DÌ ");
    standardizedString = standardizedString.replaceAll("\\sDỶ\\s", " DỈ ");
    standardizedString = standardizedString.replaceAll("\\sDỸ\\s", " DĨ ");
    standardizedString = standardizedString.replaceAll("\\sDỴ\\s", " DỊ ");

    standardizedString = standardizedString.replaceAll("\\sHY\\s", " HI ");
    standardizedString = standardizedString.replaceAll("\\sHÝ\\s", " HÍ ");
    standardizedString = standardizedString.replaceAll("\\sHỲ\\s", " HÌ ");
    standardizedString = standardizedString.replaceAll("\\sHỶ\\s", " HỈ ");
    standardizedString = standardizedString.replaceAll("\\sHỸ\\s", " HĨ ");
    standardizedString = standardizedString.replaceAll("\\sHỴ\\s", " HỊ ");

    standardizedString = standardizedString.replaceAll("\\sKY\\s", " KI ");
    standardizedString = standardizedString.replaceAll("\\sKÝ\\s", " KÍ ");
    standardizedString = standardizedString.replaceAll("\\sKỲ\\s", " KÌ ");
    standardizedString = standardizedString.replaceAll("\\sKỶ\\s", " KỈ ");
    standardizedString = standardizedString.replaceAll("\\sKỸ\\s", " KĨ ");
    standardizedString = standardizedString.replaceAll("\\sKỴ\\s", " KỊ ");

    standardizedString = standardizedString.replaceAll("\\sLY\\s", " LI ");
    standardizedString = standardizedString.replaceAll("\\sLÝ\\s", " LÍ ");
    standardizedString = standardizedString.replaceAll("\\sLỲ\\s", " LÌ ");
    standardizedString = standardizedString.replaceAll("\\sLỶ\\s", " LỈ ");
    standardizedString = standardizedString.replaceAll("\\sLỸ\\s", " LĨ ");
    standardizedString = standardizedString.replaceAll("\\sLỴ\\s", " LỊ ");

    standardizedString = standardizedString.replaceAll("\\sMY\\s", " MI ");
    standardizedString = standardizedString.replaceAll("\\sMÝ\\s", " MÍ ");
    standardizedString = standardizedString.replaceAll("\\sMỲ\\s", " MÌ ");
    standardizedString = standardizedString.replaceAll("\\sMỶ\\s", " MỈ ");
    standardizedString = standardizedString.replaceAll("\\sMỸ\\s", " MĨ ");
    standardizedString = standardizedString.replaceAll("\\sMỴ\\s", " MỊ ");

    standardizedString = standardizedString.replaceAll("\\sSY\\s", " SI ");
    standardizedString = standardizedString.replaceAll("\\sSÝ\\s", " SÍ ");
    standardizedString = standardizedString.replaceAll("\\sSỲ\\s", " SÌ ");
    standardizedString = standardizedString.replaceAll("\\sSỶ\\s", " SỈ ");
    standardizedString = standardizedString.replaceAll("\\sSỸ\\s", " SĨ ");
    standardizedString = standardizedString.replaceAll("\\sSỴ\\s", " SỊ ");

    standardizedString = standardizedString.replaceAll("\\sTY\\s", " TI ");
    standardizedString = standardizedString.replaceAll("\\sTÝ\\s", " TÍ ");
    standardizedString = standardizedString.replaceAll("\\sTỲ\\s", " TÌ ");
    standardizedString = standardizedString.replaceAll("\\sTỶ\\s", " TỈ ");
    standardizedString = standardizedString.replaceAll("\\sTỸ\\s", " TĨ ");
    standardizedString = standardizedString.replaceAll("\\sTỴ\\s", " TỊ ");

    standardizedString = standardizedString.replaceAll("\\sVY\\s", " VI ");
    standardizedString = standardizedString.replaceAll("\\sVÝ\\s", " VÍ ");
    standardizedString = standardizedString.replaceAll("\\sVỲ\\s", " VÌ ");
    standardizedString = standardizedString.replaceAll("\\sVỶ\\s", " VỈ ");
    standardizedString = standardizedString.replaceAll("\\sVỸ\\s", " VĨ ");
    standardizedString = standardizedString.replaceAll("\\sVỴ\\s", " VỊ ");

    standardizedString = standardizedString.replaceAll("\\sQUI\\s", " QUY ");
    standardizedString = standardizedString.replaceAll("\\sQUÍ\\s", " QUÝ ");
    standardizedString = standardizedString.replaceAll("\\sQUÌ\\s", " QUỲ ");
    standardizedString = standardizedString.replaceAll("\\sQUỈ\\s", " QUỶ ");
    standardizedString = standardizedString.replaceAll("\\sQUĨ\\s", " QUỸ ");
    standardizedString = standardizedString.replaceAll("\\sQUỊ\\s", " QUỴ ");

    // chuẩn hóa cách bỏ dấu
    // vần oa
    standardizedString = standardizedString.replaceAll("ÓA\\s", "OÁ ");
    standardizedString = standardizedString.replaceAll("ÒA\\s", "OÀ ");
    standardizedString = standardizedString.replaceAll("ỎA\\s", "OẢ ");
    standardizedString = standardizedString.replaceAll("ÕA\\s", "OÃ ");
    standardizedString = standardizedString.replaceAll("ỌA\\s", "OẠ ");
    // vần oe
    standardizedString = standardizedString.replaceAll("ÓE\\s", "OÉ ");
    standardizedString = standardizedString.replaceAll("ÒE\\s", "OÈ ");
    standardizedString = standardizedString.replaceAll("ỎE\\s", "OẺ ");
    standardizedString = standardizedString.replaceAll("ÕE\\s", "OẼ ");
    standardizedString = standardizedString.replaceAll("ỌE\\s", "OẸ ");
    // vần ua
    standardizedString = standardizedString.replaceAll("ÚA\\s", "UÁ ");
    standardizedString = standardizedString.replaceAll("ÙA\\s", "UÀ ");
    standardizedString = standardizedString.replaceAll("ỦA\\s", "UẢ ");
    standardizedString = standardizedString.replaceAll("ŨA\\s", "UÃ ");
    standardizedString = standardizedString.replaceAll("ỤA\\s", "UẠ ");
    // vần ue
    standardizedString = standardizedString.replaceAll("ÚE\\s", "UÉ ");
    standardizedString = standardizedString.replaceAll("ÙE\\s", "UÈ ");
    standardizedString = standardizedString.replaceAll("ỦE\\s", "UẺ ");
    standardizedString = standardizedString.replaceAll("ŨE\\s", "UẼ ");
    standardizedString = standardizedString.replaceAll("ỤE\\s", "UẸ ");
    // vần uy
    standardizedString = standardizedString.replaceAll("ÚY\\s", "UÝ ");
    standardizedString = standardizedString.replaceAll("ÙY\\s", "UỲ ");
    standardizedString = standardizedString.replaceAll("ỦY\\s", "UỶ ");
    standardizedString = standardizedString.replaceAll("ŨY\\s", "UỸ ");
    standardizedString = standardizedString.replaceAll("ỤY\\s", "UỴ ");

    // remove two consecutive whitespace
    standardizedString = standardizedString.replaceAll("\\s+", " ");
    standardizedString = standardizedString.trim();

    LinkingChars = original.length() - standardizedString.length();
    return standardizedString;
  }

}
