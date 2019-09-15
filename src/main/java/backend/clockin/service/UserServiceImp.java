package backend.clockin.service;

import backend.clockin.mapper.ShiroMapper;
import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    ShiroMapper shiroMapper;

    @Autowired
    GeneralService generalService;

    public String addUser(SysUser user) {
        if (getUser(user.getAccount()).size() == 0) {
            Random r = new Random();
            user.setUid(r.nextInt(899999) + 100000 + "");
            // make sure no duplicated user id
            while (generalService.quickGet("uid", "sysuser", "uid", user.getUid()).size() != 0) {
                System.out.println("regenerate uid");
                user.setUid(r.nextInt(899999) + 100000 + "");
            }
            user.setSalt(1024 + "");
            user.setPassword(getInputPasswordCiph(user.getPassword(), user.getSalt()));
            shiroMapper.addUser(user);
            return "Add success";
        }
        return "账户已存在";
    }

    public String deleteUser(String account) {
        if (getUser(account).size() == 1) {
            shiroMapper.deleteUser(account);
            return "Delete success";
        }
        return "user does not exist";
    }

    public SysResult changeUser(HashMap<String, String> info, String account) {
        // no need to verify column name, since all the fields are fixed
        if (getUser(account).size() == 1) {
            shiroMapper.changeUser(info, account);
            return SysResult.build(200,"修改成功");
        }
        return SysResult.build(201,"用户信息错误");
    }

    public ArrayList<SysUser> getUser(String account) {
        if (account.equals("")) {
            return shiroMapper.getAll();
        }
        return shiroMapper.getUser(account);
    }

    public String getInputPasswordCiph(String password, String salt) {
        if (salt == null) {
            password = "";
        }

        String ciphertext = new Md5Hash(password, salt, 3).toString(); //生成的密文

        return ciphertext;
    }

    public SysResult comparePassword(String account, String password) {
        ArrayList<SysUser> userList = getUser(account);
        if (userList.size() != 1) {
            return SysResult.build(201, "账户信息异常, 请检查账户信息表");
        }
        SysUser user = userList.get(0);
        String inputPassword = getInputPasswordCiph(password, user.getSalt());
        if (!inputPassword.equals(user.getPassword())) {
            return SysResult.build(201, "输入密码错误");
        }
        return SysResult.build(200,"验证成功");
    }

    @Override
    public SysResult getUserByCond(String info, Integer startLimit, Integer num){
        //ArrayList<HashMap<String, Object>> userInfo;
        if(info == null || info.equals("")){
            // 搜索全部
            return SysResult.build(200,"搜索成功",shiroMapper.getAllUser(startLimit,num));
        }else{
            // 有搜索条件
            info = "\'%" + info + "%\'";
            return SysResult.build(200,"搜索成功",shiroMapper.getUserByCond(info, startLimit, num));
        }
    }

    @Override
    public SysResult softDeleteUser(String uid){
        try{
            shiroMapper.softDeleteStaff(uid);
            return SysResult.build(200,"软删除员工成功");
        }catch (Exception e)
        {
            return SysResult.build(201,"软删除异常   "+e);
        }
    }

    @Override
    public SysResult resetPassword(String newPassword, String account){
        try{
            shiroMapper.resetPassword(newPassword, account);
            return SysResult.build(200,"重设密码成功, 密码重置为123456");
        }catch (Exception e)
        {
            return SysResult.build(201,"重设密码失败  "+e);
        }
    }
}
