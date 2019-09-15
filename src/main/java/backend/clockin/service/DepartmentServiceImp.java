package backend.clockin.service;

import backend.clockin.mapper.DepartmentMapper;
import backend.clockin.mapper.StaffMapper;
import backend.clockin.pojo.department.Department;
import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class DepartmentServiceImp implements DepartmentService {

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    StaffMapper staffMapper;

    @Autowired
    GeneralService generalService;

    @Override
    public SysResult addDepartment(Department department) {
        // 验证departmentId 和name有无重复
        ArrayList<String> duplicate = departmentMapper.checkDuplicate(department.getDepartmentId(), department.getName());
        if (duplicate.size() != 0) {
            return SysResult.build(201, "部门名称重复");
        } else {
            department.setStaffing("0");
            String chargerName = department.getChargerName();
            ArrayList<String> chargerUidList = staffMapper.getUidByUserName(chargerName);
            if (chargerUidList.size() == 0) {
                return SysResult.build(201, "管理者名称错误, 无法取得管理者的信息");
            }
            department.setChargerUid(chargerUidList.get(0));
            departmentMapper.addDepartment(department);
            return SysResult.build(200, "添加部门成功");
        }
    }

    @Override
    public SysResult deleteDepartment(String departmentId, String name) {
        try {
            // 验证部门是否正在使用
            ArrayList<Object> checkDepartmentInUse = generalService.quickGet("department_id","workers","department_id",departmentId);
            if(checkDepartmentInUse.size() != 0){
                return SysResult.build(201,"部门中仍有职员");
            }
            departmentMapper.deleteDepartment(departmentId, name);
            return SysResult.build(200,"删除成功");
        } catch (Exception e) {
            return SysResult.build(201,"删除部门失败");
        }
    }

    @Override
    public SysResult changeDepartment(String id, HashMap<String, String> info){
        String chargerName = info.get("charger_name");
        ArrayList<String> chargerUidList = staffMapper.getUidByUserName(chargerName);
        if (chargerUidList.size() == 0) {
            return SysResult.build(201, "管理者名称错误, 无法取得管理者的信息");
        }else{
            // 管理者验证正常, 执行修改
            return generalService.changeRow("department",info,id);
        }
    }

    @Override
    public SysResult updateStaffNumber(){
        ArrayList<String> userDepartmentIdList = departmentMapper.getAllUserDepartmentId();
        ArrayList<String> departmentList = departmentMapper.getAllDepartmentId();
        int departmentNum = departmentList.size();
        for(int i = 0 ;i<departmentNum; i++){
            String departmentId = departmentList.get(i);
            int staffNum = 0;
            int userSize = userDepartmentIdList.size();
            for(int j = 0; j<userSize;){
                if(userDepartmentIdList.get(j).equals(departmentId)){
                    staffNum++;
                    userDepartmentIdList.remove(j);
                    userSize = userDepartmentIdList.size();
                }else{
                    j++;
                }
            }
            departmentMapper.updateStaffing(staffNum, departmentId);
        }
        return SysResult.build(200,"更新部门人员数据完成");
    }
}
