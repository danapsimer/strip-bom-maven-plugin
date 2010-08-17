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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Goal to strip BOMs from UTF-8 text files.
 *
 * @goal strip-bom
 * @phase process-sources
 * @requiresProject false
 */
public class StripBOMMojo
  extends AbstractMojo {

  protected static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
  /**
   * Locations of a single file to strip the BOM from.
   * @parameter expression="${file}"
   */
  private File file;
  /**
   * Locations of the files to strip BOMs.
   * @parameter
   */
  private List files;
  /**
   * Set to true if you only want StripBOM to issue a warning message when a file contains a BOM.
   *
   * @parameter expression="${strip-bom.warnOnly}" default-value="false"
   */
  private boolean warnOnly = false;

  /**
   * Set to true if you want the build to fail when BOMs are found, really only useful if warnOnly = true.
   * @parameter expression="${strip-bom.failBuild}" default-value="false"
   */
  private boolean failBuild = false;

  /**
   * The list of fileSets that will specify the files to strip BOMs from.
   *
   * @return the file sets.
   */
  public List getFiles() {
    return files;
  }

  /**
   * Sets the file set list.
   *
   * @param files the file sets.
   */
  public void setFiles(List files) {
    this.files = files;
  }

  /**
   * Get the single file to strip.
   *
   * @return
   */
  public File getFile() {
    return file;
  }

  /**
   * Set the single file to strip.
   * @param file
   */
  public void setFile(File file) {
    this.file = file;
  }

  /**
   * True if we should only warn about BOMs and not actually strip them.
   * @return true if warnOnly is true.
   */
  public boolean isWarnOnly() {
    return warnOnly;
  }

  /**
   * Set to true to warn only on finding a BOM.
   *
   * @param warnOnly true if we should only warn when finding a BOM.
   */
  public void setWarnOnly(boolean warnOnly) {
    this.warnOnly = warnOnly;
  }

  /**
   * True if the build should fail when BOMs are found in any of the files processed.
   *
   * @return the value of the failBuild property.
   */
  public boolean isFailBuild() {
    return failBuild;
  }

  /**
   * Sets the failBuild property.
   * @see #isFailBuild
   * @param failBuild the new value for the failBuild property
   */
  public void setFailBuild(boolean failBuild) {
    this.failBuild = failBuild;
  }

  /**
   * Strips the BOMs, if found, from the specified files.
   *
   * @throws MojoExecutionException
   */
  public void execute()
    throws MojoExecutionException {
    boolean foundBOM = false;
    if (file != null) {
      try {
        foundBOM |= stripBOM(file);
      } catch (IOException ex) {
        throw new MojoExecutionException("IOException attempting to strip BOM from " + file, ex);
      }
    } else {
      FileSetManager fsm = new FileSetManager(getLog());
      Iterator fileSetIter = files.iterator();
      while(fileSetIter.hasNext()) {
        FileSet fileSet = (FileSet)fileSetIter.next();
        String[] fileNames = fsm.getIncludedFiles(fileSet);
        for (int f = 0; f < fileNames.length; f++) {
          String fileName = fileNames[f];
          File currFile = new File(fileSet.getDirectory(), fileName);
          try {
            foundBOM |= stripBOM(currFile);
          } catch (IOException ex) {
            throw new MojoExecutionException("IOException attempting to strip BOM from " + currFile, ex);
          }
        }
      }
    }
    if ( foundBOM && failBuild ) {
      throw new MojoExecutionException("BOM(s) Found in files, see output log for specifics.");
    }
  }

  /**
   * Strips the BOM, if found from a single file.
   *
   * @param file the file to remove the BOM from, if it is there.
   *
   * @throws IOException
   * @throws MojoExecutionException
   */
  private boolean stripBOM(File file) throws IOException, MojoExecutionException {
    if ( getLog().isDebugEnabled() ) {
      getLog().debug("Checking for BOM in " + file);
    }
    InputStream in = new FileInputStream(file);
    byte[] bom = new byte[3];
    in.read(bom);
    if (Arrays.equals(bom, UTF8_BOM)) {
      if ( getLog().isWarnEnabled() ) {
        getLog().warn("Found BOM in " + file + ", " + (warnOnly ? "not " : "") + "removing.");
      }
      if (warnOnly) {
        return true; // BOM Found
      }

      File tempFile = File.createTempFile(file.getName(), ".tmp", file.getParentFile());
      OutputStream out = new FileOutputStream(tempFile);
      byte[] buffer = new byte[1024];
      int cnt = -1;
      while ((cnt = in.read(buffer)) >= 0) {
        out.write(buffer, 0, cnt);
      }
      out.close();
      File backupFile = File.createTempFile(file.getName(), ".bak", file.getParentFile());
      if (!file.renameTo(backupFile)) {
        throw new MojoExecutionException("Could not rename target file, " + file + ", to backup file, " + backupFile);
      }
      if (!tempFile.renameTo(file)) {
        if (!backupFile.renameTo(file)) {
          getLog().error("Could not undo rename of backup file, " + backupFile + ", to target file, " + file);
        }
        throw new MojoExecutionException("Could not rename temp file, " + tempFile + ", to target file, " + file);
      }
      backupFile.delete();
      return true; // BOM Found
    }
    return false; // No BOM Found
  }
}
