package backend.clockin.pojo.dayOff;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class DayOffOptions {

    private Integer id;
    private String dayOffType;
    private String description;
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

    public String getDayOffType() {
        return dayOffType;
    }

    public void setDayOffType(String dayOffType) {
        this.dayOffType = dayOffType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
