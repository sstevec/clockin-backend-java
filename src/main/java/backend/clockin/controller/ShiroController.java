package backend.clockin.controller;


import backend.clockin.mapper.ShiroMapper;
import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;
import backend.clockin.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@CrossOrigin
@RestController
@RequestMapping("/api")
public class ShiroController {

    @Autowired
    UserService userService;

    @Autowired
    ShiroMapper shiroMapper;


    @RequestMapping("/login")
    public SysResult login(String account, String password) throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        if (account == null) {
            return SysResult.build(400, "请输入账户", null);
        }
        if (password == null) {
            return SysResult.build(400, "请输入密码", null);
        }

        if (userService.getUser(account).size() != 1) {
            return SysResult.build(201, "账户不存在", null);
        }
        // 验证员工离职
        if(shiroMapper.checkExpired(account).size() != 1){
            return SysResult.build(201,"账户已注销");
        }

        // 验证账户存在后取得账户盐值，并且对输入的密码进行盐值加密
        UsernamePasswordToken token = new UsernamePasswordToken(account,
                userService.getInputPasswordCiph(password, userService.getUser(account).get(0).getSalt()));
        //token.setRememberMe(true);// 默认不记住密码

        try {
            currentUser.login(token); //登录
            System.out.println("login success");
            return SysResult.build(200, "登陆成功", null);
        } catch (DisabledAccountException e) {
            return SysResult.build(201, "账户失效", null);
        } catch (IncorrectCredentialsException e) {
            return SysResult.build(400, "密码错误", null);
        } catch (ExcessiveAttemptsException e) {
            return SysResult.build(201, "过多尝试", null);
        } catch (RuntimeException e) {
            return SysResult.build(201, "运行异常", null);
        }
    }

    @RequestMapping("/redlogin")
    public SysResult redlogin() {
        return SysResult.build(201, "需要登录", "/login");
    }

    @RequestMapping("/logout")
    public SysResult logOut() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return SysResult.build(200, "登出成功", "/login");
    }

    @RequestMapping("/shiroVerify")
    public SysResult verify() {
        SysUser loginUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        String uid = loginUser.getUid();
        return SysResult.build(200, "用户uid信息", uid);
    }

    @RequestMapping("/Register")
    public SysResult register(String account, String password) {
        if (account == null) {
            return SysResult.build(201, "请输入账户", null);
        }
        if (password == null) {
            return SysResult.build(201, "请输入密码", null);
        }
        SysUser user = new SysUser();
        user.setAccount(account);
        user.setPassword(password);
        user.setRoles("user");
        String resultMsg = userService.addUser(user);
        if (resultMsg.equals("Add success")) {
            return SysResult.build(200, "注册成功", "/login");
        }
        return SysResult.build(201, "用户已存在", null);
    }

    @RequestMapping("/changePassword")
    public SysResult changePassword(String account, String oldPassword, String newPassword) {
        if (account == null || account.equals("")) {
            return SysResult.build(201, "请输入账户", null);
        }
        if (oldPassword == null || oldPassword.equals("")) {
            return SysResult.build(201, "请输入密码", null);
        }
        if (newPassword == null || newPassword.equals("")) {
            return SysResult.build(201, "请输入密码", null);
        }
        // 验证旧密码正确
        SysResult compareResult = userService.comparePassword(account, oldPassword);
        if (compareResult.isOk()) {
            // 验证通过
            HashMap<String, String> info = new HashMap<>();
            String newPasswordCiph = userService.getInputPasswordCiph(newPassword, userService.getUser(account).get(0).getSalt());
            info.put("password", newPasswordCiph);
            return userService.changeUser(info, account);
        }
        // 密码错误
        return compareResult;
    }

    @RequiresRoles(value = {"admin","manager"},logical= Logical.OR )
    @RequestMapping("/advanceVerify")
    public SysResult verify2() {
        SysUser loginUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        String uid = loginUser.getUid();
        return SysResult.build(200, "用户uid信息", uid);
    }

    @RequestMapping("/rehome")
    @ExceptionHandler(AuthorizationException.class)
    public SysResult rehome() {
        return SysResult.build(201, "需要登录", "/");
    }
}
