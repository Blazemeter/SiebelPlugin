package com.blazemeter.jmeter.correlation.core.extractors;

import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import com.blazemeter.jmeter.correlation.core.RegexMatcher;
import com.blazemeter.jmeter.correlation.core.ResultField;
import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import java.util.List;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexCorrelationExtractor<T extends CorrelationContext> extends Extractor<T> {

  private static final Logger LOG = LoggerFactory.getLogger(RegexCorrelationExtractor.class);
  private static final String REGEX_DEFAULT_VALUE = "NOT_FOUND";
  private static final String REGEX_EXTRACTOR_GUI_CLASS = RegexExtractorGui.class.getName();
  private static final int REGEX_DEFAULT_MATCH_NUMBER = 1;
  private static final int REGEX_DEFAULT_GROUP = 1;

  protected final String regex;
  private final int matchNumber;
  private final int group;

  public int getGroup() {
    return group;
  }

  public RegexCorrelationExtractor(String regex) {
    this(regex, REGEX_DEFAULT_GROUP);
  }

  public RegexCorrelationExtractor(String regex, int group) {
    this(regex, group, 1, ResultField.BODY);
  }

  public RegexCorrelationExtractor(String regex, int matchNumber, ResultField target) {
    this(regex, REGEX_DEFAULT_GROUP, matchNumber, target);
  }

  public RegexCorrelationExtractor(String regex, int group, int matchNumber, ResultField target) {
    super(target);
    this.regex = regex;
    this.matchNumber = matchNumber;
    this.group = group;
  }

  public RegexCorrelationExtractor(CorrelationRuleTestElement testElem,
      CorrelationContext context) {
    super(testElem, context);
    regex = testElem.getExtractorRegex();
    matchNumber = getMatchNumber(testElem);
    group = getGroupNumber(testElem);
  }

  @Override
  public void updateTestElem(CorrelationRuleTestElement testElem) {
    super.updateTestElem(testElem);
    testElem.setExtractorRegex(regex);
    testElem.setMatchNumber(String.valueOf(matchNumber));
    testElem.setGroupNumber(String.valueOf(group));
  }

  private static int getMatchNumber(CorrelationRuleTestElement testElem) {
    try {
      return Integer.valueOf(testElem.getMatchNumber());
    } catch (NumberFormatException e) {
      LOG.warn("Invalid value for Match Number ({}), falling back to default value: {}",
          testElem.getMatchNumber(), REGEX_DEFAULT_MATCH_NUMBER);
      return REGEX_DEFAULT_MATCH_NUMBER;
    }
  }

  private static int getGroupNumber(CorrelationRuleTestElement testElem) {
    try {
      return Integer.valueOf(testElem.getGroupNumber());
    } catch (NumberFormatException e) {
      LOG.warn("Invalid value for Group Number ({}), falling back to default value: {}",
          testElem.getMatchNumber(), REGEX_DEFAULT_GROUP);
      return REGEX_DEFAULT_GROUP;
    }
  }

  @Override
  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result,
      JMeterVariables vars) {
    if (regex.isEmpty()) {
      return;
    }
    String match = new RegexMatcher(target.getField(result), regex, group)
        .findMatch(matchNumber == -1 ? 1 : matchNumber);
    if (match != null) {
      children.add(createPostProcessor());
      vars.put(variableName, match);
    }
  }

  private RegexExtractor createPostProcessor() {
    RegexExtractor regexExtractor = new RegexExtractor();
    regexExtractor.setProperty(TestElement.GUI_CLASS, REGEX_EXTRACTOR_GUI_CLASS);
    regexExtractor.setName("RegExp - " + variableName);
    regexExtractor.setRefName(variableName);
    regexExtractor.setRegex(regex);
    regexExtractor.setTemplate("$" + group + "$");
    regexExtractor.setMatchNumber(matchNumber);
    regexExtractor.setDefaultValue(REGEX_DEFAULT_VALUE);
    regexExtractor.setUseField(target.getCode());
    return regexExtractor;
  }

}
