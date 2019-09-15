package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface ClockInService {

    // 调用时创建新的打卡表，并且更新打卡表日志，将新表设置成当前表
    SysResult creatClockInForm(String month, String year);

    // 调用时调取人员信息，获取上班时间，是否为假期等情况, 写入当天的打卡表，并将状态设为 1
    SysResult addClockInRecord();

    // 根据uid找打打卡信息，并获取状态为 1 的条目，第一次打上班卡并判断是否迟到，第二次更新下班卡时间，并判断是否早退，
    // 但不记录早退
    SysResult clockIn(String uid);

    // 调取所有打卡记录，计算上班时长，是否加班，是否早退，然后将状态设置为 0
    SysResult calculateClockIn();

    // 统计函数，调取一段时间内的打卡记录，并返回一个统计结果
    SysResult getStatistic(String uid, String start, String end, Integer pageNum, Integer pageMax);

    // 管理员统计函数, 调取特定人群在一段时间内的打卡记录
    SysResult managerGetStatistic(ArrayList<String> uidList, String start, String end);

    // 刷uid应该先返回打卡及放假信息，然后如果确认了，那么就调用打卡函数
    SysResult confirmBeforeClockIn(String uid);

    // 获取某人某天的记录
    SysResult getClockInRecordByDate(String uid, String start, String end);

    // 补卡
    SysResult makeUpClockInRecord(String uid, String start, String end, String makeUpType);

    // 获取目标字段的统计信息
    SysResult getTargetColumnStatistic(String targetColumn, String cond);
}
