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

import com.newtowndata.disk.service.CacheService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cache")
public class CacheResource {

	private final CacheService cacheService;

	public CacheResource(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@GetMapping("/reload")
	public String reload(@RequestParam(required = false) String path) {
		cacheService.reload();
		if (path == null) {
			return "redirect:/";
		} else {
			return "redirect:/?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8);
		}
	}

}
