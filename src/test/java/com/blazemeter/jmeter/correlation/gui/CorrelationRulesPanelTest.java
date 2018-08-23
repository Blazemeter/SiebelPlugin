package com.blazemeter.jmeter.correlation.gui;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import com.blazemeter.jmeter.correlation.CorrelationProxyControlGui;
import com.blazemeter.jmeter.correlation.core.ResultField;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class CorrelationRulesPanelTest {

  private static final long TIMEOUT_MILLIS = 10000;

  private FrameFixture frame;
  private CorrelationRulesPanel correlationRulesPanel;

  @Mock
  private CorrelationProxyControlGui correlationProxyControlGui;

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    correlationRulesPanel = new CorrelationRulesPanel();
    frame = showInFrame(correlationRulesPanel);
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }


  @Test
  public void shouldAddRowToTableWhenClickAddButton() {
    JTableFixture table = frame.table("table");
    int rowCount = table.target().getRowCount();
    frame.button("addButton").click();
    pause(new Condition("New row added to table") {
      @Override
      public boolean test() {
        return table.target().getRowCount() == rowCount + 1;
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldAddRowFromClipboardToTableWhenClickAddFromClipboardButton() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(
        new StringSelection("ReferenceName\tRegEx1\tRegEx2\t1\t1\tBody"),
        new StringSelection(""));
    JTableFixture table = frame.table("table");
    frame.button("addFromClipboardButton").click();
    pause(new Condition("New row added to table form clipboard") {
      @Override
      public boolean test() {
        return table.target().getModel().getValueAt(0, 0).equals("ReferenceName");
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldAddRowFromClipboardToTableWithDefaultFieldValueWhenFieldEnumDoesNotExist() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(
        new StringSelection("ReferenceName\tRegEx1\tRegEx2\t1\t1\tNOTENUM"),
        new StringSelection(""));
    frame.button("addFromClipboardButton").click();
    CorrelationRulesTestElement correlationRulesTestElement = (CorrelationRulesTestElement) correlationRulesPanel
        .createTestElement();
    pause(new Condition(
        "New row added to table from clipboard and change target for a default target") {
      @Override
      public boolean test() {
        return correlationRulesTestElement.getRules().get(0).getTargetField()
            .equals(ResultField.BODY);
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldEnableDeleteButtonWhenAddRow() {
    frame.button("addButton").click();
    waitButtonEnable("deleteButton", true, "Delete button enable");
  }

  public void waitButtonEnable(String buttonName, boolean enable, String message) {
    pause(new Condition(message) {
      @Override
      public boolean test() {
        return enable ? frame.button(buttonName).isEnabled() : !frame.button(buttonName).isEnabled();
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldEnableDownButtonWhenAddMoreThanOneRow() {
    frame.button("addButton").click();
    frame.button("addButton").click();
    waitButtonEnable("downButton", true, "Down button enable");
  }

  @Test
  public void shouldEnableUpButtonWhenAddMoreThanOneRow() {
    frame.button("addButton").click();
    frame.button("addButton").click();
    waitButtonEnable("upButton", true, "Up button enable");
  }

  @Test
  public void shouldRemoveOneRowFromTableWhenClickDeleteButton() {
    initializePanel(2);
    JTableFixture table = frame.table("table");
    int rowCount = table.target().getRowCount();
    table.target().setRowSelectionInterval(1, 1);
    frame.button("deleteButton").click();
    pause(new Condition("One row removed from table") {
      @Override
      public boolean test() {
        return table.target().getRowCount() == rowCount - 1;
      }
    }, TIMEOUT_MILLIS);
  }

  private void initializePanel(int rows) {
    List<CorrelationRuleTestElement> rules = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      CorrelationRuleTestElement rule = new CorrelationRuleTestElement();
      rule.setReferenceName("ReferenceName" + i);
      rules.add(rule);
    }
    CorrelationRulesTestElement correlationRulesTestElement = new CorrelationRulesTestElement(
        rules);
    correlationRulesPanel.configure(correlationRulesTestElement);
  }

  @Test
  public void shouldRemoveMultiplesRowsFromTableWhenClickDeleteButtonAndMoreThanOneRowAreSelected() {
    initializePanel(5);
    JTableFixture table = frame.table("table");
    int rowCount = table.target().getRowCount();
    table.target().setRowSelectionInterval(2, 3);
    frame.button("deleteButton").click();
    pause(new Condition("Multiples rows removed from table") {
      @Override
      public boolean test() {
        return table.target().getRowCount() == rowCount - 2;
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldDisableUpButtonWhenAreLessThanTwoRows() {
    initializePanel(2);
    JTableFixture table = frame.table("table");
    table.target().setRowSelectionInterval(1, 1);
    frame.button("deleteButton").click();
    waitButtonEnable("upButton", false, "Disable up button");
  }

  @Test
  public void shouldDisableDownButtonWhenAreLessThanTwoRows() {
    initializePanel(2);
    JTableFixture table = frame.table("table");
    table.target().setRowSelectionInterval(1, 1);
    frame.button("deleteButton").click();
    waitButtonEnable("downButton", false, "Disable down button");
  }

  @Test
  public void shouldDisableDeleteButtonWhenAreNotRows() {
    initializePanel(1);
    JTableFixture table = frame.table("table");
    table.target().setRowSelectionInterval(0, 0);
    frame.button("deleteButton").click();
    waitButtonEnable("deleteButton", false, "Disable delete button");
  }

  @Test
  public void shouldChangeRowsOrderWhenClickUpButton() {
    initializePanel(2);
    JTableFixture table = frame.table("table");
    table.target().setRowSelectionInterval(1, 1);
    frame.button("upButton").click();
    pause(new Condition("Change rows order") {
      @Override
      public boolean test() {
        return table.target().getValueAt(0, 0).equals("ReferenceName1") && table.target()
            .getValueAt(1, 0).equals("ReferenceName0");
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldChangeRowsOrderWhenClickDownButton() {
    initializePanel(2);
    JTableFixture table = frame.table("table");
    table.target().setRowSelectionInterval(0, 0);
    frame.button("downButton").click();
    pause(new Condition("Change rows order") {
      @Override
      public boolean test() {
        return table.target().getValueAt(0, 0).equals("ReferenceName1") && table.target()
            .getValueAt(1, 0).equals("ReferenceName0");
      }
    }, TIMEOUT_MILLIS);
  }

}
