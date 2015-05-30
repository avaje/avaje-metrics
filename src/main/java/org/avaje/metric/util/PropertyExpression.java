package org.avaje.metric.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Helper used to evaluate expressions such as ${HOME}, ${CATALINA_HOME} etc.
 * <p>
 * The expressions can contain environment variables, system properties or JNDI
 * properties. JNDI expressions take the form ${jndi:propertyName} where you
 * substitute propertyName with the name of the jndi property you wish to
 * evaluate.
 * </p>
 */
final class PropertyExpression {

  private static final Logger logger = LoggerFactory.getLogger(PropertyExpression.class.getName());

  /**
   * Prefix for looking up JNDI Environment variable.
   */
  private static final String JAVA_COMP_ENV = "java:comp/env/";

  /**
   * Used to detect the start of an expression.
   */
  private static String START = "${";

  /**
   * Used to detect the end of an expression.
   */
  private static String END = "}";

  /**
   * Specify the PropertyHolder.
   */
  private PropertyExpression() {
  }

  /**
   * Return the property value evaluating and replacing any expressions such as
   * ${CATALINA_HOME}.
   */
  static String eval(String val, Properties map) {
    if (val == null) {
      return null;
    }
    int sp = val.indexOf(START);
    if (sp > -1) {
      int ep = val.indexOf(END, sp + 1);
      if (ep > -1) {
        return eval(val, sp, ep, map);
      }
    }
    return val;
  }

  /**
   * Convert the expression using JNDI, Environment variables, System Properties
   * or existing an property in Properties itself.
   */
  private static String evaluateExpression(String exp, Properties map) {

    if (isJndiExpression(exp)) {
      // JNDI property lookup...
      String val = getJndiProperty(exp);
      if (val != null) {
        return val;
      }
    }

    // check Environment Variables first
    String val = System.getenv(exp);
    if (val == null) {
      // then check system properties
      val = System.getProperty(exp);
    }
    if (val == null && map != null) {
      // then check PropertyMap
      val = map.getProperty(exp);
    }

    if (val != null) {
      return val;

    } else {
      // unable to evaluate yet... but maybe later based on the order
      // in which properties are being set/loaded. You can use
      logger.debug("Unable to evaluate expression [{}]", exp);
      return null;
    }
  }

  private static String eval(String val, int sp, int ep, Properties map) {

    StringBuilder sb = new StringBuilder();
    sb.append(val.substring(0, sp));

    String cal = evalExpression(val, sp, ep, map);
    sb.append(cal);

    eval(val, ep + 1, sb, map);

    return sb.toString();
  }

  private static void eval(String val, int startPos, StringBuilder sb, Properties map) {

    if (startPos < val.length()) {
      int sp = val.indexOf(START, startPos);
      if (sp > -1) {
        // append what is between the last token and the new one (if startPos ==
        // sp nothing gets added)
        sb.append(val.substring(startPos, sp));
        int ep = val.indexOf(END, sp + 1);
        if (ep > -1) {
          String cal = evalExpression(val, sp, ep, map);
          sb.append(cal);
          eval(val, ep + 1, sb, map);
          return;
        }
      }
    }
    // append what is left...
    sb.append(val.substring(startPos));
  }

  private static String evalExpression(String val, int sp, int ep, Properties map) {

    // trim off start and end ${ and }
    String exp = val.substring(sp + START.length(), ep);

    // evaluate the variable
    String evaled = evaluateExpression(exp, map);
    if (evaled != null) {
      return evaled;
    } else {
      // unable to evaluate at this stage (maybe later)
      return START + exp + END;
    }
  }

  private static boolean isJndiExpression(String exp) {
    return exp.startsWith("JNDI:") || exp.startsWith("jndi:");
  }

  /**
   * Returns null if JNDI is not setup or if the property is not found.
   *
   * @param key the key of the JNDI Environment property including a JNDI: prefix.
   */
  private static String getJndiProperty(String key) {

    try {
      // remove the JNDI: prefix
      key = key.substring(5);

      return (String) getJndiObject(key);

    } catch (NamingException ex) {
      return null;
    }
  }

  /**
   * Similar to getProperty but throws NamingException if JNDI is not setup or
   * if the property is not found.
   */
  private static Object getJndiObject(String key) throws NamingException {

    InitialContext ctx = new InitialContext();
    return ctx.lookup(JAVA_COMP_ENV + key);
  }

}