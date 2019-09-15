package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;

public interface TimerService {

    // 计时器， 1月和7月激活调用打卡服务的方法，创建新的打卡表
    SysResult createClockInForm();

    // 计时器，每天凌晨1点，读取人员及相关信息，写入新的打卡表
    SysResult addClockInRecord();

    // 计时器，每天0点，结算当日打卡记录
    SysResult calculateClockIn();
}
