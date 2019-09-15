package backend.clockin.controller;


import backend.clockin.pojo.tool.SysResult;
import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.service.*;
import com.alibaba.fastjson.JSON;
import com.xunsiya.tools.common.config.PathUtils;
import com.xunsiya.tools.common.random.RandomUtil;
import com.xunsiya.tools.common.string.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@RestController
public class RecognizeFaceController {

    @Value("${recog.path}")
    private String recogPath;

    private final static String DOT = ".";

    @Autowired
    GeneralService generalService;

    @Autowired
    RecognizeFaceService recognizeFaceService;

    @Autowired
    UserService userService;

    @Autowired
    ClockInService clockInService;

    @Autowired
    ToolService toolService;

    /**
     * 添加员工
     * info 是jason类型数据
     *
     * @return
     */
    @RequestMapping("/api/addStaff")
    public SysResult save(String info) {
        if (info == null) {
            return SysResult.build(400, "JSON参数为空");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        // 参数验证
        if (input.size() == 0) {
            return SysResult.build(400, "Did not get info");
        }
        if (input.get("password") == null || input.get("password").equals("")) {
            return SysResult.build(400, "密码为空");
        }
        if (input.get("account") == null || input.get("account").equals("")) {
            return SysResult.build(400, "账户为空");
        }
        if (input.get("name") == null || input.get("name").equals("")) {
            return SysResult.build(400, "姓名为空");
        }
        if (input.get("tel") == null || input.get("tel").equals("")) {
            return SysResult.build(400, "电话为空");
        }
        if (input.get("email") == null || input.get("email").equals("")) {
            return SysResult.build(400, "邮箱为空");
        }
        if (input.get("manager") == null || input.get("manager").equals("")) {
            return SysResult.build(400, "上级经理为空");
        }
        if (input.get("department_id") == null || input.get("department_id").equals("")) {
            return SysResult.build(400, "部门为空");
        }
        if (input.get("position") == null || input.get("position").equals("")) {
            return SysResult.build(400, "职位为空");
        }
        if (input.get("work_id") == null || input.get("work_id").equals("")) {
            return SysResult.build(400, "工号为空");
        }
        if (input.get("work_place") == null || input.get("work_place").equals("")) {
            return SysResult.build(400, "工作地址为空");
        }
        if (input.get("day_off_type") == null || input.get("day_off_type").equals("")) {
            return SysResult.build(400, "放假类型为空");
        }
        if (input.get("file_path") == null) {
            return SysResult.build(400, "上传图片为空");
        }
        if (input.get("reviewers") == null || input.get("reviewers").equals("")) {
            return SysResult.build(400, "审批人为空");
        }
        if (input.get("change_day_off") == null || input.get("change_day_off").equals("")) {
            return SysResult.build(400, "调休时间为空");
        }
        if (input.get("annual_day_off") == null || input.get("annual_day_off").equals("")) {
            return SysResult.build(400, "年假时间为空");
        }

        String filePath = input.get("file_path");
        // 注册账户,取得uid
        SysUser user = new SysUser();
        user.setAccount(input.get("account"));
        user.setPassword(input.get("password"));
        user.setRoles("user");
        String resultMsg = userService.addUser(user);
        if (resultMsg.equals("Add success")) {
            String uid = userService.getUser(input.get("account")).get(0).getUid();
            input.put("uid", uid);
            // 添加员工，需要uid, 需要去除密码和路径
            input.remove("password");
            input.remove("file_path");
            generalService.addRow("workers", input);

            // 添加图片
            if (filePath == null || filePath.equals("")) {
                // 图片上传失败
                return SysResult.build(200, "图片上传失败, 员工添加成功");
            } else {
                // 保存图片和对应的MD5
                return recognizeFaceService.save(filePath, uid);
            }
        }
        return SysResult.build(201, resultMsg);
    }

    @RequestMapping("/api/addUserImage")
    public SysResult addUserImage(String filePath, String uid) {
        if (filePath == null || uid == null) {
            return SysResult.build(400, "参数为空");
        }
        return recognizeFaceService.save(filePath, uid);
    }

    @RequestMapping("/api/deleteUserImage")
    public SysResult deleteUserImage(String uid) {
        if (uid == null) {
            return SysResult.build(400, "参数为空");
        }
        return recognizeFaceService.deleteStaffImage(uid);
    }

    /**
     * 人脸识别
     *
     * @param file 文件流
     * @return
     */
    @RequestMapping("/faceClockIn")
    public SysResult faceClockIn(MultipartFile file) {
        SysResult sysResult = new SysResult();
        if (file == null) {
            return SysResult.build(400, "文件错误");
        }

        try {
            // 保存文件
            SysResult saveResult = saveFile(file);
            if(!saveResult.isOk()){
                return saveResult;
            }
            String path = (String) saveResult.getData();

            String companyId = "clock_in";

//             识别文件返回结果
            SysResult checkResult = recognizeFaceService.recogFile(path);
            sysResult.setMsg(checkResult.getMsg());
//            SysResult checkResult = new SysResult(200,"","925183");
            if (checkResult.isOk()) {
                // 成功的取得了目标的uid
                String uid = checkResult.getData().toString();
                return clockInService.confirmBeforeClockIn(uid);
            }
        } catch (Exception e) {
            return SysResult.build(201, "文件保存失败");
        }

        sysResult.setStatus(201);
        return sysResult;
    }


    /**
     * 保存文件
     *
     * @param file 文件流
     * @return
     * @throws IOException
     */
    private SysResult saveFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        Random r = new Random();
        String filePath = "/opt/RD011/clockin/tempFace";
        String finalPath;
        if (file.isEmpty()) {
            return SysResult.build(201, "文件为空！");
        }
        try {
            finalPath = toolService.uploadFile(file.getBytes(), filePath, r.nextInt(1000000) + "face" + r.nextInt(10000000) + fileName);
        } catch (Exception e) {
            return SysResult.build(201, "上传文件出错  " + e);
        }
        return SysResult.build(200, "上传成功", finalPath);
    }

    @RequestMapping("/clockInAfterConfirm")
    public SysResult clockInAfterConfirm(String uid) {
        return clockInService.clockIn(uid);
    }


}