package backend.clockin.controller;

import backend.clockin.mapper.ShiroMapper;
import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.*;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    @Autowired
    StaffManageService staffManageService;

    @Autowired
    GeneralService generalService;

    @Autowired
    UserService userService;

    @Autowired
    RecognizeFaceService recognizeFaceService;

    @Autowired
    EmailService emailService;

    @Autowired
    ToolService toolService;

    @Autowired
    ShiroMapper shiroMapper;

    @RequestMapping("/addForm")
    public SysResult addStaffRelevantForm(String formName) {
        if (formName != null) {
            return staffManageService.addStaffRelevantForm(formName);
        }
        return SysResult.build(201, "Get null value");
    }

    @RequestMapping("/deleteForm")
    public SysResult deleteStaffRelevantForm(String formName) {
        if (formName != null) {
            return staffManageService.deleteStaffRelevantForm(formName);
        }
        return SysResult.build(201, "Get null value");
    }

    @RequestMapping("/changeForm")
    public SysResult changStaffRelevantForm(String oldFormName, String newFormName) {
        if (oldFormName != null && newFormName != null) {
            return staffManageService.changStaffRelevantForm(oldFormName, newFormName);
        }
        return SysResult.build(201, "Get null value");
    }

    @RequestMapping("/getForm")
    public SysResult getStaffRelevantFormNames() {
        return staffManageService.getStaffRelevantFormNames();
    }


    @RequestMapping("/addColumn")
    public SysResult addStaffRelevantFormColumn(String formName, String columnName, String type, String place) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (columnName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (type == null) {
            return SysResult.build(201, "Get null value");
        }
        if (place == null) {
            return SysResult.build(201, "Get null value");
        }
        return staffManageService.addStaffRelevantFormColumn(formName, columnName, type, place);
    }

    @RequestMapping("/deleteColumn")
    public SysResult deleteStaffRelevantFormColumn(String columnName) {
        if (columnName == null) {
            return SysResult.build(201, "Get null value");
        }
        return staffManageService.deleteStaffRelevantFormColumn(columnName);
    }

    @RequestMapping("/changeColumn")
    public SysResult changeStaffRelevantFormColumn(String formName, String oldColumn, String newColumn, String type) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (oldColumn == null) {
            return SysResult.build(201, "Get null value");
        }
        if (type == null) {
            return SysResult.build(201, "Get null value");
        }
        if (newColumn == null) {
            return SysResult.build(201, "Get null value");
        }
        return staffManageService.changeStaffRelevantFormColumn(formName, oldColumn, newColumn, type);
    }

    @RequestMapping("/getColumn")
    public SysResult getStaffRelevantFormAllColumns() {
        SysResult allColumnsResult = staffManageService.getStaffRelevantFormAllColumns();
        if (!allColumnsResult.isOk()) {
            return allColumnsResult;
        }
        ArrayList<String> names = (ArrayList<String>) allColumnsResult.getData();
        ArrayList<HashMap<String, String>> changedNameColumn = new ArrayList<>();
        int size = names.size();
        for (int i = 0; i < size; i++) {
            if (names.get(i).equals("image")) {
                continue;
            }
            HashMap<String, String> temp = new HashMap<>();
            temp.put("name", names.get(i));
            // 将英文的系统名换成中文
            switch (names.get(i)) {
                case "account":
                    temp.put("value", "账户");
                    break;
                case "name":
                    temp.put("value", "姓名");
                    break;
                case "tel":
                    temp.put("value", "电话");
                    break;
                case "email":
                    temp.put("value", "邮箱");
                    break;
                case "manager":
                    temp.put("value", "上级经理");
                    break;
                case "department_id":
                    temp.put("value", "部门编号");
                    break;
                case "position":
                    temp.put("value", "职位");
                    break;
                case "work_id":
                    temp.put("value", "工号");
                    break;
                case "work_place":
                    temp.put("value", "工作地点");
                    break;
                case "day_off_type":
                    temp.put("value", "放假组");
                    break;
                case "reviewers":
                    temp.put("value", "审批人");
                    break;
                case "change_day_off":
                    temp.put("value", "调休时间");
                    break;
                case "annual_day_off":
                    temp.put("value", "年假时间");
                    break;
                case "expired":
                    temp.put("value", "离职时间");
                    break;
                default:
                    temp.put("value", names.get(i));
                    break;
            }
            changedNameColumn.add(temp);
        }
        return SysResult.build(200, "获取成功", changedNameColumn);
    }


    @RequestMapping("/addRow")
    public SysResult addStaffRelevantFormRow(String formName, String info) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        return staffManageService.addStaffRelevantFormRow(formName, input);
    }

    @RequestMapping("/deleteRow")
    public SysResult deleteStaffRelevantFormRow(String formName, String uid) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (uid == null) {
            return SysResult.build(201, "Get null value");
        }
        return staffManageService.deleteStaffRelevantFormRow(formName, uid);
    }

    @RequestMapping("/changeRow")
    public SysResult changeStaffRelevantFormRow(String formName, String info, String uid) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        if (uid == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        return staffManageService.changeStaffRelevantFormRow(formName, input, uid);
    }

    @RequestMapping("/advanceChangeRow")
    public SysResult advanceChangeStaffRelevantFormRow(String info, String uid) {
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        if (uid == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        // 检查是否需要更新图片
        if (!input.get("filePath").equals("")) {
            // 先更新图片
            ArrayList<Object> imageResult = generalService.quickGet("image", "workers", "uid", uid);
            if (imageResult.size() != 0 && imageResult.get(0) != null) {
                // 存在图片,先删除
                recognizeFaceService.deleteStaffImage(uid);
            }
            // 添加图片
            SysResult addResult = recognizeFaceService.save(input.get("filePath"), uid);
            if (!addResult.isOk()) {
                return addResult;
            }
        }
        // 修改审批人和放假类型
        input.remove("filePath");
        return staffManageService.editStaffRelevantForm(input, uid);
    }

    @RequestMapping("/editStaff")
    public SysResult editStaff(String info, String uid) {
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        if (uid == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        if (input.get("department_id") != null) {
            // 有修改department_id 的项
            ArrayList<Object> checkExist = generalService.quickGet("department_id", "department"
                    , "department_id", input.get("department_id"));
            if (checkExist.size() != 1) {
                return SysResult.build(201, "部门编号不存在");
            }
        }
        return staffManageService.editStaffRelevantForm(input, uid);
    }

    @RequestMapping("/deleteStaff")
    public SysResult deleteStaff(String uid) {
        if (uid == null) {
            return SysResult.build(201, "Get null value");
        }
        // 检测是否已离职
        ArrayList<String> checkResult = shiroMapper.checkUserAlreadyExpired(uid);
        if (checkResult.size() != 1) {
            return SysResult.build(201, "员工已经离职");
        }
        // 软删除用户和系统账号
        SysResult result1 = staffManageService.softDeleteStaff(uid);
        SysResult result2 = userService.softDeleteUser(uid);
        if (result1.isOk() && result2.isOk()) {
            return SysResult.build(200, "删除成功");
        } else {
            return SysResult.build(201, result1.getMsg() + "  " + result2.getMsg());
        }
    }

    @RequestMapping("/getRow")
    public SysResult staffRelevantFormSearch(String targetColumn, String conditions, String searchType, String separate,
                                             String pageNum, String pageMax) {
        if (targetColumn == null) {
            return SysResult.build(201, "Get null value");
        }
        if (conditions == null) {
            return SysResult.build(201, "Get null value");
        }
        if (searchType == null) {
            return SysResult.build(201, "Get null value");
        }
        if (separate == null) {
            return SysResult.build(201, "Get null value");
        }
        if (pageNum == null) {
            return SysResult.build(201, "Get null value");
        }
        if (pageMax == null) {
            return SysResult.build(201, "Get null value");
        }
        return staffManageService.staffRelevantFormSearch(targetColumn, conditions, searchType, separate, pageNum, pageMax);
    }

    @RequestMapping("/getRow2")
    public SysResult staffRelevantFormSearch2(String targetColumn, String conditions, String searchType, String separate,
                                              String pageNum, String pageMax) {
        if (targetColumn == null) {
            return SysResult.build(201, "Get null value");
        }
        if (conditions == null) {
            return SysResult.build(201, "Get null value");
        }
        if (searchType == null) {
            return SysResult.build(201, "Get null value");
        }
        if (separate == null) {
            return SysResult.build(201, "Get null value");
        }
        if (pageNum == null) {
            return SysResult.build(201, "Get null value");
        }
        if (pageMax == null) {
            return SysResult.build(201, "Get null value");
        }
        int start = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageMax);
        SysResult rowResult = staffManageService.staffRelevantFormSearch2(targetColumn, conditions, searchType, separate, start + "", pageMax);
        if (!rowResult.isOk()) {
            return rowResult;
        }
        ArrayList<LinkedHashMap<String, String>> rows = (ArrayList<LinkedHashMap<String, String>>) rowResult.getData();
        int rowSize = rows.size();
        if (rowSize > 0) {
            if (rows.get(0).get("reviewers") != null) {
                // 取得了reviewersUid
                for (int i = 0; i < rowSize; i++) {
                    String reviewerId = rows.get(i).get("reviewers");
                    String[] reviewersArray = reviewerId.split(",");
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
                    ArrayList<LinkedHashMap<String, Object>> tempNameList = (ArrayList<LinkedHashMap<String, Object>>) nameResult.getData();
                    String names = "";
                    for (LinkedHashMap<String, Object> nameTemp : tempNameList
                    ) {
                        names = names + nameTemp.get("name").toString() + ",";
                    }
                    names = names.substring(0, names.length() - 1);
                    rows.get(i).put("reviewers", names);
                    rows.get(i).put("reviewerUid", reviewerId);
                }
                return SysResult.build(200, "获取成功", rows);
            }
        }
        return rowResult;
    }

    @RequestMapping("/outFile")
    public SysResult staffRelevantFormOutFile(String targetColumn, String cond, String searchType,
                                              String separate, String start, String pageMax) {
        if (targetColumn == null || targetColumn.equals("")) {
            return SysResult.build(201, "需要选择字段");
        }
        if (cond == null || cond.equals("")) {
            return SysResult.build(201, "需要选择条件");
        }
        if (searchType == null || searchType.equals("")) {
            return SysResult.build(201, "需要选择搜索方式");
        }
        if (separate == null || separate.equals("")) {
            return SysResult.build(201, "需要选择并列关系");
        }
        if (start == null || start.equals("")) {
            return SysResult.build(201, "需要选择开始位置");
        }
        if (pageMax == null || pageMax.equals("")) {
            return SysResult.build(201, "需要选择导出数量");
        }
        String tempFormName = "";
        try {
            // 获取登录用户的uid，拼接在表名后以作标识符
            SysUser loginUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            String uid = loginUser.getUid();

            // 创建临时表格，用于储存导出数据
            tempFormName = "staff_out_file_temp_form_" + uid;

            // 确保文件夹存在
            String filePath = "/opt/RD011/clockin/outExcel";
            File targetDir = new File(filePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            //创建新的excel
            File file = new File(filePath + "/" + tempFormName + ".xlsx");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            // 获取字段名
            String[] targetColumnNames = targetColumn.split(",");

            // 获取数据
            SysResult dataResult = staffManageService.staffRelevantFormSearch2(targetColumn, cond, searchType, separate, start, pageMax);
            if (dataResult.isOk()) {
                ArrayList<LinkedHashMap<String, Object>> data = (ArrayList<LinkedHashMap<String, Object>>) dataResult.getData();

                // 写入Excel
                return generalService.outFile2(filePath + "/" + tempFormName + ".xlsx", targetColumnNames, data);

            } else {
                // 数据获取失败，回滚，删除创建的临时表格
                return SysResult.build(201, "数据获取失败： " + dataResult.getMsg());
            }

        } catch (Exception e) {
            return SysResult.build(201, "初始化导出环境错误    " + e);
        }

    }

    @RequestMapping("/inFile")
    public SysResult inFile(String filePath, String formName) {
        if (filePath == null || filePath.equals("")) {
            return SysResult.build(201, "需要选择上传文件");
        }
        if (formName == null || formName.equals("")) {
            return SysResult.build(201, "需要选择默认目标表格");
        }
        return staffManageService.staffRelevantFormInFile(filePath, formName);
    }

    @RequestMapping("/upload")
    public SysResult uploadImg(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        Random r = new Random();
        String filePath = "/opt/RD011/clockin/img";
        String finalPath;
        if (file.isEmpty()) {
            return SysResult.build(201, "文件为空！");
        }
        try {
            finalPath = toolService.uploadFile(file.getBytes(), filePath, r.nextInt(1000000) + "s" + r.nextInt(10000000) + fileName);
        } catch (Exception e) {
            return SysResult.build(201, "上传文件出错  " + e);
        }
        return SysResult.build(200, "上传成功", finalPath);
    }

    @RequestMapping("/uploadHomePageImage")
    public SysResult uploadHomePageImage(MultipartFile file, String index) {
        if (index == null || index.equals("")) {
            return SysResult.build(201, "上传失败");
        }
        String filePath = "/opt/RD011/clockin/HomePage";
        String finalPath;
        if (file.isEmpty()) {
            return SysResult.build(201, "文件为空！");
        }
        try {
            // 删除重复文件
            File temp = new File(filePath + "/" + "home" + index + ".jpg");
            if (temp.exists()) {
                temp.delete();
            }
            generalService.deleteRow("homepage_image", (Integer.valueOf(index) + 1) + "");

            finalPath = toolService.uploadFile(file.getBytes(), filePath, "home" + index + ".jpg");
            HashMap<String, String> info = new HashMap<>();
            info.put("url", filePath + "/" + "home" + index + ".jpg");
            info.put("id", (Integer.valueOf(index) + 1) + "");
            generalService.addRow("homepage_image", info);
        } catch (Exception e) {
            return SysResult.build(201, "上传文件出错  " + e);
        }
        return SysResult.build(200, "上传成功", finalPath);
    }


    @RequestMapping("/uselessAction")
    public void uselessAction() {

    }

    @RequestMapping("/uploadStaffInfo")
    public SysResult uploadStaffInfo(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        Random r = new Random();
        String filePath = "/opt/RD011/clockin/upload";
        String finalPath;
        if (file.isEmpty()) {
            return SysResult.build(201, "文件为空！");
        }
        try {
            finalPath = toolService.uploadFile(file.getBytes(), filePath, r.nextInt(900000) + 100000 + "staffInfo" + r.nextInt(10000000) + fileName);
        } catch (Exception e) {
            return SysResult.build(201, "上传文件出错  " + e);
        }
        return SysResult.build(200, "上传成功", finalPath);
    }

    @RequestMapping("/countRow")
    public SysResult countRow(String targetColumn, String conditions, String searchType) {
        if (targetColumn == null) {
            return SysResult.build(201, "Get null value");
        }
        if (conditions == null) {
            return SysResult.build(201, "Get null value");
        }
        if (searchType == null) {
            return SysResult.build(201, "Get null value");
        }
        return staffManageService.staffRelevantFormSearch2(targetColumn, conditions, searchType, "OR", 0 + "", "1000");
    }
}
