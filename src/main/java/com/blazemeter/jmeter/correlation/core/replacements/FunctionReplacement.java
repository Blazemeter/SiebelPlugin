package com.blazemeter.jmeter.correlation.core.replacements;

import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.oro.text.regex.MalformedPatternException;

public class FunctionReplacement<T extends CorrelationContext> extends RegexReplacement<T> {

  public FunctionReplacement(String regex) {
    super(regex);
  }

  public FunctionReplacement(CorrelationRuleTestElement testElem, CorrelationContext context) {
    super(testElem, context);
  }

  @Override
  protected String replaceWithRegex(String input, String regex, String variableName,
      JMeterVariables vars) throws MalformedPatternException {
    return replaceExpression(input, regex, variableName, match -> true);
  }

}
