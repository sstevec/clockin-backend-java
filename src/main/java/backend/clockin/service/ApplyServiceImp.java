package backend.clockin.service;

import backend.clockin.mapper.ApplicationMapper;
import backend.clockin.mapper.ClockInMapper;
import backend.clockin.pojo.applyRecord.ApplicationRecord;
import backend.clockin.pojo.applyRecord.ApplicationReviewList;
import backend.clockin.pojo.clockIn.ClockIn;
import backend.clockin.pojo.dayOff.BoundedDayOffRecord;
import backend.clockin.pojo.dayOff.DayOffRecord;
import backend.clockin.pojo.dayOff.MergedDayOffRecord;
import backend.clockin.pojo.extraWork.ExtraWorkRecord;
import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ApplyServiceImp implements ApplyService {

    @Autowired
    ClockInMapper clockInMapper;

    @Autowired
    GeneralService generalService;

    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    ClockInService clockInService;

    @Autowired
    ToolService toolService;

    @Override
    public SysResult addDayOffRecord(String offStartDate, String offEndDate, String offStartTime, String offEndTime,
                                     String uid, String name, String offType, String offDescription) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newStartDate, startDate, endDate;
        String newDate;
        ArrayList<Date> dayOffList = new ArrayList<>();

        try {
            startDate = dateFormat.parse(offStartDate);
            endDate = dateFormat.parse(offEndDate);
        } catch (ParseException e) {
            return SysResult.build(201, "写入请假记录出错    " + e);
        }
        //防止日期格式不同
        offStartDate = dateFormat.format(startDate);
        offEndDate = dateFormat.format(endDate);

        // 获取上下班时间
        String departmentId = generalService.quickGet("department_id",
                "workers", "uid", uid).get(0).toString();
        String workTimeScheduleId = generalService.quickGet("work_time_schedule_id",
                "department", "department_id", departmentId).get(0).toString();
        String startWork = generalService.quickGet("start_work",
                "work_time_schedule", "work_time_schedule_id", workTimeScheduleId).get(0).toString();
        String endWork = generalService.quickGet("end_work",
                "work_time_schedule", "work_time_schedule_id", workTimeScheduleId).get(0).toString();


        String[] startWorkArray = startWork.split(":");
        int startWorkHour = Integer.valueOf(startWorkArray[0]);
        int startWorkMinute = Integer.valueOf(startWorkArray[1]);
        String[] endWorkArray = endWork.split(":");
        int endWorkHour = Integer.valueOf(endWorkArray[0]);
        int endWorkMinute = Integer.valueOf(endWorkArray[1]);

        String[] offStart = offStartTime.split(":");
        int offStartHour = Integer.valueOf(offStart[0]);
        int offStartMinute = Integer.valueOf(offStart[1]);
        String[] offEnd = offEndTime.split(":");
        int offEndHour = Integer.valueOf(offEnd[0]);
        int offEndMinute = Integer.valueOf(offEnd[1]);

        // 判断假期开始时间是否比上班时间早
        if (offStartHour < startWorkHour) {
            // 假期开始的过早, 将假期修改至上班时间
            offStartTime = startWorkHour + ":" + startWorkMinute;
        } else if (offStartHour == startWorkHour) {
            // 需要比较分钟
            if (offStartMinute < startWorkMinute) {
                // 假期开始的过早, 将假期修改至上班时间
                offStartTime = startWorkHour + ":" + startWorkMinute;
            }
        }

        // 判断假期开始时间是否比下班时间晚,以防止负放假
        if (offStartHour > endWorkHour) {
            // 假期开始的过晚, 将假期修改至下班时间
            offStartTime = endWorkHour + ":" + endWorkMinute;
        } else if (offStartHour == endWorkHour) {
            // 需要比较分钟
            if (offStartMinute > endWorkMinute) {
                // 假期开始的过晚, 将假期修改至下班时间
                offStartTime = endWorkHour + ":" + endWorkMinute;
            }
        }

        // 判断假期结束是否比下班时间晚
        if (offEndHour > endWorkHour) {
            // 假期结束过晚, 将假期修改至下班时间
            offEndTime = endWorkHour + ":" + endWorkMinute;
        } else if (offEndHour == endWorkHour) {
            // 需要比较分钟
            if (offEndMinute > endWorkMinute) {
                // 假期结束过晚, 将假期修改至下班时间
                offEndTime = endWorkHour + ":" + endWorkMinute;
            }
        }

        // 判断假期结束是否比上班时间早
        if (offEndHour < startWorkHour) {
            // 假期结束过早, 将假期修改至上班时间
            offEndTime = startWorkHour + ":" + startWorkMinute;
        } else if (offEndHour == startWorkHour) {
            // 需要比较分钟
            if (offEndMinute < startWorkMinute) {
                // 假期结束过晚, 将假期修改至下班时间
                offEndTime = startWorkHour + ":" + startWorkMinute;
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        // 判断请假是否跨天
        if (offStartDate.equals(offEndDate)) {
            // 没有跨天
            // 判断那天是否为假期
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                return SysResult.build(400, "请假时间段无效");
            }
            // 判断开始结束是否有时间段
            if (offStartTime.equals(offEndTime)) {
                // 没有时间段
                return SysResult.build(201, "请假时间为空");
            }
            DayOffRecord dayOff = new DayOffRecord();
            dayOff.setUid(uid);
            dayOff.setName(name);
            dayOff.setOffDate(startDate);
            dayOff.setOffStart(offStartTime);
            dayOff.setOffEnd(offEndTime);
            dayOff.setOffType(offType);
            dayOff.setOffDescription(offDescription);

            // 获取更新前的假期时长
            SysResult offHourResult = countDayOffHour(startDate, uid);
            if (!offHourResult.isOk()) {
                return SysResult.build(201, "获取更新前假期时间失败");
            }
            HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
            // 将之前的时长归还
            updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
            updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);

            // 每次添加假期前添加午休
            if (!toolService.addNoonBreak(uid, startDate)) {
                return SysResult.build(201, "添加午休失败");
            }
            // 写入记录
            clockInMapper.addDayOffRecord(dayOff);
            dayOffList.add(startDate);
            // 整合记录
            mergeDayOffRecord(startDate, uid);

            // 更新假期时长
            SysResult offHourResultAfterUpdate = countDayOffHour(startDate, uid);
            if (!offHourResultAfterUpdate.isOk()) {
                return SysResult.build(201, "获取更新后假期时间失败");
            }
            HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
            // 将时长扣除
            updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
            updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);


            return SysResult.build(200, "写入请假记录成功", dayOffList);
        } else {
            // 请假跨天了
            // 先确认结束日期在开始日期之后
            if (endDate.before(startDate)) {
                return SysResult.build(201, "无效的请假时间");
            }

            // 如果第一天不是周末的话
            // 第一条请假记录将是请假开始至上班时间结束
            if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                DayOffRecord dayOff = new DayOffRecord();
                dayOff.setUid(uid);
                dayOff.setName(name);
                dayOff.setOffDate(startDate);
                dayOff.setOffStart(offStartTime);
                dayOff.setOffEnd(endWork);
                dayOff.setOffType(offType);
                dayOff.setOffDescription(offDescription);

                // 获取更新前的假期时长
                SysResult offHourResult = countDayOffHour(startDate, uid);
                if (!offHourResult.isOk()) {
                    return SysResult.build(201, "获取更新前假期时间失败");
                }
                HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
                // 将之前的时长归还
                updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
                updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);

                // 每次添加假期前添加午休
                if (!toolService.addNoonBreak(uid, startDate)) {
                    return SysResult.build(201, "添加午休失败");
                }
                // 写入记录
                clockInMapper.addDayOffRecord(dayOff);
                dayOffList.add(startDate);
                // 整合记录
                mergeDayOffRecord(startDate, uid);

                // 更新假期时长
                SysResult offHourResultAfterUpdate = countDayOffHour(startDate, uid);
                if (!offHourResultAfterUpdate.isOk()) {
                    return SysResult.build(201, "获取更新后假期时间失败");
                }
                HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
                // 将时长扣除
                updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
                updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);

                // 请假开始时间向后推一天
                newStartDate = new Date(startDate.getTime() + 60 * 60 * 24 * 1000);
                newDate = dateFormat.format(newStartDate);
            } else {
                // 第一天是个周末
                newStartDate = startDate;
                newDate = offStartDate;
            }

            while (!newDate.equals(offEndDate)) {
                cal.setTime(newStartDate);
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                    // 检测到是周末直接向后推一天
                    newStartDate = new Date(newStartDate.getTime() + 60 * 60 * 24 * 1000);
                    newDate = dateFormat.format(newStartDate);
                    continue;
                }
                // 没有到达结束日期, 请全天的假期
                DayOffRecord dayOffTemp = new DayOffRecord();
                dayOffTemp.setUid(uid);
                dayOffTemp.setName(name);
                dayOffTemp.setOffDate(newStartDate);
                dayOffTemp.setOffStart(startWork);
                dayOffTemp.setOffEnd(endWork);
                dayOffTemp.setOffType(offType);
                dayOffTemp.setOffDescription(offDescription);

                // 获取更新前的假期时长
                SysResult offHourResult = countDayOffHour(newStartDate, uid);
                if (!offHourResult.isOk()) {
                    return SysResult.build(201, "获取更新前假期时间失败");
                }
                HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
                // 将之前的时长归还
                updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
                updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);


                // 每次添加假期前添加午休
                if (!toolService.addNoonBreak(uid, newStartDate)) {
                    return SysResult.build(201, "添加午休失败");
                }
                // 写入记录
                clockInMapper.addDayOffRecord(dayOffTemp);
                dayOffList.add(newStartDate);
                // 整合记录
                mergeDayOffRecord(newStartDate, uid);

                // 更新假期时长
                SysResult offHourResultAfterUpdate = countDayOffHour(newStartDate, uid);
                if (!offHourResultAfterUpdate.isOk()) {
                    return SysResult.build(201, "获取更新后假期时间失败");
                }
                HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
                // 将时长扣除
                updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
                updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);

                newStartDate = new Date(newStartDate.getTime() + 60 * 60 * 24 * 1000);
                newDate = dateFormat.format(newStartDate);
            }

            // 如果请假的结束日期是周末则直接跳过
            cal.setTime(endDate);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                return SysResult.build(200, "假期添加完成", dayOffList);
            } else {
                // 到达了请假结束的日期，最后一条记录将是从上班时间到请假结束时间
                System.out.println(endDate);
                DayOffRecord dayOffEnd = new DayOffRecord();
                dayOffEnd.setUid(uid);
                dayOffEnd.setName(name);
                dayOffEnd.setOffDate(endDate);
                dayOffEnd.setOffStart(startWork);
                dayOffEnd.setOffEnd(offEndTime);
                dayOffEnd.setOffType(offType);
                dayOffEnd.setOffDescription(offDescription);

                // 获取更新前的假期时长
                SysResult offHourResult = countDayOffHour(endDate, uid);
                if (!offHourResult.isOk()) {
                    return SysResult.build(201, "获取更新前假期时间失败");
                }
                HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
                // 将之前的时长归还
                updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
                updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);


                // 每次添加假期前添加午休
                if (!toolService.addNoonBreak(uid, endDate)) {
                    return SysResult.build(201, "添加午休失败");
                }
                // 写入记录
                clockInMapper.addDayOffRecord(dayOffEnd);
                dayOffList.add(endDate);
                // 整合记录
                mergeDayOffRecord(endDate, uid);

                // 更新假期时长
                SysResult offHourResultAfterUpdate = countDayOffHour(endDate, uid);
                if (!offHourResultAfterUpdate.isOk()) {
                    return SysResult.build(201, "获取更新后假期时间失败");
                }
                HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
                // 将时长扣除
                updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
                updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);

                return SysResult.build(200, "请假记录添加成功", dayOffList);
            }
        }
    }

    @Override
    public SysResult calculateOffTime(String uid, String offStartDate, String offStartTime, String offEndDate, String offEndTime, String offType) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newStartDate, startDate, endDate;
        String newDate;
        Double totalOffTime = 0.0;

        try {
            startDate = dateFormat.parse(offStartDate);
            endDate = dateFormat.parse(offEndDate);
        } catch (ParseException e) {
            return SysResult.build(201, "初始化时间出错    " + e);
        }
        //防止日期格式不同
        offStartDate = dateFormat.format(startDate);
        offEndDate = dateFormat.format(endDate);

        // 获取上下班时间
        String startWork, endWork;
        try {
            String departmentId = generalService.quickGet("department_id",
                    "workers", "uid", uid).get(0).toString();
            String workTimeScheduleId = generalService.quickGet("work_time_schedule_id",
                    "department", "department_id", departmentId).get(0).toString();
            startWork = generalService.quickGet("start_work",
                    "work_time_schedule", "work_time_schedule_id", workTimeScheduleId).get(0).toString();
            endWork = generalService.quickGet("end_work",
                    "work_time_schedule", "work_time_schedule_id", workTimeScheduleId).get(0).toString();
        } catch (Exception e) {
            return SysResult.build(201, "获取员工信息异常");
        }

        String[] startWorkArray = startWork.split(":");
        int startWorkHour = Integer.valueOf(startWorkArray[0]);
        int startWorkMinute = Integer.valueOf(startWorkArray[1]);
        String[] endWorkArray = endWork.split(":");
        int endWorkHour = Integer.valueOf(endWorkArray[0]);
        int endWorkMinute = Integer.valueOf(endWorkArray[1]);

        String[] offStart = offStartTime.split(":");
        int offStartHour = Integer.valueOf(offStart[0]);
        int offStartMinute = Integer.valueOf(offStart[1]);
        String[] offEnd = offEndTime.split(":");
        int offEndHour = Integer.valueOf(offEnd[0]);
        int offEndMinute = Integer.valueOf(offEnd[1]);

        // 判断假期开始时间是否比上班时间早
        if (offStartHour < startWorkHour) {
            // 假期开始的过早, 将假期修改至上班时间
            offStartTime = startWorkHour + ":" + startWorkMinute;
        } else if (offStartHour == startWorkHour) {
            // 需要比较分钟
            if (offStartMinute < startWorkMinute) {
                // 假期开始的过早, 将假期修改至上班时间
                offStartTime = startWorkHour + ":" + startWorkMinute;
            }
        }

        // 判断假期开始时间是否比下班时间晚,以防止负放假
        if (offStartHour > endWorkHour) {
            // 假期开始的过晚, 将假期修改至下班时间
            offStartTime = endWorkHour + ":" + endWorkMinute;
        } else if (offStartHour == endWorkHour) {
            // 需要比较分钟
            if (offStartMinute > endWorkMinute) {
                // 假期开始的过晚, 将假期修改至下班时间
                offStartTime = endWorkHour + ":" + endWorkMinute;
            }
        }

        // 判断假期结束是否比下班时间晚
        if (offEndHour > endWorkHour) {
            // 假期结束过晚, 将假期修改至下班时间
            offEndTime = endWorkHour + ":" + endWorkMinute;
        } else if (offEndHour == endWorkHour) {
            // 需要比较分钟
            if (offEndMinute > endWorkMinute) {
                // 假期结束过晚, 将假期修改至下班时间
                offEndTime = endWorkHour + ":" + endWorkMinute;
            }
        }

        // 判断假期结束是否比上班时间早
        if (offEndHour < startWorkHour) {
            // 假期结束过早, 将假期修改至上班时间
            offEndTime = startWorkHour + ":" + startWorkMinute;
        } else if (offEndHour == startWorkHour) {
            // 需要比较分钟
            if (offEndMinute < startWorkMinute) {
                // 假期结束过晚, 将假期修改至下班时间
                offEndTime = startWorkHour + ":" + startWorkMinute;
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        // 判断请假是否跨天
        if (offStartDate.equals(offEndDate)) {
            // 没有跨天
            // 判断那天是否为假期
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                return SysResult.build(400, "请假时间段无效");
            }
            // 判断开始结束是否有时间段
            if (offStartTime.equals(offEndTime)) {
                // 没有时间段
                return SysResult.build(201, "请假时间为空");
            }
            Double time = calculateMergedOffTime(uid, offStartTime, offEndTime, endDate, offType);
            if (time >= 0) {
                totalOffTime = totalOffTime + time;
            } else {
                return SysResult.build(201, "计算出错");
            }

            return SysResult.build(200, "请假时间获取成功", totalOffTime);
        } else {
            // 请假跨天了
            // 先确认结束日期在开始日期之后
            if (endDate.before(startDate)) {
                return SysResult.build(201, "无效的请假时间");
            }

            // 如果第一天不是周末的话
            // 第一条请假记录将是请假开始至上班时间结束
            if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {

                Double time = calculateMergedOffTime(uid, offStartTime, endWork, startDate, offType);
                if (time >= 0) {
                    totalOffTime = totalOffTime + time;
                } else {
                    return SysResult.build(201, "计算出错");
                }

                // 请假开始时间向后推一天
                newStartDate = new Date(startDate.getTime() + 60 * 60 * 24 * 1000);
                newDate = dateFormat.format(newStartDate);
            } else {
                // 第一天是个周末
                newStartDate = startDate;
                newDate = offStartDate;
            }

            while (!newDate.equals(offEndDate)) {
                cal.setTime(newStartDate);
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                    // 检测到是周末直接向后推一天
                    newStartDate = new Date(newStartDate.getTime() + 60 * 60 * 24 * 1000);
                    newDate = dateFormat.format(newStartDate);
                    continue;
                }
                // 没有到达结束日期, 请全天的假期
                Double time = calculateMergedOffTime(uid, startWork, endWork, newStartDate, offType);
                if (time >= 0) {
                    totalOffTime = totalOffTime + time;
                } else {
                    return SysResult.build(201, "计算出错");
                }

                newStartDate = new Date(newStartDate.getTime() + 60 * 60 * 24 * 1000);
                newDate = dateFormat.format(newStartDate);
            }

            // 如果请假的结束日期是周末则直接跳过
            cal.setTime(endDate);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                return SysResult.build(200, "请假时间获取成功", totalOffTime);
            } else {
                // 到达了请假结束的日期，最后一条记录将是从上班时间到请假结束时间
                Double time = calculateMergedOffTime(uid, startWork, offEndTime, endDate, offType);
                if (time >= 0) {
                    totalOffTime = totalOffTime + time;
                } else {
                    return SysResult.build(201, "计算出错");
                }
                return SysResult.build(200, "请假时间获取成功", totalOffTime);
            }
        }
    }

    private Double calculateMergedOffTime(String uid, String start, String end, Date date, String offType) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 取出当天所有假期记录
        ArrayList<DayOffRecord> records = clockInMapper.getDayOffRecord(uid, dateFormat.format(date));

        // 先检测原先记录中有无午休记录
        int size = records.size();
        for (int i = 0; i < size; i++) {
            if (records.get(i).getOffType().equals("NoonBreak")) {
                records.remove(i);
                size = records.size();
            } else {
                records.get(i).setId(i);
            }
        }
        // 将正在申请的记录添加进去作为假设记录
        DayOffRecord pendingRecord = new DayOffRecord();
        pendingRecord.setUid(uid);
        pendingRecord.setOffDate(date);
        pendingRecord.setOffStart(start);
        pendingRecord.setOffEnd(end);
        pendingRecord.setOffType(offType);
        pendingRecord.setId(size + 1);
        records.add(pendingRecord);

        // 将午休添加进去作为假设记录
        // 根据uid 获取部门id
        ArrayList<Object> departmentId = generalService.quickGet("department_id", "workers", "uid", uid);
        if (departmentId.size() == 0) {
            return -1.0;
        }
        String department_id = departmentId.get(0).toString();

        // 获取午休id
        ArrayList<Object> noonBreakId = generalService.quickGet("noon_break_id", "department", "department_id", department_id);
        if (noonBreakId.size() == 0) {
            return -1.0;
        }
        String noon_break_id = noonBreakId.get(0).toString();

        // 获取午休的全部信息
        HashMap<String, String> noonBreakResult = clockInMapper.getNoonBreakInfo(noon_break_id);
        String startTime = noonBreakResult.get("start_time");
        String endTime = noonBreakResult.get("end_time");

        if (startTime == null || endTime == null) {
            return -1.0;
        }

        DayOffRecord breakRecord = new DayOffRecord();
        breakRecord.setOffType("NoonBreak");
        breakRecord.setOffStart(startTime);
        breakRecord.setOffEnd(endTime);
        breakRecord.setOffDate(date);
        breakRecord.setUid(uid);
        breakRecord.setId(size + 2);
        records.add(breakRecord);

        // 开始整合
        ArrayList<MergedDayOffRecord> tempMerge = new ArrayList<>();

        int currentNum = 1;
        int earliestId = records.get(0).getId();
        String[] earliestStartTime = records.get(0).getOffStart().split(":");
        int earliestStartHour = Integer.valueOf(earliestStartTime[0]);
        int earliestStartMinute = Integer.valueOf(earliestStartTime[1]);
        // 循环寻找开始时间最小的的记录
        while (records.size() != 0) {
            if (currentNum == records.size()) {
                // 循环完一遍list了, 找到了当前list中最小的开始时间
                currentNum = 1;
                // 取出最小开始时间的结束时间
                int currentLength = records.size();
                int earliestEndHour = 0, earliestEndMinute = 0;
                for (int i = 0; i < currentLength; i++) {
                    if (records.get(i).getId().equals(earliestId)) {
                        earliestEndHour = Integer.valueOf(records.get(i).getOffEnd().split(":")[0]);
                        earliestEndMinute = Integer.valueOf(records.get(i).getOffEnd().split(":")[1]);
                        // 将最小开始时间取出
                        records.remove(i);
                        break;
                    }
                }

                int currentMerge = 0;
                // 开始将其他时间向最小时间整合
                while (currentMerge != records.size()) {
                    String[] tempStartTime = records.get(currentMerge).getOffStart().split(":");
                    int tempStartHour = Integer.valueOf(tempStartTime[0]);
                    int tempStartMinute = Integer.valueOf(tempStartTime[1]);
                    if (tempStartHour < earliestEndHour || (tempStartHour == earliestEndHour && tempStartMinute <= earliestEndMinute)) {
                        // 代表该时间段可整合, 判断结束时间来决定是否有整合价值
                        String[] tempEndTime = records.get(currentMerge).getOffEnd().split(":");
                        int tempEndHour = Integer.valueOf(tempEndTime[0]);
                        int tempEndMinute = Integer.valueOf(tempEndTime[1]);
                        if (tempEndHour > earliestEndHour || (tempEndHour == earliestEndHour && tempEndMinute > earliestEndMinute)) {
                            earliestEndHour = tempEndHour;
                            earliestEndMinute = tempEndMinute;
                            // 取出已整合的片段
                            records.remove(currentMerge);
                            currentMerge = 0;
                        } else {
                            // 开始时间确实比整合小，但是结束的也比整合早，没有整合价值
                            records.remove(currentMerge);
                            currentMerge = 0;
                        }
                    } else {
                        // 开始时间在整合时间段外，跳到下一个
                        currentMerge++;
                    }
                }
                // 一个时间段已经全部都整合完毕了，添加进整合表然后进入下一个整合时间段
                MergedDayOffRecord mergedDayOffRecord = new MergedDayOffRecord();
                mergedDayOffRecord.setUid(uid);
                mergedDayOffRecord.setOffDate(date);
                mergedDayOffRecord.setStartTime(earliestStartHour + ":" + earliestStartMinute);
                mergedDayOffRecord.setEndTime(earliestEndHour + ":" + earliestEndMinute);
                tempMerge.add(mergedDayOffRecord);

                if (records.size() != 0) {
                    earliestStartTime = records.get(0).getOffStart().split(":");
                    earliestStartHour = Integer.valueOf(earliestStartTime[0]);
                    earliestStartMinute = Integer.valueOf(earliestStartTime[1]);
                    earliestId = records.get(0).getId();
                } else {
                    // 放假记录已被全部整合
                    break;
                }

            } else {
                String[] tempStartTime = records.get(currentNum).getOffStart().split(":");
                int tempStartHour = Integer.valueOf(tempStartTime[0]);
                int tempStartMinute = Integer.valueOf(tempStartTime[1]);
                if (tempStartHour < earliestStartHour || (tempStartHour == earliestStartHour && tempStartMinute < earliestStartMinute)) {
                    // 新的开始时间更小
                    earliestStartHour = tempStartHour;
                    earliestStartMinute = tempStartMinute;
                    earliestId = records.get(currentNum).getId();
                }
                currentNum++;
            }
        }

        // 计算添加了假设假期的目标假期总时长
        // 计算整合总时长
        int allRecordsNum = tempMerge.size();
        double totalDayOffHour = 0;
        for (int i = 0; i < allRecordsNum; i++) {
            MergedDayOffRecord tempRecord = tempMerge.get(i);
            String[] tempStartTime = tempRecord.getStartTime().split(":");
            int tempStartHour = Integer.valueOf(tempStartTime[0]);
            int tempStartMinute = Integer.valueOf(tempStartTime[1]);

            String[] tempEndTime = tempRecord.getEndTime().split(":");
            int tempEndHour = Integer.valueOf(tempEndTime[0]);
            int tempEndMinute = Integer.valueOf(tempEndTime[1]);

            totalDayOffHour = totalDayOffHour + (tempEndHour + tempEndMinute / 60.0 - tempStartHour - tempStartMinute / 60.0);
        }

        // 取得节假日记录
        ArrayList<DayOffRecord> holidayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "Holiday");
        // 取得事假记录
        ArrayList<DayOffRecord> NormalDayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "DayOff");
        // 取得调休记录
        ArrayList<DayOffRecord> changeDayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "ChangeDayOff");
        // 取得年假记录
        ArrayList<DayOffRecord> AnnualDayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "AnnualDayOff");
        // 取得午休记录
        ArrayList<DayOffRecord> NoonBreakRecords = new ArrayList<>();
        NoonBreakRecords.add(breakRecord);

        if (offType.equals("Holiday")) {
            holidayOffRecords.add(pendingRecord);
        } else if (offType.equals("ChangeDayOff")) {
            changeDayOffRecords.add(pendingRecord);
        } else if (offType.equals("AnnualDayOff")) {
            AnnualDayOffRecords.add(pendingRecord);
        } else if (offType.equals("NoonBreak")) {
            NoonBreakRecords.add(pendingRecord);
        }else if (offType.equals("DayOff")) {
            NormalDayOffRecords.add(pendingRecord);
        }

        // 记录各种放假时间
        HashMap<String, Double> dayOffHours = new HashMap<>();
        ArrayList<DayOffRecord> holidayRecords = new ArrayList<>();
        String offName = "";

        for (int offNum = 0; offNum < 5; offNum++) {
            // 规定优先级
            if (offNum == 0) {
                offName = "Holiday";
                holidayRecords = holidayOffRecords;
            } else if (offNum == 2) {
                offName = "ChangeDayOff";
                holidayRecords = changeDayOffRecords;
            } else if (offNum == 3) {
                offName = "AnnualDayOff";
                holidayRecords = AnnualDayOffRecords;
            } else if (offNum == 1) {
                offName = "NoonBreak";
                holidayRecords = NoonBreakRecords;
            } else if (offNum == 4) {
                offName = "DayOff";
                holidayRecords = NormalDayOffRecords;
            }

            // 将节假日记录从整合记录中剔除
            int holidayNum = holidayRecords.size();
            if (holidayNum != 0) {
                // 有节假日记录
                for (int i = 0; i < holidayNum; i++) {
                    DayOffRecord tempDayOff = holidayRecords.get(i);
                    String[] tempStartTime = tempDayOff.getOffStart().split(":");
                    int tempStartHour = Integer.valueOf(tempStartTime[0]);
                    int tempStartMinute = Integer.valueOf(tempStartTime[1]);
                    double holidayStartTime = tempStartHour + tempStartMinute / 60.0;

                    String[] tempEndTime = tempDayOff.getOffEnd().split(":");
                    int tempEndHour = Integer.valueOf(tempEndTime[0]);
                    int tempEndMinute = Integer.valueOf(tempEndTime[1]);
                    double holidayEndTime = tempEndHour + tempEndMinute / 60.0;
                    int currentAt = 0;

                    while (currentAt != tempMerge.size()) {
                        String[] mergedStartTime = tempMerge.get(currentAt).getStartTime().split(":");
                        int mergedStartHour = Integer.valueOf(mergedStartTime[0]);
                        int mergedStartMinute = Integer.valueOf(mergedStartTime[1]);
                        double StartTime = mergedStartHour + mergedStartMinute / 60.0;

                        String[] mergedEndTime = tempMerge.get(currentAt).getEndTime().split(":");
                        int mergedEndHour = Integer.valueOf(mergedEndTime[0]);
                        int mergedEndMinute = Integer.valueOf(mergedEndTime[1]);
                        double EndTime = mergedEndHour + mergedEndMinute / 60.0;

                        // merge 两头都小于等于 temp, 直接去掉merge
                        if (StartTime >= holidayStartTime && EndTime <= holidayEndTime) {
                            tempMerge.remove(currentAt);
                            currentAt = 0;
                            continue;
                        }

                        // merge 两头都大于 temp, 原有merge被截成两段
                        if (StartTime < holidayStartTime && EndTime > holidayEndTime) {
                            tempMerge.remove(currentAt);

                            MergedDayOffRecord pieceOne = new MergedDayOffRecord();
                            pieceOne.setStartTime(mergedStartHour + ":" + mergedStartMinute);
                            pieceOne.setEndTime(tempStartHour + ":" + tempStartMinute);
                            tempMerge.add(pieceOne);

                            MergedDayOffRecord pieceTwo = new MergedDayOffRecord();
                            pieceTwo.setStartTime(tempEndHour + ":" + tempEndMinute);
                            pieceTwo.setEndTime(mergedEndHour + ":" + mergedEndMinute);
                            tempMerge.add(pieceTwo);
                            currentAt = 0;
                            continue;
                        }

                        // merge 开始的早结束的也早, merge仅保留上半段
                        if (StartTime < holidayStartTime && EndTime <= holidayEndTime && EndTime > holidayStartTime) {
                            tempMerge.remove(currentAt);

                            MergedDayOffRecord pieceOne = new MergedDayOffRecord();
                            pieceOne.setStartTime(mergedStartHour + ":" + mergedStartMinute);
                            pieceOne.setEndTime(tempStartHour + ":" + tempStartMinute);
                            tempMerge.add(pieceOne);

                            currentAt = 0;
                            continue;
                        }

                        // merge 开始的晚结束的也晚, merge仅保留下半段
                        if (StartTime >= holidayStartTime && EndTime > holidayEndTime && StartTime < holidayEndTime) {
                            tempMerge.remove(currentAt);

                            MergedDayOffRecord pieceTwo = new MergedDayOffRecord();
                            pieceTwo.setStartTime(tempEndHour + ":" + tempEndMinute);
                            pieceTwo.setEndTime(mergedEndHour + ":" + mergedEndMinute);
                            tempMerge.add(pieceTwo);

                            currentAt = 0;
                            continue;
                        }
                        currentAt++;
                    }
                }
                // 去掉所有的目标节日记录了, 通过两次merge的差值, 计算假日的时间
                double tempHour = 0;
                int currentRecordsNum = tempMerge.size();
                for (int i = 0; i < currentRecordsNum; i++) {
                    MergedDayOffRecord tempRecord = tempMerge.get(i);
                    String[] tempStartTime = tempRecord.getStartTime().split(":");
                    int tempStartHour = Integer.valueOf(tempStartTime[0]);
                    int tempStartMinute = Integer.valueOf(tempStartTime[1]);

                    String[] tempEndTime = tempRecord.getEndTime().split(":");
                    int tempEndHour = Integer.valueOf(tempEndTime[0]);
                    int tempEndMinute = Integer.valueOf(tempEndTime[1]);

                    tempHour = tempHour + (tempEndHour + tempEndMinute / 60.0 - tempStartHour - tempStartMinute / 60.0);
                }
                double subHour = totalDayOffHour - tempHour;
                totalDayOffHour = tempHour;
                dayOffHours.put(offName, subHour);
            } else {
                dayOffHours.put(offName, 0.0);
            }
        }

        Double timeAfter = dayOffHours.get(offType);
        SysResult timeBeforeResult = countDayOffHour(date, uid);
        if (!timeBeforeResult.isOk()) {
            return -1.0;
        }
        HashMap<String, Double> beforeDayOffHours = (HashMap<String, Double>) timeBeforeResult.getData();
        Double timeBefore = beforeDayOffHours.get(offType);

        return timeAfter - timeBefore;
    }


    @Override
    public SysResult updateOffHour(String type, Double num, String uid) {
        ArrayList<Object> changeResult = generalService.quickGet(type, "workers", "uid", uid);
        if (changeResult.size() != 0) {
            String changeTime = changeResult.get(0).toString();
            Double changeHour = Double.valueOf(changeTime);
            changeHour = changeHour + num;
            // 更新调休时间
            applicationMapper.updateChangeDayOffType(type, changeHour + "", uid);
            return SysResult.build(200, "更新假期剩余时间成功");
        } else {
            return SysResult.build(201, "更新假期剩余时间失败");
        }
    }


    @Override
    public SysResult recalculateClockInRecord(ArrayList<Date> dates, String uid) {
        int numDate = dates.size();
        if (numDate == 0) {
            return SysResult.build(200, "没有记录需要重新计算");
        }

        for (int i = 0; i < numDate; i++) {
            Date recalDate = dates.get(i);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Calendar cal = Calendar.getInstance();
            cal.setTime(recalDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            String monthName, formName;

            // 根据日期算出打卡记录所在的表
            if (month < 7) {
                monthName = "jan";
            } else {
                monthName = "jul";
            }
            formName = "clock_in_" + monthName + "_" + year;

            // 判断表格是否存在
            ArrayList<String> allForms = clockInMapper.getAllClockInForm();
            if(!allForms.contains(formName)){
                return SysResult.build(200,"表格不存在无法重新计算");
            }

            String start = dateFormat.format(recalDate);
            String end = dateFormat.format(new Date(recalDate.getTime() + 1000 * 60 * 60 * 24));
            // 根据uid和日期，取出打卡表中需要更新的打卡记录
            ClockIn clockInInfo = clockInMapper.getClockInRecordByDate(formName, uid, start, end);
            if (clockInInfo == null) {
                continue;
            }
            // 根据日期取出放假记录
            BoundedDayOffRecord boundedDayOffRecord = applicationMapper.getBoundedDayOffRecord(uid, start);

            // 判断有无上班卡
            if (clockInInfo.getClockInStatus().equals("miss")) {
                // 没有上班卡，意味着一天都没有打卡，直接跳过
                continue;
            } else {
                // 有上班卡，获取时间开始计算
                // 获取上班时间
                String[] startWork = clockInInfo.getStartWork().split(":");
                int startWorkHour = Integer.valueOf(startWork[0]);
                int startWorkMinute = Integer.valueOf(startWork[1]);
                String[] clockIn = clockInInfo.getClockIn().split(":");
                int clockInHour = Integer.valueOf(clockIn[0]);
                int clockInMinute = Integer.valueOf(clockIn[1]);
                String clockInTime = clockInHour + ":" + clockInMinute;

                if (boundedDayOffRecord == null) {
                    // 没有放假记录，正常上班
                    if (clockInHour > startWorkHour) {
                        // 迟到了
                        clockInMapper.clockIn(formName, uid, clockInTime, "late");
                    } else if (clockInHour == startWorkHour) {
                        // 需要判断分钟
                        if (clockInMinute > startWorkMinute) {
                            // 迟到了
                            clockInMapper.clockIn(formName, uid, clockInTime, "late");
                        } else {
                            // 正常
                            clockInMapper.clockIn(formName, uid, clockInTime, "normal");
                        }
                    } else {
                        // 正常
                        clockInMapper.clockIn(formName, uid, clockInTime, "normal");
                    }
                } else {
                    // 取得最早放假记录的整合
                    String startId = boundedDayOffRecord.getStartBoundId();
                    MergedDayOffRecord mergedDayOffRecord = applicationMapper.getMergedDayOffRecordById(startId);
                    // 有放假记录，需要进一步判断
                    String[] offStart = mergedDayOffRecord.getStartTime().split(":");
                    int offStartHour = Integer.valueOf(offStart[0]);
                    int offStartMinute = Integer.valueOf(offStart[1]);
                    String[] offEnd = mergedDayOffRecord.getEndTime().split(":");
                    int offEndHour = Integer.valueOf(offEnd[0]);
                    int offEndMinute = Integer.valueOf(offEnd[1]);

                    // 假期是从上班就开始的，那么就将上班时间滞后
                    if (startWorkHour == offStartHour && startWorkMinute == offStartMinute) {
                        // 那么就将上班时间滞后为假期结束时间即offEnd
                        if (clockInHour > offEndHour) {
                            // 迟到了
                            clockInMapper.clockIn(formName, uid, clockInTime, "late");
                        } else if (clockInHour == offEndHour) {
                            // 需要判断分钟
                            if (clockInMinute > offEndMinute) {
                                // 迟到了
                                clockInMapper.clockIn(formName, uid, clockInTime, "late");
                            } else {
                                // 正常
                                clockInMapper.clockIn(formName, uid, clockInTime, "normal");
                            }
                        } else {
                            // 正常
                            clockInMapper.clockIn(formName, uid, clockInTime, "normal");
                        }
                    } else {
                        // 假期并不是从上班就开始，那么上班时间照旧
                        if (clockInHour > startWorkHour) {
                            // 迟到了
                            clockInMapper.clockIn(formName, uid, clockInTime, "late");
                        } else if (clockInHour == startWorkHour) {
                            // 需要判断分钟
                            if (clockInMinute > startWorkMinute) {
                                // 迟到了
                                clockInMapper.clockIn(formName, uid, clockInTime, "late");
                            } else {
                                // 正常
                                clockInMapper.clockIn(formName, uid, clockInTime, "normal");
                            }
                        } else {
                            // 正常
                            clockInMapper.clockIn(formName, uid, clockInTime, "normal");
                        }
                    }
                }
            }
            // 判断有无下班卡
            if (clockInInfo.getClockOutStatus().equals("miss")) {
                // 没有下班卡，上班卡计算完成了，直接跳转到下一个日期
                continue;
            } else {
                // 有下班卡, 需要获取时间重新计算
                String[] endWork = clockInInfo.getEndWork().split(":");
                int endWorkHour = Integer.valueOf(endWork[0]);
                int endWorkMinute = Integer.valueOf(endWork[1]);
                String[] clockOut = clockInInfo.getClockOut().split(":");
                int clockOutHour = Integer.valueOf(clockOut[0]);
                int clockOutMinute = Integer.valueOf(clockOut[1]);
                String clockOutTime = clockOutHour + ":" + clockOutMinute;

                // 判断有无请假记录
                if (boundedDayOffRecord == null) {
                    // 没有请假，下班时间照旧
                    if (clockOutHour < endWorkHour) {
                        // 早退了
                        clockInMapper.clockOut(formName, uid, clockOutTime, "early");
                    } else if (clockOutHour == endWorkHour) {
                        // 需要判断分钟
                        if (clockOutMinute < endWorkMinute) {
                            // 早退了
                            clockInMapper.clockOut(formName, uid, clockOutTime, "early");
                        } else {
                            // 正常
                            clockInMapper.clockOut(formName, uid, clockOutTime, "normal");
                        }
                    } else {
                        // 正常
                        clockInMapper.clockOut(formName, uid, clockOutTime, "normal");
                    }
                } else {
                    // 取得最后的假期整合
                    String endId = boundedDayOffRecord.getEndBoundId();
                    MergedDayOffRecord mergedDayOffRecord = applicationMapper.getMergedDayOffRecordById(endId);
                    // 有请假，需要进一步判断
                    String[] offStart = mergedDayOffRecord.getStartTime().split(":");
                    int offStartHour = Integer.valueOf(offStart[0]);
                    int offStartMinute = Integer.valueOf(offStart[1]);
                    String[] offEnd = mergedDayOffRecord.getEndTime().split(":");
                    int offEndHour = Integer.valueOf(offEnd[0]);
                    int offEndMinute = Integer.valueOf(offEnd[1]);
                    // 如果假期持续到上班结束，那么下班时间提前至假期开始
                    if (offEndHour == endWorkHour && offEndMinute == endWorkMinute) {
                        // 假期持续到上班结束，下班时间提前为假期开始时间
                        if (clockOutHour < offStartHour) {
                            // 早退了
                            clockInMapper.clockOut(formName, uid, clockOutTime, "early");
                        } else if (clockOutHour == offStartHour) {
                            // 需要判断分钟
                            if (clockOutMinute < offStartMinute) {
                                // 早退了
                                clockInMapper.clockOut(formName, uid, clockOutTime, "early");
                            } else {
                                // 正常
                                clockInMapper.clockOut(formName, uid, clockOutTime, "normal");
                            }
                        } else {
                            // 正常
                            clockInMapper.clockOut(formName, uid, clockOutTime, "normal");
                        }
                    } else {
                        // 假期并未持续到下班时间，那么下班时间照旧
                        if (clockOutHour < endWorkHour) {
                            // 早退了
                            clockInMapper.clockOut(formName, uid, clockOutTime, "early");
                        } else if (clockOutHour == endWorkHour) {
                            // 需要判断分钟
                            if (clockOutMinute < endWorkMinute) {
                                // 早退了
                                clockInMapper.clockOut(formName, uid, clockOutTime, "early");
                            } else {
                                // 正常
                                clockInMapper.clockOut(formName, uid, clockOutTime, "normal");
                            }
                        } else {
                            // 正常
                            clockInMapper.clockOut(formName, uid, clockOutTime, "normal");
                        }
                    }
                }
            }
            recalculateWorkTime(recalDate, uid);
        }
        return SysResult.build(200, "重新计算打卡记录完成");
    }

    @Override
    public SysResult recalculateWorkTime(Date date, String uid) {
        // 取得需要重算工作时间的日期
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String start = dateFormat.format(date);
        String end = dateFormat.format(new Date(date.getTime() + 1000 * 60 * 60 * 24));

        // 取得对应打卡记录
        SysResult clockInRecord = clockInService.getClockInRecordByDate(uid, start, end);
        if (!clockInRecord.isOk()) {
            return SysResult.build(201, "打卡记录获取失败：" + clockInRecord.getMsg());
        }
        ClockIn clockInInfo = (ClockIn) clockInRecord.getData();

        // 开始更新
        // 获取表格名称
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        String monthName, formName;

        // 根据日期算出打卡记录所在的表
        if (month < 7) {
            monthName = "jan";
        } else {
            monthName = "jul";
        }
        formName = "clock_in_" + monthName + "_" + year;


        // 先判断是否打卡了，否则没有打卡信息，也取得不到打卡时间
        if (clockInInfo.getClockInStatus().equals("miss") || clockInInfo.getClockOutStatus().equals("miss")) {
            // 没有打卡, 则上班时间为0
            clockInInfo.setWorkHour(0 + "");
            clockInInfo.setExtraWorkHour(0 + "");
            clockInMapper.reupdateCalculatedRecord(formName, clockInInfo, clockInInfo.getId());
            return SysResult.build(200, "更新完成，员工有缺卡记录");
        }

        // 打过卡了
        // 获取当前人员的上下班时间和加班时间段
        String[] workStart = clockInInfo.getStartWork().split(":");
        int startWorkHour = Integer.valueOf(workStart[0]);
        int startWorkMinute = Integer.valueOf(workStart[1]);

        String[] endWork = clockInInfo.getEndWork().split(":");
        int endWorkHour = Integer.valueOf(endWork[0]);
        int endWorkMinute = Integer.valueOf(endWork[1]);

        String[] startExtraWork = clockInInfo.getStartExtraWork().split(":");
        int startExtraWorkHour = Integer.valueOf(startExtraWork[0]);
        int startExtraWorkMinute = Integer.valueOf(startExtraWork[1]);

        String[] endExtraWork = clockInInfo.getEndExtraWork().split(":");
        int endExtraWorkHour = Integer.valueOf(endExtraWork[0]);
        int endExtraWorkMinute = Integer.valueOf(endExtraWork[1]);

        // 获取打卡时间
        String[] clockIn = clockInInfo.getClockIn().split(":");
        int clockInHour = Integer.valueOf(clockIn[0]);
        int clockInMinute = Integer.valueOf(clockIn[1]);
        String[] clockOut = clockInInfo.getClockOut().split(":");
        int clockOutHour = Integer.valueOf(clockOut[0]);
        int clockOutMinute = Integer.valueOf(clockOut[1]);

        // 计算总上班时长
        double workStartTime;
        double workEndTime;
        double ExtraWorkStartTime;
        double ExtraWorkEndTime;

        // 判断是否早到
        if (clockInInfo.getClockInStatus().equals("late")) {
            // 迟到了，上班时间就要按照打卡时间算
            workStartTime = clockInHour + clockInMinute / 60.0;
        } else {
            // 早到了或者正点，上班时间按照标准时间计算
            workStartTime = startWorkHour + startWorkMinute / 60.0;
        }

        // 判断是否早退
        if (clockInInfo.getClockOutStatus().equals("early")) {
            // 早退了，下班时间按照打卡时间计算
            workEndTime = clockOutHour + clockOutMinute / 60.0;
        } else {
            // 加班了或者正点下班, 下班时间按照标准时间计算
            workEndTime = endWorkHour + endWorkMinute / 60.0;
        }

        // 标准上班时间段内上班的时间
        double totalWorkHour = workEndTime - workStartTime;

        // 取得最小加班时间
        String departmentId = generalService.quickGet("department_id", "workers",
                "uid", clockInInfo.getUid()).get(0).toString();
        String extraWorkTimeScheduleId = generalService.quickGet("extra_work_time_schedule_id",
                "department", "department_id", departmentId).get(0).toString();
        String minimumWorkTime = generalService.quickGet("minimum_work_time", "extra_work_time_schedule",
                "extra_work_time_schedule_id", extraWorkTimeScheduleId).get(0).toString();
        int minimumExtraWorkTime = Integer.valueOf(minimumWorkTime);

        // 判断是否加班了
        if (clockOutHour > startExtraWorkHour) {
            // 加班了
            ExtraWorkStartTime = startExtraWorkHour + startExtraWorkMinute / 60.0;
        } else if (clockOutHour == startExtraWorkHour) {
            // 需要判断分钟
            if (clockOutMinute > startExtraWorkMinute) {
                // 加班了
                ExtraWorkStartTime = startExtraWorkHour + startExtraWorkMinute / 60.0;
            } else {
                // 正常
                ExtraWorkStartTime = 0;
            }
        } else {
            ExtraWorkStartTime = 0;
        }

        // 判断是否超出加班时间极限
        if (clockOutHour > endExtraWorkHour) {
            // 超出上限了，按照上限计算
            ExtraWorkEndTime = endExtraWorkHour + endExtraWorkMinute / 60.0;
        } else if (clockOutHour == endExtraWorkHour) {
            // 需要判断分钟
            if (clockOutMinute > endExtraWorkMinute) {
                // 超出上限了，按照上限计算
                ExtraWorkEndTime = endExtraWorkHour + endExtraWorkMinute / 60.0;
            } else {
                // 正常
                ExtraWorkEndTime = clockOutHour + clockOutMinute / 60.0;
            }
        } else {
            ExtraWorkEndTime = clockOutHour + clockOutMinute / 60.0;
        }
        if (ExtraWorkStartTime == 0) {
            // 没有加班
            clockInInfo.setExtraWorkHour(0 + "");
        } else {
            // 加班了
            // 判断加班时间是否够最小加班时长
            double extraWorkTime = ExtraWorkEndTime - ExtraWorkStartTime;
            if (extraWorkTime < minimumExtraWorkTime) {
                extraWorkTime = 0;
            }
            clockInInfo.setExtraWorkHour(extraWorkTime + "");
        }
        clockInInfo.setWorkHour(totalWorkHour + "");
        clockInMapper.reupdateCalculatedRecord(formName, clockInInfo, clockInInfo.getId());
        return SysResult.build(200, "更新上班时间完成");
    }


    @Override
    public SysResult countDayOffHour(Date date, String uid) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 取得整合后的放假记录
        ArrayList<MergedDayOffRecord> allMergedRecords = applicationMapper.getMergedDayOffRecord(uid, dateFormat.format(date));
        // 计算整合总时长
        int allRecordsNum = allMergedRecords.size();
        double totalDayOffHour = 0;
        for (int i = 0; i < allRecordsNum; i++) {
            MergedDayOffRecord tempRecord = allMergedRecords.get(i);
            String[] tempStartTime = tempRecord.getStartTime().split(":");
            int tempStartHour = Integer.valueOf(tempStartTime[0]);
            int tempStartMinute = Integer.valueOf(tempStartTime[1]);

            String[] tempEndTime = tempRecord.getEndTime().split(":");
            int tempEndHour = Integer.valueOf(tempEndTime[0]);
            int tempEndMinute = Integer.valueOf(tempEndTime[1]);

            totalDayOffHour = totalDayOffHour + (tempEndHour + tempEndMinute / 60.0 - tempStartHour - tempStartMinute / 60.0);
        }

        // 取得节假日记录
        ArrayList<DayOffRecord> holidayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "Holiday");
        // 取得调休记录
        ArrayList<DayOffRecord> changeDayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "ChangeDayOff");
        // 取得年假记录
        ArrayList<DayOffRecord> AnnualDayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "AnnualDayOff");
        // 取得午休记录
        ArrayList<DayOffRecord> NoonBreakRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "NoonBreak");
        // 取得事假记录
        ArrayList<DayOffRecord> NormalDayOffRecords = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(date), "DayOff");

        // 记录各种放假时间
        HashMap<String, Double> dayOffHours = new HashMap<>();
        ArrayList<DayOffRecord> holidayRecords = new ArrayList<>();
        String offName = "";

        for (int offNum = 0; offNum < 5; offNum++) {
            // 规定优先级
            if (offNum == 0) {
                offName = "Holiday";
                holidayRecords = holidayOffRecords;
            } else if (offNum == 2) {
                offName = "ChangeDayOff";
                holidayRecords = changeDayOffRecords;
            } else if (offNum == 3) {
                offName = "AnnualDayOff";
                holidayRecords = AnnualDayOffRecords;
            } else if (offNum == 1) {
                offName = "NoonBreak";
                holidayRecords = NoonBreakRecords;
            }else if (offNum == 4) {
                offName = "DayOff";
                holidayRecords = NormalDayOffRecords;
            }

            // 将节假日记录从整合记录中剔除
            int holidayNum = holidayRecords.size();
            if (holidayNum != 0) {
                // 有节假日记录
                for (int i = 0; i < holidayNum; i++) {
                    DayOffRecord tempDayOff = holidayRecords.get(i);
                    String[] tempStartTime = tempDayOff.getOffStart().split(":");
                    int tempStartHour = Integer.valueOf(tempStartTime[0]);
                    int tempStartMinute = Integer.valueOf(tempStartTime[1]);
                    double holidayStartTime = tempStartHour + tempStartMinute / 60.0;

                    String[] tempEndTime = tempDayOff.getOffEnd().split(":");
                    int tempEndHour = Integer.valueOf(tempEndTime[0]);
                    int tempEndMinute = Integer.valueOf(tempEndTime[1]);
                    double holidayEndTime = tempEndHour + tempEndMinute / 60.0;
                    int currentAt = 0;

                    while (currentAt != allMergedRecords.size()) {
                        String[] mergedStartTime = allMergedRecords.get(currentAt).getStartTime().split(":");
                        int mergedStartHour = Integer.valueOf(mergedStartTime[0]);
                        int mergedStartMinute = Integer.valueOf(mergedStartTime[1]);
                        double StartTime = mergedStartHour + mergedStartMinute / 60.0;

                        String[] mergedEndTime = allMergedRecords.get(currentAt).getEndTime().split(":");
                        int mergedEndHour = Integer.valueOf(mergedEndTime[0]);
                        int mergedEndMinute = Integer.valueOf(mergedEndTime[1]);
                        double EndTime = mergedEndHour + mergedEndMinute / 60.0;

                        // merge 两头都小于等于 temp, 直接去掉merge
                        if (StartTime >= holidayStartTime && EndTime <= holidayEndTime) {
                            allMergedRecords.remove(currentAt);
                            currentAt = 0;
                            continue;
                        }

                        // merge 两头都大于 temp, 原有merge被截成两段
                        if (StartTime < holidayStartTime && EndTime > holidayEndTime) {
                            allMergedRecords.remove(currentAt);

                            MergedDayOffRecord pieceOne = new MergedDayOffRecord();
                            pieceOne.setStartTime(mergedStartHour + ":" + mergedStartMinute);
                            pieceOne.setEndTime(tempStartHour + ":" + tempStartMinute);
                            allMergedRecords.add(pieceOne);

                            MergedDayOffRecord pieceTwo = new MergedDayOffRecord();
                            pieceTwo.setStartTime(tempEndHour + ":" + tempEndMinute);
                            pieceTwo.setEndTime(mergedEndHour + ":" + mergedEndMinute);
                            allMergedRecords.add(pieceTwo);
                            currentAt = 0;
                            continue;
                        }

                        // merge 开始的早结束的也早, merge仅保留上半段
                        if (StartTime < holidayStartTime && EndTime <= holidayEndTime && EndTime > holidayStartTime) {
                            allMergedRecords.remove(currentAt);

                            MergedDayOffRecord pieceOne = new MergedDayOffRecord();
                            pieceOne.setStartTime(mergedStartHour + ":" + mergedStartMinute);
                            pieceOne.setEndTime(tempStartHour + ":" + tempStartMinute);
                            allMergedRecords.add(pieceOne);

                            currentAt = 0;
                            continue;
                        }

                        // merge 开始的晚结束的也晚, merge仅保留下半段
                        if (StartTime >= holidayStartTime && EndTime > holidayEndTime && StartTime < holidayEndTime) {
                            allMergedRecords.remove(currentAt);

                            MergedDayOffRecord pieceTwo = new MergedDayOffRecord();
                            pieceTwo.setStartTime(tempEndHour + ":" + tempEndMinute);
                            pieceTwo.setEndTime(mergedEndHour + ":" + mergedEndMinute);
                            allMergedRecords.add(pieceTwo);

                            currentAt = 0;
                            continue;
                        }
                        currentAt++;
                    }
                }
                // 去掉所有的目标节日记录了, 通过两次merge的差值, 计算假日的时间
                double tempHour = 0;
                int currentRecordsNum = allMergedRecords.size();
                for (int i = 0; i < currentRecordsNum; i++) {
                    MergedDayOffRecord tempRecord = allMergedRecords.get(i);
                    String[] tempStartTime = tempRecord.getStartTime().split(":");
                    int tempStartHour = Integer.valueOf(tempStartTime[0]);
                    int tempStartMinute = Integer.valueOf(tempStartTime[1]);

                    String[] tempEndTime = tempRecord.getEndTime().split(":");
                    int tempEndHour = Integer.valueOf(tempEndTime[0]);
                    int tempEndMinute = Integer.valueOf(tempEndTime[1]);

                    tempHour = tempHour + (tempEndHour + tempEndMinute / 60.0 - tempStartHour - tempStartMinute / 60.0);
                }
                double subHour = totalDayOffHour - tempHour;
                totalDayOffHour = tempHour;
                dayOffHours.put(offName, subHour);
            } else {
                dayOffHours.put(offName, 0.0);
            }
        }
        return SysResult.build(200, "分级计算假期时间成功", dayOffHours);
    }

    @Override
    public SysResult mergeDayOffRecord(Date date, String uid) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 取出当天所有假期记录
        ArrayList<DayOffRecord> records = clockInMapper.getDayOffRecord(uid, dateFormat.format(date));

        if (records.size() == 0) {
            return SysResult.build(200, "没有需要整合的假期记录");
        } else {
            // 有记录，先清空原有的整合
            applicationMapper.deleteMergedRecord(uid, dateFormat.format(date));
            applicationMapper.deleteBoundedRecord(uid, dateFormat.format(date));
        }

        int currentNum = 1;
        int earliestId = records.get(0).getId();
        String[] earliestStartTime = records.get(0).getOffStart().split(":");
        int earliestStartHour = Integer.valueOf(earliestStartTime[0]);
        int earliestStartMinute = Integer.valueOf(earliestStartTime[1]);
        // 循环寻找开始时间最小的的记录
        while (records.size() != 0) {
            if (currentNum == records.size()) {
                // 循环完一遍list了, 找到了当前list中最小的开始时间
                currentNum = 1;
                // 取出最小开始时间的结束时间
                int currentLength = records.size();
                int earliestEndHour = 0, earliestEndMinute = 0;
                for (int i = 0; i < currentLength; i++) {
                    if (records.get(i).getId().equals(earliestId)) {
                        earliestEndHour = Integer.valueOf(records.get(i).getOffEnd().split(":")[0]);
                        earliestEndMinute = Integer.valueOf(records.get(i).getOffEnd().split(":")[1]);
                        // 将最小开始时间取出
                        records.remove(i);
                        break;
                    }
                }

                int currentMerge = 0;
                // 开始将其他时间向最小时间整合
                while (currentMerge != records.size()) {
                    String[] tempStartTime = records.get(currentMerge).getOffStart().split(":");
                    int tempStartHour = Integer.valueOf(tempStartTime[0]);
                    int tempStartMinute = Integer.valueOf(tempStartTime[1]);
                    if (tempStartHour < earliestEndHour || (tempStartHour == earliestEndHour && tempStartMinute <= earliestEndMinute)) {
                        // 代表该时间段可整合, 判断结束时间来决定是否有整合价值
                        String[] tempEndTime = records.get(currentMerge).getOffEnd().split(":");
                        int tempEndHour = Integer.valueOf(tempEndTime[0]);
                        int tempEndMinute = Integer.valueOf(tempEndTime[1]);
                        if (tempEndHour > earliestEndHour || (tempEndHour == earliestEndHour && tempEndMinute > earliestEndMinute)) {
                            earliestEndHour = tempEndHour;
                            earliestEndMinute = tempEndMinute;
                            // 取出已整合的片段
                            records.remove(currentMerge);
                            currentMerge = 0;
                        } else {
                            // 开始时间确实比整合小，但是结束的也比整合早，没有整合价值
                            records.remove(currentMerge);
                            currentMerge = 0;
                        }
                    } else {
                        // 开始时间在整合时间段外，跳到下一个
                        currentMerge++;
                    }
                }
                // 一个时间段已经全部都整合完毕了，添加进整合表然后进入下一个整合时间段
                MergedDayOffRecord mergedDayOffRecord = new MergedDayOffRecord();
                mergedDayOffRecord.setUid(uid);
                mergedDayOffRecord.setOffDate(date);
                mergedDayOffRecord.setStartTime(earliestStartHour + ":" + earliestStartMinute);
                mergedDayOffRecord.setEndTime(earliestEndHour + ":" + earliestEndMinute);
                applicationMapper.addMergedDayOffRecord(mergedDayOffRecord);

                if (records.size() != 0) {
                    earliestStartTime = records.get(0).getOffStart().split(":");
                    earliestStartHour = Integer.valueOf(earliestStartTime[0]);
                    earliestStartMinute = Integer.valueOf(earliestStartTime[1]);
                    earliestId = records.get(0).getId();
                } else {
                    // 放假记录已被全部整合
                    break;
                }

            } else {
                String[] tempStartTime = records.get(currentNum).getOffStart().split(":");
                int tempStartHour = Integer.valueOf(tempStartTime[0]);
                int tempStartMinute = Integer.valueOf(tempStartTime[1]);
                if (tempStartHour < earliestStartHour || (tempStartHour == earliestStartHour && tempStartMinute < earliestStartMinute)) {
                    // 新的开始时间更小
                    earliestStartHour = tempStartHour;
                    earliestStartMinute = tempStartMinute;
                    earliestId = records.get(currentNum).getId();
                }
                currentNum++;
            }
        }
        // 整合完毕，开始寻找上下限
        ArrayList<MergedDayOffRecord> mergedRecords = applicationMapper.getMergedDayOffRecord(uid, dateFormat.format(date));
        if (mergedRecords.size() == 0) {
            return SysResult.build(200, "没有需要取上下限的假期记录");
        } else {
            String[] startBoundTime = mergedRecords.get(0).getStartTime().split(":");
            int startBoundHour = Integer.valueOf(startBoundTime[0]);
            int startBoundMinute = Integer.valueOf(startBoundTime[1]);
            String startBoundId = mergedRecords.get(0).getId() + "";

            String[] endBoundTime = mergedRecords.get(0).getEndTime().split(":");
            int endBoundHour = Integer.valueOf(endBoundTime[0]);
            int endBoundMinute = Integer.valueOf(endBoundTime[1]);
            String endBoundId = mergedRecords.get(0).getId() + "";

            int mergedNum = mergedRecords.size();
            for (int i = 1; i < mergedNum; i++) {
                String[] tempStartBoundTime = mergedRecords.get(i).getStartTime().split(":");
                int tempStartBoundHour = Integer.valueOf(tempStartBoundTime[0]);
                int tempStartBoundMinute = Integer.valueOf(tempStartBoundTime[1]);

                String[] tempEndBoundTime = mergedRecords.get(i).getEndTime().split(":");
                int tempEndBoundHour = Integer.valueOf(tempEndBoundTime[0]);
                int tempEndBoundMinute = Integer.valueOf(tempEndBoundTime[1]);

                if (tempStartBoundHour < startBoundHour || (tempStartBoundHour == startBoundHour && tempStartBoundMinute < startBoundMinute)) {
                    startBoundHour = tempStartBoundHour;
                    startBoundMinute = tempStartBoundMinute;
                    startBoundId = mergedRecords.get(i).getId() + "";
                }
                if (tempEndBoundHour > endBoundHour || (tempEndBoundHour == endBoundHour && tempEndBoundMinute > endBoundMinute)) {
                    endBoundHour = tempEndBoundHour;
                    endBoundMinute = tempEndBoundMinute;
                    endBoundId = mergedRecords.get(i).getId() + "";
                }
            }
            // 添加上下限记录
            BoundedDayOffRecord boundedDayOffRecord = new BoundedDayOffRecord();
            boundedDayOffRecord.setUid(uid);
            boundedDayOffRecord.setOffDate(date);
            boundedDayOffRecord.setStartBoundId(startBoundId);
            boundedDayOffRecord.setEndBoundId(endBoundId);
            boundedDayOffRecord.setStartBound(startBoundHour + ":" + startBoundMinute);
            boundedDayOffRecord.setEndBound(endBoundHour + ":" + endBoundMinute);
            applicationMapper.addBoundedDayOffRecord(boundedDayOffRecord);
        }
        return SysResult.build(200, "整合完毕");
    }


    @Override
    public SysResult addExtraWorkRecord(String uid, String applicationId, String date, String workStart, String workEnd, String type) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date workDate;
        try {
            workDate = dateFormat.parse(date);
        } catch (ParseException e) {
            return SysResult.build(201, "获取加班日期出错");
        }
        String[] workStartTime = workStart.split(":");
        int workStartHour = Integer.valueOf(workStartTime[0]);
        int workStartMinute = Integer.valueOf(workStartTime[1]);
        String[] workEndTime = workEnd.split(":");
        int workEndHour = Integer.valueOf(workEndTime[0]);
        int workEndMinute = Integer.valueOf(workEndTime[1]);

        int workHour = (workEndHour + workEndMinute / 60) - (workStartHour + workStartMinute / 60);

        ExtraWorkRecord extraWorkRecord = new ExtraWorkRecord();
        extraWorkRecord.setUid(uid);
        extraWorkRecord.setApplicationId(applicationId);
        extraWorkRecord.setDate(workDate);
        extraWorkRecord.setExtraWorkStart(workStart);
        extraWorkRecord.setExtraWorkEnd(workEnd);
        extraWorkRecord.setExtraWorkHour(workHour + "");
        extraWorkRecord.setType(type);

        applicationMapper.addExtraWorkRecord(extraWorkRecord);
        // 根据加班单类型，增加调休时间
        if (type.equals("ChangeDayOff")) {
            // 加班类型为调休
            // 获取调休时间
            ArrayList<Object> changeResult = generalService.quickGet("change_day_off", "workers", "uid", uid);
            if (changeResult.size() != 0) {
                String changeTime = changeResult.get(0).toString();
                int changeHour = Integer.valueOf(changeTime);
                changeHour = changeHour + workHour;
                // 更新调休时间
                applicationMapper.updateChangeDayOffType("change_day_off", changeHour + "", uid);
            } else {
                return SysResult.build(201, "加班记录添加成功，调休时间更新失败");
            }
        }
        return SysResult.build(200, "加班单添加成功");
    }


    @Override
    public SysResult addApplyRecord(String uid, String applicationType, String applicationDetail,
                                    String applicationDescription) {

        ArrayList<Object> managerList = generalService.quickGet("manager", "workers",
                "uid", uid);
        ArrayList<Object> reviewerList = generalService.quickGet("reviewers", "workers",
                "uid", uid);
        if (managerList.size() == 0 || reviewerList.size() == 0) {
            return SysResult.build(201, "uid 不存在");
        }
        String manager = managerList.get(0).toString();
        String reviewers = reviewerList.get(0).toString();
        if (manager.equals("unknown") || reviewers.equals("unknown")) {
            return SysResult.build(201, "职员缺少基础信息，无法提交申请");
        }

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        String applicationId = "" + year + month + day + hour + minute + second + uid;


        ApplicationRecord applicationRecord = new ApplicationRecord();
        applicationRecord.setApplicationId(applicationId);
        applicationRecord.setUid(uid);
        applicationRecord.setApplicationType(applicationType);
        applicationRecord.setApplicationDetail(applicationDetail);
        applicationRecord.setApplicationDescription(applicationDescription);
        applicationRecord.setManager(manager);
        applicationRecord.setReviewers(reviewers);
        applicationRecord.setStatus("0"); // 状态默认为0， 表示未审核状态

        // 添加申请记录之前先添加审批队列
        // 获取目标员工的审批队列
        int reviewerNum = reviewers.split(",").length;
        String reviewList = "";
        for (int i = 0; i < reviewerNum; i++) {
            if (i == reviewerNum - 1) {
                reviewList = reviewList + "0";
            } else {
                reviewList = reviewList + "0,";
            }
        }
        ApplicationReviewList applicationReviewList = new ApplicationReviewList();
        applicationReviewList.setApplicationId(applicationId);
        applicationReviewList.setUid(uid);
        applicationReviewList.setReviewList(reviewList);
        applicationReviewList.setStatus("0");
        applicationReviewList.setReviewers(reviewers);
        applicationMapper.addReviewList(applicationReviewList);

        try {
            applicationMapper.addApplicationRecord(applicationRecord);
            return SysResult.build(200, "插入申请记录成功");
        } catch (Exception e) {
            return SysResult.build(201, "插入申请记录出错   " + e);
        }
    }

    @Override
    // 删除申请(如果是未批准的申请则可以直接删除，如果是已批准或是正在审批的申请则变为提交一个新的删除申请）
    public SysResult deleteApplyRecord(String applicationId, String uid, String applicationDescription) {
        // 获取申请
        ApplicationRecord applicationRecord = applicationMapper.getApplicationRecord(applicationId);
        if (applicationRecord == null) {
            return SysResult.build(400, "要删除的申请记录不存在");
        }
        // 判断申请种类，有些申请无法撤销
        if (applicationRecord.getApplicationType().equals("MakeUp")) {
            return SysResult.build(201, "补卡申请无法撤销");
        }
        // 判断申请状态
        if (applicationRecord.getStatus().equals("0")) {
            // 0 代表未审核状态，可以自由撤销
            // 先软删除审批队列, 把状态设置为3
            applicationMapper.deleteReviewList(applicationId);
            // 再软删除申请, 把状态设置为3
            applicationMapper.deleteApplicationRecord(applicationId);
            return SysResult.build(200, "撤销成功");
        } else {
            // 已经审核过了，撤销需要提交新的申请
            if (applicationRecord.getApplicationType().equals("RollBack")) {
                return SysResult.build(200, "无法为撤销申请添加撤销申请");
            }
            addApplyRecord(uid, "RollBack",
                    applicationId + "," + applicationRecord.getApplicationType(), applicationDescription);
            return SysResult.build(200, "提交撤销申请成功");
        }
    }

    @Override
    // 修改申请(如果是未批准的可以直接修改，如果批准了就不允许修改)
    public SysResult changeApplyRecord(String applicationId, String applicationDetail, String applicationDescription) {
        // 获取申请
        ApplicationRecord applicationRecord = applicationMapper.getApplicationRecord(applicationId);
        if (applicationRecord == null) {
            return SysResult.build(400, "申请记录不存在");
        }
        // 判断申请状态
        if (applicationRecord.getStatus().equals("0")) {
            // 0 代表未审核状态，可以自由修改
            applicationMapper.changeApplicationRecord(applicationId, applicationDetail, applicationDescription);
            return SysResult.build(200, "修改成功");
        } else {
            // 已经审核过了，无法修改，只能申请撤销
            return SysResult.build(200, "申请已被审核，无法修改");
        }
    }

    @Override
    // 查看申请
    public SysResult getApplyRecord(String cond, String pageNum, String pageMax) {
        SysResult recordResult = generalService.getRow2("application_id,uid,application_type,application_detail,application_description,manager,status,created,updated,reviewers",
                "application_record", cond, "0", "AND", Integer.valueOf(pageNum), Integer.valueOf(pageMax));

        if (!recordResult.isOk()) {
            return SysResult.build(201, "获取记录失败 ：  " + recordResult.getMsg());
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<LinkedHashMap<String, Object>> unChangedRecord = (ArrayList<LinkedHashMap<String, Object>>) recordResult.getData();
        int length = unChangedRecord.size();
        for (int i = 0; i < length; i++) {
            String created = dateFormat.format(unChangedRecord.get(i).get("created"));
            String updated = dateFormat.format(unChangedRecord.get(i).get("updated"));
            unChangedRecord.get(i).put("created", created);
            unChangedRecord.get(i).put("updated", updated);
        }
        return SysResult.build(200, "获取记录成功", unChangedRecord);
    }


    // targetStatus 0 代表未审核, 1 代表同意, 2 代表不同意
    @Override
    public SysResult reviewApplication(String reviewerName, String applicationId, String targetStatus) {
        //获取原申请，判断状态
        ApplicationRecord applicationRecord = applicationMapper.getApplicationRecord(applicationId);
        if (applicationRecord.getStatus().equals("3")) {
            // 已被撤销的申请,无法审批
            return SysResult.build(201, "申请已被撤销, 无法审批");
        }
        // 获取申请队列
        ApplicationReviewList applicationReviewList = applicationMapper.getReviewList(applicationId);
        if (applicationReviewList == null) {
            return SysResult.build(201, "获取审批队列失败");
        }
        // 获取成功
        String reviewers = applicationReviewList.getReviewers();
        String reviewList = applicationReviewList.getReviewList();
        String[] reviewerList = reviewers.split(",");
        String[] reviewResultList = reviewList.split(",");

        int reviewNum = reviewerList.length;
        for (int i = 0; i < reviewNum; i++) {
            if (reviewerList[i].equals(reviewerName)) {
                // 找到了审批人对应的位置, 开始检查审批人的审批状态
                if (reviewResultList[i].equals("0")) {
                    // 未审批, 将状态改为审批目标状态
                    reviewResultList[i] = targetStatus;
                } else {
                    return SysResult.build(201, "已经审批过了, 不能出尔反尔");
                }
            }
        }
        // 检查是否审批队列已全部完成
        String status = "1";
        for (int i = 0; i < reviewNum; i++) {
            if (reviewResultList[i].equals("0")) {
                // 代表还有人没审批, 审批记录状态应该为 4 代表审批中
                applicationMapper.changeApplicationStatus(applicationId, "4");
                break;
            }
            if (i == reviewNum - 1) {
                // 所有人都审批过了, 1个0也没有了, 审批队列完成
                status = "2"; // 完成状态是2,  1代表进行中, 3代表未开始
                // 开始判断审批结果, 默认审批通过
                String reviewFinalResult = "1";
                for (int j = 0; j < reviewNum; j++) {
                    if (reviewResultList[j].equals("2")) {
                        // 有人拒绝, 审批没有通过
                        reviewFinalResult = "2";
                        break;
                    }
                }
                // 触发审批结果
                SysResult permitResult = applicationPermit(applicationId, reviewFinalResult);
                if (!permitResult.isOk()) {
                    return SysResult.build(201, "审批处理异常：  " + permitResult.getMsg());
                }
            }
        }

        // 审批完毕后更新审批队列
        String newReviewResult = "";
        for (int i = 0; i < reviewNum; i++) {
            if (i == reviewNum - 1) {
                newReviewResult = newReviewResult + reviewResultList[i];
            } else {
                newReviewResult = newReviewResult + reviewResultList[i] + ",";
            }
        }
        applicationMapper.updateReviewList(applicationId, newReviewResult, status);
        return SysResult.build(200, "审批成功");
    }


    @Override
    // 审批申请(这里面会有N多联动项)  status -1 代表全部申请 0 代表待审批, 1 代表通过, 2 代表没通过, 3 代表撤销, 4 代表审批中
    public SysResult applicationPermit(String applicationId, String targetStatus) {
        // 获取申请
        ApplicationRecord applicationRecord = applicationMapper.getApplicationRecord(applicationId);
        if (applicationRecord == null) {
            return SysResult.build(400, "申请记录不存在");
        }
        if (!applicationRecord.getStatus().equals("4") && !applicationRecord.getStatus().equals("0")) {
            return SysResult.build(400, "不是正在进行的审批");
        }

        // 申请被拒绝, 直接修改申请状态
        if (targetStatus.equals("2")) {
            applicationMapper.changeApplicationStatus(applicationId, targetStatus);
            return SysResult.build(200, "审批成功，已拒绝申请");
        }

        // 申请通过
        // 根据申请类别, 有不同的联动效果
        if (applicationRecord.getApplicationType().equals("DayOff")) {
            /*
            *  请假记录/放假记录 对应表格 day_off_record,  对应Type DayOff
            *   applicationDetail 格式：String offStartDate, String offEndDate, String offStartTime, String offEndTime,
                            String name,String offType

             */
            String[] applicationDetails = applicationRecord.getApplicationDetail().split(",");
            if (applicationDetails.length != 6) {
                return SysResult.build(201, "申请记录缺失细节");
            }
            // 请假申请, 同意后需要添加放假记录并重新计算打卡日期
            SysResult addResult = addDayOffRecord(applicationDetails[0], applicationDetails[1], applicationDetails[2],
                    applicationDetails[3], applicationRecord.getUid(), applicationDetails[4], applicationDetails[5],
                    applicationRecord.getApplicationDescription());
            if (addResult.isOk()) {
                // 添加成功，开始重新计算打卡记录
                try {
                    ArrayList<Date> dates = (ArrayList<Date>) addResult.getData();
                    recalculateClockInRecord(dates, applicationRecord.getUid());
                    applicationMapper.changeApplicationStatus(applicationId, targetStatus);
                    return SysResult.build(200, "审批申请成功, 假期已成功添加");
                } catch (Exception e) {
                    return SysResult.build(201, "重新计算打卡记录出错   " + e);
                }
            } else {
                return SysResult.build(201, "添加放假记录失败");
            }
        } else if (applicationRecord.getApplicationType().equals("RollBack")) {
            /*
             *  撤销记录 对应表格 application_record, 对应Type RollBack
             *   applicationDetail 格式：String applicationId, String applicationType

             */
            String[] applicationDetails = applicationRecord.getApplicationDetail().split(",");
            if (applicationDetails.length != 2) {
                return SysResult.build(201, "申请记录缺失细节");
            }

            // 需要删除原有记录并撤销原有记录做出的修改
            // 获取原有记录
            ApplicationRecord oldApplication = applicationMapper.getApplicationRecord(applicationDetails[0]);
            if (!oldApplication.getStatus().equals("1")) {
                // 原有记录是一条不是一条通过的记录，仅需要软删除即可
                applicationMapper.deleteApplicationRecord(applicationDetails[0]);
                applicationMapper.changeApplicationStatus(applicationId, targetStatus);
                return SysResult.build(200, "撤销被拒绝的记录成功");
            } else {
                // 原有记录是一条被通过的记录，做出了对应的修改，需要先撤销修改再删除
                // 原有记录是一条申请假期的记录
                if (oldApplication.getApplicationType().equals("DayOff")) {

                    String[] oldApplicationDetail = oldApplication.getApplicationDetail().split(",");

                    // 获取受影响的日期的列表, 重新计算打卡信息
                    ArrayList<Date> dates = new ArrayList<>();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate, endDate;
                    try {
                        startDate = dateFormat.parse(oldApplicationDetail[0]);
                        endDate = dateFormat.parse(oldApplicationDetail[1]);
                    } catch (ParseException e) {
                        return SysResult.build(201, "转化日期出错");
                    }
                    String uid = applicationRecord.getUid();
                    while (!startDate.equals(endDate)) {
                        // 获取更新前的假期时长
                        SysResult offHourResult = countDayOffHour(startDate, uid);
                        if (!offHourResult.isOk()) {
                            return SysResult.build(201, "获取更新前假期时间失败");
                        }
                        HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
                        // 将之前的时长归还
                        updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
                        updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);

                        // 删除假期
                        applicationMapper.deleteDayOffRecordByDate(dateFormat.format(startDate), dateFormat.format(startDate), oldApplicationDetail[5]);
                        // 整合记录
                        mergeDayOffRecord(startDate, uid);

                        // 更新假期时长
                        SysResult offHourResultAfterUpdate = countDayOffHour(startDate, uid);
                        if (!offHourResultAfterUpdate.isOk()) {
                            return SysResult.build(201, "获取更新后假期时间失败");
                        }
                        HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
                        // 将时长扣除
                        updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
                        updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);

                        dates.add(startDate);
                        startDate = new Date(startDate.getTime() + 1000 * 60 * 60 * 24);
                    }
                    // 单独对endDate 进行更新
                    // 获取更新前的假期时长
                    SysResult offHourResult = countDayOffHour(endDate, uid);
                    if (!offHourResult.isOk()) {
                        return SysResult.build(201, "获取更新前假期时间失败");
                    }
                    HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
                    // 将之前的时长归还
                    updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
                    updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);

                    // 删除假期
                    applicationMapper.deleteDayOffRecordByDate(dateFormat.format(endDate), dateFormat.format(endDate), oldApplicationDetail[5]);
                    // 整合记录
                    mergeDayOffRecord(endDate, uid);

                    // 更新假期时长
                    SysResult offHourResultAfterUpdate = countDayOffHour(endDate, uid);
                    if (!offHourResultAfterUpdate.isOk()) {
                        return SysResult.build(201, "获取更新后假期时间失败");
                    }
                    HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
                    // 将时长扣除
                    updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
                    updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);

                    dates.add(endDate);
                    recalculateClockInRecord(dates, oldApplication.getUid());
                    applicationMapper.deleteApplicationRecord(applicationDetails[0]);
                    applicationMapper.changeApplicationStatus(applicationId, targetStatus);
                    return SysResult.build(200, "撤回请假记录成功");
                } else if (oldApplication.getApplicationType().equals("ExtraWork")) {
                    // 撤回加班单的收益
                    ApplicationRecord oldExtraWork = applicationMapper.getApplicationRecord(applicationDetails[0]);
                    String[] oldDetails = oldExtraWork.getApplicationDetail().split(",");
                    Double hourBack = toolService.getTimeSub(oldDetails[1], oldDetails[2]);
                    if (oldDetails[3].equals("ChangeDayOff")) {
                        updateOffHour("change_day_off", hourBack * (-1), applicationRecord.getUid());
                    } else if (oldDetails[3].equals("AnnualDayOff")) {
                        updateOffHour("annual_day_off", hourBack * (-1), applicationRecord.getUid());
                    }
                    // 删除加班记录, 然后软删除申请加班的记录
                    try {
                        applicationMapper.deleteExtraWorkRecord(applicationDetails[0]);
                        applicationMapper.deleteApplicationRecord(applicationDetails[0]);
                        applicationMapper.changeApplicationStatus(applicationId, targetStatus);
                        return SysResult.build(200, "撤销加班单申请成功");
                    } catch (Exception e) {
                        return SysResult.build(201, "撤销加班申请出错   " + e);
                    }
                }


            }
        } else if (applicationRecord.getApplicationType().equals("MakeUp")) {
            /*
             *  补卡记录 对应表格 clock_in_???_???? 对应Type MakeUp
             *   applicationDetail 格式：String start, String makeUpType

             */
            String[] applicationDetails = applicationRecord.getApplicationDetail().split(",");
            if (applicationDetails.length != 2) {
                return SysResult.build(201, "申请记录缺失细节");
            }

            // 需要计算补卡的对应日期，激活补卡，重新计算上班时长
            String start = applicationDetails[0];
            String end;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                end = dateFormat.format(new Date(dateFormat.parse(start).getTime() + 1000 * 60 * 60 * 24));
            } catch (ParseException e) {
                return SysResult.build(201, "计算补卡日期出错   " + e);
            }
            // 激活补卡
            SysResult makeUpResult = clockInService.makeUpClockInRecord(applicationRecord.getUid(), start, end, applicationDetails[1]);
            if (makeUpResult.isOk()) {
                // 补卡成功，开始重算时间
                try {
                    recalculateWorkTime(dateFormat.parse(start), applicationRecord.getUid());
                    applicationMapper.changeApplicationStatus(applicationId, targetStatus);
                    return SysResult.build(200, "补卡成功，上班时长已重新计算");
                } catch (ParseException e) {
                    return SysResult.build(201, "补卡成功，上班时长计算出错   " + e);
                }
            } else {
                return SysResult.build(201, "补卡出错： " + makeUpResult.getMsg());
            }
        } else if (applicationRecord.getApplicationType().equals("ExtraWork")) {
            /*
             *  加班记录 对应表格 extra_work_record 对应Type ExtraWork
             *   applicationDetail 格式：String date, String workStart, String workEnd, String type
             * */
            String[] applicationDetails = applicationRecord.getApplicationDetail().split(",");
            if (applicationDetails.length != 4) {
                return SysResult.build(201, "申请记录缺失细节");
            }
            // 取得申请记录, 添加加班记录
            SysResult addExtraWorkResult = addExtraWorkRecord(applicationRecord.getUid(), applicationId, applicationDetails[0],
                    applicationDetails[1], applicationDetails[2], applicationDetails[3]);
            if (addExtraWorkResult.isOk()) {
                applicationMapper.changeApplicationStatus(applicationId, targetStatus);
                return SysResult.build(200, "加班记录生成成功");
            } else {
                return SysResult.build(201, "获取申请信息成功, 生成加班单失败");
            }
        }


        return SysResult.build(201, "无法识别申请类型");
    }

    @Override
    public SysResult countMyApplication(String cond) {
        return generalService.getRow2("count(0)",
                "application_record", cond, "0", "AND", 1, 100000);
    }

    @Override
    public SysResult getReviewApplyRecord(String cond, String pageNum, String pageMax) {
        SysResult recordResult = generalService.getRow2("application_id,uid,application_type,application_detail,application_description,manager,status,created,updated,reviewers",
                "application_record", cond, "2", "AND", Integer.valueOf(pageNum), Integer.valueOf(pageMax));

        if (!recordResult.isOk()) {
            return SysResult.build(201, "获取记录失败 ：  " + recordResult.getMsg());
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<LinkedHashMap<String, Object>> unChangedRecord = (ArrayList<LinkedHashMap<String, Object>>) recordResult.getData();
        int length = unChangedRecord.size();
        for (int i = 0; i < length; i++) {
            String created = dateFormat.format(unChangedRecord.get(i).get("created"));
            String updated = dateFormat.format(unChangedRecord.get(i).get("updated"));
            unChangedRecord.get(i).put("created", created);
            unChangedRecord.get(i).put("updated", updated);
            // 获取申请人姓名
            ArrayList<Object> nameList = generalService.quickGet("name", "workers", "uid", unChangedRecord.get(i).get("uid").toString());
            if (nameList.size() != 1) {
                return SysResult.build(201, "获取记录失败, 员工uid重复");
            }
            unChangedRecord.get(i).put("name", nameList.get(0));
        }
        return SysResult.build(200, "获取记录成功", unChangedRecord);
    }

    @Override
    public SysResult reviewerCountApplication(String cond) {
        return generalService.getRow2("count(0)",
                "application_record", cond, "2", "AND", 1, 100000);
    }
}
