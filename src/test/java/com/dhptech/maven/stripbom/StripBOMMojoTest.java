/*
 * Copyright (c) DHP Technologies, Inc. 2010
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import org.testng.annotations.BeforeClass;

import org.testng.annotations.Test;



/**
 *
 * @author danap
 */
@Test(suiteName="Strip BOM Plugin Tests", testName="Strip BOM Mojo Test")
public class StripBOMMojoTest {

  private void insertBOM(File file) throws IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(file));
    byte[] bom = new byte[3];
    in.mark(10);
    in.read(bom);
    if (!Arrays.equals(bom, StripBOMMojo.UTF8_BOM)) {
      in.reset();
      File tempFile = File.createTempFile(file.getName(), ".tmp",file.getParentFile());
      OutputStream out = new FileOutputStream(tempFile);
      out.write(StripBOMMojo.UTF8_BOM);
      byte[] buffer = new byte[1024];
      int cnt = -1;
      while ((cnt = in.read(buffer)) >= 0) {
        out.write(buffer, 0, cnt);
      }
      out.close();
      File backupFile = new File(file.getAbsoluteFile().getCanonicalPath() + ".bak");
      if ( !file.renameTo(backupFile) ) {
        assert false : "Could not rename target file, "+file+", to backup file, "+backupFile;
      }
      if ( !tempFile.renameTo(file) ) {
        if ( !backupFile.renameTo(file) ) {
          assert false : "Could not undo rename of backup file, "+backupFile+", to target file, "+file;
        }
        assert false : "Could not rename temp file, "+tempFile+", to target file, "+file;
      }
      backupFile.delete();
    }
  }

  @BeforeClass
  public void prepareTestData() throws Exception {
    insertBOM(new File("./src/test/testFiles/testFileWithBOM.txt"));
  }

  @Test
  public void testStripBOMMojo() throws Exception {
    StripBOMMojo mojo = new StripBOMMojo();

    Log log = createMock(Log.class);
    Log logDelegate = new Log() {

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

    };
    mojo.setLog(log);

    List<FileSet> fileSets = new ArrayList<FileSet>();
    FileSet fileSet = new FileSet();
    fileSet.setDirectory("./src/test/testFiles");
    fileSet.addInclude("**/*.txt");
    fileSet.addExclude("**/*-Exclude.txt");
    fileSets.add(fileSet);
    mojo.setFiles(fileSets);

    log.debug("Checking for BOM in ./src/test/testFiles/testFileWithBOM.txt");
    expectLastCall().andDelegateTo(logDelegate).once();
    log.debug("Checking for BOM in ./src/test/testFiles/testFileWithoutBOM.txt");
    expectLastCall().andDelegateTo(logDelegate).once();
    log.info("Found BOM in ./src/test/testFiles/testFileWithBOM.txt, removing.");
    expectLastCall().andDelegateTo(logDelegate).once();

    replay(log);
    mojo.execute();
    verify(log);
  }
}
