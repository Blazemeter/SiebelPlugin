package com.blazemeter.jmeter.correlation;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.blazemeter.jmeter.correlation.core.CorrelationEngine;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith (MockitoJUnitRunner.class)
public class CorrelationProxyControlTest {
  
  private CorrelationProxyControl correlationProxyControl;
  
  @Mock
  private CorrelationEngine correlationEngine;
  private TestElement[] testElements = new TestElement[0];
  private SampleResult sampleResult = new SampleResult();
  private HTTPSampler sampler = new HTTPSampler();
  
  @Test
  public void shouldNotInvokeCorrelationEngineProcessWhenSamplerIsNull() {
    correlationProxyControl = new CorrelationProxyControl();
    GuiPackage.initInstance(null, Mockito.mock(JMeterTreeModel.class));
    correlationProxyControl.deliverSampler(null, testElements, sampleResult);
    verify(correlationEngine, never()).process(Mockito.any(), Mockito.any(), Mockito.any());
  }
  
  @Test
  public void shouldInvokeCorrelationEngineProcessWhenSamplerIsNotNull() {
    correlationProxyControl = new CorrelationProxyControl(correlationEngine);
    GuiPackage.initInstance(null, Mockito.mock(JMeterTreeModel.class));
    correlationProxyControl.deliverSampler(sampler, testElements, sampleResult);
    List<TestElement> children = new ArrayList<>();
    verify(correlationEngine, times(1)).process(sampler, children, sampleResult);
  }
}
