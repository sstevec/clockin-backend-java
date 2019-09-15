package backend.clockin.pojo.applyRecord;

import java.util.Date;

public class ApplicationRecord {

    private Integer id;
    private String applicationId;
    private String uid;
    private String applicationType;
    private String applicationDetail;
    private String applicationDescription;
    private String manager;
    private String reviewers;
    private String status;
    private Date created;
    private Date updated;


/*
*  请假记录/放假记录 对应表格 day_off_record,  对应Type DayOff
*   applicationDetail 格式：String offStartDate, String offEndDate, String offStartTime, String offEndTime,
                            String name,String offType
*
*  撤销记录 对应表格 application_record, 对应Type RollBack
*   applicationDetail 格式：String applicationId, String applicationType
*
*  补卡记录 对应表格 clock_in_???_???? 对应Type MakeUp
*   applicationDetail 格式：String start, String makeUpType
*
*  加班记录 对应表格 extra_work_record 对应Type ExtraWork
*   applicationDetail 格式：String date, String workStart, String workEnd, String type
* */

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

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getApplicationDetail() {
        return applicationDetail;
    }

    public void setApplicationDetail(String applicationDetail) {
        this.applicationDetail = applicationDetail;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
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

    public String getReviewers() {
        return reviewers;
    }

    public void setReviewers(String reviewers) {
        this.reviewers = reviewers;
    }
}
