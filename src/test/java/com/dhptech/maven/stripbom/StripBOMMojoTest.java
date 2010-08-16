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

import org.apache.maven.plugin.MojoExecutionException;
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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.expect;

/**
 *
 * @author danap
 */
@Test(suiteName = "Strip BOM Plugin Tests", testName = "Strip BOM Mojo Test")
public class StripBOMMojoTest {

  public static final String CHECKING_FOR_BOM_MSG = "Checking for BOM in ";
  public static final String FOUND_BOM_MSG_PREFIX = "Found BOM in ";
  public static final String FOUND_BOM_MSG_SUFFIX = ", removing.";
  public static final String FOUND_BOM_WARN_ONLY_MSG_SUFFIX = ", not removing.";
  public static final String BOM_TEST_FILE_NAME = "./src/test/testFiles/testFileWithBOM.txt";
  public static final String NO_BOM_TEST_FILE_NAME = "./src/test/testFiles/testFileWithoutBOM.txt";

  private void insertBOM(File file) throws IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(file));
    try {
      byte[] bom = new byte[3];
      in.mark(10);
      in.read(bom);
      if (!Arrays.equals(bom, StripBOMMojo.UTF8_BOM)) {
        in.reset();
        File tempFile = File.createTempFile(file.getName(), ".tmp", file.getParentFile());
        OutputStream out = new FileOutputStream(tempFile);
        out.write(StripBOMMojo.UTF8_BOM);
        byte[] buffer = new byte[1024];
        int cnt = -1;
        while ((cnt = in.read(buffer)) >= 0) {
          out.write(buffer, 0, cnt);
        }
        out.close();
        File backupFile = new File(file.getAbsoluteFile().getCanonicalPath() + ".bak");
        if (!file.renameTo(backupFile)) {
          assert false : "Could not rename target file, " + file + ", to backup file, " + backupFile;
        }
        if (!tempFile.renameTo(file)) {
          if (!backupFile.renameTo(file)) {
            assert false : "Could not undo rename of backup file, " + backupFile + ", to target file, " + file;
          }
          assert false : "Could not rename temp file, " + tempFile + ", to target file, " + file;
        }
        backupFile.delete();
      }
    } finally {
      in.close();
    }
  }

  private boolean checkForBOM(File file) throws IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(file));
    try {
      byte[] bom = new byte[3];
      in.read(bom);
      return Arrays.equals(bom, StripBOMMojo.UTF8_BOM);
    } finally {
      in.close();
    }
  }

  @BeforeClass
  public void prepareTestData() throws Exception {
    insertBOM(new File(BOM_TEST_FILE_NAME));
  }

  @Test
  public void testStripBOMMojoWarnOnlyFailBuild() throws Exception {
    System.out.println("*** testStripBOMMojoWarnOnlyFailBuild");
    StripBOMMojo mojo = new StripBOMMojo();

    Log log = createMock(Log.class);
    Log logDelegate = new StdOutMavenLogger();
    mojo.setLog(log);

    List<FileSet> fileSets = new ArrayList<FileSet>();
    FileSet fileSet = new FileSet();
    fileSet.setDirectory("./src/test/testFiles");
    fileSet.addInclude("**/*.txt");
    fileSet.addExclude("**/*-Exclude.txt");
    fileSets.add(fileSet);
    mojo.setFiles(fileSets);

    mojo.setWarnOnly(true);
    mojo.setFailBuild(true);
    
    log.debug(CHECKING_FOR_BOM_MSG + BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.debug(CHECKING_FOR_BOM_MSG + NO_BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.warn(FOUND_BOM_MSG_PREFIX + BOM_TEST_FILE_NAME + FOUND_BOM_WARN_ONLY_MSG_SUFFIX);
    expectLastCall().andDelegateTo(logDelegate).once();
    expect(log.isDebugEnabled()).andStubDelegateTo(logDelegate);
    expect(log.isWarnEnabled()).andStubDelegateTo(logDelegate);

    replay(log);
    try {
      mojo.execute();
      assert false : "expected a MojoExecutionException";
    } catch(MojoExecutionException ex) {
      assert ex.getMessage().equals("BOM(s) Found in files, see output log for specifics.") : "recieved unknown MojoExecutionException: "+ex;
      System.out.println("Got expected MojoExecutionException");
    }
    verify(log);

    assert checkForBOM(new File(BOM_TEST_FILE_NAME));
  }

  @Test
  public void testStripBOMMojoWarnOnly() throws Exception {
    System.out.println("*** testStripBOMMojoWarnOnly");
    StripBOMMojo mojo = new StripBOMMojo();

    Log log = createMock(Log.class);
    Log logDelegate = new StdOutMavenLogger();
    mojo.setLog(log);

    List<FileSet> fileSets = new ArrayList<FileSet>();
    FileSet fileSet = new FileSet();
    fileSet.setDirectory("./src/test/testFiles");
    fileSet.addInclude("**/*.txt");
    fileSet.addExclude("**/*-Exclude.txt");
    fileSets.add(fileSet);
    mojo.setFiles(fileSets);

    mojo.setWarnOnly(true);

    log.debug(CHECKING_FOR_BOM_MSG + BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.debug(CHECKING_FOR_BOM_MSG + NO_BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.warn(FOUND_BOM_MSG_PREFIX + BOM_TEST_FILE_NAME + FOUND_BOM_WARN_ONLY_MSG_SUFFIX);
    expectLastCall().andDelegateTo(logDelegate).once();
    expect(log.isDebugEnabled()).andStubDelegateTo(logDelegate);
    expect(log.isWarnEnabled()).andStubDelegateTo(logDelegate);

    replay(log);
    mojo.execute();
    verify(log);

    assert checkForBOM(new File(BOM_TEST_FILE_NAME));
  }

 @Test(dependsOnMethods={"testStripBOMMojoWarnOnly","testStripBOMMojoWarnOnlyFailBuild"})
  public void testStripBOMMojo() throws Exception {
    System.out.println("*** testStripBOMMojo");
    StripBOMMojo mojo = new StripBOMMojo();

    Log log = createMock(Log.class);
    Log logDelegate = new StdOutMavenLogger();
    mojo.setLog(log);

    List<FileSet> fileSets = new ArrayList<FileSet>();
    FileSet fileSet = new FileSet();
    fileSet.setDirectory("./src/test/testFiles");
    fileSet.addInclude("**/*.txt");
    fileSet.addExclude("**/*-Exclude.txt");
    fileSets.add(fileSet);
    mojo.setFiles(fileSets);

    log.debug(CHECKING_FOR_BOM_MSG + BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.debug(CHECKING_FOR_BOM_MSG + NO_BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.warn(FOUND_BOM_MSG_PREFIX + BOM_TEST_FILE_NAME + FOUND_BOM_MSG_SUFFIX);
    expectLastCall().andDelegateTo(logDelegate).once();
    expect(log.isDebugEnabled()).andStubDelegateTo(logDelegate);
    expect(log.isWarnEnabled()).andStubDelegateTo(logDelegate);

    replay(log);
    mojo.execute();
    verify(log);

    assert !checkForBOM(new File(BOM_TEST_FILE_NAME));
  }

  @Test(dependsOnMethods="testStripBOMMojo")
  public void testStripBOMMojoWarnOnlyFailBuildNoBOMs() throws Exception {
    System.out.println("*** testStripBOMMojoWarnOnlyFailBuildNoBOMs");
    StripBOMMojo mojo = new StripBOMMojo();

    Log log = createMock(Log.class);
    Log logDelegate = new StdOutMavenLogger();
    mojo.setLog(log);

    List<FileSet> fileSets = new ArrayList<FileSet>();
    FileSet fileSet = new FileSet();
    fileSet.setDirectory("./src/test/testFiles");
    fileSet.addInclude("**/*.txt");
    fileSet.addExclude("**/*-Exclude.txt");
    fileSets.add(fileSet);
    mojo.setFiles(fileSets);

    mojo.setWarnOnly(true);
    mojo.setFailBuild(true);

    log.debug(CHECKING_FOR_BOM_MSG + BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    log.debug(CHECKING_FOR_BOM_MSG + NO_BOM_TEST_FILE_NAME);
    expectLastCall().andDelegateTo(logDelegate).once();
    expect(log.isDebugEnabled()).andStubDelegateTo(logDelegate);

    replay(log);
    mojo.execute();
    verify(log);
  }
}
