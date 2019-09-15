package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

import java.util.HashMap;

public interface HolidayService {

    SysResult addHoliday(String startDate, String endDate, String name, String dayOffTarget);

    SysResult deleteHoliday(String date, String name);

    SysResult changeHoliday(String date, String name, HashMap<String, String> info);

    SysResult getHolidayInfoByDate(String startDate, String endDate, String searchInfo, Integer startLimit, Integer num);
}
