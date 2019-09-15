package backend.clockin.service;


import backend.clockin.mapper.StaffMapper;
import backend.clockin.pojo.faceRec.PersonsVo;
import backend.clockin.pojo.tool.SysResult;
import com.xunsiya.modules.algorithm.*;
import com.xunsiya.modules.algorithm.face.FaceResultParse;
import com.xunsiya.modules.algorithm.face.FaceSingleRecog;
import com.xunsiya.modules.algorithm.face.FaceUpdateDb;
import com.xunsiya.modules.algorithm.face.vo.FaceResultVo;
import com.xunsiya.modules.algorithm.face.vo.MatchDetect;
import com.xunsiya.modules.algorithm.face.vo.MatchFace;
import com.xunsiya.modules.algorithm.rpc.RPCReturnCode;
import com.xunsiya.tools.file.cryptography.FileMD5Util;
import com.xunsiya.tools.file.type.FileTypeJudge;
import com.xunsiya.tools.file.type.MediaFileType;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class RecognizeFaceServiceImp implements RecognizeFaceService {

    @Value("${face.linux-root-path}")
    private String facePath;

    @Value("${face.linux-show-path}")
    private String faceShow;

    @Value("${algorithm.face-xmlrpc-name}")
    private String faceModuleName;

    @Value("${face.operation-type}")
    private String operationType;

    public static final String ANALYSIS_TXT_SUFFIX = ".txt";

    @Autowired
    GeneralService generalService;

    @Autowired
    StaffMapper staffMapper;

    // 录入人脸

    /**
     * 保存人物图片
     *
     * @return
     */
    @Override
    public SysResult addStaffImage(String filePath, String uid) {
        File faceFile = new File(filePath);
        if (!faceFile.exists()) {
            return SysResult.build(201, "待加载的文件不存在或者是一个目录");
        }

        try {
            if (!FileTypeJudge.isImage(faceFile.getAbsolutePath())) {
                return SysResult.build(201, "待加载的文件不是图片");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 文件名称
        String mediaName = faceFile.getName();

        // 获取文件的md5名称，并准备将其储存在worker的image下用来做关联
        String md5 = "";
        try {
            md5 = FileMD5Util.getMD5FromFile(faceFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 媒体文件存储目录
        String destFaceDir = facePath + "clock_in" + "/" + uid;
        if (!new File(destFaceDir).exists()) {
            new File(destFaceDir).mkdirs();
        }

        // 文件后缀
        String type = mediaName.indexOf(".") != -1 ? mediaName.substring(mediaName.lastIndexOf(".") + 1, mediaName.length()) : null;
        // 文件名称
        String name = type != null ? md5 + "." + type : md5 + ".jpg";

        String destFilePath = destFaceDir + "/" + mediaName;
        try {
            //将文件移到指定路径下,并且重命名
            FileUtils.moveFile(faceFile, new File(destFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 查询是否存在相同的图片
        ArrayList<Object> imageMd5List = generalService.quickGet("image", "workers", "image", md5);
        if (imageMd5List.size() != 0) {
            return SysResult.build(400, "image already exist");
        }

        // 保存人脸指纹库
        SysResult sysResult = saveAlg(destFilePath, md5, uid);
        if (!sysResult.isOk()) {
            return sysResult;
        }

        // 添加md5至worker的image栏
        staffMapper.setWorkerMd5(md5, uid);
        return SysResult.build(200, "Add success");
    }

    /**
     * 保存人脸指纹库
     *
     * @param destFilePath 图片地址
     * @param md5          图片md5
     * @return
     */
    private SysResult saveAlg(String destFilePath, String md5, String uid) {
        // 初始化更新人脸指纹库
        FaceUpdateDb faceUpdateDb = new FaceUpdateDb(faceModuleName, OpType.ADD, "clock_in", uid, destFilePath, md5);
        Map<String, Object> map = null;
        try {
            // 添加
            map = faceUpdateDb.updateSingleDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (CollectionUtils.isEmpty(map)) {
            return SysResult.build(201, "报错:XMLRPC无返回");
        }

        String faceId = map.get("result").toString();
        int result = Integer.parseInt(faceId);
        if (result >= RPCReturnCode.SUCCESS.getValue()) {
            //日志文件记录
        } else {
            // 日志文件记录错误
            if (result != RPCReturnCode.OBJ_EXIST.getValue()) {
                //已经添加此指纹
                return SysResult.build(201, map.get("url") + "");
            } else {
                return SysResult.build(201, "指纹已存在");
            }
        }
        return SysResult.build(200, "Success");
    }

    /**
     * 删除人物图片
     * uid worker uid
     *
     * @return
     */
    @Override
    public SysResult deleteStaffImage(String uid) {
        // 当前用户
        String companyId = "clock_in";
        // 查询要删除的图片
        String Md5 = staffMapper.getMd5ByUid(uid);
        if (Md5 == null) {
            return SysResult.build(201, "Worker does not exist, uid is wrong");
        }

        // 初始化更新人脸指纹库
        FaceUpdateDb faceUpdateDb = new FaceUpdateDb(faceModuleName, OpType.DELETE, companyId + "", uid, Md5);
        Map<String, Object> map = null;
        try {
            // 添加
            map = faceUpdateDb.updateSingleDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (CollectionUtils.isEmpty(map)) {
            return SysResult.build(201, "报错:XMLRPC无返回");
        }

        int result = Integer.parseInt(map.get("result").toString());
        if (result == RPCReturnCode.SUCCESS.getValue()) {
            // 日志记录成功，然后执行删除
            staffMapper.deleteMd5ByUid(uid);
            return SysResult.build(200, "Delete Success");
        } else {
            return SysResult.build(201, "删除失败");
        }
    }


    // 人脸识别打卡接口

    /**
     * 人脸识别
     *
     * @param filePath 图片地址
     * @return
     */
    public SysResult recogFile(String filePath) {
        String companyId = "clock_in";
        try {
            // 文件
            File file = new File(filePath);

            // MD5
            String fileMd5 = FileMD5Util.getMD5FromFile(file);
            MediaFileType fileType;
            if (FileTypeJudge.isImage(filePath)) {
                fileType = MediaFileType.IMAGE;
            } else if (FileTypeJudge.isVideo(filePath)) {
                fileType = MediaFileType.VIDEO;
            } else {
                return SysResult.build(400, "待加载物品不是图片也不是视频");
            }
            // 日志记录人脸识别开始
            String resultFile = facePath + companyId + "/" + fileMd5 + ANALYSIS_TXT_SUFFIX;
            // 人脸识别初始化
            AbstractRecognize faceSingleRecog = new FaceSingleRecog(fileType, faceModuleName, operationType, filePath, fileMd5, companyId, resultFile);
            // 人脸识别
            int code = faceSingleRecog.processFile();
            System.out.println(code);
            // 日志记录人脸识别结果
            if (code < RPCReturnCode.SUCCESS.getValue()) {
                return SysResult.build(201,"识别失败:  "+  code);
            }
            // 日志记录人脸识别结束

            // ocr结果解析初始化
            AbstractResultParser faceResultParser = new FaceResultParse();
            // ocr结果解析
            ResultVo<List<FaceResultVo>> resultVo = faceResultParser.parse(resultFile, 300);
            System.out.println(resultVo.isSuccess());
            System.out.println(resultVo.getData());
            // 返回结果
            // 搜索返回的图片。视频
            List<PersonsVo> personsList = new ArrayList<>();
            if (MediaFileType.IMAGE.getValue() == fileType.getValue()) {
                // 图片人物识别结果解析
                resultVo.getData().forEach(vo -> personsList.addAll(analysisImage(vo)));
            } else {
                // 视频人物识别结果解析
                resultVo.getData().forEach(vo -> personsList.addAll(analysisVideo(vo)));
            }
            if (personsList.size() != 0) {
                return SysResult.build(200, "Find user success", personsList.get(0).getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SysResult.build(201, "Recognize Fail", null);
    }

    /**
     * 图片人物识别结果解析
     *
     * @param data 识别结果
     * @return
     */
    private List<PersonsVo> analysisImage(FaceResultVo data) {
        MatchFace matchFace = null;
        List<PersonsVo> personsVoList = new ArrayList<>();
        PersonsVo persons;
        for (MatchDetect matchDetect : data.getFaceResults()) {
            // 循环匹配的人物
            for (MatchFace e : matchDetect.getDetects()) {
                boolean bool = null == matchFace || (matchFace != null && !matchFace.getClassName().equals(e.getClassName()));
                if (bool) {
                    matchFace = e;
                    persons = new PersonsVo();

                    // 人物
                    String className = e.getClassName();
                    // 日志记录人脸识别分数
//                    log.info("人脸识别的人物：" + className);
//                    log.info("人脸识别的分数：" + e.getClassScore());

                    // 取出小于0的结果
                    if (e.getClassScore() <= 0) {
                        continue;
                    }

                    // 查询人物信息，className里面装的是Uid
                    ArrayList<Object> uidList = generalService.quickGet("uid", "workers", "uid", className);
                    if (uidList.size() == 0) {
                        // 这个uid没有查到人
                        continue;
                    }
                    persons.setUid(className);
                    personsVoList.add(persons);
                }
            }
        }
        return personsVoList;
    }

    /**
     * 视频人物识别结果解析
     *
     * @param data 识别结果
     * @return
     */
    private List<PersonsVo> analysisVideo(FaceResultVo data) {

        List<PersonsVo> personsVoList = new ArrayList<>();
        PersonsVo persons;
        for (MatchDetect matchDetect : data.getFaceResults()) {
            // 循环匹配的人物
            for (MatchFace matchFace : matchDetect.getDetects()) {
                persons = new PersonsVo();
                // 人物
                String className = matchFace.getClassName();
                String classPath = matchFace.getImagePath();
//                日志记录人脸识别结果
//                log.info("人脸识别的人物：" + className);
//                log.info("人脸识别的分数：" + matchFace.getClassScore());
//                log.info("人脸识别的截图：" + classPath);

                // 查询人物信息，className里面装的是Uid
                ArrayList<Object> uidList = generalService.quickGet("uid", "workers", "uid", className);
                if (uidList.size() == 0) {
                    // 这个uid没有查到人
                    continue;
                }
                persons.setUid(className);
                personsVoList.add(persons);
            }
        }
        return personsVoList;
    }

    /**
     * 保存
     *
     * @param path 人物头像
     * @return
     */
    @Override
    public SysResult save(String path, String uid) {

        ArrayList<Object> imageMd5List = generalService.quickGet("uid", "workers", "uid", uid);

        if (imageMd5List.size() == 0) {
            return SysResult.build(201, "用户不存在");
        }
        // 头像入算法
        String md5;
        if (path != null && !path.equals("")) {
            SysResult uploadResult = uploadHeadImgAlg(uid, path);
            if (!uploadResult.isOk()) {
                return uploadResult;
            }
            md5 = (String) uploadResult.getData();
        } else {
            return SysResult.build(201, "路径为空");
        }

        staffMapper.setWorkerMd5(md5, uid);

        return SysResult.build(200, "保存头像成功");
    }

    /**
     * 头像入算法
     *
     * @param path    图片地址
     */
    private SysResult uploadHeadImgAlg(String uid, String path) {
        String md5;
        try {
            // 图片
            File file = new File(path);
            // MD5
            md5 = FileMD5Util.getMD5FromFile(file);
            // 文件后缀
            String type = path.indexOf(".") != -1 ? path.substring(path.lastIndexOf(".") + 1, path.length()) : null;
            // 文件名称
            String name = type != null ? md5 + "." + type : md5 + ".jpg";

            String newPath = faceShow + "clock_in" + "/" + uid;
            // 复制文件
            if (!new File(newPath).exists()) {
                new File(newPath).mkdirs();
            }

            FileUtils.copyFile(file, new File(newPath  + "/" + name));
            // 头像入算法库
            String destPath = newPath + "/" +name;
            // 保存人脸指纹库
            SysResult saveResult = saveAlg(destPath,md5,uid);
            if (!saveResult.isOk()) {
                return saveResult;
            }


        } catch (Exception e) {
            return SysResult.build(201,"头像入算法错误");
        }
        return SysResult.build(200,"入算法成功",md5);
    }
}
