package backend.clockin.pojo.applyRecord;

import java.util.Date;

public class ApplicationReviewList {

    private Integer id;
    private String applicationId;
    private String uid;
    private String reviewers;
    private String reviewList;
    private String status;
    private Date created;
    private Date updated;

    public String getReviewers() {
        return reviewers;
    }

    public void setReviewers(String reviewers) {
        this.reviewers = reviewers;
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

    public String getReviewList() {
        return reviewList;
    }

    public void setReviewList(String reviewList) {
        this.reviewList = reviewList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
