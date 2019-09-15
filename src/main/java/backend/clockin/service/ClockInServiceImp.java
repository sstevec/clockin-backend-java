package backend.clockin.service;

import backend.clockin.mapper.ApplicationMapper;
import backend.clockin.mapper.ClockInMapper;
import backend.clockin.pojo.clockIn.ClockIn;
import backend.clockin.pojo.clockIn.ClockInLog;
import backend.clockin.pojo.clockIn.ConfirmInfo;
import backend.clockin.pojo.dayOff.BoundedDayOffRecord;
import backend.clockin.pojo.dayOff.DayOffRecord;
import backend.clockin.pojo.dayOff.MergedDayOffRecord;
import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
public class ClockInServiceImp implements ClockInService {

    @Autowired
    GeneralService generalService;

    @Autowired
    ClockInMapper clockInMapper;

    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    ApplyService applyService;

    @Autowired
    ToolService toolService;

    // 调用时创建新的打卡表，并且更新打卡表日志，将新表设置成当前表, 0:30执行
    public SysResult creatClockInForm(String month, String year) {
        // 设置月份
        String monthString = "";
        if (month.equals("1")) {
            monthString = "jan";
        } else {
            monthString = "jul";
        }

        String formName = "clock_in_" + monthString + "_" + year;
        // 创建表格
        SysResult createForm = generalService.createForm(formName, "Clock in form for " + monthString + " " + year);
        //表格创建成功
        if (createForm.isOk()) {
            // 添加字段
            try {
                generalService.changeColumn(formName, "column1", "id",
                        "int,10,NO,primary key unsigned auto_increment");
                generalService.addColumn(formName, "uid", "varchar,255,NO", "id");
                generalService.addColumn(formName, "account", "varchar,255,NO", "uid");
                generalService.addColumn(formName, "name", "varchar,255,NO", "account");
                generalService.addColumn(formName, "start_work", "varchar,255,NO", "name");
                generalService.addColumn(formName, "end_work", "varchar,255,NO", "start_work");
                generalService.addColumn(formName, "start_extra_work", "varchar,255,NO", "end_work");
                generalService.addColumn(formName, "end_extra_work", "varchar,255,NO", "start_extra_work");
                generalService.addColumn(formName, "clock_in", "varchar,255,YES", "end_extra_work");
                generalService.addColumn(formName, "clock_in_status", "varchar,255,YES, null default 'miss'", "clock_in");
                generalService.addColumn(formName, "clock_out", "varchar,255,YES", "clock_in_status");
                generalService.addColumn(formName, "clock_out_status", "varchar,255,YES, null default 'miss'", "clock_out");
                generalService.addColumn(formName, "work_hour", "varchar,255,YES", "clock_out_status");
                generalService.addColumn(formName, "extra_work_hour", "varchar,255,YES", "work_hour");
                generalService.addColumn(formName, "status", "varchar,255,NO", "extra_work_hour");
                generalService.addColumn(formName, "created", "timestamp,0,YES, null default current_timestamp", "status");
            } catch (Exception e) {
                return SysResult.build(201, "Error when adding columns to clock in form");
            }
        } else {
            return SysResult.build(201, "Error when creating new clock in form");
        }

        // 更新打卡表日志
        clockInMapper.updateClockInLog();

        // 添加新的打卡日志
        ClockInLog tempLog = new ClockInLog();
        tempLog.setFormName(formName);
        tempLog.setStatus("current");
        clockInMapper.addClockInLog(tempLog);
        return SysResult.build(200, "Create new clock in form success");
    }

    // 调用时调取人员信息，获取上班时间，是否为假期等情况, 写入当天的打卡表，并将状态设为 1, 1:00执行，周一至周五
    public SysResult addClockInRecord() {
        // 取得所有员工信息
        ArrayList<HashMap<String, Object>> workerInfo = clockInMapper.getWorkerClockInInfo();
        int numWorker = workerInfo.size();
        String formName;
        try {
            formName = generalService.quickGet("form_name",
                    "clock_in_form_log", "status", "current").get(0).toString();
        } catch (Exception e) {
            return SysResult.build(201, "Error when getting current clock in form");
        }
        for (int i = 0; i < numWorker; i++) {
            // 写入打卡记录
            try {
                ClockIn workerClockInRecord = new ClockIn();
                workerClockInRecord.setUid(workerInfo.get(i).get("uid").toString());
                workerClockInRecord.setAccount(workerInfo.get(i).get("account").toString());
                workerClockInRecord.setName(workerInfo.get(i).get("name").toString());

                String departmentId = workerInfo.get(i).get("department_id").toString();
                String workTimeScheduleId = generalService.quickGet("work_time_schedule_id",
                        "department", "department_id", departmentId).get(0).toString();
                String extraWorkTimeScheduleId = generalService.quickGet("extra_work_time_schedule_id",
                        "department", "department_id", departmentId).get(0).toString();

                workerClockInRecord.setStartWork(generalService.quickGet("start_work",
                        "work_time_schedule", "work_time_schedule_id", workTimeScheduleId).get(0).toString());
                workerClockInRecord.setEndWork(generalService.quickGet("end_work",
                        "work_time_schedule", "work_time_schedule_id", workTimeScheduleId).get(0).toString());
                workerClockInRecord.setStartExtraWork(generalService.quickGet("start_work",
                        "extra_work_time_schedule", "extra_work_time_schedule_id", extraWorkTimeScheduleId).get(0).toString());
                workerClockInRecord.setEndExtraWork(generalService.quickGet("end_work",
                        "extra_work_time_schedule", "extra_work_time_schedule_id", extraWorkTimeScheduleId).get(0).toString());

                workerClockInRecord.setStatus(1);
                clockInMapper.addClockInRecord(formName, workerClockInRecord);
            } catch (Exception e) {
                // 为了不影响下面的记录插入，这里直接继续
                continue;
                // 报错信息写进日志
                //return SysResult.build(201, "Error happened when write in clock in record for worker at position " + i + "  " + e);
            }

            // 添加午休，并重新计算时间
            String uid = workerInfo.get(i).get("uid").toString();
            Date date = new Date();
            // 获取更新前的假期时长
            SysResult offHourResult = applyService.countDayOffHour(date, uid);
            if (!offHourResult.isOk()) {
                return SysResult.build(201, "获取更新前假期时间失败");
            }
            HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
            // 将之前的时长归还
            applyService.updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
            applyService.updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);

            toolService.addNoonBreak(uid,date);
            // 整合记录
            applyService.mergeDayOffRecord(date, uid);

            // 更新假期时长
            SysResult offHourResultAfterUpdate = applyService.countDayOffHour(date, uid);
            if (!offHourResultAfterUpdate.isOk()) {
                return SysResult.build(201, "获取更新后假期时间失败");
            }
            HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
            // 将时长扣除
            applyService.updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
            applyService.updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);
        }

        // 判断是否为假期，并取得假期对象
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String day = f.format(c.getTime());
        ArrayList<Object> tempHoliday = generalService.quickGet("day_off_types", "holiday_form", "date", day);
        // 今天不是假日
        if (tempHoliday.size() == 0) {
            return SysResult.build(200, "Write in clock in record success");
        } else {
            // 今天是假日
            int holidaySize = tempHoliday.size();
            for (int k = 0; k < holidaySize; k++) {
                ArrayList<String> day_off_targets = (ArrayList<String>) Arrays.asList(tempHoliday.get(k).toString().split(","));
                for (int i = 0; i < numWorker; i++) {
                    //判断这个员工是否属于放假范围
                    if (day_off_targets.contains(workerInfo.get(i).get("day_off_type").toString()) || day_off_targets.contains("all")) {
                        // 属于放假范围，添加放假记录
                        String uid = workerInfo.get(i).get("uid").toString();
                        DayOffRecord dayOffRecord = new DayOffRecord();
                        dayOffRecord.setUid(uid);
                        dayOffRecord.setName(workerInfo.get(i).get("name").toString());
                        Date date;
                        try {
                            date = f.parse(day);
                        } catch (ParseException e) {
                            // 需要日志记录错误
                            continue;
                        }
                        dayOffRecord.setOffDate(date);
                        dayOffRecord.setOffStart("1:00");
                        dayOffRecord.setOffEnd("24:00");
                        dayOffRecord.setOffType("Holiday");
                        dayOffRecord.setOffDescription("This is a legal holiday");

                        // 获取更新前的假期时长
                        SysResult offHourResult = applyService.countDayOffHour(date, uid);
                        if (!offHourResult.isOk()) {
                            return SysResult.build(201, "获取更新前假期时间失败");
                        }
                        HashMap<String, Double> offHours = (HashMap<String, Double>) offHourResult.getData();
                        // 将之前的时长归还
                        applyService.updateOffHour("change_day_off", offHours.get("ChangeDayOff"), uid);
                        applyService.updateOffHour("annual_day_off", offHours.get("AnnualDayOff"), uid);

                        clockInMapper.addDayOffRecord(dayOffRecord);
                        // 整合记录
                        applyService.mergeDayOffRecord(date, uid);

                        // 更新假期时长
                        SysResult offHourResultAfterUpdate = applyService.countDayOffHour(date, uid);
                        if (!offHourResultAfterUpdate.isOk()) {
                            return SysResult.build(201, "获取更新后假期时间失败");
                        }
                        HashMap<String, Double> offHoursAfterUpdate = (HashMap<String, Double>) offHourResultAfterUpdate.getData();
                        // 将时长扣除
                        applyService.updateOffHour("change_day_off", offHoursAfterUpdate.get("ChangeDayOff") * (-1), uid);
                        applyService.updateOffHour("annual_day_off", offHoursAfterUpdate.get("AnnualDayOff") * (-1), uid);
                    }
                }
            }
            return SysResult.build(200, "Add day off records success");
        }
    }

    // 根据uid找打打卡信息，并获取状态为 1 的条目，第一次打上班卡并判断是否迟到，第二次更新下班卡时间，并判断是否早退
    public SysResult clockIn(String uid) {
        // 获取打卡记录写入的表格名称
        String formName;
        try {
            formName = generalService.quickGet("form_name",
                    "clock_in_form_log", "status", "current").get(0).toString();
        } catch (Exception e) {
            return SysResult.build(201, "Error when getting current clock in form");
        }

        // 根据uid，取出打卡表中预先写好的打卡记录
        ClockIn clockInInfo = clockInMapper.getClockInRecord(formName, uid);
        if(clockInInfo == null){
            return SysResult.build(201,"错误的uid");
        }
        // 判断是否上班请假
        // 获取当前日期
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String day = f.format(c.getTime());

        // 根据日期取出放假记录
        BoundedDayOffRecord boundedDayOffRecord = applicationMapper.getBoundedDayOffRecord(uid, day);

        // 获取当前时间
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String currentTime = hour + ":" + minute;
        // 获取上班时间
        String[] startWork = clockInInfo.getStartWork().split(":");
        int startWorkHour = Integer.valueOf(startWork[0]);
        int startWorkMinute = Integer.valueOf(startWork[1]);

        // 没有打上班卡，这是第一次打卡
        if (clockInInfo.getClockInStatus().equals("miss")) {
            if (boundedDayOffRecord == null) {
                // 没有放假记录，正常上班
                if (hour > startWorkHour) {
                    // 迟到了
                    clockInMapper.clockIn(formName, uid, currentTime, "late");
                    return SysResult.build(200, "Success but late");
                } else if (hour == startWorkHour) {
                    // 需要判断分钟
                    if (minute > startWorkMinute) {
                        // 迟到了
                        clockInMapper.clockIn(formName, uid, currentTime, "late");
                        return SysResult.build(200, "Success but late");
                    } else {
                        // 正常
                        clockInMapper.clockIn(formName, uid, currentTime, "normal");
                        return SysResult.build(200, "Success");
                    }
                } else {
                    // 正常
                    clockInMapper.clockIn(formName, uid, currentTime, "normal");
                    return SysResult.build(200, "Success");
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
                    if (hour > offEndHour) {
                        // 迟到了
                        clockInMapper.clockIn(formName, uid, currentTime, "late");
                        return SysResult.build(200, "Success but late");
                    } else if (hour == offEndHour) {
                        // 需要判断分钟
                        if (minute > offEndMinute) {
                            // 迟到了
                            clockInMapper.clockIn(formName, uid, currentTime, "late");
                            return SysResult.build(200, "Success but late");
                        } else {
                            // 正常
                            clockInMapper.clockIn(formName, uid, currentTime, "normal");
                            return SysResult.build(200, "Success");
                        }
                    } else {
                        // 正常
                        clockInMapper.clockIn(formName, uid, currentTime, "normal");
                        return SysResult.build(200, "Success");
                    }
                } else {
                    // 假期并不是从上班就开始，那么上班时间照旧
                    if (hour > startWorkHour) {
                        // 迟到了
                        clockInMapper.clockIn(formName, uid, currentTime, "late");
                        return SysResult.build(200, "Success but late");
                    } else if (hour == startWorkHour) {
                        // 需要判断分钟
                        if (minute > startWorkMinute) {
                            // 迟到了
                            clockInMapper.clockIn(formName, uid, currentTime, "late");
                            return SysResult.build(200, "Success but late");
                        } else {
                            // 正常
                            clockInMapper.clockIn(formName, uid, currentTime, "normal");
                            return SysResult.build(200, "Success");
                        }
                    } else {
                        // 正常
                        clockInMapper.clockIn(formName, uid, currentTime, "normal");
                        return SysResult.build(200, "Success");
                    }
                }
            }
        } else {
            // 打过上班卡了，这是第二次打卡，需要刷新第二次打卡的时间
            String[] endWork = clockInInfo.getEndWork().split(":");
            int endWorkHour = Integer.valueOf(endWork[0]);
            int endWorkMinute = Integer.valueOf(endWork[1]);
            // 判断有无请假记录
            if (boundedDayOffRecord == null) {
                // 没有请假，下班时间照旧
                if (hour < endWorkHour) {
                    // 早退了
                    clockInMapper.clockOut(formName, uid, currentTime, "early");
                    return SysResult.build(200, "Success but early leave");
                } else if (hour == endWorkHour) {
                    // 需要判断分钟
                    if (minute < endWorkMinute) {
                        // 早退了
                        clockInMapper.clockOut(formName, uid, currentTime, "early");
                        return SysResult.build(200, "Success but early leave");
                    } else {
                        // 正常
                        clockInMapper.clockOut(formName, uid, currentTime, "normal");
                        return SysResult.build(200, "Success");
                    }
                } else {
                    // 正常
                    clockInMapper.clockOut(formName, uid, currentTime, "normal");
                    return SysResult.build(200, "Success");
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
                    if (hour < offStartHour) {
                        // 早退了
                        clockInMapper.clockOut(formName, uid, currentTime, "early");
                        return SysResult.build(200, "Success but early leave");
                    } else if (hour == offStartHour) {
                        // 需要判断分钟
                        if (minute < offStartMinute) {
                            // 早退了
                            clockInMapper.clockOut(formName, uid, currentTime, "early");
                            return SysResult.build(200, "Success but early leave");
                        } else {
                            // 正常
                            clockInMapper.clockOut(formName, uid, currentTime, "normal");
                            return SysResult.build(200, "Success");
                        }
                    } else {
                        // 正常
                        clockInMapper.clockOut(formName, uid, currentTime, "normal");
                        return SysResult.build(200, "Success");
                    }
                } else {
                    // 假期并未持续到下班时间，那么下班时间照旧
                    if (hour < endWorkHour) {
                        // 早退了
                        clockInMapper.clockOut(formName, uid, currentTime, "early");
                        return SysResult.build(200, "Success but early leave");
                    } else if (hour == endWorkHour) {
                        // 需要判断分钟
                        if (minute < endWorkMinute) {
                            // 早退了
                            clockInMapper.clockOut(formName, uid, currentTime, "early");
                            return SysResult.build(200, "Success but early leave");
                        } else {
                            // 正常
                            clockInMapper.clockOut(formName, uid, currentTime, "normal");
                            return SysResult.build(200, "Success");
                        }
                    } else {
                        // 正常
                        clockInMapper.clockOut(formName, uid, currentTime, "normal");
                        return SysResult.build(200, "Success");
                    }
                }
            }
        }
    }

    // 调取所有打卡记录，计算上班时长，是否加班，然后将状态设置为 0，每天结算，以防有人加班，0:00执行
    public SysResult calculateClockIn() {
        // 获取表名
        String formName;
        try {
            formName = generalService.quickGet("form_name",
                    "clock_in_form_log", "status", "current").get(0).toString();
        } catch (Exception e) {
            return SysResult.build(201, "Error when getting current clock in form");
        }
        ArrayList<ClockIn> UnCalculatedRecords = clockInMapper.getNotCalculatedRecord(formName);
        int numWorker = UnCalculatedRecords.size();
        // 获取前一天的时间
        Calendar cal = Calendar.getInstance();
        int year, month, day;
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DATE) - 1;
        String date = year + "-" + month + "-" + day;

        for (int i = 0; i < numWorker; i++) {
            ClockIn tempRecord = UnCalculatedRecords.get(i);
            // 获取当前员工的放假信息
            DayOffRecord dayOffRecord = null;
            ArrayList<DayOffRecord> records = clockInMapper.getDayOffRecord(tempRecord.getUid(), date);
            if (records.size() != 0) {
                dayOffRecord = records.get(0);
            }

            // 先判断是否打卡了，否则没有打卡信息，也取得不到打卡时间
            if (tempRecord.getClockInStatus().equals("miss") || tempRecord.getClockOutStatus().equals("miss")) {
                // 没有打卡, 则上班时间为0
                tempRecord.setWorkHour(0 + "");
                tempRecord.setExtraWorkHour(0 + "");
                clockInMapper.updateCalculatedRecord(formName, tempRecord, tempRecord.getUid());
                continue;
            }

            // 打过卡了
            // 获取当前人员的上下班时间和加班时间段
            String[] workStart = tempRecord.getStartWork().split(":");
            int startWorkHour = Integer.valueOf(workStart[0]);
            int startWorkMinute = Integer.valueOf(workStart[1]);

            String[] endWork = tempRecord.getEndWork().split(":");
            int endWorkHour = Integer.valueOf(endWork[0]);
            int endWorkMinute = Integer.valueOf(endWork[1]);

            String[] startExtraWork = tempRecord.getStartExtraWork().split(":");
            int startExtraWorkHour = Integer.valueOf(startExtraWork[0]);
            int startExtraWorkMinute = Integer.valueOf(startExtraWork[1]);

            String[] endExtraWork = tempRecord.getEndExtraWork().split(":");
            int endExtraWorkHour = Integer.valueOf(endExtraWork[0]);
            int endExtraWorkMinute = Integer.valueOf(endExtraWork[1]);

            // 获取打卡时间
            String[] clockIn = tempRecord.getClockIn().split(":");
            int clockInHour = Integer.valueOf(clockIn[0]);
            int clockInMinute = Integer.valueOf(clockIn[1]);
            String[] clockOut = tempRecord.getClockOut().split(":");
            int clockOutHour = Integer.valueOf(clockOut[0]);
            int clockOutMinute = Integer.valueOf(clockOut[1]);

            // 计算总上班时长
            double workStartTime;
            double workEndTime;
            double ExtraWorkStartTime;
            double ExtraWorkEndTime;

            // 判断是否早到
            if (tempRecord.getClockInStatus().equals("late")) {
                // 迟到了，上班时间就要按照打卡时间算
                workStartTime = clockInHour + clockInMinute / 60.0;
            } else {
                // 早到了或者正点，上班时间按照标准时间计算
                workStartTime = startWorkHour + startWorkMinute / 60.0;
            }

            // 判断是否早退
            if (tempRecord.getClockOutStatus().equals("early")) {
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
                    "uid", tempRecord.getUid()).get(0).toString();
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
                tempRecord.setExtraWorkHour(0 + "");
            } else {
                // 加班了
                // 判断加班时间是否够最小加班时长
                double extraWorkTime = ExtraWorkEndTime - ExtraWorkStartTime;
                if (extraWorkTime < minimumExtraWorkTime) {
                    extraWorkTime = 0;
                }
                tempRecord.setExtraWorkHour(extraWorkTime + "");
            }
            tempRecord.setWorkHour(totalWorkHour + "");
            clockInMapper.updateCalculatedRecord(formName, tempRecord, tempRecord.getUid());
        }
        return SysResult.build(200, "Calculate clock in record success");
    }

    // 统计函数，调取一段时间内的打卡记录，并返回一个统计结果，7.1-8.1是7月的记录
    @Override
    public SysResult getStatistic(String uid, String start, String end, Integer pageNum, Integer pageMax) {
        try {
            // 获取全部打卡表表名
            ArrayList<String> clockInFormNames = clockInMapper.getAllClockInForm();

            // 根据日期找出需要的记录
            int limitStart = (pageNum - 1) * pageMax;
            ArrayList<LinkedHashMap<String, Object>> selectedResult = clockInMapper.getTempResultByDate(clockInFormNames, uid, start, end, limitStart, pageMax);

            return SysResult.build(200, "Search Result Success", toolService.objectToString(selectedResult));
        } catch (Exception e) {
            return SysResult.build(201, "获取统计信息失败    " + e);
        }
    }

    @Override
    // 管理员统计函数, 调取特定人群在一段时间内的打卡记录
    public SysResult managerGetStatistic(ArrayList<String> uidList, String start, String end) {
        try {
            // 获取全部打卡表表名
            ArrayList<String> clockInFormNames = clockInMapper.getAllClockInForm();

            ArrayList<ArrayList<LinkedHashMap<String, String>>> data = new ArrayList<>();
            int uidNum = uidList.size();
            System.out.println(start);
            for (int i = 0; i < uidNum; i++) {
                // 根据日期找出该uid下的记录
                ArrayList<LinkedHashMap<String, Object>> selectedResult = clockInMapper.getTempResultByDate(clockInFormNames, uidList.get(i), start, end, 0, 10000000);
                data.add(toolService.objectToString(selectedResult));
            }
            return SysResult.build(200, "获取成功", data);
        } catch (Exception e) {
            return SysResult.build(201, "获取打卡记录失败  " + e);
        }
    }


    // 刷uid应该先返回打卡及放假信息，然后如果确认了，那么就调用打卡函数
    public SysResult confirmBeforeClockIn(String uid) {
        // 获取表名
        String formName;
        try {
            formName = generalService.quickGet("form_name",
                    "clock_in_form_log", "status", "current").get(0).toString();
        } catch (Exception e) {
            return SysResult.build(201, "Error when getting current clock in form");
        }
        // 获取打卡信息
        ClockIn clockIn = clockInMapper.getClockInRecord(formName, uid);
        // 获取放假信息
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String day = f.format(c.getTime());
        DayOffRecord dayOffRecord = null;
        ArrayList<DayOffRecord> records = clockInMapper.getDayOffRecord(uid, day);
        if (records.size() != 0) {
            dayOffRecord = records.get(0);
        }

        ConfirmInfo confirmInfo = new ConfirmInfo();

        // 获取员工信息
        LinkedHashMap<String, Object> workerInfo = clockInMapper.getWorkerBasicInfo(uid);
        // 添加员工信息
        confirmInfo.setUid(uid);
        confirmInfo.setName(workerInfo.get("name").toString());
        confirmInfo.setAccount(workerInfo.get("account").toString());
        confirmInfo.setTel(workerInfo.get("tel").toString());
        confirmInfo.setPosition(workerInfo.get("position").toString());
        confirmInfo.setWorkId(workerInfo.get("work_id").toString());


        ArrayList<Object> departmentName = generalService.quickGet("name","department","department_id",workerInfo.get("department_id").toString());
        if(departmentName.size() != 1){
            confirmInfo.setDepartment(workerInfo.get("department_id").toString());
        }else{
            confirmInfo.setDepartment(departmentName.get(0).toString());
        }
        // 判断是否有打卡信息
        if (clockIn == null) {
            // 没有打卡信息，不需要上班
            confirmInfo.setNeedWork(false);
        } else {
            // 有打卡信息
            confirmInfo.setNeedWork(true);
            confirmInfo.setStartWork(clockIn.getStartWork());
            confirmInfo.setEndWork(clockIn.getEndWork());
            confirmInfo.setStartExtraWork(clockIn.getStartExtraWork());
            confirmInfo.setEndExtraWork(clockIn.getEndExtraWork());
            confirmInfo.setClockIn(clockIn.getClockIn());
            confirmInfo.setClockInStatus(clockIn.getClockInStatus());
            confirmInfo.setClockOut(clockIn.getClockOut());
            confirmInfo.setClockOutStatus(clockIn.getClockOutStatus());
        }
        // 判断是否有放假信息
        if (dayOffRecord == null) {
            // 没有放假信息
            confirmInfo.setHaveDayOff(false);
        } else {
            confirmInfo.setHaveDayOff(true);
            confirmInfo.setStartOff(dayOffRecord.getOffStart());
            confirmInfo.setOffType(dayOffRecord.getOffType());
            confirmInfo.setEndOff(dayOffRecord.getOffEnd());
        }
        return SysResult.build(200, "This is the confirmation information", confirmInfo);
    }

    @Override
    public SysResult getClockInRecordByDate(String uid, String start, String end) {
        String formName = "clock_in_";
        // 判断需要哪张表
        try {
            int year = Integer.valueOf(start.split("-")[0]);
            int month = Integer.valueOf(start.split("-")[1]);
            if (month < 7) {
                formName = formName + "jan_";
            } else {
                formName = formName + "jul_";
            }
            formName = formName + year;
        } catch (Exception e) {
            return SysResult.build(201, "获取表格名称失败  " + e);
        }
        ClockIn clockIn;
        try {
            clockIn = clockInMapper.getClockInRecordByDate(formName, uid, start, end);
        } catch (Exception e) {
            return SysResult.build(201, "记录获取失败  " + e);
        }
        if (clockIn == null) {
            // 没有对应记录
            return SysResult.build(201, "没有对应记录");
        } else {
            return SysResult.build(200, "打卡记录获取成功", clockIn);
        }
    }

    @Override
    public SysResult makeUpClockInRecord(String uid, String start, String end, String makeUpType) {
        String formName = "clock_in_";
        // 判断需要哪张表
        try {
            int year = Integer.valueOf(start.split("-")[0]);
            int month = Integer.valueOf(start.split("-")[1]);
            if (month < 7) {
                formName = formName + "jan_";
            } else {
                formName = formName + "jul_";
            }
            formName = formName + year;
        } catch (Exception e) {
            return SysResult.build(201, "获取表格名称失败  " + e);
        }
        SysResult getRecordResult = getClockInRecordByDate(uid, start, end);
        if (getRecordResult.isOk()) {
            // 获取到了需要修改的记录
            ClockIn oldClockIn = (ClockIn) getRecordResult.getData();
            // 检查是否有补卡的必要
            if (makeUpType.equals("IN")) {
                // 检查上班是否缺卡
                if (oldClockIn.getClockInStatus().equals("miss")) {
                    // 确实缺上班卡了
                    clockInMapper.makeUpClockIn(formName, oldClockIn.getStartWork(), oldClockIn.getId());
                    return SysResult.build(200, "补卡成功");
                } else {
                    // 没有缺卡
                    return SysResult.build(201, "记录不需要补卡");
                }
            } else if (makeUpType.equals("OUT")) {
                // 检查下班是否缺卡
                if (oldClockIn.getClockOutStatus().equals("miss")) {
                    // 确实缺下班卡了
                    clockInMapper.makeUpClockOut(formName, oldClockIn.getEndWork(), oldClockIn.getId());
                    return SysResult.build(200, "补卡成功");
                } else {
                    // 没有缺卡
                    return SysResult.build(201, "记录不需要补卡");
                }
            } else {
                return SysResult.build(201, "非法的补卡位置");
            }
        } else {
            return SysResult.build(201, "获取需要补卡的记录失败： " + getRecordResult.getMsg());
        }
    }

    @Override
    public SysResult getTargetColumnStatistic(String targetColumn, String cond) {
        // 获取全部打卡表表名
        ArrayList<String> clockInFormNames = clockInMapper.getAllClockInForm();

        // 获取目标字段名
        String[] targetColumnNames = targetColumn.split(",");

        // cond 是3项为一个单位的
        String[] allConds = cond.split(",");
        int allCondsLength = allConds.length;
        String[][] conditions = new String[allCondsLength / 3][2];
        try {
            for (int i = 0; i < allCondsLength; i = i + 3) {
                conditions[i / 3][0] = allConds[i] + allConds[i + 1];
                conditions[i / 3][1] = allConds[i + 2];
            }
        } catch (Exception e) {
            return SysResult.build(201, "条件解析出错   " + e);
        }

        return SysResult.build(200,"获取成功",clockInMapper.getTargetColumnStatistic(clockInFormNames,targetColumnNames,conditions));
    }
}
