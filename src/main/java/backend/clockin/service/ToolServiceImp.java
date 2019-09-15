package backend.clockin.service;

import backend.clockin.mapper.ApplicationMapper;
import backend.clockin.mapper.ClockInMapper;
import backend.clockin.pojo.dayOff.DayOffRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ToolServiceImp implements ToolService{

    @Autowired
    GeneralService generalService;

    @Autowired
    ClockInMapper clockInMapper;

    @Autowired
    ApplicationMapper applicationMapper;

    @Override
    public Double getTimeSub(String start, String end)
    {
        String[] startBoundTime = start.split(":");
        int startBoundHour = Integer.valueOf(startBoundTime[0]);
        int startBoundMinute = Integer.valueOf(startBoundTime[1]);

        String[] endBoundTime = end.split(":");
        int endBoundHour = Integer.valueOf(endBoundTime[0]);
        int endBoundMinute = Integer.valueOf(endBoundTime[1]);

        return endBoundHour+endBoundMinute/60.0-startBoundHour-startBoundMinute/60.0;
    }

    @Override
    public ArrayList<LinkedHashMap<String, String>> objectToString(ArrayList<LinkedHashMap<String, Object>> data)
    {
        ArrayList<LinkedHashMap<String, String>> newData = new ArrayList<>();
        if(data.size() != 0)
        {
            int numRow = data.size();
            for(int i = 0; i<numRow; i++) {
                // 取得该行字段名
                LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
                int numberColumn = data.get(i).size();
                String[] column_name = new String[numberColumn];
                Set<String> col_name_set = data.get(i).keySet();
                col_name_set.toArray(column_name);
                // 循环字段名, 将object转化为String
                for(int j = 0 ;j<numberColumn; j++){
                    if(data.get(i).get(column_name[j]) == null){
                        tempMap.put(column_name[j],"");
                    }else {
                        tempMap.put(column_name[j],data.get(i).get(column_name[j]).toString());
                    }
                }
                newData.add(tempMap);
            }
            return newData;
        }
        return new ArrayList<>();
    }

    @Override
    public String camelToSlash(String oldString){
        char[] old = oldString.toCharArray();
        StringBuilder newString = new StringBuilder();
        for (char c : old) {
            if(c >= 65 && c <= 90){
                newString.append("_").append((char)(c + 32));
            }else{
                newString.append(c);
            }
        }
        return newString.toString();
    }

    @Override
    public String uploadFile(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + "/" + fileName);
        out.write(file);
        out.flush();
        out.close();
        return filePath + "/" + fileName;
    }

    @Override
    public Boolean addNoonBreak(String uid, Date day){
        // 如果不重复
        if(!checkDuplicatedNoonBreak(uid,day)) {
            // 根据uid 获取部门id
            ArrayList<Object> departmentId = generalService.quickGet("department_id","workers","uid",uid);
            if(departmentId.size() == 0){
                return false;
            }
            String department_id = departmentId.get(0).toString();

            // 获取午休id
            ArrayList<Object> noonBreakId = generalService.quickGet("noon_break_id","department","department_id",department_id);
            if(noonBreakId.size() == 0){
                return false;
            }
            String noon_break_id = noonBreakId.get(0).toString();

            // 获取午休的全部信息
            HashMap<String, String> noonBreakResult = clockInMapper.getNoonBreakInfo(noon_break_id);
            String startTime = noonBreakResult.get("start_time");
            String endTime = noonBreakResult.get("end_time");

            if(startTime == null || endTime == null){
                return false;
            }

            DayOffRecord breakRecord = new DayOffRecord();
            breakRecord.setOffType("NoonBreak");
            breakRecord.setOffStart(startTime);
            breakRecord.setOffEnd(endTime);
            breakRecord.setOffDate(day);
            breakRecord.setUid(uid);
            breakRecord.setName("NoonBreak");
            breakRecord.setOffDescription("");

            clockInMapper.addDayOffRecord(breakRecord);
            return true;
        }else{
            // 重复了，不需要添加
            return true;
        }
    }

    @Override
    public Boolean checkDuplicatedNoonBreak(String uid, Date day){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<DayOffRecord> records = applicationMapper.getDayOffRecordByType(uid, dateFormat.format(day), "NoonBreak");
        if(records.size() == 0){
            return false;
        }else{
            return true;
        }
    }
}
