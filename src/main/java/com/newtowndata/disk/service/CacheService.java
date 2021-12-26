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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

  private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

  private final PathService pathService;
  private final Path cachePath;

  private final AtomicReference<PathGraph> reference = new AtomicReference<>();

  public CacheService(PathService pathService, @Value("${disk-analysis.cache}") Path cachePath) {
    this.pathService = pathService;
    this.cachePath = cachePath;
  }

  public PathGraph getPathGraph() {
    PathGraph graph = reference.get();
    if (graph == null) {
      graph = deserialize().orElse(null);
      reference.set(graph);
    }

    if (graph == null) {
      try {
        graph = pathService.create();
        serialize(graph);
        reference.set(graph);
      } catch (IOException e) {
        LOG.warn("Failed to load graph", e);
      }
    }

    if (graph == null) {
      throw new IllegalArgumentException("Graph is empty");
    }

    return graph;
  }

  public void reload() {
    try {
      reference.set(null);
      Files.deleteIfExists(cachePath);
    } catch (IOException e) {
      LOG.error("Failed to remove cache path {}", cachePath, e);
    }
    getPathGraph();
  }

  private void serialize(PathGraph pathGraph) {
    try (
        OutputStream fos =
            Files.newOutputStream(cachePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        GZIPOutputStream gzip = new GzipOutputQuickStream(bos);
        DataOutputStream dos = new DataOutputStream(gzip)) {
      dos.writeInt(pathGraph.roots().size());
      for (String root : pathGraph.roots()) {
        serializePath(dos, pathGraph, root);
      }
    } catch (IOException e) {
      LOG.error("Failed to write graph to cache {}", cachePath, e);
    }
  }

  private void serializePath(DataOutputStream dos, PathGraph pathGraph, String path)
      throws IOException {
    dos.writeUTF(path);

    PathInfo pathInfo = pathGraph.pathInfos().get(path);
    dos.writeUTF(pathInfo.name());
    dos.writeUTF(pathInfo.fileType());
    dos.writeLong(pathInfo.timestamp());
    dos.writeLong(pathInfo.size());

    dos.writeInt(pathInfo.children().size());
    for (String child : pathInfo.children()) {
      serializePath(dos, pathGraph, child);
    }
  }

  private Optional<PathGraph> deserialize() {
    if (!Files.exists(cachePath)) {
      return Optional.empty();
    }

    try (InputStream fis = Files.newInputStream(cachePath, StandardOpenOption.READ);
        BufferedInputStream bis = new BufferedInputStream(fis);
        GZIPInputStream gzip = new GZIPInputStream(bis);
        DataInputStream dis = new DataInputStream(gzip)) {
      int rootCount = dis.readInt();
      List<String> roots = new ArrayList<>(rootCount);
      Map<String, PathInfo> paths = new HashMap<>();
      for (int i = 0; i < rootCount; i++) {
        roots.add(deserializePath(dis, paths, null));
      }
      return Optional.of(new PathGraph(roots, paths));
    } catch (IOException e) {
      LOG.error("Failed to read graph from cache {}", cachePath, e);
      return Optional.empty();
    }
  }

  private String deserializePath(DataInputStream dis, Map<String, PathInfo> paths, String parent)
      throws IOException {
    String path = dis.readUTF();

    String name = dis.readUTF();
    String fileType = dis.readUTF();
    long timestamp = dis.readLong();
    long size = dis.readLong();

    int childCount = dis.readInt();
    List<String> children = new ArrayList<>(childCount);
    for (int i = 0; i < childCount; i++) {
      children.add(deserializePath(dis, paths, path));
    }

    PathInfo pathInfo = new PathInfo(parent, fileType, name, timestamp, size, children);
    paths.put(path, pathInfo);
    return path;
  }

  private static class GzipOutputQuickStream extends GZIPOutputStream {

    public GzipOutputQuickStream(OutputStream out) throws IOException {
      super(out);
      super.def.setLevel(5);
    }
  }
}
