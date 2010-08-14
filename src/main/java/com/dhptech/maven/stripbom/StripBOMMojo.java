package com.dhptech.maven.stripbom;
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
  private List<FileSet> files;

  /**
   * The list of fileSets that will specify the files to strip BOMs from.
   *
   * @return the file sets.
   */
  public List<FileSet> getFiles() {
    return files;
  }

  /**
   * Sets the file set list.
   *
   * @param files the file sets.
   */
  public void setFiles(List<FileSet> files) {
    this.files = files;
  }

  /**
   * Strips the BOMs, if found, from the specified files.
   *
   * @throws MojoExecutionException
   */
  @Override
  public void execute()
    throws MojoExecutionException {
    if (file != null) {
      try {
        stripBOM(file);
      } catch (IOException ex) {
        throw new MojoExecutionException("IOException attempting to strip BOM from " + file, ex);
      }
    } else {
      FileSetManager fsm = new FileSetManager(getLog());
      for (FileSet fileSet : files) {
        for (String fileName : fsm.getIncludedFiles(fileSet)) {
          File file = new File(fileSet.getDirectory(), fileName);
          try {
            stripBOM(file);
          } catch (IOException ex) {
            throw new MojoExecutionException("IOException attempting to strip BOM from " + file, ex);
          }
        }
      }
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
  private void stripBOM(File file) throws IOException, MojoExecutionException {
    getLog().debug("Checking for BOM in " + file);
    InputStream in = new FileInputStream(file);
    byte[] bom = new byte[3];
    in.read(bom);
    if (Arrays.equals(bom, UTF8_BOM)) {
      getLog().info("Found BOM in " + file + ", removing.");
      File tempFile = File.createTempFile(file.getName(), ".tmp", file.getParentFile());
      OutputStream out = new FileOutputStream(tempFile);
      byte[] buffer = new byte[1024];
      int cnt = -1;
      while ((cnt = in.read(buffer)) >= 0) {
        out.write(buffer, 0, cnt);
      }
      out.close();
      File backupFile = new File(file.getAbsoluteFile().getCanonicalPath() + ".bak");
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
    }
  }
}
