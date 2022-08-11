package io.avaje.metrics.report;

/**
 * Information that is common across all the metrics collected on a specific JVM.
 * <p>
 * Typically this information is sent with the collected metrics to a repository to identify the specific
 * Application, Environment and server instance that the metrics were collected on.
 */
public class HeaderInfo {

  /**
   * A key used to authenticate/identify the client sending the metrics.
   */
  protected String key;

  /**
   * Key name of the Application.
   */
  protected String app;

  /**
   * Key identifying the environment (PROD, TEST, DEV etc).
   */
  protected String env;

  /**
   * The server name or IP Address.
   */
  protected String server;


  /**
   * Return a key used to authenticate/identify the client sending the metrics.
   */
  public String getKey() {
    return key;
  }

  /**
   * Set a key used to authenticate/identify the client sending the metrics.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Return the name of the Application.
   */
  public String getApp() {
    return app;
  }

  /**
   * Set the name of the Application.
   */
  public void setApp(String app) {
    this.app = app;
  }

  /**
   * Return the code identifying the environment (PROD, TEST, DEV etc).
   */
  public String getEnv() {
    return env;
  }

  /**
   * Set the code identifying the environment (PROD, TEST, DEV etc).
   */
  public void setEnv(String env) {
    this.env = env;
  }

  /**
   * Return the server name or IP Address.
   */
  public String getServer() {
    return server;
  }

  /**
   * Set the server name or IP Address.
   */
  public void setServer(String server) {
    this.server = server;
  }

}
