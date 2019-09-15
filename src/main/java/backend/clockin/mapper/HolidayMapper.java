package backend.clockin.mapper;

import backend.clockin.pojo.dayOff.DayOffOptions;
import backend.clockin.pojo.dayOff.Holiday;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface HolidayMapper {
    void addHoliday(@Param("Holiday") Holiday holiday);

    void deleteHoliday(@Param("date") String date,@Param("name") String name);

    void changeHoliday(@Param("date") String date,@Param("name") String name,@Param("info") HashMap<String, String> info);

    ArrayList<Holiday> getHolidayByDate(@Param("start") String startDate,@Param("end") String endDate, @Param("startLimit") Integer startLimit,@Param("num") Integer num);

    ArrayList<Holiday> getHolidayByCond(@Param("start") String startDate,@Param("end") String endDate,@Param("info") String searchInfo,@Param("startLimit") Integer startLimit,@Param("num") Integer num);

    Integer countHoliday(@Param("start") String startDate, @Param("end") String endDate, @Param("searchInfo") String searchInfo);

    Integer countAllHoliday(@Param("start") String startDate, @Param("end") String endDate);

    ArrayList<DayOffOptions> getDayOffOptions();

    void deleteDayOffType(@Param("dayOffType") String dayOffType);

    void addDayOffType(@Param("dayOffType") String dayOffType,@Param("description") String description);

    void editDayOffType(@Param("dayOffType") String dayOffType,@Param("description") String description,
                        @Param("id") String id);
}
