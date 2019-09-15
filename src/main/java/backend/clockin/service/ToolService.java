package backend.clockin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public interface ToolService {

    Double getTimeSub(String start, String end);

    ArrayList<LinkedHashMap<String, String>> objectToString(ArrayList<LinkedHashMap<String, Object>> temp);

    String camelToSlash(String oldString);

    String uploadFile(byte[] file, String filePath, String fileName) throws Exception;

    Boolean addNoonBreak(String uid, Date day);

    Boolean checkDuplicatedNoonBreak(String uid, Date day);
}
