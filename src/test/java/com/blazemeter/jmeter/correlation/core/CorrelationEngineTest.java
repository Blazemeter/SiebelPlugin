package com.blazemeter.jmeter.correlation.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.gui.CorrelationRulesTestElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.junit.Test;

public class CorrelationEngineTest {

  @Test
  public void shouldGetTheSameCorrelationRuleTestElementListWhenInitializingAndGettingElements() {
    List<CorrelationRuleTestElement> rules = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      CorrelationRuleTestElement rule = new CorrelationRuleTestElement();
      rule.setReferenceName("ReferenceName" + i);
      rule.setReplacementRegex("ReplaceRegEx" + i);
      rule.setExtractorRegex("ExtractorRegEx" + i);
      rule.setGroupNumber("1");
      rule.setMatchNumber("1");
      rule.setTargetField(ResultField.BODY);
      rule.setExtractorClass(RegexCorrelationExtractor.class);
      rule.setReplacementClass(RegexReplacement.class);
      rules.add(rule);
    }
    CorrelationRulesTestElement rulesIn = new CorrelationRulesTestElement(
        rules);
    CorrelationEngine correlationEngine = new CorrelationEngine() {};
    correlationEngine.setCorrelationRulesTestElement(rulesIn);
    CorrelationRulesTestElement rulesOut = correlationEngine.getCorrelationRulesTestElement();
    assertThat(comparableFrom(rulesOut.getRules())).isEqualTo(comparableFrom(rulesIn.getRules()));
  }

  private List<Map<String, String>> comparableFrom(List<CorrelationRuleTestElement> children) {
    return children.stream()
        .map(te -> {
          Map<String, String> props = new HashMap<>();
          PropertyIterator it = te.propertyIterator();
          while (it.hasNext()) {
            JMeterProperty prop = it.next();
            props.put(prop.getName(), prop.getStringValue());
          }
          return props;
        })
        .collect(Collectors.toList());
  }

}
