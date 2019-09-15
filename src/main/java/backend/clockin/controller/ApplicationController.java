package backend.clockin.controller;


import backend.clockin.mapper.ApplicationMapper;
import backend.clockin.mapper.ClockInMapper;
import backend.clockin.pojo.applyRecord.ApplicationReviewList;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.ApplyService;
import backend.clockin.service.ClockInService;
import backend.clockin.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/apply")
public class ApplicationController {

    @Autowired
    ApplyService applyService;

    @Autowired
    ClockInService clockInService;

    @Autowired
    GeneralService generalService;

    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    ClockInMapper clockInMapper;

    @RequestMapping("/addApplication")
    public SysResult addApplication(String uid, String applicationType, String applicationDetail, String applicationDescription) {
        if (uid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationType == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationDetail == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationDescription == null) {
            return SysResult.build(400, "参数为空");
        }
        return applyService.addApplyRecord(uid, applicationType, applicationDetail, applicationDescription);
    }

    @RequestMapping("/deleteApplication")
    public SysResult deleteApplication(String applicationId, String uid, String applicationDescription) {
        if (uid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationId == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationDescription == null) {
            return SysResult.build(400, "参数为空");
        }
        return applyService.deleteApplyRecord(applicationId, uid, applicationDescription);
    }

    @RequestMapping("/changeApplication")
    public SysResult changeApplication(String applicationId, String applicationDetail, String applicationDescription) {
        if (applicationDetail == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationId == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationDescription == null) {
            return SysResult.build(400, "参数为空");
        }
        return applyService.changeApplyRecord(applicationId, applicationDetail, applicationDescription);
    }

    @RequestMapping("/userGetApplication")
    public SysResult userGetApplication(String uid, String status, String type, String pageNum, String pageMax) {
        if (uid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (status == null) {
            return SysResult.build(400, "参数为空");
        }
        if (type == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageNum == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageMax == null) {
            return SysResult.build(400, "参数为空");
        }
        String cond = "uid,=," + uid;

        if (!type.equals("All")) {
            cond = cond + "," + "application_type,=," + type;
        }

        if (status.equals("-1")) {
            // 获取该uid下全部记录
            return applyService.getApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("0")) {
            // 获取所有未审批的记录
            cond = cond + "," + "status,=,0";
            return applyService.getApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("1")) {
            // 获取所有通过审批的记录
            cond = cond + "," + "status,=,1";
            return applyService.getApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("2")) {
            // 获取所有被拒绝的记录
            cond = cond + "," + "status,=,2";
            return applyService.getApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("3")) {
            // 获取所有被撤销的记录
            cond = cond + "," + "status,=,3";
            return applyService.getApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("4")) {
            // 获取所有审批中的记录
            cond = cond + "," + "status,=,4";
            return applyService.getApplyRecord(cond, pageNum, pageMax);
        } else {
            return SysResult.build(201, "无效的状态");
        }

    }

    @RequestMapping("/reviewerGetApplication")
    public SysResult reviewerGetApplication(String reviewerUid, String status, String type, String pageNum, String pageMax) {
        if (reviewerUid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (status == null) {
            return SysResult.build(400, "参数为空");
        }
        if (type == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageNum == null) {
            return SysResult.build(400, "参数为空");
        }
        if (pageMax == null) {
            return SysResult.build(400, "参数为空");
        }

        String cond = "reviewers,," + reviewerUid;
        if (!type.equals("All")) {
            cond = cond + ",application_type,," + type;
        }

        if (status.equals("-1")) {
            // 获取全部记录
            return applyService.getReviewApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("0")) {
            // 获取所有未审批的记录
            cond = cond + "," + "status,,0";
            return applyService.getReviewApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("1")) {
            // 获取所有通过审批的记录
            cond = cond + "," + "status,,1";
            return applyService.getReviewApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("2")) {
            // 获取所有被拒绝的记录
            cond = cond + "," + "status,,2";
            return applyService.getReviewApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("3")) {
            // 获取所有被撤销的记录
            cond = cond + "," + "status,,3";
            return applyService.getReviewApplyRecord(cond, pageNum, pageMax);
        } else if (status.equals("4")) {
            // 获取所有审批中的记录
            cond = cond + "," + "status,,4";
            return applyService.getReviewApplyRecord(cond, pageNum, pageMax);
        } else {
            return SysResult.build(201, "无效的状态");
        }

    }

    @RequestMapping("/reviewApplication")
    public SysResult reviewApplication(String applicationId) {
        if (applicationId == null) {
            return SysResult.build(400, "参数为空");
        }
        String cond = "application_id,=," + applicationId;
        SysResult result = applyService.getApplyRecord(cond, "1", "10");
        if (result.getData() == null) {
            return SysResult.build(201, "申请记录不存在");
        } else {
            return result;
        }
    }

    @RequestMapping("/changeApplicationStatus")
    public SysResult changeApplicationStatus(String applicationId, String targetStatus) {
        if (applicationId == null) {
            return SysResult.build(400, "参数为空");
        }
        if (targetStatus == null) {
            return SysResult.build(400, "参数为空");
        }
        return applyService.applicationPermit(applicationId, targetStatus);
    }

    @RequestMapping("/getMakeUpOption")
    public SysResult getMakeUpOption(String uid) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        Date today = new Date();
        Date tomorrow = new Date(today.getTime() + 1000 * 3600 * 24);
        Date weekAgo = new Date(today.getTime() - 1000 * 3600 * 24 * 6);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SysResult clockInResult = clockInService.getStatistic(uid, dateFormat.format(weekAgo), dateFormat.format(tomorrow), 1, 1000);

        if (!clockInResult.isOk()) {
            return SysResult.build(201, "获取打卡信息出错：   " + clockInResult.getMsg());
        }
        ArrayList<LinkedHashMap<String, String>> clockInRecord = (ArrayList<LinkedHashMap<String, String>>) clockInResult.getData();

        ArrayList<HashMap<String, String>> makeUpRecord = new ArrayList<>();
        int recordSize = clockInRecord.size();
        for (int i = 0; i < recordSize; i++) {
            if (clockInRecord.get(i).get("clock_in_status").equals("miss")) {
                // 缺上班卡
                HashMap<String, String> tempRecord = new HashMap<>();
                try {
                    String date = dateFormat.format(dateFormat.parse(clockInRecord.get(i).get("created")));
                    tempRecord.put("date", date);
                } catch (ParseException e) {
                    return SysResult.build(201, "转化日期出错    " + e);
                }
                tempRecord.put("type", "上班卡");
                makeUpRecord.add(tempRecord);
            }
            if (clockInRecord.get(i).get("clock_out_status").equals("miss")) {
                // 缺下班卡
                HashMap<String, String> tempRecord = new HashMap<>();
                try {
                    String date = dateFormat.format(dateFormat.parse(clockInRecord.get(i).get("created")));
                    tempRecord.put("date", date);
                } catch (ParseException e) {
                    return SysResult.build(201, "转化日期出错    " + e);
                }
                tempRecord.put("type", "下班卡");
                makeUpRecord.add(tempRecord);
            }
        }
        return SysResult.build(200, "获取缺卡日期成功", makeUpRecord);
    }

    @RequestMapping("/calculateOffTime")
    public SysResult calculateOffTime(String uid, String startDate, String startTime, String endDate, String endTime, String offType) {
        if (uid == null || uid.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        if (startDate == null) {
            return SysResult.build(400, "参数为空");
        }
        if (startTime == null) {
            return SysResult.build(400, "参数为空");
        }
        if (endDate == null) {
            return SysResult.build(400, "参数为空");
        }
        if (endTime == null) {
            return SysResult.build(400, "参数为空");
        }
        if (offType == null) {
            return SysResult.build(400, "参数为空");
        }
        return applyService.calculateOffTime(uid, startDate, startTime, endDate, endTime, offType);
    }

    @RequestMapping("/getReviewerName")
    public SysResult getReviewerName(String reviewers) {
        if (reviewers == null || reviewers.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        String[] reviewersArray = reviewers.split(",");
        String cond = "";
        for (String uid : reviewersArray
        ) {
            cond = cond + "uid,=," + uid + ",";
        }
        cond = cond.substring(0, cond.length() - 1);
        SysResult nameResult = generalService.getRow2("name", "workers", cond, "0", "OR", 1, 10000);
        if (!nameResult.isOk()) {
            return SysResult.build(201, "获取审批人出错");
        }
        return nameResult;
    }

    @RequestMapping("/getReviewProcess")
    public SysResult getReviewProcess(String applicationId) {
        if (applicationId == null || applicationId.equals("")) {
            return SysResult.build(400, "参数为空");
        }
        ApplicationReviewList applicationReviewList = applicationMapper.getReviewList(applicationId);
        String[] reviewers = applicationReviewList.getReviewers().split(",");
        String[] reviewStatus = applicationReviewList.getReviewList().split(",");
        ArrayList<HashMap<String, String>> reviewProcess = new ArrayList<>();

        int reviewerNum = reviewers.length;
        for (int j = 1; j < 4; j++) {
            int statusNum = j;
            String status = "";
            if (j == 3) {
                statusNum = 0;
            }
            if (statusNum == 0) {
                status = "未审核";
            } else if (statusNum == 1) {
                status = "已同意";
            } else if (statusNum == 2) {
                status = "已拒绝";
            }
            for (int i = 0; i < reviewerNum; i++) {
                if (reviewStatus[i].equals("" + statusNum)) {
                    HashMap<String, String> tempProcessPoint = new HashMap<>();
                    LinkedHashMap<String, Object> reviewerInfo = clockInMapper.getWorkerBasicInfo(reviewers[i]);
                    tempProcessPoint.put("name", reviewerInfo.get("name").toString());
                    tempProcessPoint.put("status", status);
                    reviewProcess.add(tempProcessPoint);
                }
            }
        }
        return SysResult.build(200, "获取审核信息成功", reviewProcess);
    }

    @RequestMapping("/makeDecision")
    public SysResult makeDecision(String reviewerUid, String applicationId, String targetStatus) {
        if (reviewerUid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (applicationId == null) {
            return SysResult.build(400, "参数为空");
        }
        if (targetStatus == null) {
            return SysResult.build(400, "参数为空");
        }
        return applyService.reviewApplication(reviewerUid, applicationId, targetStatus);
    }

    @RequestMapping("/countMyApplication")
    public SysResult countMyApplication(String uid, String status, String type) {
        if (uid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (status == null) {
            return SysResult.build(400, "参数为空");
        }
        if (type == null) {
            return SysResult.build(400, "参数为空");
        }

        String cond = "uid,=," + uid;

        if (!type.equals("All")) {
            cond = cond + "," + "application_type,=," + type;
        }

        if (status.equals("-1")) {
            // 获取该uid下全部记录
            return applyService.countMyApplication(cond);
        } else if (status.equals("0")) {
            // 获取所有未审批的记录
            cond = cond + "," + "status,=,0";
            return applyService.countMyApplication(cond);
        } else if (status.equals("1")) {
            // 获取所有通过审批的记录
            cond = cond + "," + "status,=,1";
            return applyService.countMyApplication(cond);
        } else if (status.equals("2")) {
            // 获取所有被拒绝的记录
            cond = cond + "," + "status,=,2";
            return applyService.countMyApplication(cond);
        } else if (status.equals("3")) {
            // 获取所有被撤销的记录
            cond = cond + "," + "status,=,3";
            return applyService.countMyApplication(cond);
        } else if (status.equals("4")) {
            // 获取所有审批中的记录
            cond = cond + "," + "status,=,4";
            return applyService.countMyApplication(cond);
        } else {
            return SysResult.build(201, "无效的状态");
        }
    }

    @RequestMapping("/reviewerCountApplication")
    public SysResult reviewerCountApplication(String reviewerUid, String status, String type) {
        if (reviewerUid == null) {
            return SysResult.build(400, "参数为空");
        }
        if (status == null) {
            return SysResult.build(400, "参数为空");
        }
        if (type == null) {
            return SysResult.build(400, "参数为空");
        }

        String cond = "reviewers,," + reviewerUid;
        if (!type.equals("All")) {
            cond = cond + ",application_type,," + type;
        }

        if (status.equals("-1")) {
            // 获取全部记录
            return applyService.reviewerCountApplication(cond);
        } else if (status.equals("0")) {
            // 获取所有未审批的记录
            cond = cond + "," + "status,,0";
            return applyService.reviewerCountApplication(cond);
        } else if (status.equals("1")) {
            // 获取所有通过审批的记录
            cond = cond + "," + "status,,1";
            return applyService.reviewerCountApplication(cond);
        } else if (status.equals("2")) {
            // 获取所有被拒绝的记录
            cond = cond + "," + "status,,2";
            return applyService.reviewerCountApplication(cond);
        } else if (status.equals("3")) {
            // 获取所有被撤销的记录
            cond = cond + "," + "status,,3";
            return applyService.reviewerCountApplication(cond);
        } else if (status.equals("4")) {
            // 获取所有审批中的记录
            cond = cond + "," + "status,,4";
            return applyService.reviewerCountApplication(cond);
        } else {
            return SysResult.build(201, "无效的状态");
        }

    }
}
