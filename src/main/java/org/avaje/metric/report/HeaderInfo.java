package org.avaje.metric.report;

/**
 * Information that is typically sent with the collected metrics to a repository.
 * <p>
 * This information includes values which identify the Application, Environment and specific server
 * that the metrics were collected for.
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

  
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

}
