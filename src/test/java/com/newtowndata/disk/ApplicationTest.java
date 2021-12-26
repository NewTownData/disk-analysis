package com.newtowndata.disk;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.newtowndata.disk.service.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTest {

  @Autowired
  CacheService cacheService;

  @Test
  void contextLoads() {
    assertNotNull(cacheService);
  }

}
