package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

import java.util.ArrayList;
import java.util.Date;

public interface ApplyService {

    // 添加请假记录
    SysResult addDayOffRecord(String offStartDate, String offEndDate, String offStartTime, String offEndTime,
                              String uid, String name,String offType, String offDescription);

    // 更新假期剩余时间
    SysResult updateOffHour(String type, Double num, String uid);

    // 添加完请假记录后重新计算打卡时间
    SysResult recalculateClockInRecord(ArrayList<Date> dates, String uid);

    // 重新计算打卡时间之后更新上班时长
    SysResult recalculateWorkTime(Date date, String uid);

    // 整合请假记录
    SysResult mergeDayOffRecord(Date date, String uid);

    // 添加加班记录, 加班时间全按加班记录算, 打卡表的不算
    SysResult addExtraWorkRecord(String uid,String applicationId, String date, String workStart, String workEnd, String type);

    // 提交申请
    SysResult addApplyRecord(String uid, String applicationType,String applicationDetail,String applicationDescription);

    // 删除申请(如果是未批准的申请则可以直接删除，如果是已批准的申请则变为提交一个新的删除申请）
    SysResult deleteApplyRecord(String applicationId, String uid, String applicationDescription);

    // 修改申请(如果是未批准的可以直接修改，如果批准了就不允许修改)
    SysResult changeApplyRecord(String applicationId,String applicationDetail,String applicationDescription);

    // 查看申请
    SysResult getApplyRecord(String cond, String pageNum, String pageMax);

    // 审批申请生效(这里面会有N多联动项)
    SysResult applicationPermit(String applicationId, String targetStatus);

    // 审批流程
    SysResult reviewApplication(String reviewerName, String applicationId, String targetStatus);

    // 计算各类假期，节假日 > 调休 > 年假 > 请假
    SysResult countDayOffHour(Date date, String uid);

    // 根据获得的时间计算放假时长
    SysResult calculateOffTime(String uid, String startDate, String startTime, String endDate, String endTime, String offType);

    // 获取一共有多少记录
    SysResult countMyApplication(String cond);

    // 获取审批人需要审批的记录
    SysResult getReviewApplyRecord(String cond, String pageNum, String pageMax);

    // 获取一共有多少审批者的浏览记录
    SysResult reviewerCountApplication(String cond);
}
