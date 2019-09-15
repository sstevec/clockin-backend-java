package backend.clockin.controller;


import backend.clockin.mapper.ApplicationMapper;
import backend.clockin.mapper.ClockInMapper;
import backend.clockin.mapper.DepartmentMapper;
import backend.clockin.pojo.dayOff.Holiday;
import backend.clockin.pojo.extraWork.ExtraWorkRecord;
import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.*;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/clockInInfo")
public class ClockInInfoController {

    @Autowired
    ClockInService clockInService;

    @Autowired
    StaffManageService staffManageService;

    @Autowired
    GeneralService generalService;

    @Autowired
    ClockInMapper clockInMapper;

    @Autowired
    ApplyService applyService;

    @Autowired
    ToolService toolService;

    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    UserService userService;

    @Autowired
    HolidayService holidayService;

    @RequestMapping("/getLoginUserInfo")
    public SysResult getLoginInUserInfo(String uid) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(201, "获取uid参数异常");
        }
        SysResult searchResult = staffManageService.staffRelevantFormSearch2("name,position,work_id,account,tel,email," +
                        "reviewers,department_id,work_place,change_day_off,annual_day_off",
                "uid,=," + uid, "0", "AND", "0", "100");
        if (!searchResult.isOk()) {
            return searchResult;
        }
        ArrayList<LinkedHashMap<String, String>> resultList = (ArrayList<LinkedHashMap<String, String>>) searchResult.getData();
        if (resultList.size() != 1) {
            return SysResult.build(201, "用户信息获取异常");
        }
        LinkedHashMap<String, String> map = resultList.get(0);
        String departmentId = map.get("department_id");
        ArrayList<Object> departmentName = generalService.quickGet("name", "department", "department_id", departmentId);
        if (departmentName.size() != 1) {
            return SysResult.build(201, "用户信息获取异常");
        }
        String name = departmentName.get(0).toString();
        map.put("department_name", name);
        resultList.clear();
        resultList.add(map);
        return SysResult.build(200, "获取成功", resultList);
    }

    @RequestMapping("/getLoginUserRole")
    public SysResult getLoginInUserRole(String account) {
        ArrayList<SysUser> temp = userService.getUser(account);
        if (temp.size() != 1) {
            return SysResult.build(201, "获取失败，角色信息异常");
        }
        SysUser user = temp.get(0);
        return SysResult.build(200, "获取成功", user.getRoles());
    }

    @RequestMapping("/getUserClockInInfo")
    public SysResult getUserClockInInfo(String uid, String start) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(201, "获取uid参数异常");
        }
        if (start == null || start.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        // 前端传来的月份总是需要加1
        String endDate;
        String startDate;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = dateFormat.format(dateFormat.parse(start));
            endDate = dateFormat.format(new Date(dateFormat.parse(start).getTime() + 1000 * 60 * 60 * 24));
        } catch (Exception e) {
            return SysResult.build(201, "时间格式错误");
        }
        return clockInService.getClockInRecordByDate(uid, startDate, endDate);
    }

    @RequestMapping("/outFile")
    public SysResult clockInInfoOutFile(String uid, String startDate, String endDate, String info) {
        // 字段名和中文名
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);

        // 获取统计信息
        SysResult statisticResult = getStatistic(uid, startDate, endDate, "1", "10000");
        if (!statisticResult.isOk()) {
            return statisticResult;
        }
        ArrayList<LinkedHashMap<String, Object>> statisticInfo = (ArrayList<LinkedHashMap<String, Object>>) statisticResult.getData();

        // 创建新临时表
        String tempFormName = "";
        try {
            // 创建临时表格，用于储存导出数据
            tempFormName = "temp_clock_in_out_file_form_" + uid;

            // 确保文件夹存在
            String filePath = "/opt/RD011/clockin/outExcel";
            File targetDir = new File(filePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            //创建新的excel
            File file = new File(filePath + "/" + tempFormName + ".xlsx");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            int numberColumn = input.size();
            String[] column_name = new String[numberColumn];
            Set<String> col_name_set = input.keySet();
            col_name_set.toArray(column_name);

            // 写入Excel
            return generalService.outFile3(filePath + "/" + tempFormName + ".xlsx", column_name,input, statisticInfo);

        } catch (Exception e) {
            return SysResult.build(201,"导出失败   "+e);
        }

    }

    @RequestMapping("/getStatistic")
    public SysResult getStatistic(String uid, String startDate, String endDate, String pageNum, String pageMax) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(201, "获取uid参数异常");
        }
        if (startDate == null || startDate.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (endDate == null || endDate.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (pageNum == null || pageNum.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (pageMax == null || pageMax.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = dateFormat.format(dateFormat.parse(startDate));
            endDate = dateFormat.format(dateFormat.parse(endDate));
        } catch (ParseException e) {
            return SysResult.build(201, "转化时间错误");
        }
        // 获取所有打卡记录
        SysResult statisticResult = clockInService.getStatistic(uid, startDate, endDate, Integer.valueOf(pageNum), Integer.valueOf(pageMax));

        if (!statisticResult.isOk()) {
            return statisticResult;
        }
        ArrayList<LinkedHashMap<String, String>> records = (ArrayList<LinkedHashMap<String, String>>) statisticResult.getData();
        if (records.size() == 0) {
            return SysResult.build(201, "该日期段内无记录");
        }

        ArrayList<LinkedHashMap<String, String>> statisticInfo = new ArrayList<>();
        // 加入总览统计信息的Map
        LinkedHashMap<String, String> generalInfo = new LinkedHashMap<>();
        statisticInfo.add(generalInfo);

        int totalWorkDate, totalMissClock = 0, totalLate = 0, totalEarly = 0;
        Double totalWorkHours = 0.0, totalExtraWorkHour = 0.0, totalChangeDayOff = 0.0,
                totalAnnualDayOff = 0.0, totalExchangeMoneyHour = 0.0, totalExchangeChangeDayOffHour = 0.0, totalExchangeHolidayMoneyHour = 0.0,
                totalSupposeWorkHour = 0.0, totalMissHour = 0.0;
        // 加入按照日期的具体统计信息
        int size = records.size();
        totalWorkDate = size;
        statisticInfo.get(0).put("totalWorkDate", totalWorkDate + " 天");
        for (int i = 0; i < size; i++) {
            LinkedHashMap<String, String> tempRecord = records.get(i);
            LinkedHashMap<String, String> tempInfo = new LinkedHashMap<>();
            // 先解析日期
            String timeStamp = tempRecord.get("created");
            String date;
            Date day;
            try {
                day = dateFormat.parse(timeStamp);
                date = dateFormat.format(day);
            } catch (ParseException e) {
                System.out.println(timeStamp);
                return SysResult.build(201, "时间转换错误");
            }
            tempInfo.put("date", date);
            tempInfo.put("WorkHour", tempRecord.get("work_hour"));
            tempInfo.put("extraWorkHour", tempRecord.get("extra_work_hour"));
            tempInfo.put("clockIn", tempRecord.get("clock_in"));
            tempInfo.put("clockOut", tempRecord.get("clock_out"));
            tempInfo.put("workStart", tempRecord.get("start_work"));
            tempInfo.put("workEnd", tempRecord.get("end_work"));
            if (tempRecord.get("extra_work_hour") != null && !tempRecord.get("extra_work_hour").equals("")) {
                totalExtraWorkHour = totalExtraWorkHour + Double.valueOf(tempRecord.get("extra_work_hour"));
            }
            totalSupposeWorkHour = totalSupposeWorkHour + toolService.getTimeSub(tempRecord.get("start_work"), tempRecord.get("end_work"));

            if (tempRecord.get("clock_in_status").equals("miss")) {
                totalMissClock++;
                tempInfo.put("missClockIn", "是");
            } else {
                tempInfo.put("missClockIn", "");
            }
            if (tempRecord.get("clock_out_status").equals("miss")) {
                totalMissClock++;
                tempInfo.put("missClockOut", "是");
            } else {
                tempInfo.put("missClockOut", "");
            }
            if (tempRecord.get("clock_in_status").equals("late")) {
                totalLate++;
                tempInfo.put("late", "是");
            } else {
                tempInfo.put("late", "");
            }
            if (tempRecord.get("clock_out_status").equals("early")) {
                totalEarly++;
                tempInfo.put("earlyLeave", "是");
            } else {
                tempInfo.put("earlyLeave", "");
            }

            // 获取当天的放假时长
            SysResult offHour = applyService.countDayOffHour(day, uid);
            if (!offHour.isOk()) {
                return offHour;
            }
            HashMap<String, Double> tempOffHour = (HashMap<String, Double>) offHour.getData();
            tempInfo.put("changDayOff", tempOffHour.get("ChangeDayOff") + "");
            totalChangeDayOff = totalChangeDayOff + tempOffHour.get("ChangeDayOff");
            tempInfo.put("annualDayOff", tempOffHour.get("AnnualDayOff") + "");
            totalAnnualDayOff = totalAnnualDayOff + tempOffHour.get("AnnualDayOff");
            Double totalWorkHour = toolService.getTimeSub(tempRecord.get("start_work"), tempRecord.get("end_work"));
            if (tempRecord.get("work_hour") != null && !tempRecord.get("work_hour").equals("")) {
                totalWorkHours = totalWorkHours + Double.valueOf(tempRecord.get("work_hour"));
                Double missHour = totalWorkHour - (Double.valueOf(tempRecord.get("work_hour")) +
                        tempOffHour.get("ChangeDayOff") + tempOffHour.get("AnnualDayOff"));
                totalMissHour = totalMissHour + missHour;
                tempInfo.put("missHour", missHour + "");
            } else {
                tempInfo.put("missHour", "");
            }
            statisticInfo.add(tempInfo);

            ExtraWorkRecord tempExtraWorkRecord = applicationMapper.getExtraWorkRecord(uid, date);

            if (tempExtraWorkRecord != null) {
                Double lastHour = toolService.getTimeSub(tempExtraWorkRecord.getExtraWorkStart(), tempExtraWorkRecord.getExtraWorkEnd());
                if (tempExtraWorkRecord.getType().equals("Money")) {
                    totalExchangeMoneyHour = totalExchangeMoneyHour + lastHour;
                } else if (tempExtraWorkRecord.getType().equals("HolidayMoney")) {
                    totalExchangeHolidayMoneyHour = totalExchangeHolidayMoneyHour + lastHour;
                } else if (tempExtraWorkRecord.getType().equals("ChangeDayOff")) {
                    totalExchangeChangeDayOffHour = totalExchangeChangeDayOffHour + lastHour;
                }
            }
        }
        statisticInfo.get(0).put("totalMissClock", totalMissClock + " 次");
        statisticInfo.get(0).put("totalLate", totalLate + " 次");
        statisticInfo.get(0).put("totalEarly", totalEarly + " 次");
        statisticInfo.get(0).put("totalWorkHours", (Math.round(totalWorkHours * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalExtraWorkHour", (Math.round(totalExtraWorkHour * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalChangeDayOff", (Math.round(totalChangeDayOff * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalAnnualDayOff", (Math.round(totalAnnualDayOff * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalExchangeMoneyHour", (Math.round(totalExchangeMoneyHour * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalExchangeHolidayMoneyHour", (Math.round(totalExchangeHolidayMoneyHour * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalExchangeChangeDayOffHour", (Math.round(totalExchangeChangeDayOffHour * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalCountedExtraWork", (Math.round((totalExchangeChangeDayOffHour + totalExchangeHolidayMoneyHour + totalExchangeMoneyHour) * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalSupposeWorkHour", (Math.round(totalSupposeWorkHour * 100) / 100.0) + " 小时");
        statisticInfo.get(0).put("totalMissHour", (Math.round(totalMissHour * 100) / 100.0) + " 小时");
        return SysResult.build(200, "数据获取成功", statisticInfo);
    }

    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/managerGetStatistic")
    public SysResult managerGetStatistic(String departmentId, String startDate, String endDate, String pageNum, String pageMax) {
        if (departmentId == null || departmentId.equals("")) {
            return SysResult.build(201, "获取uid参数异常");
        }
        if (startDate == null || startDate.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (endDate == null || endDate.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (pageNum == null || pageNum.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (pageMax == null || pageMax.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = dateFormat.format(dateFormat.parse(startDate));
            endDate = dateFormat.format(dateFormat.parse(endDate));
        } catch (ParseException e) {
            return SysResult.build(201, "转化时间错误");
        }
        // 处理分页
        int start = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageMax);
        int num = Integer.valueOf(pageMax);
        ArrayList<String> uidList;
        // 判定取得全部uid还是某个部门的全部uid
        if (departmentId.equals("All")) {
            // 取得全部uid
            uidList = departmentMapper.getAllUid(start, num);
        } else {
            // 取得该部门下全部的uid
            uidList = departmentMapper.getUidByDepartmentId(departmentId, start, num);
        }
        // 取出这些uid对应的打卡记录
        SysResult recordResult = clockInService.managerGetStatistic(uidList, startDate, endDate);

        if (!recordResult.isOk()) {
            return recordResult;
        }
        ArrayList<ArrayList<LinkedHashMap<String, String>>> records = (ArrayList<ArrayList<LinkedHashMap<String, String>>>)
                recordResult.getData();
        ArrayList<HashMap<String, String>> statisticResult = new ArrayList<>();

        int recordsSize = records.size();
        if (recordsSize == 0) {
            return SysResult.build(201, "该时间段内无记录");
        }

        // 验证取得的数据数量正确
        if (uidList.size() != recordsSize) {
            return SysResult.build(201, "获取的数据数量异常");
        }
        for (int i = 0; i < recordsSize; i++) {
            ArrayList<LinkedHashMap<String, String>> recordForUid = records.get(i);
            // 加入总览统计信息的Map
            HashMap<String, String> generalInfo = new HashMap<>();

            int totalWorkDate, totalMissClock = 0, totalLate = 0, totalEarly = 0;
            Double totalWorkHours = 0.0, totalExtraWorkHour = 0.0, totalChangeDayOff = 0.0,
                    totalAnnualDayOff = 0.0, totalExchangeMoneyHour = 0.0, totalExchangeChangeDayOffHour = 0.0, totalExchangeHolidayMoneyHour = 0.0,
                    totalSupposeWorkHour = 0.0, totalMissHour = 0.0;

            int size = recordForUid.size();
            totalWorkDate = size;
            generalInfo.put("totalWorkDate", totalWorkDate + " 天");
            for (int j = 0; j < size; j++) {
                LinkedHashMap<String, String> tempRecordForDay = recordForUid.get(j);
                // 先解析日期
                String timeStamp = tempRecordForDay.get("created");
                String date;
                Date day;
                try {
                    day = dateFormat.parse(timeStamp);
                    date = dateFormat.format(day);
                } catch (ParseException e) {
                    System.out.println(timeStamp);
                    return SysResult.build(201, "时间转换错误");
                }

                if (tempRecordForDay.get("extra_work_hour") != null && !tempRecordForDay.get("extra_work_hour").equals("")) {
                    totalExtraWorkHour = totalExtraWorkHour + Double.valueOf(tempRecordForDay.get("extra_work_hour"));
                }
                totalSupposeWorkHour = totalSupposeWorkHour + toolService.getTimeSub(tempRecordForDay.get("start_work"), tempRecordForDay.get("end_work"));

                if (tempRecordForDay.get("clock_in_status").equals("miss")) {
                    totalMissClock++;
                }
                if (tempRecordForDay.get("clock_out_status").equals("miss")) {
                    totalMissClock++;
                }
                if (tempRecordForDay.get("clock_in_status").equals("late")) {
                    totalLate++;
                }
                if (tempRecordForDay.get("clock_out_status").equals("early")) {
                    totalEarly++;
                }

                // 获取当天的放假时长
                SysResult offHour = applyService.countDayOffHour(day, uidList.get(i));
                if (!offHour.isOk()) {
                    return offHour;
                }
                HashMap<String, Double> tempOffHour = (HashMap<String, Double>) offHour.getData();
                totalChangeDayOff = totalChangeDayOff + tempOffHour.get("ChangeDayOff");
                totalAnnualDayOff = totalAnnualDayOff + tempOffHour.get("AnnualDayOff");
                Double totalWorkHour = toolService.getTimeSub(tempRecordForDay.get("start_work"), tempRecordForDay.get("end_work"));
                if (tempRecordForDay.get("work_hour") != null && !tempRecordForDay.get("work_hour").equals("")) {
                    totalWorkHours = totalWorkHours + Double.valueOf(tempRecordForDay.get("work_hour"));
                    Double missHour = totalWorkHour - (Double.valueOf(tempRecordForDay.get("work_hour")) +
                            tempOffHour.get("ChangeDayOff") + tempOffHour.get("AnnualDayOff"));
                    totalMissHour = totalMissHour + missHour;
                }

                ExtraWorkRecord tempExtraWorkRecord = applicationMapper.getExtraWorkRecord(uidList.get(i), date);

                if (tempExtraWorkRecord != null) {
                    Double lastHour = toolService.getTimeSub(tempExtraWorkRecord.getExtraWorkStart(), tempExtraWorkRecord.getExtraWorkEnd());
                    if (tempExtraWorkRecord.getType().equals("Money")) {
                        totalExchangeMoneyHour = totalExchangeMoneyHour + lastHour;
                    } else if (tempExtraWorkRecord.getType().equals("HolidayMoney")) {
                        totalExchangeHolidayMoneyHour = totalExchangeHolidayMoneyHour + lastHour;
                    } else if (tempExtraWorkRecord.getType().equals("ChangeDayOff")) {
                        totalExchangeChangeDayOffHour = totalExchangeChangeDayOffHour + lastHour;
                    }
                }
            }
            generalInfo.put("totalMissClock", totalMissClock + " 次");
            generalInfo.put("totalLate", totalLate + " 次");
            generalInfo.put("totalEarly", totalEarly + " 次");
            generalInfo.put("totalWorkHours", (Math.round(totalWorkHours * 100) / 100.0) + " 小时");
            generalInfo.put("totalExtraWorkHour", (Math.round(totalExtraWorkHour * 100) / 100.0) + " 小时");
            generalInfo.put("totalChangeDayOff", (Math.round(totalChangeDayOff * 100) / 100.0) + " 小时");
            generalInfo.put("totalAnnualDayOff", (Math.round(totalAnnualDayOff * 100) / 100.0) + " 小时");
            generalInfo.put("totalExchangeMoneyHour", (Math.round(totalExchangeMoneyHour * 100) / 100.0) + " 小时");
            generalInfo.put("totalExchangeHolidayMoneyHour", (Math.round(totalExchangeHolidayMoneyHour * 100) / 100.0) + " 小时");
            generalInfo.put("totalExchangeChangeDayOffHour", (Math.round(totalExchangeChangeDayOffHour * 100) / 100.0) + " 小时");
            generalInfo.put("totalCountedExtraWork", (Math.round((totalExchangeChangeDayOffHour + totalExchangeHolidayMoneyHour + totalExchangeMoneyHour) * 100) / 100.0) + " 小时");
            generalInfo.put("totalSupposeWorkHour", (Math.round(totalSupposeWorkHour * 100) / 100.0) + " 小时");
            generalInfo.put("totalMissHour", (Math.round(totalMissHour * 100) / 100.0) + " 小时");

            if (totalWorkDate != 0) {
                generalInfo.put("missInTotal", (Math.round((totalMissClock / (totalWorkDate * 2.0) * 100))) + "%");
                generalInfo.put("lateInTotal", (Math.round((totalLate + totalEarly) / (totalWorkDate * 2.0) * 100)) + "%");
            } else {
                generalInfo.put("missInTotal", 0 + "%");
                generalInfo.put("lateInTotal", 0 + "%");
            }
            if (totalSupposeWorkHour != 0) {
                generalInfo.put("totalWorkInSuppose", (Math.round(totalWorkHours / totalSupposeWorkHour * 100)) + "%");
                generalInfo.put("changeInSuppose", (Math.round((totalAnnualDayOff + totalChangeDayOff) / totalSupposeWorkHour * 100)) + "%");
                generalInfo.put("missInSuppose", (Math.round(totalMissHour / totalSupposeWorkHour * 100)) + "%");
            } else {
                generalInfo.put("totalWorkInSuppose", 0 + "%");
                generalInfo.put("changeInSuppose", 0 + "%");
                generalInfo.put("missInSuppose", 0 + "%");
            }
            if ((totalExchangeChangeDayOffHour + totalExchangeHolidayMoneyHour + totalExchangeMoneyHour) != 0) {
                generalInfo.put("moneyInExtra", (Math.round(totalExchangeMoneyHour / (totalExchangeChangeDayOffHour + totalExchangeHolidayMoneyHour + totalExchangeMoneyHour) * 100)) + "%");
                generalInfo.put("holidayMoneyInExtra", (Math.round(totalExchangeHolidayMoneyHour / (totalExchangeChangeDayOffHour + totalExchangeHolidayMoneyHour + totalExchangeMoneyHour) * 100)) + "%");
                generalInfo.put("changeInExtra", (Math.round(totalExchangeChangeDayOffHour / (totalExchangeChangeDayOffHour + totalExchangeHolidayMoneyHour + totalExchangeMoneyHour) * 100)) + "%");
            } else {
                generalInfo.put("moneyInExtra", 0 + "%");
                generalInfo.put("holidayMoneyInExtra", 0 + "%");
                generalInfo.put("changeInExtra", 0 + "%");
            }
            // 取员工信息
            LinkedHashMap<String, Object> workerInfo = clockInMapper.getWorkerBasicInfo(uidList.get(i));
            generalInfo.put("name", workerInfo.get("name").toString());
            generalInfo.put("manager", workerInfo.get("manager").toString());
            generalInfo.put("departmentId", workerInfo.get("department_id").toString());
            generalInfo.put("position", workerInfo.get("position").toString());
            generalInfo.put("uid", uidList.get(i));
            statisticResult.add(generalInfo);
        }
        return SysResult.build(200, "获取成功", statisticResult);
    }

    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/DepartmentManagerGetStatistic")
    public SysResult DepartmentManagerGetStatistic(String departmentId, String startDate, String endDate, String pageNum, String pageMax) {
        // 取得统计信息
        SysResult statisticResult = managerGetStatistic(departmentId,startDate,endDate,pageNum,pageMax);
        if(!statisticResult.isOk()){
            return statisticResult;
        }
        ArrayList<HashMap<String,String>> statistic = (ArrayList<HashMap<String, String>>) statisticResult.getData();

        // 取得员工信息
        SysResult columnNameResult = staffManageService.getStaffRelevantFormAllColumns();
        if(!columnNameResult.isOk()){
            return columnNameResult;
        }
        ArrayList<String> columns = (ArrayList<String>) columnNameResult.getData();
        String targetColumn = "";
        for (String columnName: columns
             ) {
            targetColumn = targetColumn + columnName + ",";
        }
        targetColumn = targetColumn.substring(0, targetColumn.length() - 1);
        int start = (Integer.valueOf(pageNum) - 1)*Integer.valueOf(pageMax);
        SysResult staffInfoResult = staffManageService.staffRelevantFormSearch2(targetColumn,"department_id,=,"+departmentId,"0","AND",start+"",pageMax);
        if(!staffInfoResult.isOk()){
            return staffInfoResult;
        }
        ArrayList<LinkedHashMap<String, String>> staffInfo = (ArrayList<LinkedHashMap<String, String>>) staffInfoResult.getData();

        // 拼接两组数据
        ArrayList<HashMap<String, String>> finalResult = new ArrayList<>();
        int size = staffInfo.size();
        for(int i = 0; i<size; i++){
            for(int j =0; j<size; j++) {
                if (statistic.get(i).get("uid").equals(staffInfo.get(j).get("uid"))) {
                    // uid 相符, 拼接
                    HashMap<String, String> temp = new HashMap<>(staffInfo.get(j));
                    temp.putAll(statistic.get(i));
                    finalResult.add(temp);
                    break;
                }
            }
        }
        return SysResult.build(200,"获取成功",finalResult);
    }

    @RequestMapping("/countUserStatisticRecord")
    public SysResult countUserStatisticRecord(String uid, String start, String end) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(201, "获取uid参数异常");
        }
        if (start == null || start.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }
        if (end == null || end.equals("")) {
            return SysResult.build(201, "获取开始日期参数异常");
        }

        //防止日期格式不同
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
        } catch (ParseException e) {
            return SysResult.build(201, "写入请假记录出错    " + e);
        }
        start = dateFormat.format(startDate);
        end = dateFormat.format(endDate);

        // 获取全部打卡表表名
        ArrayList<String> clockInFormNames = clockInMapper.getAllClockInForm();

        // 拼接条件
        String[][] cond = {{"uid=", uid}, {"created>=", start}, {"created<=", end}};
        String[] columnName = {"count(0)", "created"};

        ArrayList<LinkedHashMap<String, Object>> result = clockInMapper.getTargetColumnStatistic(clockInFormNames, columnName, cond);
        if (result.size() == 0) {
            return SysResult.build(200, "没有记录", 0);
        }
        int size = result.size();
        long total = 0;
        for (int i = 0; i < size; i++) {
            total = total + ((long) (result.get(i).get("count(0)")));
        }
        return SysResult.build(200, "取得记录总数成功", total);
    }

    @RequestMapping("/managerCountStatistic")
    public SysResult managerCountStatistic(String departmentId) {
        if (departmentId == null || departmentId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        String identifier, identifyValue;
        if (departmentId.equals("All")) {
            identifier = "1";
            identifyValue = "1";
        } else {
            identifier = "department_id";
            identifyValue = departmentId;
        }
        return SysResult.build(200, "获取成功", clockInMapper.getManagerCountStatistic(identifier, identifyValue));
    }

    @RequestMapping("/getHolidayInfo")
    public SysResult getHolidayInfo(String uid, String start, String end) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
        } catch (ParseException e) {
            return SysResult.build(201, "转化格式出错    " + e);
        }
        //防止日期格式不同
        start = dateFormat.format(startDate);
        end = dateFormat.format(endDate);

        // 取得放假组
        ArrayList<Object> breakGroup = generalService.quickGet("day_off_type", "workers", "uid", uid);
        if (breakGroup.size() != 1) {
            return SysResult.build(201, "员工信息错误");
        }
        String dayOffGroup = breakGroup.get(0).toString();

        ArrayList<String> result = new ArrayList<>();

        // 取得目标端内所有假期记录
        SysResult holidayResult = holidayService.getHolidayInfoByDate(start, end, "", 0, 1000);
        if (!holidayResult.isOk()) {
            return SysResult.build(201, "获取假期信息出错");
        }
        ArrayList<Holiday> holidays = (ArrayList<Holiday>) holidayResult.getData();
        if (holidays.size() == 0) {
            return SysResult.build(200, "没有假期", result);
        }

        // 分析假期是否适用
        for (Holiday temp: holidays
             ) {
            String[] targets = temp.getDayOffTypes().split(",");
            List<String> tempList = Arrays.asList(targets);
            if(tempList.contains(dayOffGroup)){
                result.add(Integer.valueOf(dateFormat.format(temp.getDate()).split("-")[2])+"");
            }
        }
        return SysResult.build(200,"获取成功",result);
    }
}
