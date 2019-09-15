package backend.clockin.service;



import backend.clockin.pojo.shiro.SysRole;

import java.util.ArrayList;


public interface RoleService {

    String addRolePerm(SysRole role);

    String deleteRolePerm(SysRole role);

    String deleteRole(String name);

    String changeRolePerm(SysRole role, String newPerm);

    ArrayList<String> getRole(String name);
}
