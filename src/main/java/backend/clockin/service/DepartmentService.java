package backend.clockin.service;

import backend.clockin.pojo.department.Department;
import backend.clockin.pojo.tool.SysResult;

import java.util.HashMap;

public interface DepartmentService {

    SysResult addDepartment(Department department);

    SysResult deleteDepartment(String departmentId, String name);

    SysResult changeDepartment(String id, HashMap<String, String> info);

    SysResult updateStaffNumber();
}
