package backend.clockin.pojo.dayOff;

import java.util.Date;

public class DayOffRecord {

    Integer id;
    String uid;
    String name;
    Date offDate;
    String offStart;
    String offEnd;
    String offType;
    String offDescription;
    Date created;
    Date updated;

    public Date getOffDate() {
        return offDate;
    }

    public void setOffDate(Date offDate) {
        this.offDate = offDate;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOffStart() {
        return offStart;
    }

    public void setOffStart(String offStart) {
        this.offStart = offStart;
    }

    public String getOffEnd() {
        return offEnd;
    }

    public void setOffEnd(String offEnd) {
        this.offEnd = offEnd;
    }

    public String getOffType() {
        return offType;
    }

    public void setOffType(String offType) {
        this.offType = offType;
    }

    public String getOffDescription() {
        return offDescription;
    }

    public void setOffDescription(String offDescription) {
        this.offDescription = offDescription;
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
