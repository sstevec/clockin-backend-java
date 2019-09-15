package backend.clockin.mapper;

import backend.clockin.pojo.department.ClockInRule;
import backend.clockin.pojo.department.Department;
import backend.clockin.pojo.department.ExtraWorkRule;
import backend.clockin.pojo.department.NoonBreakRule;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

public interface DepartmentMapper {

    ArrayList<String> getUidByDepartmentId(@Param("departmentId") String departmentId,@Param("start") Integer start,@Param("num") Integer num);

    ArrayList<String> getAllUid(@Param("start") Integer start,@Param("num") Integer num);

    ArrayList<String> checkDuplicate(@Param("departmentId") String departmentId,@Param("name") String name);

    void addDepartment(@Param("department") Department department);

    void deleteDepartment(@Param("departmentId") String departmentId,@Param("name") String name);

    ArrayList<String> getAllUserDepartmentId();

    ArrayList<String> getAllDepartmentId();

    void updateStaffing(@Param("staffing") int staffNum,@Param("departmentId") String departmentId);

    ArrayList<ClockInRule> getAllClockInRule();

    ArrayList<String> checkDuplicateWorkTimeId(@Param("newScheduleId") String newScheduleId);

    void updateClockInRule(@Param("clockInRule") ClockInRule clockInRule, @Param("oldId") String oldId);

    void updateDepartmentWorkTimeSchedule(@Param("newSchedule") String newScheduleId,@Param("oldSchedule") String oldId);

    void addClockInRule(ClockInRule clockInRule);

    void deleteClockInSchedule(@Param("workTimeId") String workTimeId);

    ArrayList<String> checkWorkTimeIdInUse(@Param("workTimeId") String workTimeId);


    ArrayList<ExtraWorkRule> getAllExtraWorkRule();

    ArrayList<String> checkDuplicateExtraWorkTimeId(@Param("newScheduleId") String newScheduleId);

    void updateExtraWorkRule(@Param("extraWorkRule") ExtraWorkRule extraWorkRule, @Param("oldId") String oldId);

    void updateDepartmentExtraWorkTimeSchedule(@Param("newSchedule") String newScheduleId,@Param("oldSchedule") String oldId);

    void addExtraWorkRule(ExtraWorkRule extraWorkRule);

    void deleteExtraWorkSchedule(@Param("workTimeId") String workTimeId);

    ArrayList<String> checkExtraWorkTimeIdInUse(@Param("workTimeId") String workTimeId);

    ArrayList<NoonBreakRule> getAllNoonBreakRule();

    ArrayList<String> checkDuplicateNoonBreakId(@Param("newScheduleId") String newScheduleId);

    void updateDepartmentNoonBreakSchedule(@Param("newSchedule") String newScheduleId,@Param("oldSchedule") String oldId);

    void updateNoonBreakRule(@Param("noonBreakRule") NoonBreakRule noonBreakRule,@Param("oldId") String oldId);

    void addNoonBreakRule(NoonBreakRule noonBreakRule);

    ArrayList<String> checkNoonBreakIdInUse(@Param("noonBreakId") String noonBreakId);

    void deleteNoonBreakSchedule(@Param("noonBreakId") String noonBreakId);
}
