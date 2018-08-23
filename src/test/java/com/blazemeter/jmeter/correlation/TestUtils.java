package com.blazemeter.jmeter.correlation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

public class TestUtils {

  public static List<Map<String, String>> comparableFrom(List<TestElement> children) {
    return children.stream()
        .map(te -> {
          Map<String, String> props = new HashMap<>();
          PropertyIterator it = te.propertyIterator();
          while (it.hasNext()) {
            JMeterProperty prop = it.next();
            if (!"cacheKey".equals(prop.getName())) {
              props.put(prop.getName(), prop.getStringValue());
            }
          }
          return props;
        })
        .collect(Collectors.toList());
  }

  public static String readFile(String path, Charset encoding) throws IOException {
    try {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    } catch (IOException e) {
      return "";
    }
  }

}
