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
package com.newtowndata.disk.logic.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class FileUtils {

	private static final String[] LEVELS = {"", "k", "M", "G", "T"};
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private FileUtils() {}

	public static String renderFileSize(double size) {
		return renderFileSize(size, 0);
	}

	public static String renderFileSize(double size, int level) {
		if (size == 0.0) {
			return "";
		}

		if (size < 1000) {
			if (size < 10) {
				return String.format("%.1f", size) + LEVELS[level];
			} else {
				return ((int) size) + LEVELS[level];
			}
		} else {
			return renderFileSize(size / 1000.0, level + 1);
		}
	}

	public static String renderTimestamp(long timestamp) {
		return DATE_FORMAT.format(new Date(timestamp));
	}
}
