package com.blazemeter.jmeter.correlation.siebel;

import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.siebel.SiebelContext.Field;
import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import com.blazemeter.jmeter.correlation.core.RegexMatcher;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;

public class SiebelRowParamsReplacement extends RegexReplacement<SiebelContext> {

  private String rowVarPrefix;

  public SiebelRowParamsReplacement(String regex, SiebelContext context) {
    super(regex);
    this.context = context;
  }

  public SiebelRowParamsReplacement(CorrelationRuleTestElement testElement,
      CorrelationContext context) {
    super(testElement, context);
  }

  @Override
  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result,
      JMeterVariables vars) {
    String rowId = new RegexMatcher(result.getSamplerData(), regex, 1).findMatch(1);
    rowVarPrefix = context.getRowVars().get(rowId);
    super.process(sampler, children, result, vars);
  }

  @Override
  protected String replaceWithRegex(String input, String regex, String variableName,
      JMeterVariables vars)
      throws MalformedPatternException {
    for (Map.Entry<String, Field> entry : context.getParamRowFields().entrySet()) {
      /*
      we remove _\d+$ from param names and then add same regex, since when navigating rows the last
      index is the position of the row and is dynamic.
       */
      String paramRegex =
          Perl5Compiler.quotemeta(entry.getKey().replaceAll("_\\d+$", "")) + "_\\d+=(.*)";
      String varName = rowVarPrefix + "_" + (entry.getValue().getPosition() + 1);
      String varValue = vars.get(varName);
      Predicate<String> matchCondition = match -> varValue != null && varValue
          .equals(match.replaceAll(entry.getValue().getIgnoredCharsRegex(), ""));
      input = replaceExpression(input, paramRegex, varName, matchCondition);
    }
    return input;
  }

}
