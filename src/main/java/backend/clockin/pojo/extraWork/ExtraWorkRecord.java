package backend.clockin.pojo.extraWork;

import java.util.Date;

public class ExtraWorkRecord {

    private Integer id;
    private String applicationId;
    private String uid;
    private Date date;
    private String extraWorkStart;
    private String extraWorkEnd;
    private String extraWorkHour;
    private String type;
    private Date created;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getExtraWorkStart() {
        return extraWorkStart;
    }

    public void setExtraWorkStart(String extraWorkStart) {
        this.extraWorkStart = extraWorkStart;
    }

    public String getExtraWorkEnd() {
        return extraWorkEnd;
    }

    public void setExtraWorkEnd(String extraWorkEnd) {
        this.extraWorkEnd = extraWorkEnd;
    }

    public String getExtraWorkHour() {
        return extraWorkHour;
    }

    public void setExtraWorkHour(String extraWorkHour) {
        this.extraWorkHour = extraWorkHour;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
