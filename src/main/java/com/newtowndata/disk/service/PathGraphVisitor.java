/*
 * Copyright 2021 Voyta Krizek, https://github.com/NewTownData
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.newtowndata.disk.service;

import com.newtowndata.disk.core.PathConstants;
import com.newtowndata.disk.core.PathUtils;
import com.newtowndata.disk.core.entity.PathInfo;
import com.newtowndata.disk.logic.utils.InfoThread;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathGraphVisitor implements FileVisitor<Path> {

  private static final Logger LOG = LoggerFactory.getLogger(PathGraphVisitor.class);

  private final Map<String, PathInfo> paths;
  private final InfoThread infoThread;

  public PathGraphVisitor(Map<String, PathInfo> paths, InfoThread infoThread) {
    this.paths = paths;
    this.infoThread = infoThread;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes)
      throws IOException {
    paths.put(path.toString(), PathUtils.createPathInfo(path, attributes));
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path path, IOException err) throws IOException {
    if (err != null) {
      LOG.warn("Cannot read directory {}: {}", path, err.toString());
      paths.put(path.toString(),
          PathUtils.createEmptyPathInfo(path, PathConstants.FILE_TYPE_UNREADABLE_DIRECTORY));
    }
    infoThread.setLastPath(path.toString());
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
    paths.put(path.toString(), PathUtils.createPathInfo(path, attributes));
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path path, IOException err) throws IOException {
    if (err != null) {
      LOG.warn("Cannot read file {}: {}", path, err.toString());
      paths.put(path.toString(),
          PathUtils.createEmptyPathInfo(path, PathConstants.FILE_TYPE_UNREADABLE_FILE));
    }
    return FileVisitResult.CONTINUE;
  }

}
