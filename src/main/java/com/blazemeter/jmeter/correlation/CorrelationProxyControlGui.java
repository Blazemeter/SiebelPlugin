package com.blazemeter.jmeter.correlation;

import com.blazemeter.jmeter.correlation.gui.BlazemeterLabsLogo;
import com.blazemeter.jmeter.correlation.gui.CorrelationRulesPanel;
import com.blazemeter.jmeter.correlation.gui.CorrelationRulesTestElement;
import com.blazemeter.jmeter.correlation.gui.TemplateRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.JTabbedPane;
import org.apache.jmeter.protocol.http.proxy.gui.ProxyControlGui;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorrelationProxyControlGui extends ProxyControlGui {

  private static final Logger LOG = LoggerFactory.getLogger(CorrelationProxyControlGui.class);

  private static final String SIEBEL_TEMPLATE_NAME = "recording-siebel.jmx";
  private static final String SIEBEL_DESC_TEMPLATE_NAME = "siebelTemplateDescription.xml";
  private static final String SIEBEL_TEMPLATE_XML_NAME = "Recording Siebel";

  private final CorrelationRulesPanel rulesPanel;

  public CorrelationProxyControlGui() {
    rulesPanel = new CorrelationRulesPanel();
    JTabbedPane siebelPane = findTabbedPane();
    siebelPane.add("Siebel Correlation", rulesPanel);
    this.add(new BlazemeterLabsLogo(), BorderLayout.SOUTH);
    TemplateRepository templateRepository = new TemplateRepository(
        getJMeterBinDirPath() + "/templates/");
    templateRepository.addSiebelTemplate(SIEBEL_TEMPLATE_NAME, "/templates/" + SIEBEL_TEMPLATE_NAME,
        "/templates/" + SIEBEL_DESC_TEMPLATE_NAME, SIEBEL_TEMPLATE_XML_NAME);
  }

  private JTabbedPane findTabbedPane() {
    LinkedList<Component> queue = new LinkedList<>(Arrays.asList(this.getComponents()));
    while (!queue.isEmpty()) {
      Component component = queue.removeFirst();
      if (component instanceof JTabbedPane) {
        return (JTabbedPane) component;
      } else if (component instanceof Container) {
        queue.addAll(Arrays.asList(((Container) component).getComponents()));
      }
    }
    return null;
  }

  @Override
  public String getStaticLabel() {
    return "Siebel HTTP(S) Test Script Recorder";
  }

  public String getJMeterBinDirPath() {
    String siebelPluginPath = getClass().getProtectionDomain().getCodeSource().getLocation()
        .getPath();

    /*This is done to obtain and remove the initial `/` from the path.
      i.e: In Windows the path would be something like `/C:`,
      so we check if the char at position 3 is ':' and if so, we remove the initial '/'.
    */
    char a_char = siebelPluginPath.charAt(2);
    if (a_char == ':') {
      siebelPluginPath = siebelPluginPath.substring(1);
    }
    int index = siebelPluginPath.indexOf("/lib/ext/");
    String binPath = siebelPluginPath.substring(0, index) + "/bin";
    return binPath;
  }

  @Override
  public void modifyTestElement(TestElement el) {
    super.modifyTestElement(el);
    if (el instanceof CorrelationProxyControl) {
      CorrelationProxyControl model = (CorrelationProxyControl) el;
      model.setCorrelationRules((CorrelationRulesTestElement) rulesPanel.createTestElement());
    }
  }

  @Override
  public void configure(TestElement el) {
    LOG.debug("Configuring gui with {}", el);
    super.configure(el);
    if (el instanceof CorrelationProxyControl) {
      CorrelationProxyControl model = (CorrelationProxyControl) el;
      rulesPanel.configure(model.getCorrelationRules());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TestElement createTestElement() {
    CorrelationProxyControl model = new CorrelationProxyControl();
    LOG.debug("creating/configuring model = {}", model);
    configure(model);
    return model;
  }

}
