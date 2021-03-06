package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.data.DownloadHistory;
import mara.mybox.data.FileHistory;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.db.table.TableDataset;
import mara.mybox.db.table.TableDownloadHistory;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableFileHistory;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.db.table.TableMatrix;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-11
 * @License Apache License Version 2.0
 */
public class BaseDataTools {

    public static BaseTable getTable(BaseData data) {
        if (data == null) {
            return null;
        }
        if (data instanceof GeographyCode) {
            return new TableGeographyCode();

        } else if (data instanceof Dataset) {
            return new TableDataset();

        } else if (data instanceof DownloadHistory) {
            return new TableDownloadHistory();

        } else if (data instanceof EpidemicReport) {
            return new TableEpidemicReport();

        } else if (data instanceof FileHistory) {
            return new TableFileHistory();

        } else if (data instanceof Location) {
            return new TableLocationData();

        } else if (data instanceof Matrix) {
            return new TableMatrix();

        } else if (data instanceof MatrixCell) {
            return new TableMatrixCell();

        } else if (data instanceof MyBoxLog) {
            return new TableMyBoxLog();

        } else if (data instanceof DataDefinition) {
            return new TableDataDefinition();

        } else if (data instanceof ColumnDefinition) {
            return new TableDataColumn();

        }
        return null;
    }

    public static boolean valid(BaseData data) {
        if (data == null) {
            return false;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.valid((GeographyCode) data);

        } else if (data instanceof Dataset) {
            return Dataset.valid((Dataset) data);

        } else if (data instanceof DownloadHistory) {
            return DownloadHistory.valid((DownloadHistory) data);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.valid((EpidemicReport) data);

        } else if (data instanceof FileHistory) {
            return FileHistory.valid((FileHistory) data);

        } else if (data instanceof Location) {
            return Location.valid((Location) data);

        } else if (data instanceof Matrix) {
            return Matrix.valid((Matrix) data);

        } else if (data instanceof MatrixCell) {
            return MatrixCell.valid((MatrixCell) data);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.valid((MyBoxLog) data);

        } else if (data instanceof DataDefinition) {
            return DataDefinition.valid((DataDefinition) data);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.valid((ColumnDefinition) data);

        }
        return false;
    }

    public static Object getColumnValue(BaseData data, String name) {
        if (data == null || name == null) {
            return null;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.getValue((GeographyCode) data, name);

        } else if (data instanceof Dataset) {
            return Dataset.getValue((Dataset) data, name);

        } else if (data instanceof DownloadHistory) {
            return DownloadHistory.getValue((DownloadHistory) data, name);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.getValue((EpidemicReport) data, name);

        } else if (data instanceof FileHistory) {
            return FileHistory.getValue((FileHistory) data, name);

        } else if (data instanceof Location) {
            return Location.getValue((Location) data, name);

        } else if (data instanceof Matrix) {
            return Matrix.getValue((Matrix) data, name);

        } else if (data instanceof MatrixCell) {
            return MatrixCell.getValue((MatrixCell) data, name);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.getValue((MyBoxLog) data, name);

        } else if (data instanceof DataDefinition) {
            return DataDefinition.getValue((DataDefinition) data, name);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.getValue((ColumnDefinition) data, name);

        }
        return null;
    }

    public static boolean setColumnValue(BaseData data, String name, Object value) {
        if (data == null || name == null) {
            return false;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.setValue((GeographyCode) data, name, value);

        } else if (data instanceof Dataset) {
            return Dataset.setValue((Dataset) data, name, value);

        } else if (data instanceof DownloadHistory) {
            return DownloadHistory.setValue((DownloadHistory) data, name, value);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.setValue((EpidemicReport) data, name, value);

        } else if (data instanceof FileHistory) {
            return FileHistory.setValue((FileHistory) data, name, value);

        } else if (data instanceof Location) {
            return Location.setValue((Location) data, name, value);

        } else if (data instanceof Matrix) {
            return Matrix.setValue((Matrix) data, name, value);

        } else if (data instanceof MatrixCell) {
            return MatrixCell.setValue((MatrixCell) data, name, value);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.setValue((MyBoxLog) data, name, value);

        } else if (data instanceof DataDefinition) {
            return DataDefinition.setValue((DataDefinition) data, name, value);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.setValue((ColumnDefinition) data, name, value);

        }
        return false;
    }

    public static String displayData(BaseTable table, BaseData data, List<String> columns, boolean isHtml) {
        if (data == null) {
            return null;
        }
        try {
            if (table == null) {
                table = getTable(data);
            }
            if (table == null) {
                return null;
            }
            String lineBreak = isHtml ? "<BR>" : "\n";
            String info = null;
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getName())) {
                    continue;
                }
                Object value = getColumnValue(data, column.getName());
                String display = displayColumn(data, column, value);
                if (display == null || display.isBlank()) {
                    continue;
                }
                if (column.getType() == ColumnDefinition.ColumnType.Image
                        && (lineBreak.toLowerCase().equals("<br>") || lineBreak.toLowerCase().equals("</br>"))) {
                    display = "<img src=\"file:///" + display.replaceAll("\\\\", "/") + "\" width=200px>";
                }
                if (info != null) {
                    info += lineBreak;
                } else {
                    info = "";
                }
                info += column.getLabel() + ": " + display;
            }
            return info + displayDataMore(data, lineBreak);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String displayColumn(BaseData data, ColumnDefinition column, Object value) {
        if (data == null || column == null) {
            return null;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.displayColumn((GeographyCode) data, column, value);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.displayColumn((EpidemicReport) data, column, value);

        } else if (data instanceof Location) {
            return Location.displayColumn((Location) data, column, value);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.displayColumn((MyBoxLog) data, column, value);

        }
        return displayColumnBase(data, column, value);
    }

    public static String displayColumnBase(BaseData data, ColumnDefinition column, Object value) {
        if (data == null || column == null || value == null) {
            return null;
        }
        try {
            switch (column.getType()) {
                case String:
                case Text:
                case Color:
                case File:
                case Image:
                    String rvalue = (String) value;
                    return rvalue;
                case Double:
                    double dvalue = (double) value;
                    if (column.getMaxValue() != null && dvalue > (double) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && dvalue < (double) column.getMinValue()) {
                        return null;
                    }
                    return dvalue != CommonValues.InvalidDouble ? dvalue + "" : null;
                case Float:
                    float fvalue = (float) value;
                    if (column.getMaxValue() != null && fvalue > (float) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && fvalue < (float) column.getMinValue()) {
                        return null;
                    }
                    return fvalue != CommonValues.InvalidDouble ? fvalue + "" : null;
                case Long:
                case Era:
                    long lvalue = (long) value;
                    if (column.getMaxValue() != null && lvalue > (long) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && lvalue < (long) column.getMinValue()) {
                        return null;
                    }
                    return lvalue != CommonValues.InvalidLong ? lvalue + "" : null;
                case Integer:
                    int ivalue = (int) value;
                    if (column.getMaxValue() != null && ivalue > (int) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && ivalue < (int) column.getMinValue()) {
                        return null;
                    }
                    return ivalue != CommonValues.InvalidInteger ? ivalue + "" : null;
                case Boolean:
                    boolean bvalue = (boolean) value;
                    return bvalue + "";
                case Short:
                    short svalue = (short) value;
                    if (column.getMaxValue() != null && svalue > (short) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && svalue < (short) column.getMinValue()) {
                        return null;
                    }
                    return svalue != CommonValues.InvalidShort ? svalue + "" : null;
                case Datetime:
                case Date:
                    return DateTools.datetimeToString((Date) value);
            }
        } catch (Exception e) {
            MyBoxLog.error(e, column.getName());
        }
        return null;
    }

    public static String displayDataMore(BaseData data, String lineBreak) {
        if (data == null || lineBreak == null) {
            return "";
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.displayDataMore((GeographyCode) data, lineBreak);

        }
        return "";
    }

    public static String htmlData(BaseTable table, BaseData data) {
        try {
            if (table == null) {
                table = getTable(data);
            }
            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            StringTable htmlTable = new StringTable(names);
            names.addAll(Arrays.asList(message("Name"), message("Value")));
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                Object value = getColumnValue(data, column.getName());
                String html = htmlColumn(data, column, value);
                if (html != null) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(column.getLabel(), html));
                    htmlTable.add(row);
                }
            }
            return StringTable.tableDiv(htmlTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String htmlColumn(BaseData data, ColumnDefinition column, Object value) {
        String display = displayColumn(data, column, value);
        if (display == null) {
            return null;
        }
        return display.replaceAll("\n", "<BR>");
    }

    public static String htmlDataList(BaseTable table, List<BaseData> dataList, List<String> columns) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                return null;
            }
            if (table == null) {
                table = getTable(dataList.get(0));
            }
            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getName())) {
                    continue;
                }
                names.add(column.getLabel());
            }
            StringTable stringTable = new StringTable(names);
            for (BaseData data : dataList) {
                List<String> row = new ArrayList<>();
                for (Object o : table.getColumns()) {
                    ColumnDefinition column = (ColumnDefinition) o;
                    if (columns != null && !columns.contains(column.getName())) {
                        continue;
                    }
                    Object value = getColumnValue(data, column.getName());
                    String display = displayColumn(data, column, value);
                    if (display == null || display.isBlank()) {
                        display = "";
                    }
                    row.add(display);
                }
                stringTable.add(row);
            }
            return StringTable.tableDiv(stringTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
