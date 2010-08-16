/*
 * Copyright 2010 DHP Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
