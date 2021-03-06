package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class DataConvertExcelController extends BaseDataConvertController {

    @FXML
    protected CheckBox withNamesCheck;

    public DataConvertExcelController() {
        baseTitle = AppVariables.message("ExcelConvert");

        SourceFileType = VisitHistory.FileType.Excel;
        SourcePathType = VisitHistory.FileType.Excel;
        AddFileType = VisitHistory.FileType.Excel;
        AddPathType = VisitHistory.FileType.Excel;
        TargetPathType = VisitHistory.FileType.All;
        TargetFileType = VisitHistory.FileType.All;
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Excel);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.All);
        sourceExtensionFilter = CommonFxValues.ExcelExtensionFilter;
        targetExtensionFilter = CommonFxValues.AllExtensionFilter;
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();
            convertController.setControls(this, pdfOptionsController);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "xlsx".equals(suffix) || "xls".equals(suffix);
    }

    public String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue() + "";
            case BOOLEAN:
                return cell.getBooleanCellValue() + "";
        }
        return null;
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        countHandling(srcFile);
        String result;
        try ( Workbook wb = WorkbookFactory.create(srcFile)) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                updateLogs(message("Reading") + " " + message("Sheet") + ":" + sheet.getSheetName());
                convertController.names = new ArrayList<>();
                int startRow = sheet.getFirstRowNum();
                if (withNamesCheck.isSelected()) {
                    Row row = sheet.getRow(startRow);
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        convertController.names.add(cellString(row.getCell(c)));
                    }
                    startRow++;
                } else {
                    Row row = sheet.getRow(startRow);
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        convertController.names.add(message("col") + c);
                    }
                }
                String filePrefix = FileTools.getFilePrefix(srcFile.getName()) + "_" + sheet.getSheetName();
                convertController.openWriters(filePrefix);

                for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    List<String> rowData = new ArrayList<>();
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        rowData.add(cellString(row.getCell(c)));
                    }
                    convertController.writeRow(rowData);
                }
                convertController.closeWriters();
            }
            result = message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

}
