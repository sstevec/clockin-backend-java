package backend.clockin.service;

import backend.clockin.mapper.HolidayMapper;
import backend.clockin.pojo.dayOff.Holiday;
import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class HolidayServiceImp implements HolidayService {

    @Autowired
    HolidayMapper holidayMapper;

    @Override
    public SysResult addHoliday(String startDate, String endDate, String name, String dayOffTarget) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start, end, nextDay;

        // 防止日期格式不同
        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            return SysResult.build(201, "写入请假记录出错    " + e);
        }
        nextDay = start;
        while (!nextDay.equals(end)) {
            Holiday holiday = new Holiday();
            holiday.setDate(nextDay);
            holiday.setName(name);
            holiday.setDayOffTypes(dayOffTarget);
            holidayMapper.addHoliday(holiday);
            nextDay = new Date(nextDay.getTime() + 1000 * 60 * 60 * 24);
        }
        Holiday holiday = new Holiday();
        holiday.setDate(end);
        holiday.setName(name);
        holiday.setDayOffTypes(dayOffTarget);
        holidayMapper.addHoliday(holiday);
        return SysResult.build(200, "假期添加成功");
    }

    @Override
    public SysResult deleteHoliday(String date, String name) {
        holidayMapper.deleteHoliday(date, name);
        return SysResult.build(200, "删除成功");
    }

    @Override
    public SysResult changeHoliday(String date, String name, HashMap<String, String> info) {
        if (info.size() == 0) {
            return SysResult.build(200, "没有修改项");
        }
        holidayMapper.changeHoliday(date, name, info);
        return SysResult.build(200, "修改成功");
    }

    @Override
    public SysResult getHolidayInfoByDate(String startDate, String endDate, String searchInfo, Integer startLimit, Integer num) {
        if (searchInfo == null || searchInfo.equals("")) {
            return SysResult.build(200,"搜索成功", holidayMapper.getHolidayByDate(startDate, endDate, startLimit, num));
        } else {
            searchInfo = "\'%" + searchInfo + "%\'";
            return SysResult.build(200,"搜索成功", holidayMapper.getHolidayByCond(startDate, endDate, searchInfo, startLimit, num));
        }

    }
}
