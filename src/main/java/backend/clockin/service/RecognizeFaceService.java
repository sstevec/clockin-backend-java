package backend.clockin.service;


import backend.clockin.pojo.tool.SysResult;

public interface RecognizeFaceService {

    SysResult addStaffImage(String filePath, String uid);

    SysResult deleteStaffImage(String uid);

    SysResult recogFile(String filePath);

    SysResult save(String path, String uid);
}
