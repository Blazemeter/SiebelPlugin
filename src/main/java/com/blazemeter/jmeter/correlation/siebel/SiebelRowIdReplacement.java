package com.blazemeter.jmeter.correlation.siebel;

import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import java.util.Map;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.oro.text.regex.MalformedPatternException;

public class SiebelRowIdReplacement extends RegexReplacement<SiebelContext> {

  public SiebelRowIdReplacement(String regex, SiebelContext context) {
    super(regex);
    this.context = context;
  }

  public SiebelRowIdReplacement(CorrelationRuleTestElement testElem, CorrelationContext context) {
    super(testElem, context);
  }

  @Override
  protected String replaceWithRegex(String input, String regex, String variableName,
      JMeterVariables vars)
      throws MalformedPatternException {
    for (Map.Entry<String, String> rowVar : context.getRowVars().entrySet()) {
      input = super.replaceWithRegex(input, regex, rowVar.getValue() + "_rowId", vars);
    }
    return input;
  }

}
