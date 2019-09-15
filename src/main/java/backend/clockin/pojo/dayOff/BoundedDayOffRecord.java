package backend.clockin.pojo.dayOff;

import java.util.Date;

public class BoundedDayOffRecord {

    private Integer id;
    String uid;
    Date offDate;
    String startBoundId;
    String startBound;
    String endBoundId;
    String endBound;
    Date created;
    Date updated;

    public String getStartBoundId() {
        return startBoundId;
    }

    public void setStartBoundId(String startBoundId) {
        this.startBoundId = startBoundId;
    }

    public String getEndBoundId() {
        return endBoundId;
    }

    public void setEndBoundId(String endBoundId) {
        this.endBoundId = endBoundId;
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

    public Date getOffDate() {
        return offDate;
    }

    public void setOffDate(Date offDate) {
        this.offDate = offDate;
    }

    public String getStartBound() {
        return startBound;
    }

    public void setStartBound(String startBound) {
        this.startBound = startBound;
    }

    public String getEndBound() {
        return endBound;
    }

    public void setEndBound(String endBound) {
        this.endBound = endBound;
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
