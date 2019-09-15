package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public interface GeneralService {

    // form operations
    SysResult createForm(String name, String description);

    SysResult dropForm(String name);

    SysResult updateExistForm(String formName, String info);

    SysResult showExistForm(Integer id);


    // column operations
    SysResult addColumn(String formName, String columnName, String type, String place);

    SysResult delColumn(String formName, String columnName);

    SysResult changeColumn(String formName, String oldColumnName, String newColumnName, String type);

    SysResult getFormColumns(String formName);



    // row operations
    SysResult addRow(String formName, HashMap<String,String> info);

    SysResult deleteRow(String formName, String id);

    SysResult changeRow(String formName, HashMap<String, String> info, String id);

    SysResult getRow(String form_name, int searchType, String input, String separat);

    SysResult getRow2(String targetColumns, String formName, String cond, String searchType, String separate,
                      int pageNum, int pageMax);

    // tools
    ArrayList<Object> quickGet(String columnName, String formName, String identifier, String identifyValue);

    String autoWriteIn(String form_name, List<LinkedHashMap<String,Object>> data);

    ArrayList<String> quickSearch(String columnName, String formName, String[] cond,String searchType);

    SysResult outFile(String filePath, String formName, int start, int pageMax);

    SysResult inFile(String filePath);

    SysResult outFile2(String filePath, String[] targetColumnNames, ArrayList<LinkedHashMap<String, Object>> data);

    SysResult outFile3(String filePath, String[] targetColumnNames, HashMap<String,String> trans,  ArrayList<LinkedHashMap<String, Object>> data);
}
