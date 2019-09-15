package backend.clockin.pojo.clockIn;

public class ConfirmInfo {

    String name;
    String account;
    String uid;
    String tel;
    String department;
    String position;
    String workId;


    Boolean needWork;
    String startWork;
    String endWork;
    String startExtraWork;
    String endExtraWork;
    String clockIn;
    String clockInStatus;
    String clockOut;
    String clockOutStatus;

    Boolean haveDayOff;
    String startOff;
    String endOff;
    String offType;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Boolean getNeedWork() {
        return needWork;
    }

    public void setNeedWork(Boolean needWork) {
        this.needWork = needWork;
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

    public String getClockIn() {
        return clockIn;
    }

    public void setClockIn(String clockIn) {
        this.clockIn = clockIn;
    }

    public String getClockInStatus() {
        return clockInStatus;
    }

    public void setClockInStatus(String clockInStatus) {
        this.clockInStatus = clockInStatus;
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

    public Boolean getHaveDayOff() {
        return haveDayOff;
    }

    public void setHaveDayOff(Boolean haveDayOff) {
        this.haveDayOff = haveDayOff;
    }

    public String getStartOff() {
        return startOff;
    }

    public void setStartOff(String startOff) {
        this.startOff = startOff;
    }

    public String getEndOff() {
        return endOff;
    }

    public void setEndOff(String endOff) {
        this.endOff = endOff;
    }

    public String getOffType() {
        return offType;
    }

    public void setOffType(String offType) {
        this.offType = offType;
    }
}
