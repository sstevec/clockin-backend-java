package backend.clockin.pojo.clockIn;

import java.util.Date;

public class ClockIn {

    private Integer id;
    private String uid;
    private String account;
    private String name;
    private String startWork;
    private String endWork;
    private String startExtraWork;
    private String endExtraWork;
    private String clockIn;
    private String clockInStatus;
    private String clockOut;
    private String clockOutStatus;
    private String workHour;
    private String extraWorkHour;
    private Integer status;
    private Date created;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartWork() {
        return startWork;
    }

    public void setStartWork(String startWork) {
        this.startWork = startWork;
    }

    public String getEndWork() {
        return endWork;
    }

    public void setEndWork(String endWork) {
        this.endWork = endWork;
    }

    public String getStartExtraWork() {
        return startExtraWork;
    }

    public void setStartExtraWork(String startExtraWork) {
        this.startExtraWork = startExtraWork;
    }

    public String getEndExtraWork() {
        return endExtraWork;
    }

    public void setEndExtraWork(String endExtraWork) {
        this.endExtraWork = endExtraWork;
    }

    public String getClockInStatus() {
        return clockInStatus;
    }

    public void setClockInStatus(String clockInStatus) {
        this.clockInStatus = clockInStatus;
    }

    public String getClockIn() {
        return clockIn;
    }

    public void setClockIn(String clockIn) {
        this.clockIn = clockIn;
    }

    public String getClockOut() {
        return clockOut;
    }

    public void setClockOut(String clockOut) {
        this.clockOut = clockOut;
    }

    public String getClockOutStatus() {
        return clockOutStatus;
    }

    public void setClockOutStatus(String clockOutStatus) {
        this.clockOutStatus = clockOutStatus;
    }

    public String getWorkHour() {
        return workHour;
    }

    public void setWorkHour(String workHour) {
        this.workHour = workHour;
    }

    public String getExtraWorkHour() {
        return extraWorkHour;
    }

    public void setExtraWorkHour(String extraWorkHour) {
        this.extraWorkHour = extraWorkHour;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
