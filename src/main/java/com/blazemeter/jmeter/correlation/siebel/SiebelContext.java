package com.blazemeter.jmeter.correlation.siebel;

import com.blazemeter.jmeter.correlation.core.CorrelationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class SiebelContext implements CorrelationContext {

  private final Map<String, BCI> bcis = new HashMap<>();
  private final Map<String, Field> paramRowFields = new HashMap<>();

  private int prefixId = 0;
  private final Map<String, String> rowVars = new HashMap<>();
  private Integer counter;

  private static class BCI {

    private Map<Integer, Field> fields = new HashMap<>();

    private void addField(Field field) {
      field.position = fields.size();
      fields.put(field.id, field);
    }

    private Field getField(int id) {
      return fields.get(id);
    }
  }

  public static class Field {

    private final static int TELEPHONE_TYPE = 155;

    private final int id;
    private final int type;
    private int position;

    private Field(Map<String, String> attrs) {
      id = Integer.valueOf(attrs.get("n"));
      type = Integer.valueOf(attrs.get("t"));
    }

    public int getPosition() {
      return position;
    }

    public String getIgnoredCharsRegex() {
      if (type == TELEPHONE_TYPE) {
        return "[\\()\\- ]";
      } else {
        return "";
      }
    }

  }

  public Map<String, Field> getParamRowFields() {
    return this.paramRowFields;
  }

  public void update(String page) {
    if (page.startsWith("@0")) {
      String delimiter = Pattern.quote(page.substring(2,3));
      String[] parts = page.split(delimiter);
      parsePage("", parts, 2, null);
    }
  }

  @Override
  public void reset() {
    bcis.clear();
    paramRowFields.clear();
    prefixId = 0;
    counter = null;
  }

  private int parsePage(String parentPath, String[] parts, int index, BCI bci) {
    int attrCount = Integer.valueOf(parts[index++]);
    int childCount = Integer.valueOf(parts[index++]);
    String path = parentPath + "/" + parts[index++];
    int type = Integer.valueOf(parts[index++]);
    if (type == 3) {
      String typeId = parts[index++];
      bci = new BCI();
      this.bcis.put(typeId, bci);
    }
    Map<String, String> attrs = new HashMap<>();
    for (int i = 0; i < attrCount; i++) {
      String key = parts[index++];
      String val = parts[index++];
      attrs.put(key, val);
    }
    String bciAttr = Optional.ofNullable(attrs.get("bci")).orElse(attrs.get("bc"));
    bci = Optional.ofNullable(bciAttr).map(this.bcis::get).orElse(bci);

    if (path.endsWith("/bci/fl/f")) {
      bci.addField(new Field(attrs));
    } else if (path.endsWith("/cl/c") || path.endsWith("/col/co")) {
      String fn = attrs.get("fn");
      String sp = attrs.get("sp");
      if (fn != null && sp != null) {
        Field field = bci.getField(Integer.valueOf(fn));
        if (field != null) {
          paramRowFields.put(sp, field);
        }
      }
    }
    for (int i = 0; i < childCount; i++) {
      index = parsePage(path, parts, index, bci);
    }
    return index;
  }

  public int getNextRowPrefixId() {
    return prefixId++;
  }

  public void addRowVar(String rowId, String varNamePrefix) {
    rowVars.put(rowId, varNamePrefix);
  }

  public Map<String, String> getRowVars() {
    return rowVars;
  }

  public Integer getCounter() {
    return counter;
  }

  public void setCounter(Integer counter) {
    this.counter = counter;
  }

}
