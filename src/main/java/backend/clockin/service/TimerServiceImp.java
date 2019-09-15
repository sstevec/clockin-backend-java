package backend.clockin.service;

import backend.clockin.pojo.tool.SysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class TimerServiceImp implements TimerService {

    @Autowired
    ClockInService clockInService;
    // 计时器， 每年1月和7月1日，凌晨0:30, 激活调用打卡服务的方法，创建新的打卡表
    @Scheduled(cron="0 30 0 1 1,7 ? ")
    public SysResult createClockInForm()
    {
        Calendar cal = Calendar.getInstance();
        int year, month;
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        return clockInService.creatClockInForm(month+"",year+"");
    }

    // 计时器，每天凌晨1点，读取人员及相关信息，写入新的打卡表
    @Scheduled(cron="0 0 1 ? * MON-FRI")
    public SysResult addClockInRecord(){
        return clockInService.addClockInRecord();
    }

    // 计时器，每天0点1分，结算前日打卡记录
    @Scheduled(cron="0 1 0 ? * *")
    public SysResult calculateClockIn(){
        return clockInService.calculateClockIn();
    }
}
