package com.blazemeter.jmeter.correlation;

import com.blazemeter.jmeter.correlation.core.CorrelationEngine;
import com.blazemeter.jmeter.correlation.gui.CorrelationRulesTestElement;
import com.blazemeter.jmeter.correlation.siebel.SiebelCorrelationEngine;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;

public class CorrelationProxyControl extends ProxyControl {

  private static final String CORRELATION_RULES = "CorrelationProxyControl.rules";

  private CorrelationEngine correlationEngine;

  public CorrelationProxyControl() {
    this(new SiebelCorrelationEngine());
  }

  @VisibleForTesting
  public CorrelationProxyControl(CorrelationEngine correlationEngine) {
    this.correlationEngine = correlationEngine;
    setName("Siebel HTTP(S) Test Script Recorder");
    setProperty(new TestElementProperty(CORRELATION_RULES, correlationEngine.getCorrelationRulesTestElement()));
  }

  @Override
  public void startProxy() throws IOException {
    correlationEngine.reset();
    super.startProxy();
  }

  public void setCorrelationRules(CorrelationRulesTestElement vars) {
    setProperty(new TestElementProperty(CORRELATION_RULES, vars));
    correlationEngine.setCorrelationRulesTestElement(vars);
  }

  public CorrelationRulesTestElement getCorrelationRules() {
    return (CorrelationRulesTestElement) getProperty(CORRELATION_RULES).getObjectValue();
  }

  @Override
  public synchronized void deliverSampler(HTTPSamplerBase sampler, TestElement[] testElements,
      SampleResult result) {
    if (sampler != null) {
      List<TestElement> children = new ArrayList<>(Arrays.asList(testElements));
      correlationEngine.process(sampler, children, result);
      testElements = children.toArray(new TestElement[0]);
    }
    super.deliverSampler(sampler, testElements, result);
  }

}
