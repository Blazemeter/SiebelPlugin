package com.blazemeter.jmeter.correlation.siebel;

import com.blazemeter.jmeter.correlation.core.CorrelationEngine;
import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import com.blazemeter.jmeter.correlation.core.ResultField;
import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.replacements.FunctionReplacement;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import java.util.Arrays;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class SiebelCorrelationEngine extends CorrelationEngine<SiebelContext> {

  public SiebelCorrelationEngine() {
    context = new SiebelContext();
    rules = Arrays.asList(
        buildRule("SWEACn"),
        new CorrelationRule("Siebel_sn_cookie",
            new RegexCorrelationExtractor("_sn=(.*?);", 1, ResultField.RESPONSE_HEADERS),
            new RegexReplacement("_sn=([^&\\n]+)")),
        new CorrelationRule("Siebel_sn_body", "_sn=(.*?)[\"|&]", buildReplacementRegex("_sn")),
        new CorrelationRule("Siebel_sn_body", "name=\"_sn\" value=\"(.+?)\"",
            buildReplacementRegex("_sn")),
        buildRule("SWEBMC"),
        new CorrelationRule("Siebel_Analytic_ViewState", "ViewState\" value=\"=(.*?)\"",
            buildReplacementRegex("ViewState")),
        new CorrelationRule("Siebel_Analytic_search_id", "sid=\"(.*?)\"",
            buildReplacementRegex("sid")),
        new CorrelationRule("Siebel_SWEBID", "navigator\\.id = [\"]?(.*?)[\"]?;",
            buildReplacementRegex("SWEBID")),
        new CorrelationRule("Siebel_SWEBRS", "name=\"SWEBRS\" value=\"(.*?)\"",
            buildReplacementRegex("SWEBRS")),
        buildRule("SRN"),
        new CorrelationRule("Siebel_SWEFI", new RegexCorrelationExtractor("SWEFI([`^~\\[%|])(.*?)\\1",2),
            new RegexReplacement(buildReplacementRegex("SWEFI"))),
        new CorrelationRule("Siebel_SWEVLC", new RegexCorrelationExtractor("cks([`^~\\[%|])(.*?)\\1", 2),
            new RegexReplacement(buildReplacementRegex("ShowViewLayout"))),
        new CorrelationRule("__time()", null,
            new FunctionReplacement(buildReplacementRegex("SWETS"))),
        new CorrelationRule("Siebel_SWECount", null,
            new SiebelCounterReplacement(buildReplacementRegex("SWEC"), context)),
        new CorrelationRule("Siebel_SWERowId", null,
            new SiebelRowIdReplacement(buildReplacementRegex("SWERowId(?:0?)"), context)),
        new CorrelationRule("Siebel_Star_Array",
            new SiebelRowExtractor("([`^~\\[%|])ValueArray\\1(.*?)\\1", 2, context),
           new SiebelRowParamsReplacement(buildReplacementRegex("SWERowId"), context)),
        new CorrelationRule("Siebel_Star_Array_Op",
            new SiebelRowExtractor("([`^~\\[%|])v\\1(.*?)\\1", 2, context),
            new SiebelRowParamsReplacement(buildReplacementRegex("SWERowId"), context))
    );
  }

  private static CorrelationRule buildRule(String paramName) {
    return new CorrelationRule("Siebel_" + paramName, paramName + "=(.*?)&",
        buildReplacementRegex(paramName));
  }

  private static String buildReplacementRegex(String paramNameRegex) {
    return paramNameRegex + "=([^&\\n]+)";
  }

  @Override
  public void process(HTTPSamplerBase sampler, List<TestElement> children, SampleResult result) {
    context.update(result.getResponseDataAsString());
    rules.forEach(r -> r.applyReplacements(sampler, children, result, vars));
    if (!ContentType.TEXT_HTML.getMimeType().equals(result.getMediaType())) {
      return;
    }
    rules.forEach(r -> r.addExtractors(sampler, children, result, vars));
  }

}
