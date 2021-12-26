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
package com.newtowndata.disk.rest;

import com.newtowndata.disk.core.PathConstants;
import com.newtowndata.disk.core.entity.PathGraph;
import com.newtowndata.disk.core.entity.PathInfo;
import com.newtowndata.disk.logic.utils.FileUtils;
import com.newtowndata.disk.rest.entity.PathEntity;
import com.newtowndata.disk.service.CacheService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class IndexResource {

  private static final Logger LOG = LoggerFactory.getLogger(IndexResource.class);

  private final CacheService cacheService;

  public IndexResource(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  @GetMapping
  public String getIndex(@RequestParam(required = false) String path,
      @RequestParam(required = false) String action, Map<String, Object> model) {
    PathGraph graph = cacheService.getPathGraph();

    LOG.info("Listing {}", path);

    List<PathResult> results;
    if (path == null) {
      results = list(graph, graph.roots());
      path = "<root>";
      model.put("parent", null);
    } else {
      results = list(graph, path);
      model.put("parent", graph.pathInfos().get(path).parentPath());
    }

    model.put("path", path);

    List<PathEntity> list = convertResults(results);
    model.put("list", list);
    return "index";
  }

  private List<PathEntity> convertResults(List<PathResult> results) {
    long totalSize = results.stream().mapToLong(result -> result.totalSize).sum();

    return results.stream().map(result -> createPathEntity(result.path, result.pathInfo,
        result.totalSize, calculatePercentSize(totalSize, result.totalSize))).toList();
  }

  private int calculatePercentSize(long total, long current) {
    if (total == 0L) {
      return 0;
    }
    return (int) Math.round(current * 100.0 / (double) total);
  }

  private List<PathResult> list(PathGraph graph, List<String> currentPaths) {
    List<PathResult> results = new ArrayList<>();
    for (String currentPath : currentPaths) {
      PathInfo pathInfo = graph.pathInfos().get(currentPath);
      results.add(calculateResult(graph, currentPath, pathInfo));
    }
    return results;
  }

  private List<PathResult> list(PathGraph graph, String currentPath) {
    List<PathResult> results = new ArrayList<>();
    PathInfo pathInfo = graph.pathInfos().get(currentPath);
    for (String child : pathInfo.children()) {
      results.add(calculateResult(graph, child));
    }
    return results;
  }

  private PathEntity createPathEntity(String path, PathInfo pathInfo, long totalSize,
      int percentSize) {
    return new PathEntity(path, pathInfo.fileType(), pathInfo.name(),
        FileUtils.renderFileSize(totalSize), FileUtils.renderTimestamp(pathInfo.timestamp()),
        percentSize);
  }

  private long calculateTotalSize(PathGraph graph, String path) {
    return calculateTotalSize(graph, graph.pathInfos().get(path));
  }

  private long calculateTotalSize(PathGraph graph, PathInfo pathInfo) {
    long size = pathInfo.size();
    for (String childPath : pathInfo.children()) {
      size += calculateTotalSize(graph, childPath);
    }
    return size;
  }

  private PathResult calculateResult(PathGraph graph, String path) {
    return calculateResult(graph, path, graph.pathInfos().get(path));
  }

  private PathResult calculateResult(PathGraph graph, String path, PathInfo pathInfo) {
    long size;
    if (PathConstants.FILE_TYPE_DIRECTORY.equals(pathInfo.fileType())) {
      size = calculateTotalSize(graph, pathInfo);
    } else {
      size = pathInfo.size();
    }
    return new PathResult(path, pathInfo, size);
  }

  private static class PathResult {

    private final String path;
    private final PathInfo pathInfo;
    private final long totalSize;

    public PathResult(String path, PathInfo pathInfo, long totalSize) {
      this.path = path;
      this.pathInfo = pathInfo;
      this.totalSize = totalSize;
    }

  }

}
