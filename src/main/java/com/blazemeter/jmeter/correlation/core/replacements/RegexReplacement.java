package com.blazemeter.jmeter.correlation.core.replacements;

import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import java.util.List;
import java.util.function.Predicate;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexReplacement<T extends CorrelationContext> extends Replacement<T> {

  private static final Logger LOG = LoggerFactory.getLogger(RegexReplacement.class);
  private static final String FUNCTION_REF_PREFIX = "${"; //$NON-NLS-1$
  /**
   * Functions are wrapped in ${ and }.
   */
  private static final String FUNCTION_REF_SUFFIX = "}"; //$NON-NLS-1$

  protected final String regex;

  public RegexReplacement(String regex) {
    this.regex = regex;
  }

  public RegexReplacement(CorrelationRuleTestElement testElem, CorrelationContext context) {
    super(testElem, context);
    regex = testElem.getReplacementRegex();
  }

  @Override
  public void updateTestElem(CorrelationRuleTestElement testElem) {
    super.updateTestElem(testElem);
    testElem.setReplacementRegex(regex);
  }

  @Override
  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result,
      JMeterVariables vars) {
    if (regex.isEmpty()) {
      return;
    }
    super.process(sampler, children, result, vars);
  }

  @Override
  protected String replaceString(String input, JMeterVariables vars) {
    try {
      return replaceWithRegex(input, regex, variableName, vars);
    } catch (MalformedPatternException e) {
      LOG.warn("Malformed pattern: {}", regex);
      return input;
    }
  }

  protected String replaceWithRegex(String input, String regex, String varName,
      JMeterVariables vars) throws MalformedPatternException {
    String varValue = vars.get(varName);
    return replaceExpression(input, regex, varName,
        match -> varValue != null && varValue.equals(match));
  }

  protected String replaceExpression(String input, String regex, String expression,
      Predicate<String> matchCondition)
      throws MalformedPatternException {
    PatternMatcher matcher = JMeterUtils.getMatcher();
    Pattern pattern = new Perl5Compiler().compile(regex);
    PatternMatcherInput patternMatcherInput = new PatternMatcherInput(input);
    int beginOffset = patternMatcherInput.getBeginOffset();
    char[] inputBuffer = patternMatcherInput.getBuffer();
    StringBuilder result = new StringBuilder();
    while (matcher.contains(patternMatcherInput, pattern)) {
      MatchResult match = matcher.getMatch();
      if (matchCondition.test(match.group(1))) {
        result
            .append(inputBuffer, beginOffset, match.beginOffset(1) - beginOffset)
            .append(FUNCTION_REF_PREFIX)
            .append(expression)
            .append(FUNCTION_REF_SUFFIX)
            .append(inputBuffer, match.endOffset(1),
                patternMatcherInput.getMatchEndOffset() - match.endOffset(1));
      } else {
        result.append(inputBuffer, beginOffset,
            patternMatcherInput.getMatchEndOffset() - beginOffset);
      }
      beginOffset = patternMatcherInput.getMatchEndOffset();
    }
    result.append(inputBuffer, beginOffset, input.length() - beginOffset);
    return result.toString();
  }

}
