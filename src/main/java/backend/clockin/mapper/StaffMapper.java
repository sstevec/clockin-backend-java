package backend.clockin.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.*;

public interface StaffMapper {

    void setWorkerMd5(@Param("md5") String md5,@Param("uid") String uid);

    String getMd5ByUid(@Param("uid") String uid);

    void deleteMd5ByUid(@Param("uid") String uid);


    ArrayList<String> getStaffRelevantFormNames();

    void addStaffFormCorrespond(@Param("name") String formName,@Param("sys_name") String sysName,
                                @Param("column_name") String column_name);

    ArrayList<String> getSysNameByFormName(@Param("name") String formName);

    void deleteRecordBySysName(@Param("sys_name") String sysName);

    void changeStaffRelevantFormName(@Param("sys_name") String sysName,@Param("name") String newFormName);

    ArrayList<String> getStaffRelevantAllColumnNames();

    void deleteColumnByColumnName(@Param("column_name") String columnName);

    ArrayList<String> getSysNameByColumnName(@Param("column_name") String columnName);

    ArrayList<String> getNameByColumnName(@Param("column_name") String columnName);

    void changeStaffRelevantColumnName(@Param("old_column") String oldColumn,@Param("new_column") String newColumn);

    ArrayList<String> getAllColumns();

    void deleteStaffRelevantFormRow(@Param("form_name") String sysName,@Param("uid") String uid);

    ArrayList<LinkedHashMap<String,String>> staffSearch(@Param("search_columns") String[] searchColumns,
                                                        @Param("left_list")String[][] leftJoinList,
                                                        @Param("cond")String[][] realCond,
                                                        @Param("searchType")String searchType,
                                                        @Param("separate") String separate,
                                                        @Param("AND")String and,
                                                        @Param("OR")String or,
                                                        @Param("start") int start,
                                                        @Param("num") int num);

    ArrayList<String> getUidListBySysName(@Param("sys_name") String sysName);

    ArrayList<String> getFormNameBySysName(@Param("sys_name") String sysName);

    void softDeleteStaff(@Param("uid") String uid);

    ArrayList<String> getUidByUserName(@Param("name") String chargerName);
}
