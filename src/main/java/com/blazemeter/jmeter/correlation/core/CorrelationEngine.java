package com.blazemeter.jmeter.correlation.core;

import com.blazemeter.jmeter.correlation.core.extractors.Extractor;
import com.blazemeter.jmeter.correlation.core.replacements.Replacement;
import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.gui.CorrelationRulesTestElement;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CorrelationEngine<T extends CorrelationContext> {

  private static final Logger LOG = LoggerFactory.getLogger(CorrelationEngine.class);
  protected List<CorrelationRule> rules;
  protected JMeterVariables vars = new JMeterVariables();
  protected T context;

  public void setCorrelationRulesTestElement(CorrelationRulesTestElement rules) {
    this.rules = rules.getRules().stream()
        .map(testElem -> new CorrelationRule(testElem.getReferenceName(), buildExtractor(testElem),
            buildReplacement(testElem)))
        .collect(Collectors.toList());
  }

  private Extractor buildExtractor(CorrelationRuleTestElement testElem) {
    try {
      if (testElem.getExtractorClass() == null) {
        return null;
      }
      return testElem.getExtractorClass()
          .getConstructor(CorrelationRuleTestElement.class, CorrelationContext.class)
          .newInstance(testElem, context);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOG.error("Can't instantiate extractor {}", testElem.getExtractorClass(), e);
      return null;
    }
  }

  private Replacement buildReplacement(CorrelationRuleTestElement testElem) {
    try {
      if (testElem.getReplacementClass() == null) {
        return null;
      }
      return testElem.getReplacementClass()
          .getConstructor(CorrelationRuleTestElement.class, CorrelationContext.class)
          .newInstance(testElem, context);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOG.error("Can't instantiate replacement {}", testElem.getReplacementClass(), e);
      return null;
    }
  }

  public CorrelationRulesTestElement getCorrelationRulesTestElement() {
    return new CorrelationRulesTestElement(rules.stream()
        .map(CorrelationRule::buildTestElement)
        .collect(Collectors.toList()));
  }

  public void reset() {
    vars = new JMeterVariables();
    if (context != null) {
      context.reset();
    }
  }

  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result) {
    rules.forEach(r -> r.applyReplacements(sampler, children, result, vars));
    rules.forEach(r -> r.addExtractors(sampler, children, result, vars));
  }

}
