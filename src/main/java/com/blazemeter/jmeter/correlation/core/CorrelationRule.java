package com.blazemeter.jmeter.correlation.core;

import com.blazemeter.jmeter.correlation.core.extractors.Extractor;
import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import com.blazemeter.jmeter.correlation.core.replacements.Replacement;
import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import java.util.List;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;

public class CorrelationRule {

  private final String variableName;
  private final Extractor extractor;
  private final Replacement replacement;

  public CorrelationRule(String variableName, String extractorRegex, String replacementRegex) {
    this(variableName, new RegexCorrelationExtractor(extractorRegex),
        new RegexReplacement(replacementRegex));
  }

  public CorrelationRule(String variableName, Extractor extractor,
      Replacement replacement) {
    this.variableName = variableName;
    this.extractor = extractor;
    if (extractor != null) {
      extractor.setVariableName(variableName);
    }
    this.replacement = replacement;
    if (replacement != null) {
      replacement.setVariableName(variableName);
    }
  }

  public CorrelationRuleTestElement buildTestElement() {
    CorrelationRuleTestElement testElem = new CorrelationRuleTestElement();
    testElem.setReferenceName(variableName);
    if (extractor != null) {
      extractor.updateTestElem(testElem);
    }
    if (replacement != null) {
      replacement.updateTestElem(testElem);
    }
    return testElem;
  }

  public void applyReplacements(HTTPSamplerBase sampler, List<TestElement> children,
      SampleResult result,
      JMeterVariables vars) {
    if (replacement != null) {
      replacement.process(sampler, children, result, vars);
    }
  }

  public void addExtractors(HTTPSamplerBase sampler, List<TestElement> children,
      SampleResult result,
      JMeterVariables vars) {
    if (extractor != null) {
      extractor.process(sampler, children, result, vars);
    }
  }

}
