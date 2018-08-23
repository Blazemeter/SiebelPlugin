package com.blazemeter.jmeter.correlation.siebel;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.correlation.TestUtils;
import com.blazemeter.jmeter.correlation.siebel.SiebelContext.Field;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class SiebelContextTest {

  private static final String EXPECTED_PARAM = "s_2_1_4_0";
  private static final String NOT_EXPECTED_PARAM = "s_2_1_9_0";
  private static final String PARAM_WITH_TELEPHONE_TYPE_FIELD = "s_2_1_21_0";
  private static final String PARAM_WITH_STRING_TYPE_FIELD = "s_2_1_24_0";
  private static final String IGNORED_CHARS_REGEX = "[\\()\\- ]";
  private static final String VALID_RESPONSE_DATA_PATH = "src/test/resources/validResponseData.txt";
  private static final String INVALID_RESPONSE_DATA_PATH = "src/test/resources/invalidResponseData.txt";

  private static String VALID_RESPONSE_DATA;
  private static String INVALID_RESPONSE_DATA;

  private SiebelContext siebelContext = new SiebelContext();

  @Before
  public void setUp() throws IOException {
    VALID_RESPONSE_DATA = TestUtils.readFile(VALID_RESPONSE_DATA_PATH, Charset.defaultCharset());
    INVALID_RESPONSE_DATA = TestUtils
        .readFile(INVALID_RESPONSE_DATA_PATH, Charset.defaultCharset());
  }

  @Test
  public void validateWhetherExpectedParamRowFieldsIsPresent() {
    siebelContext.update(VALID_RESPONSE_DATA);
    Map<String, Field> paramRowFields = siebelContext.getParamRowFields();
    assertThat(paramRowFields.containsKey(EXPECTED_PARAM)).isTrue();
  }

  @Test
  public void validateWhetherNotExpectedParamRowFieldsIsNotPresent() {
    siebelContext.update(VALID_RESPONSE_DATA);
    Map<String, Field> paramRowFields = siebelContext.getParamRowFields();
    assertThat(paramRowFields.containsKey(NOT_EXPECTED_PARAM)).isFalse();
  }

  @Test
  public void validateWhetherParamsAreAddedIfInputStringMatchesSiebel() {
    siebelContext.update(VALID_RESPONSE_DATA);
    Map<String, Field> paramRowFields = siebelContext.getParamRowFields();
    assertThat(paramRowFields).isNotEmpty();
  }

  @Test
  public void validateWhetherNoParamsAreAddedIfInputStringDoesNotMatchSiebel() {
    siebelContext.update(INVALID_RESPONSE_DATA);
    Map<String, Field> paramRowFields = siebelContext.getParamRowFields();
    assertThat(paramRowFields).isEmpty();
  }

  @Test
  public void shoudlReturnExpectedCharactersWhenFieldTypeIsTelephone() {
    siebelContext.update(VALID_RESPONSE_DATA);
    Map<String, Field> paramRowFields = siebelContext.getParamRowFields();
    Field field = paramRowFields.get(PARAM_WITH_TELEPHONE_TYPE_FIELD);
    assertThat(IGNORED_CHARS_REGEX).isEqualTo(field.getIgnoredCharsRegex());
  }

  @Test
  public void shoudlReturnEmptyStringWhenFieldTypeIsString() {
    siebelContext.update(VALID_RESPONSE_DATA);
    Map<String, Field> paramRowFields = siebelContext.getParamRowFields();
    Field field = paramRowFields.get(PARAM_WITH_STRING_TYPE_FIELD);
    assertThat("").isEqualTo(field.getIgnoredCharsRegex());
  }
}
