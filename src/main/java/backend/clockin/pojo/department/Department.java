package backend.clockin.pojo.department;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Department {
    private Integer id;
    private String departmentId;
    private String name;
    private String tel;
    private String workPlace;
    private String chargerName;
    private String chargerUid;
    private String workTimeScheduleId;
    private String extraWorkTimeScheduleId;
    private String noonBreakId;
    private String staffing;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date created;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updated;

    public String getNoonBreakId() {
        return noonBreakId;
    }

    public void setNoonBreakId(String noonBreakId) {
        this.noonBreakId = noonBreakId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getWorkPlace() {
        return workPlace;
    }

    public void setWorkPlace(String workPlace) {
        this.workPlace = workPlace;
    }

    public String getChargerName() {
        return chargerName;
    }

    public void setChargerName(String chargerName) {
        this.chargerName = chargerName;
    }

    public String getChargerUid() {
        return chargerUid;
    }

    public void setChargerUid(String chargerUid) {
        this.chargerUid = chargerUid;
    }

    public String getWorkTimeScheduleId() {
        return workTimeScheduleId;
    }

    public void setWorkTimeScheduleId(String workTimeScheduleId) {
        this.workTimeScheduleId = workTimeScheduleId;
    }

    public String getExtraWorkTimeScheduleId() {
        return extraWorkTimeScheduleId;
    }

    public void setExtraWorkTimeScheduleId(String extraWorkTimeScheduleId) {
        this.extraWorkTimeScheduleId = extraWorkTimeScheduleId;
    }

    public String getStaffing() {
        return staffing;
    }

    public void setStaffing(String staffing) {
        this.staffing = staffing;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
