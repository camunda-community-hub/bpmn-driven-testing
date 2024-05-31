package org.camunda.community.bpmndt;

import java.util.Locale;

import javax.lang.model.SourceVersion;

public final class Literal {

  /**
   * Converts the given BPMN element ID into a Java literal, which can be used when generating source code. The conversion lowers all characters and retains
   * letters as well as digits. All other characters are converted into underscores. If the literal starts with a digit, an additional underscore is prepended.
   *
   * @param id The ID of a specific flow node or process.
   * @return A Java conform literal.
   */
  public static String toJavaLiteral(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is null");
    }

    String literal = toLiteral(id).toLowerCase(Locale.ENGLISH);

    if (Character.isDigit(literal.charAt(0))) {
      return String.format("_%s", literal);
    } else if (SourceVersion.isKeyword(literal)) {
      return String.format("_%s", literal);
    } else {
      return literal;
    }
  }

  /**
   * Converts the given BPMN element ID into a literal, which can be used when generating source code. The conversion retains letters and digits. All other
   * characters are converted into underscores. Moreover, upper case is also retained.
   *
   * @param id The ID of a specific flow node or process.
   * @return A conform literal.
   */
  public static String toLiteral(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is null");
    }

    String trimmedId = id.trim();
    if (trimmedId.isEmpty()) {
      throw new IllegalArgumentException("id is empty");
    }

    StringBuilder sb = new StringBuilder(trimmedId.length());
    for (int i = 0; i < trimmedId.length(); i++) {
      char c = trimmedId.charAt(i);

      if (Character.isLetterOrDigit(c)) {
        sb.append(c);
      } else {
        sb.append('_');
      }
    }

    return sb.toString();
  }

  private Literal() {
  }
}
