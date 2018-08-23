package com.blazemeter.jmeter.correlation.gui;

import com.blazemeter.jmeter.correlation.core.ResultField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorrelationRulesPanel extends JPanel implements ActionListener {

  private static final String ADD = "add";
  private static final String ADD_FROM_CLIPBOARD = "addFromClipboard";
  private static final String DELETE = "delete";
  private static final String UP = "up";
  private static final String DOWN = "down";
  private static final String CLIPBOARD_LINE_DELIMITERS = "\n";
  private static final String CLIPBOARD_RULES_DELIMITERS = "\t";

  private static final Logger LOG = LoggerFactory.getLogger(CorrelationRulesPanel.class);

  private JLabel tableLabel;
  private transient ObjectTableModel tableModel;
  private transient JTable table;
  private JButton deleteButton;
  private JButton upButton;
  private JButton downButton;

  public CorrelationRulesPanel() {
    this.tableLabel = createComponent("correlationRulesLabel", new JLabel("Correlation Rules"));
    setLayout(new BorderLayout());

    add(makeLabelPanel(), BorderLayout.NORTH);
    add(makeMainPanel(), BorderLayout.CENTER);

    add(Box.createVerticalStrut(70), BorderLayout.WEST);
    add(makeButtonPanel(), BorderLayout.SOUTH);

    setFieldToCheckComboBox();
    centerCell(3);
    centerCell(4);
    centerCell(5);
    table.revalidate();
  }

  private static <T extends JComponent> T createComponent(String name, T component) {
    component.setName(name);
    return component;
  }

  private Component makeMainPanel() {
    initializeTableModel();
    table = createComponent("table", new JTable(tableModel));
    table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer() {
      @Override
      protected String getText(Object value, int row, int column) {
        return (value == null) ? "" : value.toString();
      }
    });
    JMeterUtils.applyHiDPI(table);
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(pane.getMinimumSize());
    return pane;
  }

  private void initializeTableModel() {
    tableModel = new ObjectTableModel(
        new String[]{"Reference Name", "Request Regex", "Response Regex", "Match Number",
            "Match Group",
            "Field to Check"},
        CorrelationRuleTestElement.class,
        new Functor[]{new Functor("getReferenceName"), new Functor("getReplacementRegex"),
            new Functor("getExtractorRegex"), new Functor("getMatchNumber"),
            new Functor("getGroupNumber"),
            new Functor("getTargetField")},
        new Functor[]{new Functor("setReferenceName"), new Functor("setReplacementRegex"),
            new Functor("setExtractorRegex"), new Functor("setMatchNumber"),
            new Functor("setGroupNumber"),
            new Functor("setTargetField")},
        new Class[]{String.class, String.class, String.class, String.class, String.class, String.class});
  }

  private Component makeLabelPanel() {
    JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    labelPanel.add(tableLabel);
    return labelPanel;
  }

  private JPanel makeButtonPanel() {
    JButton add = createComponent("addButton", new JButton(JMeterUtils.getResString("add")));
    add.setActionCommand(ADD);
    add.setEnabled(true);

    JButton addFromClipboard = createComponent("addFromClipboardButton",
        new JButton(JMeterUtils.getResString("add_from_clipboard")));
    addFromClipboard.setActionCommand(ADD_FROM_CLIPBOARD);
    addFromClipboard.setEnabled(true);

    deleteButton = createComponent("deleteButton", new JButton(JMeterUtils.getResString("delete")));
    deleteButton.setActionCommand(DELETE);

    upButton = createComponent("upButton", new JButton(JMeterUtils.getResString("up")));
    upButton.setActionCommand(UP);

    downButton = createComponent("downButton", new JButton(JMeterUtils.getResString("down")));
    downButton.setActionCommand(DOWN);

    checkButtonsStatus();

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    add.addActionListener(this);
    addFromClipboard.addActionListener(this);
    deleteButton.addActionListener(this);
    upButton.addActionListener(this);
    downButton.addActionListener(this);
    buttonPanel.add(add);
    buttonPanel.add(addFromClipboard);
    buttonPanel.add(deleteButton);
    buttonPanel.add(upButton);
    buttonPanel.add(downButton);
    return buttonPanel;
  }

  private void checkButtonsStatus() {
    deleteButton.setEnabled(tableModel.getRowCount() != 0);
    boolean canMove = tableModel.getRowCount() > 1;
    upButton.setEnabled(canMove);
    downButton.setEnabled(canMove);
  }

  private void setFieldToCheckComboBox() {
    JComboBox<ResultField> comboBox = new JComboBox<>();
    for (ResultField f : ResultField.values()) {
      comboBox.addItem(f);
    }
    TableColumn fieldToCheckColumnn = table.getColumnModel().getColumn(5);
    fieldToCheckColumnn.setCellEditor(new DefaultCellEditor(comboBox));
  }

  private void centerCell(int column) {
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    table.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
  }

  public TestElement createTestElement() {
    GuiUtils.stopTableEditing(table);
    @SuppressWarnings("unchecked")
    Iterator<CorrelationRuleTestElement> modelData = (Iterator<CorrelationRuleTestElement>) tableModel
        .iterator();
    List<CorrelationRuleTestElement> rules = new ArrayList<>();
    while (modelData.hasNext()) {
      CorrelationRuleTestElement rule = modelData.next();
      if (StringUtils.isEmpty(rule.getReferenceName())
          && StringUtils.isEmpty(rule.getReplacementRegex())
          && StringUtils.isEmpty(rule.getExtractorRegex())
          && StringUtils.isEmpty(rule.getMatchNumber())) {
        continue;
      }
      rules.add(rule);
    }
    return new CorrelationRulesTestElement(rules);
  }

  public void configure(TestElement el) {
    if (el instanceof CorrelationRulesTestElement) {
      tableModel.clearData();
      for (JMeterProperty jMeterProperty : (CorrelationRulesTestElement) el) {
        CorrelationRuleTestElement rule = (CorrelationRuleTestElement) jMeterProperty
            .getObjectValue();
        tableModel.addRow(rule);
      }
    }
    checkButtonsStatus();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case DELETE:
        deleteRule();
        break;
      case ADD:
        addRule();
        break;
      case ADD_FROM_CLIPBOARD:
        addFromClipboard();
        break;
      case UP:
        moveUp();
        break;
      case DOWN:
        moveDown();
        break;
      default:
        throw new UnsupportedOperationException(action);
    }
  }

  private void deleteRule() {
    GuiUtils.cancelEditing(table);

    int[] rowsSelected = table.getSelectedRows();
    int anchorSelection = table.getSelectionModel().getAnchorSelectionIndex();
    table.clearSelection();
    if (rowsSelected.length > 0) {
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        tableModel.removeRow(rowsSelected[i]);
      }

      // Table still contains one or more rows, so highlight (select)
      // the appropriate one.
      if (tableModel.getRowCount() > 0) {
        if (anchorSelection >= tableModel.getRowCount()) {
          anchorSelection = tableModel.getRowCount() - 1;
        }
        table.setRowSelectionInterval(anchorSelection, anchorSelection);
      }

      checkButtonsStatus();
    }
  }

  private void addRule() {
    // If a table cell is being edited, we should accept the current value
    // and stop the editing before adding a new row.
    GuiUtils.stopTableEditing(table);

    tableModel.addRow(new CorrelationRuleTestElement());

    checkButtonsStatus();

    // Highlight (select) and scroll to the appropriate row.
    int rowToSelect = tableModel.getRowCount() - 1;
    table.setRowSelectionInterval(rowToSelect, rowToSelect);
    table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
  }

  private void addFromClipboard() {
    GuiUtils.stopTableEditing(table);
    int rowCount = table.getRowCount();
    try {
      String clipboardContent = GuiUtils.getPastedText();
      if (clipboardContent == null) {
        return;
      }
      String[] clipboardLines = clipboardContent.split(CLIPBOARD_LINE_DELIMITERS);
      for (String clipboardLine : clipboardLines) {
        String[] clipboardCols = clipboardLine.split(CLIPBOARD_RULES_DELIMITERS);
        if (clipboardCols.length > 0) {
          CorrelationRuleTestElement rule = createRuleFromClipboard(clipboardCols);
          tableModel.addRow(rule);
        }
      }
      if (table.getRowCount() > rowCount) {
        checkButtonsStatus();

        // Highlight (select) and scroll to the appropriate rows.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowCount, rowToSelect);
        table.scrollRectToVisible(table.getCellRect(rowCount, 0, true));
      }
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this,
          "Could not add rules from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
          JOptionPane.ERROR_MESSAGE);
    } catch (UnsupportedFlavorException ufe) {
      JOptionPane
          .showMessageDialog(this,
              "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                  + " from clipboard" + ufe.getLocalizedMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private CorrelationRuleTestElement createRuleFromClipboard(String[] clipboardCols) {
    CorrelationRuleTestElement rule = new CorrelationRuleTestElement();
    rule.setReferenceName(clipboardCols[0]);
    if (clipboardCols.length > 1) {
      rule.setReplacementRegex(clipboardCols[1]);
      if (clipboardCols.length > 2) {
        rule.setExtractorRegex(clipboardCols[2]);
        if (clipboardCols.length > 3) {
          rule.setMatchNumber(clipboardCols[3]);
          if (clipboardCols.length > 4) {
            rule.setGroupNumber(clipboardCols[4]);
            if (clipboardCols.length > 5) {
              try {
                rule.setTargetField(ResultField.valueOf(clipboardCols[5]));
              } catch (IllegalArgumentException iae) {
                rule.setTargetField(ResultField.BODY);
                LOG.warn(clipboardCols[5]
                    + " is an invalid value for Field to Check, falling back to default value: Body");
              }
            }
          }
        }
      }
    }
    return rule;
  }

  private void moveUp() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
      table.clearSelection();
      for (int rowSelected : rowsSelected) {
        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
      }

      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] - 1);
    }
  }

  private void scrollToRowIfNotVisible(int rowIndx) {
    if (table.getParent() instanceof JViewport) {
      Rectangle visibleRect = table.getVisibleRect();
      final int cellIndex = 0;
      Rectangle cellRect = table.getCellRect(rowIndx, cellIndex, false);
      if (visibleRect.y > cellRect.y) {
        table.scrollRectToVisible(cellRect);
      } else {
        Rectangle rect2 =
            table.getCellRect(rowIndx + getNumberOfVisibleRows(table), cellIndex, true);
        int width = rect2.y - cellRect.y;
        table.scrollRectToVisible(
            new Rectangle(cellRect.x, cellRect.y, cellRect.width, cellRect.height + width));
      }
    }
  }

  private static int getNumberOfVisibleRows(JTable table) {
    Rectangle vr = table.getVisibleRect();
    int first = table.rowAtPoint(vr.getLocation());
    vr.translate(0, vr.height);
    return table.rowAtPoint(vr.getLocation()) - first;
  }

  private void moveDown() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0
        && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
      table.clearSelection();
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        int rowSelected = rowsSelected[i];
        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
      }
      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] + 1);
    }
  }

}
