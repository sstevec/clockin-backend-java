package backend.clockin.pojo.dayOff;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Holiday {
    private Integer id;
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private Date date;
    private String name;
    private String dayOffTypes;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDayOffTypes() {
        return dayOffTypes;
    }

    public void setDayOffTypes(String dayOffTypes) {
        this.dayOffTypes = dayOffTypes;
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
