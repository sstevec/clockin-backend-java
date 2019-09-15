package backend.clockin.controller;

import backend.clockin.mapper.DepartmentMapper;
import backend.clockin.mapper.HolidayMapper;
import backend.clockin.mapper.ShiroMapper;
import backend.clockin.pojo.department.ClockInRule;
import backend.clockin.pojo.department.Department;
import backend.clockin.pojo.department.ExtraWorkRule;
import backend.clockin.pojo.department.NoonBreakRule;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.DepartmentService;
import backend.clockin.service.GeneralService;
import backend.clockin.service.HolidayService;
import backend.clockin.service.UserService;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/api/advance")
public class AdvanceManagementController {

    @Autowired
    HolidayService holidayService;

    @Autowired
    UserService userService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    GeneralService generalService;

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    HolidayMapper holidayMapper;

    @Autowired
    ShiroMapper shiroMapper;

    @RequiresRoles({"admin"})
    @RequestMapping("/getHolidayByCond")
    public SysResult getHolidayByCond(String startDate, String endDate, String searchInfo, String pageNum, String pageMax) {
        if (startDate == null) {
            return SysResult.build(400, "参数为空");
        }
        if (endDate == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageNum == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageMax == null) {
            return SysResult.build(400, "参数为空");
        }
        int startLimit = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageMax);
        int num = Integer.valueOf(pageMax);
        return holidayService.getHolidayInfoByDate(startDate, endDate, searchInfo, startLimit, num);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/addHoliday")
    public SysResult addHoliday(String startDate, String endDate, String name, String dayOffTarget) {
        if (startDate == null) {
            return SysResult.build(400, "参数为空");
        }
        if (endDate == null) {
            return SysResult.build(400, "参数为空");
        }
        if (name == null) {
            return SysResult.build(400, "参数为空");
        }
        if (dayOffTarget == null) {
            return SysResult.build(400, "参数为空");
        }
        return holidayService.addHoliday(startDate, endDate, name, dayOffTarget);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/getSystemUser")
    public SysResult getSystemUser(String info, String pageNum, String pageMax) {
        if (pageNum == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageMax == null) {
            return SysResult.build(400, "参数为空");
        }
        int startLimit = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageMax);
        int num = Integer.valueOf(pageMax);
        return userService.getUserByCond(info, startLimit, num);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("addDepartment")
    public SysResult addDepartment(String departmentId, String name, String tel, String workPlace, String chargerName,
                                   String workTimeScheduleId, String extraWorkTimeScheduleId, String noonBreakId) {
        if (departmentId == null || departmentId.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (name == null || name.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (tel == null || tel.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (workPlace == null || workPlace.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (chargerName == null || chargerName.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (workTimeScheduleId == null || workTimeScheduleId.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (extraWorkTimeScheduleId == null || extraWorkTimeScheduleId.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if(noonBreakId == null || noonBreakId.equals("")){
            return SysResult.build(400,"参数为空");
        }
        Department department = new Department();
        department.setDepartmentId(departmentId);
        department.setName(name);
        department.setTel(tel);
        department.setWorkPlace(workPlace);
        department.setChargerName(chargerName);
        department.setWorkTimeScheduleId(workTimeScheduleId);
        department.setExtraWorkTimeScheduleId(extraWorkTimeScheduleId);
        department.setNoonBreakId(noonBreakId);
        return departmentService.addDepartment(department);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/deleteDepartment")
    public SysResult deleteDepartment(String departmentId, String name) {
        if (departmentId == null) {
            return SysResult.build(400, "参数为空");
        }
        if (name == null) {
            return SysResult.build(400, "参数为空");
        }
        return departmentService.deleteDepartment(departmentId, name);
    }

    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/changeDepartment")
    public SysResult changeDepartment(String id, String info) {
        if (info == null || info.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (id == null || id.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        return departmentService.changeDepartment(id, input);
    }

    @RequestMapping("/updateStaffing")
    public SysResult updateStaffing() {
        return departmentService.updateStaffNumber();
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/changeUserInfo")
    public SysResult changeUserInfo(String account, String role, String password) {
        if (account == null || account.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (role == null || role.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        HashMap<String, String> info = new HashMap<>();
        if (password != null && !password.equals("")) {
            String passwordCiph = userService.getInputPasswordCiph(password, userService.getUser(account).get(0).getSalt());
            info.put("password", passwordCiph);
        }
        info.put("roles", role);
        return userService.changeUser(info, account);
    }

    @RequestMapping("/getDepartmentOptions")
    public SysResult getDepartmentOptions() {
        return generalService.getRow2("name,department_id", "department", "1,=,1",
                "0", "AND", 1, 10000);
    }

    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/getAllClockInRule")
    public SysResult getAllClockInRule() {
        return SysResult.build(200, "获取成功", departmentMapper.getAllClockInRule());
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/changeClockInSchedule")
    public SysResult changeClockInSchedule(String oldId, String newScheduleId, String newStart, String newEnd) {
        if (oldId == null || oldId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newScheduleId == null || newScheduleId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newStart == null || newStart.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newEnd == null || newEnd.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 如果新名称和旧名称不一致, 检查新名称是否重复
        if (!newScheduleId.equals(oldId)) {
            // 检查重复
            ArrayList<String> scheduleName = departmentMapper.checkDuplicateWorkTimeId(newScheduleId);
            if (scheduleName.size() != 0) {
                return SysResult.build(201, "规则编号重复");
            }
            departmentMapper.updateDepartmentWorkTimeSchedule(newScheduleId, oldId);
        }
        // 更新, 计划表和部门表
        ClockInRule clockInRule = new ClockInRule();
        clockInRule.setWorkTimeScheduleId(newScheduleId);
        clockInRule.setStartWork(newStart);
        clockInRule.setEndWork(newEnd);
        departmentMapper.updateClockInRule(clockInRule, oldId);
        return SysResult.build(200, "更新成功");
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/addClockInSchedule")
    public SysResult addClockInSchedule(String newScheduleId, String newStart, String newEnd) {
        if (newScheduleId == null || newScheduleId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newStart == null || newStart.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newEnd == null || newEnd.equals("")) {
            return SysResult.build(201, "参数为空");
        }

        // 检查重复
        ArrayList<String> scheduleName = departmentMapper.checkDuplicateWorkTimeId(newScheduleId);
        if (scheduleName.size() != 0) {
            return SysResult.build(201, "规则编号重复");
        }
        // 添加
        ClockInRule clockInRule = new ClockInRule();
        clockInRule.setWorkTimeScheduleId(newScheduleId);
        clockInRule.setStartWork(newStart);
        clockInRule.setEndWork(newEnd);
        departmentMapper.addClockInRule(clockInRule);
        return SysResult.build(200, "添加成功");
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/deleteClockInSchedule")
    public SysResult deleteClockInSchedule(String workTimeId){
        if (workTimeId == null || workTimeId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 检查该id是否正在使用
        ArrayList<String> scheduleName = departmentMapper.checkWorkTimeIdInUse(workTimeId);
        if (scheduleName.size() != 0) {
            // 说明有部门正在使用该id
            return SysResult.build(201, "无法删除正在使用的id");
        }
        departmentMapper.deleteClockInSchedule(workTimeId);
        return SysResult.build(200,"删除成功");
    }


    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/getAllNoonBreakRule")
    public SysResult getAllNoonBreakRule() {
        return SysResult.build(200, "获取成功", departmentMapper.getAllNoonBreakRule());
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/changeNoonBreakSchedule")
    public SysResult changeNoonBreakSchedule(String oldId, String newScheduleId, String newStart, String newEnd) {
        if (oldId == null || oldId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newScheduleId == null || newScheduleId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newStart == null || newStart.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newEnd == null || newEnd.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 如果新名称和旧名称不一致, 检查新名称是否重复
        if (!newScheduleId.equals(oldId)) {
            // 检查重复
            ArrayList<String> scheduleName = departmentMapper.checkDuplicateNoonBreakId(newScheduleId);
            if (scheduleName.size() != 0) {
                return SysResult.build(201, "规则编号重复");
            }
            departmentMapper.updateDepartmentNoonBreakSchedule(newScheduleId, oldId);
        }
        // 更新, 计划表和部门表
        NoonBreakRule noonBreakRule = new NoonBreakRule();
        noonBreakRule.setNoonBreakId(newScheduleId);
        noonBreakRule.setStartTime(newStart);
        noonBreakRule.setEndTime(newEnd);

        departmentMapper.updateNoonBreakRule(noonBreakRule, oldId);
        return SysResult.build(200, "更新成功");
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/addNoonBreakSchedule")
    public SysResult addNoonBreakSchedule(String newScheduleId, String newStart, String newEnd) {
        if (newScheduleId == null || newScheduleId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newStart == null || newStart.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newEnd == null || newEnd.equals("")) {
            return SysResult.build(201, "参数为空");
        }

        // 检查重复
        ArrayList<String> scheduleName = departmentMapper.checkDuplicateNoonBreakId(newScheduleId);
        if (scheduleName.size() != 0) {
            return SysResult.build(201, "规则编号重复");
        }
        // 添加
        NoonBreakRule noonBreakRule = new NoonBreakRule();
        noonBreakRule.setNoonBreakId(newScheduleId);
        noonBreakRule.setStartTime(newStart);
        noonBreakRule.setEndTime(newEnd);
        departmentMapper.addNoonBreakRule(noonBreakRule);
        return SysResult.build(200, "添加成功");
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/deleteNoonBreakSchedule")
    public SysResult deleteNoonBreakSchedule(String noonBreakId){
        if (noonBreakId == null || noonBreakId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 检查该id是否正在使用
        ArrayList<String> scheduleName = departmentMapper.checkNoonBreakIdInUse(noonBreakId);
        if (scheduleName.size() != 0) {
            // 说明有部门正在使用该id
            return SysResult.build(201, "无法删除正在使用的id");
        }
        departmentMapper.deleteNoonBreakSchedule(noonBreakId);
        return SysResult.build(200,"删除成功");
    }


    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/getAllExtraWorkRule")
    public SysResult getAllExtraWorkRule() {
        return SysResult.build(200, "获取成功", departmentMapper.getAllExtraWorkRule());
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/changeExtraWorkSchedule")
    public SysResult changeExtraWorkSchedule(String oldId, String newScheduleId, String newStart, String newEnd, String minimumWorkTime) {
        if (oldId == null || oldId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newScheduleId == null || newScheduleId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newStart == null || newStart.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newEnd == null || newEnd.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (minimumWorkTime == null || minimumWorkTime.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 如果新名称和旧名称不一致, 检查新名称是否重复
        if (!newScheduleId.equals(oldId)) {
            // 检查重复
            ArrayList<String> scheduleName = departmentMapper.checkDuplicateExtraWorkTimeId(newScheduleId);
            if (scheduleName.size() != 0) {
                return SysResult.build(201, "规则编号重复");
            }
            departmentMapper.updateDepartmentExtraWorkTimeSchedule(newScheduleId, oldId);
        }
        // 更新, 计划表和部门表
        ExtraWorkRule extraWorkRule = new ExtraWorkRule();
        extraWorkRule.setExtraWorkTimeScheduleId(newScheduleId);
        extraWorkRule.setStartWork(newStart);
        extraWorkRule.setEndWork(newEnd);
        extraWorkRule.setMinimumWorkTime(minimumWorkTime);
        departmentMapper.updateExtraWorkRule(extraWorkRule, oldId);
        return SysResult.build(200, "更新成功");
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/addExtraWorkSchedule")
    public SysResult addExtraWorkSchedule(String newScheduleId, String newStart, String newEnd, String minimumWorkTime) {
        if (newScheduleId == null || newScheduleId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newStart == null || newStart.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (newEnd == null || newEnd.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        if (minimumWorkTime == null || minimumWorkTime.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 检查重复
        ArrayList<String> scheduleName = departmentMapper.checkDuplicateExtraWorkTimeId(newScheduleId);
        if (scheduleName.size() != 0) {
            return SysResult.build(201, "规则编号重复");
        }
        // 添加
        ExtraWorkRule extraWorkRule = new ExtraWorkRule();
        extraWorkRule.setExtraWorkTimeScheduleId(newScheduleId);
        extraWorkRule.setStartWork(newStart);
        extraWorkRule.setEndWork(newEnd);
        extraWorkRule.setMinimumWorkTime(minimumWorkTime);
        departmentMapper.addExtraWorkRule(extraWorkRule);
        return SysResult.build(200, "添加成功");
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/deleteExtraWorkSchedule")
    public SysResult deleteExtraWorkSchedule(String workTimeId){
        if (workTimeId == null || workTimeId.equals("")) {
            return SysResult.build(201, "参数为空");
        }
        // 检查该id是否正在使用
        ArrayList<String> scheduleName = departmentMapper.checkExtraWorkTimeIdInUse(workTimeId);
        if (scheduleName.size() != 0) {
            // 说明有部门正在使用该id
            return SysResult.build(201, "无法删除正在使用的id");
        }
        departmentMapper.deleteExtraWorkSchedule(workTimeId);
        return SysResult.build(200,"删除成功");
    }

    @RequestMapping("/countDepartment")
    public SysResult countDepartment(String cond, String searchType){
        if(searchType == null || searchType.equals("")){
            return SysResult.build(201,"参数为空");
        }
        return generalService.getRow2("count(0)","department",cond,searchType,"OR",1,1000);
    }

    @RequestMapping("/countHoliday")
    public SysResult countHoliday(String startDate, String endDate, String searchInfo){
        if(startDate == null || startDate.equals("")){
            return SysResult.build(201,"参数为空");
        }
        if(endDate == null || endDate.equals("")){
            return SysResult.build(201,"参数为空");
        }

        //防止日期格式不同
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start, end;
        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            return SysResult.build(201, "写入请假记录出错    " + e);
        }
        startDate = dateFormat.format(start);
        endDate = dateFormat.format(end);

        String[][] cond;
        if(searchInfo.equals("")){
            return SysResult.build(200,"获取成功", holidayMapper.countAllHoliday(startDate,endDate));
        }else{
           searchInfo = "\'%"+searchInfo+"%\'";
            return SysResult.build(200,"获取成功", holidayMapper.countHoliday(startDate,endDate,searchInfo));
        }


    }


    @RequestMapping("/countSystemUser")
    public SysResult countSystemUser(String info){
        if(info == null || info.equals("")){
            // 返回全部
            return SysResult.build(200,"查询成功",shiroMapper.countAllUser());
        }else{
            info = "\'%" + info + "%\'";
            return SysResult.build(200,"查询成功",shiroMapper.countByCond(info));
        }
    }

    @RequestMapping("/getDayOffOptions")
    public SysResult getDayOffOptions(){
        try {
            return SysResult.build(200, "获取成功", holidayMapper.getDayOffOptions());
        }catch (Exception e){
            return SysResult.build(201,""+e);
        }
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/deleteDayOffType")
    public SysResult deleteDayOffType(String dayOffType){
        if(dayOffType == null || dayOffType.equals("")){
            return SysResult.build(201,"参数为空");
        }
        try {
            holidayMapper.deleteDayOffType(dayOffType);
            return SysResult.build(200, "删除成功");
        }catch (Exception e){
            return SysResult.build(201,""+e);
        }
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/addDayOffType")
    public SysResult addDayOffType(String dayOffType, String description){
        if(dayOffType == null || dayOffType.equals("")){
            return SysResult.build(201,"参数为空");
        }
        try {
            // 验证编号是否重复
            ArrayList<Object> tempResult = generalService.quickGet("day_off_type","day_off_types_form","day_off_type",dayOffType);
            if(tempResult.size() != 0){
                return SysResult.build(201,"编号重复");
            }
            // 编号未重复
            holidayMapper.addDayOffType(dayOffType,description);
            return SysResult.build(200, "添加成功");
        }catch (Exception e){
            return SysResult.build(201,""+e);
        }
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/editDayOffType")
    public SysResult editDayOffType(String dayOffType, String description, String id){
        if(dayOffType == null || dayOffType.equals("")){
            return SysResult.build(201,"参数为空");
        }
        if(id == null || id.equals("")){
            return SysResult.build(201,"参数为空");
        }
        try {
            // 验证编号是否重复
            ArrayList<Object> tempResult = generalService.quickGet("id","day_off_types_form","day_off_type",dayOffType);
            if(tempResult.size() != 0){
                // 验证id是否一致
                if(!id.equals(tempResult.get(0)+"")) {
                    return SysResult.build(201, "编号重复");
                }
            }
            // 编号未重复
            holidayMapper.editDayOffType(dayOffType,description,id);
            return SysResult.build(200, "修改成功");
        }catch (Exception e){
            return SysResult.build(201,""+e);
        }
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/getRoleOptions")
    public SysResult getRoleOptions(){
        try {
            String[] cond = {"id>", "0"};
            ArrayList<String> roles = generalService.quickSearch("name", "sysrole", cond, "0");
            return SysResult.build(200, "获取成功", roles);
        }catch (Exception e){
            return SysResult.build(201,"获取失败");
        }
    }
}
