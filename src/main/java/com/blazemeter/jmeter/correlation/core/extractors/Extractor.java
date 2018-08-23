package com.blazemeter.jmeter.correlation.core.extractors;

import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import com.blazemeter.jmeter.correlation.core.ResultField;
import java.util.List;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;

public abstract class Extractor<T extends CorrelationContext> {

  protected final ResultField target;
  protected String variableName;
  protected T context;

  public Extractor(CorrelationRuleTestElement testElem, CorrelationContext context) {
    target = testElem.getTargetField();
    this.context = (T) context;
  }

  public Extractor(ResultField target) {
    this.target = target;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public void updateTestElem(CorrelationRuleTestElement testElem) {
    testElem.setTargetField(target);
    testElem.setExtractorClass(getClass());
  }

  public abstract void process(HTTPSamplerBase sampler, List<TestElement> children,
      SampleResult result, JMeterVariables vars);

}
