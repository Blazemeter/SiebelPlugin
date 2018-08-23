package com.blazemeter.jmeter.correlation.siebel;

import com.blazemeter.jmeter.correlation.TestUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class SiebelCorrelationEngineTest {

  private static final String RESPONSE_TO_EXTRACT_ARRAY = "src/test/resources/responseToExtractArray.txt";
  private static final String REQUEST_TO_REPLACE_ARRAY = "src/test/resources/requestToReplaceArray.txt";
  private static final String STAR_ARRAY_POST_PROCESSOR = "src/test/resources/starArrayPostProcessor.groovy";

  private HTTPSamplerBase httpSampler;

  private List<TestElement> childrenExtractor;
  private List<TestElement> childrenReplacement;
  private HTTPSamplerBase httpSamplerExtractor;
  private HTTPSamplerBase httpSamplerReplacement;
  private SampleResult sampleResultExtractor;
  private SampleResult sampleResultReplacement;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Test
  public void shouldGenerateAllExtractorsAndReplacementsExceptArrays()
      throws MalformedURLException {

    SiebelCorrelationEngine siebelCorrelationEngine = new SiebelCorrelationEngine();
    httpSamplerExtractor = createHTTPSampler(new Arguments());
    childrenExtractor = new ArrayList<>();
    childrenExtractor.add(new HeaderManager());
    List<TestElement> childrenExpected = createChildrenExpectedExtractorsAndReplacementsExceptArrays();
    sampleResultExtractor = createSampleResultExtractorExceptArrays();
    siebelCorrelationEngine.process(httpSamplerExtractor, childrenExtractor, sampleResultExtractor);
    httpSamplerReplacement = createHTTPSampler(createArgumentsReplacementExceptsArray());
    childrenReplacement = new ArrayList<>();
    childrenReplacement.add(new HeaderManager());
    sampleResultReplacement = createSampleResultReplacementExceptArrays();
    siebelCorrelationEngine
        .process(httpSamplerReplacement, childrenReplacement, sampleResultReplacement);
    softly.assertThat(httpSamplerReplacement.getArguments())
        .isEqualTo(createExpectedArgumentsReplacementExceptsArray());
    softly.assertThat(TestUtils.comparableFrom(childrenExtractor))
        .isEqualTo(TestUtils.comparableFrom(childrenExpected));
  }

  private Arguments createArgumentsReplacementExceptsArray() {
    Arguments arguments = new Arguments();
    arguments.addArgument(new HTTPArgument("SWEACn", "SWEACnTest", "=", false));
    arguments.addArgument(new HTTPArgument("_sn", "snTest", "=", false));
    arguments.addArgument(new HTTPArgument("SWEBMC", "SWEBMCTest", "=", false));
    arguments.addArgument(new HTTPArgument("ViewState", "ViewStateTest", "=", false));
    arguments.addArgument(new HTTPArgument("sid", "sidTest", "=", false));
    arguments.addArgument(new HTTPArgument("SWEBRS", "SWEBRSTest", "=", false));
    arguments.addArgument(new HTTPArgument("SRN", "SRNTest", "=", false));
    arguments.addArgument(new HTTPArgument("SWEFI", "SWEFITest", "=", false));
    arguments.addArgument(new HTTPArgument("ShowViewLayout", "cksTest", "=", false));
    arguments.addArgument(new HTTPArgument("SWETS", "SWETSTest", "=", false));
    arguments.addArgument(new HTTPArgument("SWEC", "0", "=", false));
    return arguments;
  }

  private Arguments createExpectedArgumentsReplacementExceptsArray() {
    Arguments expectedArguments = new Arguments();
    expectedArguments.addArgument(new HTTPArgument("SWEACn", "${Siebel_SWEACn}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("_sn", "${Siebel_sn_cookie}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("SWEBMC", "${Siebel_SWEBMC}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("ViewState", "${Siebel_Analytic_ViewState}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("sid", "${Siebel_Analytic_search_id}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("SWEBRS", "${Siebel_SWEBRS}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("SRN", "${Siebel_SRN}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("SWEFI", "${Siebel_SWEFI}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("ShowViewLayout", "${Siebel_SWEVLC}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("SWETS", "${__time()}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("SWEC", "${Siebel_SWECount}", "=", false));
    return expectedArguments;
  }

  private SampleResult createSampleResultExtractorExceptArrays() throws MalformedURLException {
    SampleResult sampleResultExtractor = new SampleResult();
    sampleResultExtractor.setSamplerData("SWEC=0\n");
    sampleResultExtractor.setURL(new URL("https://oracle:16691/siebel/app/salesm/enu"));
    sampleResultExtractor.setResponseHeaders("_sn=snTest;\n");
    sampleResultExtractor.setRequestHeaders("");
    sampleResultExtractor.setResponseCodeOK();
    sampleResultExtractor.setResponseMessageOK();
    sampleResultExtractor.setContentType("text/html");
    sampleResultExtractor.setResponseData("SWEACn=SWEACnTest&\n"
            + "amp;SWEC=0&amp;SWEFrame=top._swe&amp;SRN=\"  tabindex=-1>\n"
            + "SWEBMC=SWEBMCTest&\n"
            + "SRN=SRNTest&\n"
            + "_sn=snTest&\n"
            + "name=\"_sn\" value=\"snTest\"\n"
            + "ViewState\" value=\"=ViewStateTest\"\n"
            + "sid=\"sidTest\"\n"
            + "name=\"SWEBRS\" value=\"SWEBRSTest\"\n"
            + "SWEFI^SWEFITest^\n"
            + "cks^cksTest^\n"
        , SampleResult.DEFAULT_HTTP_ENCODING);
    return sampleResultExtractor;
  }

  private SampleResult createSampleResultReplacementExceptArrays() throws MalformedURLException {
    SampleResult sampleResultReplacement = new SampleResult();
    sampleResultReplacement.setSamplerData("SWEC=0");
    sampleResultReplacement.setURL(new URL("https://oracle:16691/siebel/app/salesm/enu"));
    sampleResultReplacement.setResponseHeaders("");
    sampleResultReplacement.setRequestHeaders("");
    sampleResultReplacement.setResponseCodeOK();
    sampleResultReplacement.setResponseMessageOK();
    sampleResultReplacement.setResponseData("", SampleResult.DEFAULT_HTTP_ENCODING);
    return sampleResultReplacement;
  }

  private List<TestElement> createChildrenExpectedExtractorsAndReplacementsExceptArrays() {
    List<TestElement> childrenExpected = new ArrayList<>();
    childrenExpected.add(new HeaderManager());
    childrenExpected
        .add(createJSR223PreProcessor("Calculate Siebel_SWECount", "int Siebel_SWECount_var = 0;\n"
            + "vars.put(\"Siebel_SWECount\", String.valueOf(Siebel_SWECount_var));\n"));
    childrenExpected
        .add(createRegexExtractor("Siebel_SWEACn", "SWEACn=(.*?)&", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected
        .add(createRegexExtractor("Siebel_sn_cookie", "_sn=(.*?);", "1", "1",
            RegexExtractor.USE_HDRS));
    childrenExpected.add(
        createRegexExtractor("Siebel_sn_body", "_sn=(.*?)[\"|&]", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected
        .add(createRegexExtractor("Siebel_sn_body", "name=\"_sn\" value=\"(.+?)\"", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected
        .add(createRegexExtractor("Siebel_SWEBMC", "SWEBMC=(.*?)&", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected.add(
        createRegexExtractor("Siebel_Analytic_ViewState", "ViewState\" value=\"=(.*?)\"", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected
        .add(createRegexExtractor("Siebel_Analytic_search_id", "sid=\"(.*?)\"", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected.add(
        createRegexExtractor("Siebel_SWEBRS", "name=\"SWEBRS\" value=\"(.*?)\"", "1", "1",
            RegexExtractor.USE_BODY));
    childrenExpected
        .add(createRegexExtractor("Siebel_SRN", "SRN=(.*?)&", "1", "1", RegexExtractor.USE_BODY));
    childrenExpected.add(createRegexExtractor("Siebel_SWEFI", "SWEFI([`^~\\[%|])(.*?)\\1", "2", "1",
        RegexExtractor.USE_BODY));
    childrenExpected.add(
        createRegexExtractor("Siebel_SWEVLC", "cks([`^~\\[%|])(.*?)\\1", "2", "1",
            RegexExtractor.USE_BODY));
    return childrenExpected;
  }

  private HTTPSamplerBase createHTTPSampler(Arguments arguments) {
    httpSampler = new HTTPSampler();
    httpSampler.setArguments(arguments);
    httpSampler.setDomain("oracle");
    httpSampler.setPort(16691);
    httpSampler.setProtocol("https");
    httpSampler.setContentEncoding("UTF-8");
    httpSampler.setPath("/siebel/app/salesm/enu");
    httpSampler.setMethod("POST");
    return httpSampler;
  }

  private JSR223PreProcessor createJSR223PreProcessor(String name, String script) {
    JSR223PreProcessor jSR223PreProcessor = new JSR223PreProcessor();
    jSR223PreProcessor.setProperty(JSR223PreProcessor.GUI_CLASS, TestBeanGUI.class.getName());
    jSR223PreProcessor.setName(name);
    jSR223PreProcessor.setProperty("script", script);
    return jSR223PreProcessor;
  }

  private JSR223PostProcessor createJSR223PostProcessor(String name, String script) {
    JSR223PostProcessor jSR223PostProcessor = new JSR223PostProcessor();
    jSR223PostProcessor.setProperty(JSR223PostProcessor.GUI_CLASS, TestBeanGUI.class.getName());
    jSR223PostProcessor.setName(name);
    jSR223PostProcessor.setProperty("script", script);
    return jSR223PostProcessor;
  }

  private RegexExtractor createRegexExtractor(String name, String regex, String group,
      String matchNumber,
      String target) {
    RegexExtractor regexExtractor = new RegexExtractor();
    regexExtractor.setProperty(TestElement.GUI_CLASS, RegexExtractorGui.class.getName());
    regexExtractor.setName("RegExp - " + name);
    regexExtractor.setRefName(name);
    regexExtractor.setRegex(regex);
    regexExtractor.setTemplate("$" + group + "$");
    regexExtractor.setMatchNumber(matchNumber);
    regexExtractor.setDefaultValue("NOT_FOUND");
    regexExtractor.setUseField(target);
    return regexExtractor;
  }

  @Test
  public void shouldGenerateArraysExtractorAndReplacement() throws IOException {

    SiebelCorrelationEngine siebelCorrelationEngine = new SiebelCorrelationEngine();
    httpSamplerExtractor = createHTTPSampler(new Arguments());
    childrenExtractor = new ArrayList<>();
    childrenExtractor.add(new HeaderManager());
    List<TestElement> childrenExpected = createChildrenExpectedExtractorOnlyArray();
    sampleResultExtractor = createSampleResultExtractorArrays();
    siebelCorrelationEngine.process(httpSamplerExtractor, childrenExtractor, sampleResultExtractor);
    httpSamplerReplacement = createHTTPSampler(createArgumentsReplacementArray());
    childrenReplacement = new ArrayList<>();
    childrenReplacement.add(new HeaderManager());
    sampleResultReplacement = createSampleResultReplacementArrays();
    siebelCorrelationEngine
        .process(httpSamplerReplacement, childrenReplacement, sampleResultReplacement);
    softly.assertThat(httpSamplerReplacement.getArguments())
        .isEqualTo(createExpectedArgumentsReplacementArray());
    softly.assertThat(TestUtils.comparableFrom(childrenExtractor))
        .isEqualTo(TestUtils.comparableFrom(childrenExpected));
  }

  private SampleResult createSampleResultReplacementArrays() throws IOException {
    SampleResult sampleResultReplacement = new SampleResult();
    sampleResultReplacement
        .setSamplerData(TestUtils.readFile(REQUEST_TO_REPLACE_ARRAY, Charset.defaultCharset()));
    sampleResultReplacement.setURL(new URL("https://oracle:16691/siebel/app/salesm/enu"));
    sampleResultReplacement
        .setResponseHeaders("");
    sampleResultReplacement.setRequestHeaders("");
    sampleResultReplacement.setResponseCodeOK();
    sampleResultReplacement.setResponseMessageOK();
    sampleResultReplacement.setResponseData("", SampleResult.DEFAULT_HTTP_ENCODING);
    return sampleResultReplacement;
  }


  private Arguments createArgumentsReplacementArray() {
    Arguments arguments = new Arguments();
    arguments.addArgument(new HTTPArgument("s_1_2_20_0", "3 CommmmT", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_22_0", "HQ-Distribution", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_21_0", "Customer", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_14_0", "Active", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_19_0", "(650) 555-1212", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_17_0", "2000 West Embarcadero Rd", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_16_0", "", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_2_15_0", "3COM.com", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_1_2_0", "", "=", false));
    arguments.addArgument(new HTTPArgument("s_1_1_3_0", "", "=", false));
    arguments.addArgument(new HTTPArgument("SWERowId", "1-63Q9", "=", false));
    return arguments;
  }

  private Arguments createExpectedArgumentsReplacementArray() {
    Arguments expectedArguments = new Arguments();
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_20_0", "${Siebel_Star_Array_Op4_1}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_22_0", "${Siebel_Star_Array_Op4_2}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_21_0", "${Siebel_Star_Array_Op4_5}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_14_0", "${Siebel_Star_Array_Op4_6}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_19_0", "${Siebel_Star_Array_Op4_8}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_17_0", "${Siebel_Star_Array_Op4_9}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_16_0", "${Siebel_Star_Array_Op4_10}", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("s_1_2_15_0", "${Siebel_Star_Array_Op4_11}", "=", false));
    expectedArguments.addArgument(new HTTPArgument("s_1_1_2_0", "", "=", false));
    expectedArguments.addArgument(new HTTPArgument("s_1_1_3_0", "", "=", false));
    expectedArguments
        .addArgument(new HTTPArgument("SWERowId", "${Siebel_Star_Array_Op4_rowId}", "=", false));
    return expectedArguments;
  }

  private SampleResult createSampleResultExtractorArrays() throws IOException {
    SampleResult sampleResultExtractor = new SampleResult();
    sampleResultExtractor.setSamplerData("");
    sampleResultExtractor.setURL(new URL("https://oracle:16691/siebel/app/salesm/enu"));
    sampleResultExtractor.setResponseHeaders("");
    sampleResultExtractor.setRequestHeaders("");
    sampleResultExtractor.setResponseCodeOK();
    sampleResultExtractor.setResponseMessageOK();
    sampleResultExtractor.setContentType("text/html");
    sampleResultExtractor
        .setResponseData(TestUtils.readFile(RESPONSE_TO_EXTRACT_ARRAY, Charset.defaultCharset()),
            SampleResult.DEFAULT_HTTP_ENCODING);
    return sampleResultExtractor;
  }

  private List<TestElement> createChildrenExpectedExtractorOnlyArray() throws IOException {
    List<TestElement> childrenExpected = new ArrayList<>();
    childrenExpected.add(new HeaderManager());
    childrenExpected.add(
        createRegexExtractor("Siebel_Star_Array_Op", "([`^~\\[%|])v\\1(.*?)\\1", "2", "-1",
            RegexExtractor.USE_BODY));
    childrenExpected.add(createJSR223PostProcessor("Parse Array Values",
        TestUtils.readFile(STAR_ARRAY_POST_PROCESSOR, Charset.defaultCharset())));
    return childrenExpected;
  }

}
