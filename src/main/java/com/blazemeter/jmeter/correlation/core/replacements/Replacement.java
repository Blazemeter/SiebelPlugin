package com.blazemeter.jmeter.correlation.core.replacements;

import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.NumberProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Replacement<T extends CorrelationContext> {

  private static final Logger LOG = LoggerFactory.getLogger(Replacement.class);

  protected String variableName;
  protected T context;

  public Replacement() {
  }

  public Replacement(CorrelationRuleTestElement testElem, CorrelationContext context) {
    this.context = (T) context;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public void updateTestElem(CorrelationRuleTestElement testElem) {
    testElem.setReplacementClass(getClass());
  }

  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result,
      JMeterVariables vars) {
    replaceTestElementProperties(sampler, vars);
    for (TestElement child : children) {
      if (child instanceof ConfigTestElement) {
        replaceTestElementProperties(child, vars);
      }
    }
  }

  private void replaceTestElementProperties(TestElement el, JMeterVariables vars) {
    List<JMeterProperty> props = new LinkedList<>();
    PropertyIterator iter = el.propertyIterator();
    while (iter.hasNext()) {
      JMeterProperty val = replaceProperty(iter.next(), vars);
      props.add(val);
    }
    el.clear();
    for (JMeterProperty jmp : props) {
      el.setProperty(jmp);
    }
  }

  private JMeterProperty replaceProperty(JMeterProperty prop, JMeterVariables vars) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("About to replace in property of type: {}: {}", prop.getClass(), prop);
    }
    if (prop instanceof StringProperty) {
      // Must not convert TestElement.gui_class etc
      if (!prop.getName().equals(TestElement.GUI_CLASS)
          && !prop.getName().equals(TestElement.TEST_CLASS)) {
        prop = replaceSimpleProp(prop, vars);
        LOG.debug("Replacement result: {}", prop);
      }
    } else if (prop instanceof NumberProperty) {
      prop = replaceSimpleProp(prop, vars);
      LOG.debug("Replacement result: {}", prop);
    } else if (prop instanceof MultiProperty) {
      MultiProperty multiVal = (MultiProperty) prop;
      PropertyIterator iter = multiVal.iterator();
      Collection<JMeterProperty> newValues = new ArrayList<>();
      while (iter.hasNext()) {
        JMeterProperty val = replaceProperty(iter.next(), vars);
        newValues.add(val);
      }
      multiVal.clear();
      for (JMeterProperty jmp : newValues) {
        multiVal.addProperty(jmp);
      }
      if (multiVal instanceof TestElementProperty) {
        TestElementProperty multiProp = (TestElementProperty) multiVal;
        if (multiProp.getObjectValue() instanceof Argument) {
          replaceArgument((Argument) multiProp.getObjectValue(), vars);
        }
      }
      LOG.debug("Replacement result: {}", multiVal);
    } else {
      LOG.debug("Won't replace {}", prop);
    }
    return prop;
  }

  private JMeterProperty replaceSimpleProp(JMeterProperty prop, JMeterVariables vars) {
    String input = prop.getStringValue();
    if (input == null) {
      return prop;
    }
    return new StringProperty(prop.getName(), replaceString(input, vars));
  }

  protected abstract String replaceString(String input, JMeterVariables vars);

  private void replaceArgument(Argument arg, JMeterVariables vars) {
    String input = arg.getValue();
    if (input == null) {
      return;
    }
    /*
     to normalize replacement and allow to specify a particular argument for replacement when using
     argument (HTTP request body), we include argument name and '=' to the input to then apply the
     replacement and then remove the normalization to only leave the replaced value.
      */
    input = replaceString(arg.getName() + "=" + arg.getValue(), vars)
        .replace(arg.getName() + "=", "");
    arg.setValue(input);
  }

}
