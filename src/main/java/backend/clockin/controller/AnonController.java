package backend.clockin.controller;


import backend.clockin.mapper.ClockInMapper;
import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.*;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Controller
public class AnonController {

    @Autowired
    ClockInService clockInService;

    @Autowired
    GeneralService generalService;

    @Autowired
    ClockInMapper clockInMapper;

    @Autowired
    ApplyService applyService;

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    @RequestMapping("/getVerifyCode")
    @ResponseBody
    public SysResult getVerifyCode(String account){
        if(account == null || account.equals("")){
            return SysResult.build(201,"请输入账户");
        }
        // 获取用户邮箱
        ArrayList<Object> emailList = generalService.quickGet("email","workers","account",account);
        if(emailList.size() != 1){
            return SysResult.build(201,"错误的账户");
        }
        String email = emailList.get(0).toString();

        // 生成反人类的验证码
        Random random = new Random();
        String verifyCode = (random.nextInt(900000)+ 100000)  + "" + (char)(random.nextInt(27)+64)+
                (char)(random.nextInt(27)+64)+ (char)(random.nextInt(27)+64)+ (char)(random.nextInt(27)+64)
                + (random.nextInt(900000)+ 100000);

        // 向用户发送验证码, 邮箱需要被替换
        emailService.sendSimpleMail("su999chang@163.com",email,"验证码",verifyCode);

        // 将验证码返回至前端
        return SysResult.build(200,"验证码已发送至邮箱",verifyCode);
    }

    @RequestMapping("/reSetPassword")
    @ResponseBody
    public SysResult reSetPassword(String account){
        ArrayList<Object> emailList = generalService.quickGet("email","workers","account",account);
        if(emailList.size() != 1){
            return SysResult.build(201,"错误的账户");
        }
        String newPassword = userService.getInputPasswordCiph("123456", userService.getUser(account).get(0).getSalt());
        return userService.resetPassword(newPassword,account);
    }

}
