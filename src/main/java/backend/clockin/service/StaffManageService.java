package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

import java.util.HashMap;

public interface StaffManageService {

    SysResult addStaffRelevantForm(String formName);

    SysResult deleteStaffRelevantForm(String formName);

    SysResult changStaffRelevantForm(String oldFormName, String newFormName);

    SysResult getStaffRelevantFormNames();


    SysResult addStaffRelevantFormColumn(String formName, String columnName, String type, String place);

    SysResult deleteStaffRelevantFormColumn(String columnName);

    SysResult changeStaffRelevantFormColumn(String formName, String oldColumn, String newColumn, String type);

    SysResult getStaffRelevantFormAllColumns();


    SysResult addStaffRelevantFormRow(String formName, HashMap<String,String> info);

    SysResult deleteStaffRelevantFormRow(String formName, String uid);

    SysResult changeStaffRelevantFormRow(String formName, HashMap<String, String> info, String uid);

    SysResult staffRelevantFormSearch(String targetColumn, String conditions, String searchType, String separate,
                                      String pageNum, String pageMax);

    SysResult staffRelevantFormSearch2(String targetColumn, String conditions, String searchType, String separate,
                                       String start, String pageMax);

    SysResult staffRelevantFormInFile(String filePath, String formName);

    SysResult editStaffRelevantForm(HashMap<String,String> info, String uid);

    SysResult softDeleteStaff(String uid);
}
