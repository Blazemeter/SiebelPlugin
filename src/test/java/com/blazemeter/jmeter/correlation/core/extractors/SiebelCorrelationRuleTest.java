package com.blazemeter.jmeter.correlation.core.extractors;

import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import com.blazemeter.jmeter.correlation.core.ResultField;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import com.blazemeter.jmeter.correlation.core.replacements.Replacement;
import com.blazemeter.jmeter.correlation.gui.CorrelationRuleTestElement;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SiebelCorrelationRuleTest {

  private static final String VARIABLE_NAME = "variable";
  private static final ResultField resultField = ResultField.URL;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Mock
  private Extractor extractor;

  @Mock
  private Replacement replacement;


  private CorrelationRule correlationRule;

  @Before
  public void setup() {
    correlationRule = new CorrelationRule(VARIABLE_NAME, extractor, replacement);
  }

  @Test
  public void shouldReturnACorretCorrelationRuleTestElement() {
    Extractor extractor = new RegexCorrelationExtractor("", 0, resultField);
    Replacement replacement = new RegexReplacement("");
    correlationRule = new CorrelationRule(VARIABLE_NAME, extractor, replacement);
    CorrelationRuleTestElement correlationRuleTestElementResult = correlationRule
        .buildTestElement();
    softly.assertThat(correlationRuleTestElementResult.getReferenceName()).as("referenceName")
        .isEqualTo(VARIABLE_NAME);
    softly.assertThat(correlationRuleTestElementResult.getTargetField()).as("targetField")
        .isEqualTo(resultField);
    softly.assertThat(correlationRuleTestElementResult.getExtractorClass()).as("extractorClass")
        .isEqualTo(extractor.getClass());
    softly.assertThat(correlationRuleTestElementResult.getReplacementClass()).as("replacementClass")
        .isEqualTo(replacement.getClass());
  }

}
