package backend.clockin.pojo.department;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ExtraWorkRule {

    private Integer id;
    private String extraWorkTimeScheduleId;
    private String startWork;
    private String endWork;
    private String minimumWorkTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date created;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updated;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExtraWorkTimeScheduleId() {
        return extraWorkTimeScheduleId;
    }

    public void setExtraWorkTimeScheduleId(String extraWorkTimeScheduleId) {
        this.extraWorkTimeScheduleId = extraWorkTimeScheduleId;
    }

    public String getMinimumWorkTime() {
        return minimumWorkTime;
    }

    public void setMinimumWorkTime(String minimumWorkTime) {
        this.minimumWorkTime = minimumWorkTime;
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
