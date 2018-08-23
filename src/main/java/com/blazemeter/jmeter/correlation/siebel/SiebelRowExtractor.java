package com.blazemeter.jmeter.correlation.siebel;

import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import com.blazemeter.jmeter.correlation.core.RegexMatcher;
import com.blazemeter.jmeter.correlation.core.ResultField;
import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import java.util.List;
import java.util.UUID;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiebelRowExtractor extends RegexCorrelationExtractor<SiebelContext> {

  private static final Logger LOG = LoggerFactory.getLogger(SiebelRowExtractor.class);

  public SiebelRowExtractor(String regex, SiebelContext context) {
    super(regex, -1, ResultField.BODY);
    this.context = context;
  }

  public SiebelRowExtractor(String regex, int group, SiebelContext context) {
    super(regex, group, -1, ResultField.BODY);
    this.context = context;
  }

  public SiebelRowExtractor(CorrelationRuleTestElement testElem, CorrelationContext context) {
    super(testElem, context);
  }

  @Override
  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result,
      JMeterVariables vars) {
    super.process(sampler, children, result, vars);
    vars.remove(variableName);
    JSR223PostProcessor jsr223PostProcessor = buildArrayParserPostProcessor(result, vars);
    if (jsr223PostProcessor != null) {
      children.add(jsr223PostProcessor);
    }
  }

  private JSR223PostProcessor buildArrayParserPostProcessor(SampleResult result,
      JMeterVariables vars) {
    StringBuilder script = new StringBuilder();
    JSR223PostProcessor jSR223PostProcessor = new JSR223PostProcessor();
    jSR223PostProcessor.setProperty(JSR223PostProcessor.GUI_CLASS, TestBeanGUI.class.getName());
    jSR223PostProcessor.setName("Parse Array Values");
    script.append("import com.blazemeter.jmeter.correlation.siebel.SiebelArrayFunction;\n\n");
    script.append("String stringToSplit = \"\";\n");
    script.append("String rowId = \"\";");
    int matchNumber = 1;
    for (String match : new RegexMatcher(target.getField(result), regex, getGroup()).findMatches()) {
      if (match == null) {
        continue;
      }
      try {
        String varNamePrefix = variableName + context.getNextRowPrefixId();
        SiebelArrayFunction.split(match, varNamePrefix, vars);
        int numberOfVariables = Integer.valueOf(vars.get(varNamePrefix + "_n"));
        script
            .append(String.format("\n\n// Parsing Star Array parameter(s) using match number %1$d\n"
                    + "stringToSplit = vars.get(\"%2$s_%1$d\");\n"
                    + "if (stringToSplit != null) {\n"
                    + "\tSiebelArrayFunction.split(stringToSplit, \"%3$s\", vars);\n"
                    + "\trowIdValue = vars.get(\"%3$s_%4$d\");\n"
                    + "\tvars.put(\"%3$s_rowId\", rowIdValue);"
                    + "\n}", matchNumber, variableName, varNamePrefix,
                numberOfVariables));
        String rowId = vars.get(varNamePrefix + "_" + numberOfVariables);
        context.addRowVar(rowId, varNamePrefix);
        vars.put(varNamePrefix + "_rowId", rowId);
        matchNumber++;
      } catch (IllegalArgumentException e) {
        LOG.warn(e.getMessage());
      }
    }
    jSR223PostProcessor.setProperty("script", script.toString());
    jSR223PostProcessor.setProperty("cacheKey", UUID.randomUUID().toString());
    return matchNumber > 1 ? jSR223PostProcessor : null;
  }

}
