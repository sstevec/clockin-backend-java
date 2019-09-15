package backend.clockin.service;


import backend.clockin.mapper.DataManageMapper;
import backend.clockin.pojo.generalFormManage.FormInfo;
import backend.clockin.pojo.generalFormManage.ManageForm;
import backend.clockin.pojo.tool.SysResult;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


@Service
public class GereralServiceImp implements GeneralService {

    @Autowired
    DataManageMapper dataManageMapper;
    private ManageForm manageForm = new ManageForm();
    private String[] formManagementColumns = manageForm.getFormManagementColumns();


    // form management
    @Override
    public SysResult createForm(String name, String description) {
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            if (name.equals(temp.getName())) {
                return SysResult.build(400, "Fail, duplicated name");
            }
        }
        FormInfo newForm = new FormInfo();
        newForm.setName(name);
        newForm.setDescription(description);
        newForm.setColumns("column1");
        newForm.setColumnTypeOne("varchar");
        newForm.setColumnTypeTwo("255");
        newForm.setColumnTypeThree("YES");
        dataManageMapper.addExistForm(newForm);
        dataManageMapper.createForm(name);
        return SysResult.build(200, "Create form success");
    }

    @Override
    public SysResult dropForm(String name) {
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            if (name.equals(temp.getName())) {
                dataManageMapper.delExistForm(temp.getId());
                dataManageMapper.dropForm(name);
                return SysResult.build(200, "Delete form success");
            }
        }

        return SysResult.build(400, "Fail, form does not exist");
    }

    @Override
    public SysResult showExistForm(Integer id) {
        if (id == 0) {
            // 返回全部表格
            return SysResult.build(200, "this is result", dataManageMapper.showExistForm());
        } else {
            ArrayList<FormInfo> tempFormList = new ArrayList<>();
            FormInfo tempForm = dataManageMapper.getFormById(id);
            if (tempForm == null) {
                return SysResult.build(201, "No form found");
            }
            tempFormList.add(tempForm);
            return SysResult.build(200, "this is result", tempFormList);
        }
    }

    @Override
    public SysResult updateExistForm(String formName, String info) {
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            if (formName.equals(temp.getName())) {
                int id = temp.getId();
                String[] values = info.split(",");
                HashMap<String, String> updateTemp = new HashMap<>();
                updateTemp.put("name", values[0]);
                updateTemp.put("description", values[1]);
                dataManageMapper.updateExistForm(updateTemp, id);
                dataManageMapper.changeFormName(formName, values[0]);
                return SysResult.build(200, "Update form success");
            }
        }
        return SysResult.build(201, "Form does not exist");
    }


    // Column management

    @Override

    // add column after place
    public SysResult addColumn(String formName, String columnName, String type, String place) {
        // check if target form exist
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            // find target form
            if (formName.equals(temp.getName())) {

                int id = temp.getId();
                String[] types = type.split(",");
                boolean isAdd = false;
                String newColumnName;
                String newTypeOne;
                String newTypeTwo;
                String newTypeThree;
                String[] columns;
                String[] typeOne;
                String[] typeTwo;
                String[] typeThree;
                columns = temp.getColumns().split(",");
                typeOne = temp.getColumnTypeOne().split(",");
                typeTwo = temp.getColumnTypeTwo().split(",");
                typeThree = temp.getColumnTypeThree().split(",");

                if (columns[0].contentEquals(place)) {
                    newColumnName = columns[0];
                    newTypeOne = typeOne[0];
                    newTypeTwo = typeTwo[0];
                    newTypeThree = typeThree[0];
                    newColumnName = newColumnName + "," + columnName;
                    newTypeOne = newTypeOne + "," + types[0];
                    newTypeTwo = newTypeTwo + "," + types[1];
                    newTypeThree = newTypeThree + "," + types[2];
                    isAdd = true;
                } else {
                    newColumnName = columns[0];
                    newTypeOne = typeOne[0];
                    newTypeTwo = typeTwo[0];
                    newTypeThree = typeThree[0];
                }

                int length = columns.length;
                for (int i = 1; i < length; i++) {
                    if (columns[i].contentEquals(place)) {
                        newColumnName = newColumnName + "," + columns[i];
                        newTypeOne = newTypeOne + "," + typeOne[i];
                        newTypeTwo = newTypeTwo + "," + typeTwo[i];
                        newTypeThree = newTypeThree + "," + typeThree[i];
                        newColumnName = newColumnName + "," + columnName;
                        newTypeOne = newTypeOne + "," + types[0];
                        newTypeTwo = newTypeTwo + "," + types[1];
                        newTypeThree = newTypeThree + "," + types[2];
                        isAdd = true;
                    } else {
                        newColumnName = newColumnName + "," + columns[i];
                        newTypeOne = newTypeOne + "," + typeOne[i];
                        newTypeTwo = newTypeTwo + "," + typeTwo[i];
                        newTypeThree = newTypeThree + "," + typeThree[i];
                    }

                }
                if (isAdd) {
                    HashMap<String, String> updateTemp = new HashMap<>();
                    updateTemp.put(formManagementColumns[3], newColumnName);
                    updateTemp.put(formManagementColumns[4], newTypeOne);
                    updateTemp.put(formManagementColumns[5], newTypeTwo);
                    updateTemp.put(formManagementColumns[6], newTypeThree);
                    dataManageMapper.updateExistForm(updateTemp, id);

                    String typeString = types[0] + " (" + types[1] + ") ";
                    if (types[2].equals("NO")) {
                        typeString = typeString + "not null ";
                    }
                    if (types.length > 3) {
                        typeString = typeString + types[3];
                    }
                    dataManageMapper.addColumn(formName, columnName, typeString, place);
                }
                return SysResult.build(200, "Column add success");
            }
        }
        return SysResult.build(201, "Form does not exist");
    }


    @Override
    public SysResult delColumn(String formName, String oldColumnName) {
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            if (formName.equals(temp.getName())) {

                int id = temp.getId();
                String newColumnName;
                String newTypeOne;
                String newTypeTwo;
                String newTypeThree;
                String[] columns;
                String[] typeOne;
                String[] typeTwo;
                String[] typeThree;

                if (temp.getColumns().contains(",")) {
                    columns = temp.getColumns().split(",");
                    typeOne = temp.getColumnTypeOne().split(",");
                    typeTwo = temp.getColumnTypeTwo().split(",");
                    typeThree = temp.getColumnTypeThree().split(",");

                    if (columns[0].contentEquals(oldColumnName)) {
                        newColumnName = columns[1];
                        newTypeOne = typeOne[1];
                        newTypeTwo = typeTwo[1];
                        newTypeThree = typeThree[1];
                    } else {
                        newColumnName = columns[0];
                        newTypeOne = typeOne[0];
                        newTypeTwo = typeTwo[0];
                        newTypeThree = typeThree[0];
                    }

                    int length = columns.length;
                    for (int i = 1; i < length; i++) {
                        if (columns[1].contentEquals(newColumnName)) {
                            continue;
                        } else {
                            if (columns[i].contentEquals(oldColumnName)) {
                                continue;
                            }
                            newColumnName = newColumnName + "," + columns[i];
                            newTypeOne = newTypeOne + "," + typeOne[i];
                            newTypeTwo = newTypeTwo + "," + typeTwo[i];
                            newTypeThree = newTypeThree + "," + typeThree[i];
                        }
                    }
                } else {
                    return SysResult.build(201, "Need at least onr column");
                }

                HashMap<String, String> updateTemp = new HashMap<>();
                updateTemp.put(formManagementColumns[3], newColumnName);
                updateTemp.put(formManagementColumns[4], newTypeOne);
                updateTemp.put(formManagementColumns[5], newTypeTwo);
                updateTemp.put(formManagementColumns[6], newTypeThree);
                dataManageMapper.updateExistForm(updateTemp, id);
                dataManageMapper.delColumn(formName, oldColumnName);
                return SysResult.build(200, "Delete success");
            }
        }
        return SysResult.build(201, "Form does not exist");
    }


    @Override
    public SysResult changeColumn(String formName, String oldColumn, String newColumn, String type) {
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            if (formName.equals(temp.getName())) {

                int id = temp.getId();
                String newColumnName;
                String newTypeOne;
                String newTypeTwo;
                String newTypeThree;
                String[] columns;
                String[] typeOne;
                String[] typeTwo;
                String[] typeThree;
                int found = 0;
                String[] types = type.split(",");

                if (temp.getColumns().contains(",")) {
                    columns = temp.getColumns().split(",");
                    typeOne = temp.getColumnTypeOne().split(",");
                    typeTwo = temp.getColumnTypeTwo().split(",");
                    typeThree = temp.getColumnTypeThree().split(",");

                    if (columns[0].contentEquals(oldColumn)) {
                        newColumnName = newColumn;
                        newTypeOne = types[0];
                        newTypeTwo = types[1];
                        newTypeThree = types[2];
                        found = 1;
                    } else {
                        newColumnName = columns[0];
                        newTypeOne = typeOne[0];
                        newTypeTwo = typeTwo[0];
                        newTypeThree = typeThree[0];
                    }
                    int length = columns.length;
                    for (int i = 1; i < length; i++) {
                        if (columns[i].contentEquals(oldColumn)) {
                            newColumnName = newColumnName + "," + newColumn;
                            newTypeOne = newTypeOne + "," + types[0];
                            newTypeTwo = newTypeTwo + "," + types[1];
                            newTypeThree = newTypeThree + "," + types[2];
                            found = 1;
                        } else {
                            newColumnName = newColumnName + "," + columns[i];
                            newTypeOne = newTypeOne + "," + typeOne[i];
                            newTypeTwo = newTypeTwo + "," + typeTwo[i];
                            newTypeThree = newTypeThree + "," + typeThree[i];
                        }
                    }
                } else {
                    if (temp.getColumns().contentEquals(oldColumn)) {
                        newColumnName = newColumn;
                        newTypeOne = types[0];
                        newTypeTwo = types[1];
                        newTypeThree = types[2];
                        found = 1;
                    } else {
                        return SysResult.build(201, "Column not found");
                    }
                }

                if (found == 1) {
                    HashMap<String, String> updateTemp = new HashMap<>();
                    updateTemp.put(formManagementColumns[3], newColumnName);
                    updateTemp.put(formManagementColumns[4], newTypeOne);
                    updateTemp.put(formManagementColumns[5], newTypeTwo);
                    updateTemp.put(formManagementColumns[6], newTypeThree);
                    dataManageMapper.updateExistForm(updateTemp, id);

                    String typeString = types[0] + " (" + types[1] + ") ";
                    if (types[2].equals("NO")) {
                        typeString = typeString + "not null ";
                    }
                    if (types.length > 3) {
                        typeString = typeString + types[3];
                    }
                    dataManageMapper.updateExistForm(updateTemp, id);
                    dataManageMapper.changeColumn(formName, oldColumn, newColumn, typeString);
                    return SysResult.build(200, "Change column success");
                } else {
                    return SysResult.build(201, "Column not found");
                }
            }
        }
        return SysResult.build(201, "Form does not exist");

    }


    @Override
    public SysResult getFormColumns(String formName) {
        String tempColumn = dataManageMapper.getFormColumns(formName);
        if (tempColumn == null) {
            return SysResult.build(201, "Form does not exist");
        }
        return SysResult.build(200, "this is result", tempColumn);
    }

    @Override
    public SysResult addRow(String formName, HashMap<String, String> info) {
        if (info.size() == 0) {
            return SysResult.build(201, "输入为空");
        }
        try {
            dataManageMapper.addRow(formName, info);
            dataManageMapper.updateContentModifyTime(formName);
            return SysResult.build(200, "Add row success");
        } catch (Exception e) {
            return SysResult.build(201, "Fail to add row with exception " + e);
        }
    }

    @Override
    public SysResult deleteRow(String formName, String id) {
        try {
            dataManageMapper.delRow(formName, "id", id);
            dataManageMapper.updateContentModifyTime(formName);
            return SysResult.build(200, "Delete row success");
        } catch (Exception e) {
            return SysResult.build(201, "Fail to delete row with exception " + e);
        }
    }

    @Override
    public SysResult changeRow(String formName, HashMap<String, String> info, String id) {
        if (info.size() == 0) {
            return SysResult.build(201, "输入为空");
        }
        try {
            dataManageMapper.changeRow(formName, info, "id", id);
            dataManageMapper.updateContentModifyTime(formName);
            return SysResult.build(200, "Change row success");
        } catch (Exception e) {
            return SysResult.build(201, "Fail to change row with exception " + e);
        }
    }

    @Override
    public SysResult getRow(String form_name, int searchType, String input, String separat) {
        // form names
        String[] form_list = form_name.split(",");
        ArrayList<String> forms = new ArrayList<String>();
        ArrayList<String> allColumnName = new ArrayList<>();

        int numberForm = form_list.length;
        for (int i = 0; i < numberForm; i++) {
            form_list[i] = form_list[i].trim();
            forms.add(form_list[i]);
        }

        //detail names
        int numberColumns = 0;
        for (int i = 0; i < numberForm; i++) {
            String columns = getFormColumns(form_list[i]).getData().toString();
            String[] colums_list = columns.split(",");
            numberColumns = numberColumns + colums_list.length;
        }
        String[] detail_name = new String[numberColumns * 2];

        int counter = 0;
        for (int i = 0; i < numberForm; i++) {
            String columns = getFormColumns(form_list[i]).getData().toString();
            String[] colums_list = columns.split(",");
            int temp_form_length = colums_list.length;
            for (int j = 0; j < temp_form_length; j++) {
                colums_list[j] = colums_list[j].replace("]", "");
                colums_list[j] = colums_list[j].replace("[", "");
                //register all the columns
                allColumnName.add(colums_list[j]);
                detail_name[counter] = form_list[i] + "." + colums_list[j];
                counter++;
                detail_name[counter] = form_list[i] + "_" + colums_list[j];
                counter++;
                if (counter != numberColumns * 2)
                    detail_name[counter - 1] = detail_name[counter - 1] + ",";
            }
        }

        //conditions
        String[] split_input = input.split(",");
        int length = split_input.length;
        String[][] cond = new String[length / 2][2];
        for (int i = 0; i < length; i = i + 2) {
            String[] checkCompoName = split_input[i].split("\\.");
            // signal form check, no compo name
            if (checkCompoName.length == 1) {
                if (allColumnName.contains(split_input[i].substring(0, split_input[i].length() - 1)) ||
                        allColumnName.contains(split_input[i].substring(0, split_input[i].length() - 2))) {
                    cond[i / 2][0] = split_input[i];
                } else {
                    return SysResult.build(201, "Condition column is not in the form columns");
                }
                // means this is a compo name, column name is at second position
            } else if (checkCompoName.length == 2) {

                // check form is in the form name list
                if (forms.contains(checkCompoName[0])) {
                    // form is in the selected form list, then check col is in the form col
                    String[] tempFormCol = getFormColumns(checkCompoName[0]).getData().toString().split(",");
                    ArrayList<String> tempFormColList = new ArrayList<>(Arrays.asList(tempFormCol));

                    if (tempFormColList.contains(checkCompoName[1].substring(0, checkCompoName[1].length() - 1)) ||
                            tempFormColList.contains(checkCompoName[1].substring(0, checkCompoName[1].length() - 2))) {
                        cond[i / 2][0] = split_input[i];
                    } else {
                        return SysResult.build(201, "Compo check, Condition column is not in the form columns");
                    }
                } else {
                    return SysResult.build(201, "Form: " + checkCompoName[0] + " is not in the selection form list");
                }
            } else {
                return SysResult.build(201, "too many conditions details");
            }
            if (searchType == 2) {
                String tempCond = split_input[i + 1].trim();
                cond[i / 2][1] = "\"" + tempCond + "\"";
            } else {
                cond[i / 2][1] = split_input[i + 1];
            }
        }

        if (searchType > 2 || searchType < 0) {
            return SysResult.build(201, "error in search type");
        }
        return SysResult.build(200, "This is result"
                , dataManageMapper.generalSearch(forms, detail_name, searchType, cond, separat, "AND", "OR"));
    }


    @Override
    public ArrayList<Object> quickGet(String columnName, String formName, String identifier, String identifyValue) {
        return dataManageMapper.quickGet(columnName, formName, identifier, identifyValue);
    }

    @Override
    public String autoWriteIn(String form_name, List<LinkedHashMap<String, Object>> data) {
        int row_number = data.size();

        if (row_number == 0) {
            return "empty data";
        }

        boolean formExist = false;

        // check form exist or not
        ArrayList<FormInfo> formList = dataManageMapper.showExistForm();
        for (FormInfo temp : formList) {
            if (form_name.equals(temp.getName())) {
                // means form exist
                formExist = true;
                break;
            }
        }

        int numberColumn = data.get(0).size();
        String[] column_name = new String[numberColumn];
        Set<String> col_name_set = data.get(0).keySet();
        col_name_set.toArray(column_name);

        // form does not exist
        if (!formExist) {
            // create form
            createForm(form_name, "this is search result");
            System.out.println("Form does not exist, creating new form");
            for (int i = 0; i < numberColumn; i++) {
                if (i == 0) {
                    changeColumn(form_name, "column1", "id", "int,10,YES,unsigned auto_increment primary key");
                    addColumn(form_name, column_name[i], "varchar,250,NO", "id");
                } else {
                    addColumn(form_name, column_name[i], "varchar,250,NO", column_name[i - 1]);
                }
            }
        }

        // record value

        // small size insert
        if (row_number <= 2000) {
            String[][] new_data = new String[row_number][numberColumn];

            for (int i = 0; i < row_number; i++) {
                Collection<Object> row_values = data.get(i).values();
                Object[] row_value_string = row_values.toArray();

                for (int j = 0; j < numberColumn; j++) {
                    if (row_value_string[j] == null) {
                        row_value_string[j] = "";
                    }
                    new_data[i][j] = row_value_string[j].toString();
                }
            }
            dataManageMapper.autoWriteIn(form_name, column_name, new_data);
        }
        // large size insert, row > 2000
        else {
            Long currentInsertAmount = 0L;
            Long targetInsertAmount = 2000L;
            Long increaseAmount = 2000L;
            while (targetInsertAmount < row_number + 1) {
                int increaseRowNumber = targetInsertAmount.intValue() - currentInsertAmount.intValue();
                String[][] new_data = new String[increaseRowNumber][numberColumn];

                for (int i = 0; i < increaseRowNumber; i++) {

                    Collection<Object> row_values = data.get(i).values();
                    Object[] row_value_string = row_values.toArray();

                    for (int j = 0; j < numberColumn; j++) {
                        if (row_value_string[j] == null) {
                            row_value_string[j] = "";
                        }
                        new_data[i][j] = row_value_string[j].toString();
                    }
                }
                dataManageMapper.autoWriteIn(form_name, column_name, new_data);
                System.out.println("Write in " + increaseRowNumber + " lines");
                currentInsertAmount = currentInsertAmount + increaseAmount;
                if (targetInsertAmount + increaseAmount > row_number && targetInsertAmount != row_number) {
                    targetInsertAmount = (long) row_number;
                } else {
                    targetInsertAmount = targetInsertAmount + increaseAmount;
                }
            }
        }
        return "write in success";
    }


    @Override
    public ArrayList<String> quickSearch(String columnName, String formName, String[] cond, String searchType) {
        return dataManageMapper.quickSearch(columnName, formName, cond, searchType);
    }


    @Override
    public SysResult getRow2(String targetColumns, String formName, String cond, String searchType, String separate,
                             int pageNum, int pageMax) {
        // 获取目标字段名
        String[] targetColumnNames = targetColumns.split(",");

        if(!targetColumnNames[0].equals("count(0)")) {
            // 验证目标字段在目标表内
            ArrayList<String> formColumns;
            try {
                List<String> temp = Arrays.asList(dataManageMapper.getFormColumns(formName).split(","));
                formColumns = new ArrayList<>(temp);
                if (formColumns.size() == 0) {
                    return SysResult.build(201, "系统内部错误，表格字段获取失败");
                }
            } catch (Exception e) {
                return SysResult.build(201, "表格名称错误或表格不存在  " + e);
            }
            int targetColumnNum = targetColumnNames.length;
            for (int i = 0; i < targetColumnNum; i++) {
                if (!formColumns.contains(targetColumnNames[i])) {
                    return SysResult.build(400, "目标字段不在目标表格内");
                }
            }
        }
        // cond 是3项为一个单位的
        String[] allConds = cond.split(",");
        int allCondsLength = allConds.length;
        String[][] conditions = new String[allCondsLength / 3][2];
        try {
            for (int i = 0; i < allCondsLength; i = i + 3) {
                if (searchType.equals("2")) {
                    conditions[i / 3][0] = allConds[i] + allConds[i + 1];
                    conditions[i / 3][1] = "\'%" + allConds[i + 2] + "%\'";
                } else {
                    conditions[i / 3][0] = allConds[i] + allConds[i + 1];
                    conditions[i / 3][1] = allConds[i + 2];
                }
            }
        } catch (Exception e) {
            return SysResult.build(201, "条件解析出错   " + e);
        }

        // 分页处理
        int start = (pageNum - 1) * pageMax;

        try {
            return SysResult.build(200, "搜索成功",
                    dataManageMapper.getRow2(targetColumnNames, formName, conditions, searchType, separate, start, pageMax));
        } catch (Exception e) {
            return SysResult.build(201, "获取搜索结果出错    " + e);
        }
    }

    @Override
    public SysResult outFile(String filePath, String formName, int start, int pageMax) {
        try {
            dataManageMapper.outFile(filePath, formName, start, pageMax);
        } catch (Exception e) {
            return SysResult.build(201, "导出失败    " + e);
        }
        return SysResult.build(200, "文件导出成功");
    }

    // 返回字段名
    @Override
    public SysResult inFile(String filePath) {
        File xlsFile = new File(filePath);
        /**
         * 这里根据不同的excel类型
         * 可以选取不同的处理类：
         *          1.XSSFWorkbook xlsx
         *          2.HSSFWorkbook xls
         */
        // 获得工作簿
        HSSFWorkbook workbook;
        try {
            workbook = new HSSFWorkbook(new FileInputStream(xlsFile));
        } catch (IOException e) {
            return SysResult.build(201, "读取Excel失败, 文件类型错误");
        }

        // 获得工作表
        HSSFSheet sheet = workbook.getSheetAt(0);

        HSSFRow row = sheet.getRow(0);
        int columnNum = row.getLastCellNum();
        ArrayList<String> columnNames = new ArrayList<>();
        for (int i = 0; i < columnNum; i++) {
            columnNames.add(row.getCell(i).toString());
        }

        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        int rowSize = sheet.getPhysicalNumberOfRows();
        for (int i = 1; i < rowSize; i++) {
            HashMap<String, Object> tempRowData = new HashMap<>();
            HSSFRow tempRow = sheet.getRow(i);
            for (int j = 0; j < columnNum; j++) {
                Cell cell = tempRow.getCell(j);
                CellType type = cell.getCellType();
                Object value;
//                System.out.println(type);
                if (type.equals(CellType.NUMERIC)) {
                    // 处理日期格式
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getDateCellValue();
                    } else {
                        value = NumberToTextConverter.toText(cell.getNumericCellValue());
                    }
                } else if (type.equals(CellType.BLANK)) {
                    value = "";
                } else if (type.equals(CellType.ERROR)) {
                    value = "";
                } else if (type.equals(CellType.STRING)) {
                    value = cell.getRichStringCellValue().getString();
                } else {
                    value = cell;
                }
                tempRowData.put(columnNames.get(j), value);
            }
            data.add(tempRowData);
        }

        return SysResult.build(200, "success", data);
    }

    @Override
    public SysResult outFile2(String filePath, String[] targetColumnNames, ArrayList<LinkedHashMap<String, Object>> data){
        //创建工作薄对象
        XSSFWorkbook workbook=new XSSFWorkbook();//这里也可以设置sheet的Name
        //创建工作表对象
        XSSFSheet sheet = workbook.createSheet();
        //创建工作表的行
        XSSFRow row = sheet.createRow(0);//设置第一行，从零开始
        int numCol = targetColumnNames.length;

        // 添加第一行字段名
        for(int i = 0; i<numCol; i++){
            row.createCell(i).setCellValue(targetColumnNames[i]);
        }

        int size = data.size();
        for(int i = 1; i<size+1; i++){
            XSSFRow tempRow = sheet.createRow(i);//设置行
            for(int j = 0; j<numCol; j++){
                String content;
                if(data.get(i-1).get(targetColumnNames[j]) == null){
                    content = "";
                }else{
                    content = data.get(i-1).get(targetColumnNames[j]).toString();
                }
                tempRow.createCell(j).setCellValue(content);
            }
        }

        workbook.setSheetName(0,"sheet1");//设置sheet的Name

        //文档输出
        FileOutputStream out ;
        try {
            out = new FileOutputStream(filePath);
            workbook.write(out);
            out.close();
            return SysResult.build(200,"导出成功",filePath);
        } catch (Exception e) {
            return SysResult.build(201,"导出失败");
        }
    }

    @Override
    public SysResult outFile3(String filePath, String[] targetColumnNames, HashMap<String,String> trans,  ArrayList<LinkedHashMap<String, Object>> data){
        //创建工作薄对象
        XSSFWorkbook workbook=new XSSFWorkbook();//这里也可以设置sheet的Name
        //创建工作表对象
        XSSFSheet sheet = workbook.createSheet();
        //创建工作表的行
        XSSFRow row = sheet.createRow(0);//设置第一行，从零开始
        int numCol = targetColumnNames.length;

        // 添加第一行字段名
        for(int i = 0; i<numCol; i++){
            row.createCell(i).setCellValue(trans.get(targetColumnNames[i]));
        }

        int size = data.size();
        for(int i = 1; i<size+1; i++){
            XSSFRow tempRow = sheet.createRow(i);//设置行
            for(int j = 0; j<numCol; j++){
                String content;
                if(data.get(i-1).get(targetColumnNames[j]) == null){
                    content = "";
                }else{
                    content = data.get(i-1).get(targetColumnNames[j]).toString();
                }
                tempRow.createCell(j).setCellValue(content);
            }
        }

        workbook.setSheetName(0,"sheet1");//设置sheet的Name

        //文档输出
        FileOutputStream out ;
        try {
            out = new FileOutputStream(filePath);
            workbook.write(out);
            out.close();
            return SysResult.build(200,"导出成功",filePath);
        } catch (Exception e) {
            return SysResult.build(201,"导出失败");
        }
    }
}
