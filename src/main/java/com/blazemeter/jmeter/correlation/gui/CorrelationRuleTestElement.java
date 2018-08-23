package com.blazemeter.jmeter.correlation.gui;

import com.blazemeter.jmeter.correlation.core.ResultField;
import com.blazemeter.jmeter.correlation.core.extractors.Extractor;
import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.replacements.RegexReplacement;
import com.blazemeter.jmeter.correlation.core.replacements.Replacement;
import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorrelationRuleTestElement extends AbstractTestElement implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(CorrelationRuleTestElement.class);
  private static final Class<? extends Extractor> DEFAULT_EXTRACTOR_TYPE = RegexCorrelationExtractor.class;
  private static final Class<? extends Replacement> DEFAULT_REPLACEMENT_TYPE = RegexReplacement.class;

  public static final String REFERENCE_NAME = "CorrelationRule.referenceName";
  public static final String REPLACEMENT_REGEX = "CorrelationRule.replacementRegex";
  public static final String EXTRACTOR_REGEX = "CorrelationRule.extractorRegex";
  public static final String MATCH_NUMBER = "CorrelationRule.matchNumber";
  public static final String GROUP_NUMBER = "CorrelationRule.groupNumber";
  public static final String TARGET_FIELD = "CorrelationRule.targetField";
  public static final String EXTRACTOR_CLASS = "CorrelationRule.extractorClass";
  public static final String REPLACEMENT_CLASS = "CorrelationRule.replacementClass";

  public CorrelationRuleTestElement() {
    this.setTargetField(ResultField.BODY);
  }

  public String getReferenceName() {
    return getPropertyAsString(REFERENCE_NAME);
  }

  public void setReferenceName(String referenceName) {
    setProperty(new StringProperty(REFERENCE_NAME, referenceName));
  }

  public String getReplacementRegex() {
    return getPropertyAsString(REPLACEMENT_REGEX);
  }

  public void setReplacementRegex(String replacementRegex) {
    setProperty(new StringProperty(REPLACEMENT_REGEX, replacementRegex));
  }

  public String getExtractorRegex() {
    return getPropertyAsString(EXTRACTOR_REGEX);
  }

  public void setExtractorRegex(String extractorRegex) {
    setProperty(new StringProperty(EXTRACTOR_REGEX, extractorRegex));
  }

  public void setMatchNumber(String matchNumber) {
    setProperty(MATCH_NUMBER, matchNumber);
  }

  public String getMatchNumber() {
    return getPropertyAsString(MATCH_NUMBER);
  }

  public void setGroupNumber(String matchNumber) {
    setProperty(GROUP_NUMBER, matchNumber);
  }

  public String getGroupNumber() {
    return getPropertyAsString(GROUP_NUMBER);
  }

  public ResultField getTargetField() {
    return ResultField.valueOf(getPropertyAsString(TARGET_FIELD));
  }

  public void setTargetField(ResultField fieldToCheck) {
    setProperty(new StringProperty(TARGET_FIELD, fieldToCheck.name()));
  }

  public void setTargetFieldFromString(String targetField) {
    setProperty(new StringProperty(TARGET_FIELD, targetField.toUpperCase()));
  }

  public Class<? extends Extractor> getExtractorClass() {
    String extractorClass = getPropertyAsString(EXTRACTOR_CLASS);
    if (extractorClass.isEmpty()) {
      return DEFAULT_EXTRACTOR_TYPE;
    }
    try {
      return (Class<? extends Extractor>) Class.forName(extractorClass);
    } catch (ClassNotFoundException e) {
      LOG.error("Unsupported extractor type {}, defaulting to {}", extractorClass,
          DEFAULT_EXTRACTOR_TYPE, e);
      return DEFAULT_EXTRACTOR_TYPE;
    }
  }

  public void setExtractorClass(Class<? extends Extractor> extractorClass) {
    setProperty(new StringProperty(EXTRACTOR_CLASS, extractorClass.getCanonicalName()));
  }

  public Class<? extends Replacement> getReplacementClass() {
    String replacementClass = getPropertyAsString(REPLACEMENT_CLASS);
    if (replacementClass.isEmpty()) {
      return DEFAULT_REPLACEMENT_TYPE;
    }
    try {
      return (Class<? extends Replacement>) Class.forName(replacementClass);
    } catch (ClassNotFoundException e) {
      LOG.error("Unsupported replacement type {}, defaulting to {}", replacementClass,
          DEFAULT_REPLACEMENT_TYPE, e);
      return DEFAULT_REPLACEMENT_TYPE;
    }
  }

  public void setReplacementClass(Class<? extends Replacement> replacementClass) {
    setProperty(new StringProperty(REPLACEMENT_CLASS, replacementClass.getCanonicalName()));
  }

}
