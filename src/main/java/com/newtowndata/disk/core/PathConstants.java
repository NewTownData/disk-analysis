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
package com.newtowndata.disk.core;

public final class PathConstants {

  public static final String FILE_TYPE_FILE = "F";
  public static final String FILE_TYPE_DIRECTORY = "D";
  public static final String FILE_TYPE_SYMLINK = "L";
  public static final String FILE_TYPE_UNREADABLE_FILE = "X";
  public static final String FILE_TYPE_UNREADABLE_DIRECTORY = "U";

  private PathConstants() {}

}
