package backend.clockin.service;

import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.pojo.tool.SysResult;

import java.util.ArrayList;
import java.util.HashMap;

public interface UserService {

    String addUser(SysUser user);

    String deleteUser(String account);

    SysResult changeUser(HashMap<String,String> info, String account);

    ArrayList<SysUser> getUser(String account);

    String getInputPasswordCiph(String password, String salt);

    SysResult comparePassword(String account, String password);

    SysResult getUserByCond(String info, Integer startLimit, Integer num);

    SysResult softDeleteUser(String uid);

    SysResult resetPassword(String newPassword, String account);
}
