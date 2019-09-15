package backend.clockin.mapper;

import backend.clockin.pojo.clockIn.ClockIn;
import backend.clockin.pojo.clockIn.ClockInLog;
import backend.clockin.pojo.dayOff.DayOffRecord;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ClockInMapper {

    // clock in form log
    void updateClockInLog();

    void addClockInLog(ClockInLog clockInLog);


    ArrayList<HashMap<String,Object>> getWorkerClockInInfo();

    LinkedHashMap<String,Object> getWorkerBasicInfo(@Param("uid") String uid);



    void addClockInRecord(@Param("form_name") String formName,@Param("clockIn") ClockIn clockIn);

    ClockIn getClockInRecord(@Param("form_name") String formName, @Param("uid") String uid);



    void addDayOffRecord(DayOffRecord dayOffRecord);

    ArrayList<DayOffRecord> getDayOffRecord(@Param("uid") String uid,@Param("day") String day);



    void clockIn(@Param("form_name") String formName,@Param("uid") String uid,@Param("time") String time,@Param("clockInStatus") String status);

    void clockOut(@Param("form_name") String formName,@Param("uid") String uid,@Param("time") String time,@Param("clockOutStatus") String status);

    ArrayList<ClockIn> getNotCalculatedRecord(@Param("form_name") String formName);

    void updateCalculatedRecord(@Param("form_name") String formName,@Param("record") ClockIn tempRecord,@Param("uid") String uid);

    ArrayList<String> getAllClockInForm();



    ArrayList<LinkedHashMap<String, Object>> getPersonClockInRecord(@Param("form_name") String formName,@Param("uid") String uid, @Param("status") String status);

    ArrayList<LinkedHashMap<String, Object>> getTempResultByDate(@Param("form_name") ArrayList<String> formNames,
                                                                 @Param("uid") String uid,
                                                                 @Param("start") String start,@Param("end") String end,
                                                                 @Param("limitStart") Integer limitStart,@Param("pageMax") Integer pageMax);

    ClockIn getClockInRecordByDate(@Param("form_name") String formName,@Param("uid") String uid ,@Param("start") String start,@Param("end") String end);

    ArrayList<LinkedHashMap<String, Object>> getAllClockInRecord(@Param("form_name") String formName);

    void makeUpClockIn(@Param("form_name") String formName,
                       @Param("clockInTime") String startWork,
                       @Param("id") Integer id);

    void makeUpClockOut(@Param("form_name") String formName,
                        @Param("clockOutTime") String endWork,
                        @Param("id") Integer id);

    void reupdateCalculatedRecord(@Param("form_name") String formName,
                                  @Param("clockInInfo") ClockIn clockInInfo,
                                  @Param("id") Integer id);

    ArrayList<LinkedHashMap<String,Object>> getTargetColumnStatistic(@Param("form_name") ArrayList<String> clockInFormNames,
                                                                     @Param("columnNames") String[] targetColumnNames,
                                                                     @Param("cond")String[][] conditions);

    Integer getManagerCountStatistic(@Param("identifier") String identifier,
                                     @Param("identifyValue") String identifyValue);

    HashMap<String, String> getNoonBreakInfo(@Param("breakId") String noon_break_id);

}
