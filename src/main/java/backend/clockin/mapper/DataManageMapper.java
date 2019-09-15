package backend.clockin.mapper;

import backend.clockin.pojo.generalFormManage.FormInfo;
import org.apache.ibatis.annotations.Param;
import sun.awt.image.ImageWatched;

import java.util.*;

public interface DataManageMapper {



    // form operation
    void createForm(@Param("name") String name);

    void dropForm(@Param("name") String name);

    void changeFormName(@Param("old_name") String old_name, @Param("new_name") String new_name);

    List<String> showDatabase();

    FormInfo getFormById(Integer id);


    // managementform operation
    ArrayList<FormInfo> showExistForm();

    void updateExistForm(@Param("info") Map<String,String> info, @Param("id") Integer id);

    void addExistForm(FormInfo formInfo);

    void delExistForm(@Param("id") int id);

    String getFormColumns(@Param("name") String name);

    void updateContentModifyTime(@Param("form_name") String form_name);

    // form column edit
    void addColumn(@Param("form_name") String form_name, @Param("column_name") String column_name,
                   @Param("type") String type, @Param("place") String place);

    void changeColumn(@Param("form_name") String form_name, @Param("old_column_name") String old_column_name,
                      @Param("new_column_name") String new_column_name, @Param("type") String type);

    void delColumn(@Param("form_name") String form_name, @Param("column_name") String column_name);



    // form row edit
    void addRow(@Param("form_name") String form_name, @Param("input") HashMap<String, String> input);

    void changeRow(@Param("form_name") String form_name, @Param("input") Map<String, String> input, @Param("identifier") String identifier,
                   @Param("identify_value") String identify_value);

    void delRow(@Param("form_name") String form_name, @Param("identifier") String identifier,
                @Param("identify_value") String identify_value);

    List<LinkedHashMap<String,String>> generalSearch(@Param("form_name") ArrayList<String> form_name,
                                                     @Param("detail_name") String[] detail_name,
                                                     @Param("searchType") int searchType,
                                                     @Param("cond") String[][] cond,
                                                     @Param("separat") String separat,
                                                     @Param("AND") String AND, @Param("OR") String OR);

    // get column name and detail
    LinkedHashMap<String,String> getFirstRow(@Param("form_name") String form_name);

    void addTestColumn(@Param("form_name") String form_name);

    void addTestRow(@Param("form_name") String form_name,@Param("value") String value);

    LinkedHashMap<String,String> getTestRow(@Param("form_name") String form_name, @Param("value") String value);

    void deleteTestRow(@Param("form_name") String form_name,@Param("value") String value);

    void deleteTestColumn(@Param("form_name") String form_name);

    ArrayList<HashMap<String, Object>> getColumnDetails(@Param("form_name") String form_name);

    ArrayList<String> getColumnNamesFromSys(@Param("form_name") String form_name);


    // tool
    ArrayList<Object> quickGet(@Param("column_name") String columnName,@Param("form_name") String formName,
                               @Param("identifier") String identifier,@Param("identify_value") String identifyValue);

    void autoWriteIn(@Param("form_name") String form_name, @Param("column_name") String[] column_name,
                     @Param("array") String[][] new_data);

    ArrayList<String> quickSearch(@Param("column_name") String columnName,
                                  @Param("form_name") String formName,
                                  @Param("cond") String[] cond,
                                  @Param("searchType") String searchType);

    void outFile(@Param("filePath") String filePath,
                 @Param("formName") String formName,
                 @Param("start") int start,
                 @Param("num") int num);

    ArrayList<LinkedHashMap<String, Object>> getRow2(@Param("columnNames") String[] targetColumnNames,
                                                     @Param("formName")String formName,
                                                     @Param("cond") String[][] conditions,
                                                     @Param("searchType") String searchType,
                                                     @Param("separate") String separate,
                                                     @Param("start") int start,
                                                     @Param("num") int pageMax);

    void inFile(@Param("filePath") String filePath,
                @Param("formName") String tempFormName);

    ArrayList<LinkedHashMap<String,Object>> getTempInFile(@Param("formName") String tempFormName);
}
