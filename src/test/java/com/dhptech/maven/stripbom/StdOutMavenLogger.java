package com.dhptech.maven.stripbom;

import org.apache.maven.plugin.logging.Log;

class StdOutMavenLogger implements Log {

  public StdOutMavenLogger() {
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public void debug(CharSequence content) {
    System.out.println(content);
  }

  @Override
  public void debug(CharSequence content, Throwable error) {
    System.out.println(content);
    error.printStackTrace();
  }

  @Override
  public void debug(Throwable error) {
    error.printStackTrace();
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public void info(CharSequence content) {
    System.out.println(content);
  }

  @Override
  public void info(CharSequence content, Throwable error) {
    System.out.println(content);
    error.printStackTrace();
  }

  @Override
  public void info(Throwable error) {
    error.printStackTrace();
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public void warn(CharSequence content) {
    System.out.println(content);
  }

  @Override
  public void warn(CharSequence content, Throwable error) {
    System.out.println(content);
    error.printStackTrace();
  }

  @Override
  public void warn(Throwable error) {
    error.printStackTrace();
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public void error(CharSequence content) {
    System.out.println(content);
  }

  @Override
  public void error(CharSequence content, Throwable error) {
    System.out.println(content);
    error.printStackTrace();
  }

  @Override
  public void error(Throwable error) {
    error.printStackTrace();
  }
}
