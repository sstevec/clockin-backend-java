package backend.clockin.mapper;

import backend.clockin.pojo.shiro.SysRole;
import backend.clockin.pojo.shiro.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface ShiroMapper {

    // SysUser operation
    void addUser(SysUser user);

    void deleteUser(@Param("account") String account);

    void changeUser(@Param("info") HashMap<String, String> info,@Param("account") String account);

    ArrayList<SysUser> getAll();

    ArrayList<SysUser> getUser(@Param("account") String account);


    // role operation
    void addRolePerm(SysRole role);

    void deleteRolePerm(SysRole role);

    void deleteRole(@Param("name") String name);

    void changeRolePerm(SysRole role, @Param("newPerm") String newPerm);

    String getRole(@Param("name") String name);

    ArrayList<SysUser> getAllUser(@Param("startLimit") Integer startLimit,@Param("num") Integer num);

    ArrayList<SysUser> getUserByCond(@Param("info") String info, @Param("startLimit") Integer startLimit,@Param("num") Integer num);

    void softDeleteStaff(@Param("uid") String uid);

    void resetPassword(@Param("password") String newPassword,@Param("account") String account);

    ArrayList<String> checkExpired(@Param("account") String account);

    ArrayList<String> checkUserAlreadyExpired(@Param("uid") String uid);

    Integer countAllUser();

    Integer countByCond(@Param("info") String info);
}
