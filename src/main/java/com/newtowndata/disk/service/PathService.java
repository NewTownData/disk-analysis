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

import com.newtowndata.disk.core.entity.PathGraph;
import com.newtowndata.disk.core.entity.PathInfo;
import com.newtowndata.disk.logic.utils.InfoThread;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PathService {

  public PathGraph create() throws IOException {
    List<String> roots = new ArrayList<>();
    Map<String, PathInfo> paths = new HashMap<>();

    try (InfoThread infoThread = new InfoThread()) {
      for (Path root : FileSystems.getDefault().getRootDirectories()) {
        String path = root.toString();
        roots.add(path);
        try {
          Files.walkFileTree(root, new PathGraphVisitor(paths, infoThread));
        } catch (IOException e) {
          throw new IllegalArgumentException("Cannot read path " + path, e);
        }
      }
      return new PathGraph(roots, paths);
    }
  }

  public PathGraph refresh(PathGraph pathGraph, String path) {
    return pathGraph;
  }

}
