package backend.clockin.controller;

import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.GeneralService;
import backend.clockin.service.ManageFormService;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api")
public class GeneralController {

    @Autowired
    ManageFormService manageFormService;

    @Autowired
    GeneralService generalService;

    @RequiresRoles({"admin"})
    @RequestMapping("/databaseCheck")
    public String test() {
        manageFormService.checkProcessOne();
        return "check complete";
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/form/creat")
    public SysResult creatForm(String formName, String description) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (description == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.createForm(formName, description);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/form/delete")
    public SysResult deleteForm(String formName) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.dropForm(formName);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/form/change")
    public SysResult changeForm(String formName, String info) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        // info is name,description
        return generalService.updateExistForm(formName, info);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/form/get")
    public SysResult getForm(Integer id) {
        if (id == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.showExistForm(id);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/column/add")
    public SysResult addColumn(String formName, String columnName, String type, String place) {
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
        // type: varchar,255,YES,primary key
        return generalService.addColumn(formName, columnName, type, place);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/column/delete")
    public SysResult deleteColumn(String formName, String columnName) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (columnName == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.delColumn(formName, columnName);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/column/change")
    public SysResult changeColumn(String formName, String oldColumnName, String newColumnName, String type) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (oldColumnName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (newColumnName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (type == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.changeColumn(formName, oldColumnName, newColumnName, type);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/column/get")
    public SysResult getColumn(String formName) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.getFormColumns(formName);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/row/add")
    public SysResult addRow(String formName, String info) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        return generalService.addRow(formName, input);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/row/delete")
    public SysResult deleteRow(String formName, String id) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (id == null) {
            return SysResult.build(201, "Get null value");
        }
        return generalService.deleteRow(formName, id);
    }

    @RequiresRoles({"admin"})
    @RequestMapping("/general/row/change")
    public SysResult changeRow(String formName, String info, String id) {
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (info == null) {
            return SysResult.build(201, "Get null value");
        }
        if (id == null) {
            return SysResult.build(201, "Get null value");
        }
        HashMap<String, String> input = JSON.parseObject(info, HashMap.class);
        return generalService.changeRow(formName, input, id);
    }

    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/general/row/get")
    public SysResult getRow(String targetColumns, String formName, String cond, String searchType, String separate, String pageNum, String pageMax) {
        if (targetColumns == null) {
            return SysResult.build(201, "Get null value");
        }
        if (formName == null) {
            return SysResult.build(201, "Get null value");
        }
        if (cond == null) {
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

        // 转化带有时间的数据
        SysResult recordResult = generalService.getRow2(targetColumns, formName, cond, searchType, separate, Integer.valueOf(pageNum),
                Integer.valueOf(pageMax));

        if (!recordResult.isOk()) {
            return SysResult.build(201, "获取记录失败 ：  " + recordResult.getMsg());
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<LinkedHashMap<String, Object>> unChangedRecord = (ArrayList<LinkedHashMap<String, Object>>) recordResult.getData();
        if (unChangedRecord.size() > 0 && unChangedRecord.get(0).get("created") != null) {
            int length = unChangedRecord.size();
            for (int i = 0; i < length; i++) {
                String created = dateFormat.format(unChangedRecord.get(i).get("created"));
                unChangedRecord.get(i).put("created", created);
            }
        }
        return SysResult.build(200, "获取记录成功", unChangedRecord);
    }

    // 通用接口仅支持单表导出
    @RequiresRoles({"admin"})
    @RequestMapping("/general/outFile")
    public SysResult outFile(String targetColumn, String filePath, String formName,
                             String cond, String searchType, String separate, String pageNum, String pageMax) {
        String tempFormName = "";
        String[][] conditions;
        try {
            // 获取登录用户的uid，拼接在表名后以作标识符
            SysUser loginUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            String uid = loginUser.getUid();

            // 创建临时表格，用于储存导出数据
            tempFormName = "out_file_temp_form_" + uid;
            // 清除可能存在的临时表格
            generalService.dropForm(tempFormName);
            generalService.createForm(tempFormName, "temp form for out file");

            // 为临时表格添加目标字段
            String[] targetColumnNames = targetColumn.split(",");
            int columnNum = targetColumnNames.length;
            generalService.changeColumn(tempFormName, "column1", targetColumnNames[0], "varchar,255,YES");
            for (int i = 1; i < columnNum; i++) {
                generalService.addColumn(tempFormName, targetColumnNames[i], "varchar,255,YES", targetColumnNames[i - 1]);
            }

            // 为临时表格添加默认行，作为excel表格的字段名
            HashMap<String, String> defaltRow = new HashMap<>();
            for (int i = 0; i < columnNum; i++) {
                defaltRow.put(targetColumnNames[i], targetColumnNames[i]);
            }
            generalService.addRow(tempFormName, defaltRow);

        } catch (Exception e) {
            // 初始化失败，回滚，删除临时表格
            generalService.dropForm(tempFormName);
            return SysResult.build(201, "初始化导出环境错误    " + e);
        }

        // 获取数据
        SysResult dataResult = generalService.getRow2(targetColumn, formName, cond, searchType,
                separate, Integer.valueOf(pageNum), Integer.valueOf(pageMax));
        if (dataResult.isOk()) {
            try {
                // 数据获取成功
                ArrayList<LinkedHashMap<String, Object>> data = (ArrayList<LinkedHashMap<String, Object>>) dataResult.getData();
                // 将数据写入临时表格
                generalService.autoWriteIn(tempFormName, data);
                // 输出临时表格数据
                generalService.outFile("'" + filePath + "'", tempFormName,
                        Integer.valueOf(pageNum), Integer.valueOf(pageMax));
            } catch (Exception e) {
                // 数据获取失败，回滚，删除创建的临时表格
                generalService.dropForm(tempFormName);
                return SysResult.build(201, "数据导入Excel失败   " + e);
            }
            // 删除临时表格
            generalService.dropForm(tempFormName);
            return SysResult.build(200, "输出文件成功");
        } else {
            // 数据获取失败，回滚，删除创建的临时表格
            generalService.dropForm(tempFormName);
            return SysResult.build(201, "数据获取失败： " + dataResult.getMsg());
        }
    }

    @RequestMapping("/general/download")
    public void downloadFile(String filePath, HttpServletResponse resp) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            InputStream inStream = null;
            BufferedOutputStream os = null;
            try {
                inStream = new FileInputStream(file);
                // 设置输出的格式，以附件的方式输出，不用用浏览器打开
                byte[] buffer = new byte[1024*8];
                int byteread;
                try {
                    resp.reset();
                    resp.addHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
                    resp.setContentType("application/octet-stream");
                    os = new BufferedOutputStream(resp.getOutputStream());
                    while ((byteread = inStream.read(buffer)) != -1) {
                        os.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            resp.reset();
            try {
                resp.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("文件不存在", "UTF-8"));
                resp.setContentType("application/octet-stream");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping("/general/getAllHomePageImage")
    public SysResult getAllHomePageImage() {
        try {
            SysResult result = generalService.getRow2("id,url","homepage_image","id,>,0","0","AND",1,10);
            if(!result.isOk()){
                return SysResult.build(201,"获取失败");
            }
            ArrayList<LinkedHashMap<String, Object>> data = (ArrayList<LinkedHashMap<String, Object>>) result.getData();
            HashMap<String,String> base64List = new HashMap<>();
            for (LinkedHashMap file:data
            ) {
                String filepath = file.get("url").toString();
                String base64 = getImageStr(filepath);
                base64List.put(file.get("id")+"",base64);
            }
            for(int i = 1; i<7; i++){
                base64List.putIfAbsent("" + i, "");
            }
            return SysResult.build(200, "获取成功", base64List);
        } catch (Exception e) {
            return SysResult.build(201, "获取失败");
        }
    }

    private String getImageStr(String imgFile) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);//返回Base64编码过的字节数组字符串
    }
}
