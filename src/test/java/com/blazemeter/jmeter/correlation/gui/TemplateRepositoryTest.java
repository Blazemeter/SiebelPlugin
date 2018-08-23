package com.blazemeter.jmeter.correlation.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TemplateRepositoryTest {

  private TemplateRepository templateRepository;

  private static String TEMPLATES_LIST_PATH = "/templates.xml";
  private static String TEMPLATE_PATH = "/recording-siebel.jmx";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Before
  public void setup() {
    templateRepository = new TemplateRepository(tempFolder.getRoot().getPath() + "/");
  }

  @Test
  public void shouldAddATemplateAndATemplateDescriptionWhenNotExistTheTemplate()
      throws IOException {
    copyFile("/templates.xml", tempFolder.getRoot().getPath() + TEMPLATES_LIST_PATH);
    templateRepository.addSiebelTemplate("recording-siebel.jmx", "/recording-siebel.jmx",
        "/siebelTemplateDescription.xml", "Recording Siebel");
    assertion();
  }

  @Test
  public void shouldNotAddTemplateDescriptionWhenItWasAlreadyAdded() throws IOException {
    copyFile("/templatesSiebel.xml", tempFolder.getRoot().getPath() + TEMPLATES_LIST_PATH);
    templateRepository.addSiebelTemplate("recording-siebel.jmx", "/recording-siebel.jmx",
        "/siebelTemplateDescription.xml", "Recording Siebel");
    assertion();
  }

  @Test
  public void shouldNotAddATemplateWhenItWasAlreadyAdded() throws IOException {
    copyFile("/templatesSiebel.xml", tempFolder.getRoot().getPath() + TEMPLATES_LIST_PATH);
    templateRepository.addSiebelTemplate("recording-siebel.jmx", "/recording-siebel.jmx",
        "/siebelTemplateDescription.xml", "Recording Siebel");
    File result = new File(tempFolder.getRoot().getPath() + "recording-siebel.jmx");
    long lastModifiedExpected = result.lastModified();
    templateRepository.addSiebelTemplate("recording-siebel.jmx", "/recording-siebel.jmx",
        "/siebelTemplateDescription.xml", "Recording Siebel");
    result = new File(tempFolder.getRoot().getPath() + "recording-siebel.jmx");
    long lastModifiedResult = result.lastModified();
    softly.assertThat(lastModifiedResult == lastModifiedExpected);
    assertion();
  }

  private void assertion() throws IOException {
    File resultTemplate = new File(tempFolder.getRoot().getPath() + TEMPLATE_PATH);
    File resultTemplatesList = new File(tempFolder.getRoot().getPath() + TEMPLATES_LIST_PATH);
    softly.assertThat(FileUtils.readFileToString(resultTemplate, "utf-8"))
        .isEqualToNormalizingWhitespace(getFileFromResources("/recording-siebel.jmx"));
    softly.assertThat(FileUtils.readFileToString(resultTemplatesList, "utf-8"))
        .isEqualToNormalizingWhitespace(getFileFromResources("/templatesSiebel.xml"));
  }

  private void copyFile(String src, String dst) throws IOException {
    File dstFile = new File(dst);
    try (FileWriter fileWriter = new FileWriter(dstFile)) {
      fileWriter.write(getFileFromResources(src));
    }
  }

  private String getFileFromResources(String fileName) throws IOException {
    InputStream inputStream = this.getClass().getResourceAsStream(fileName);
    return IOUtils.toString(inputStream, "UTF-8");
  }
}
