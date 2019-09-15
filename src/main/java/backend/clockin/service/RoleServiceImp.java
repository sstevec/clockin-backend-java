package backend.clockin.service;

import backend.clockin.mapper.ShiroMapper;
import backend.clockin.pojo.shiro.SysRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RoleServiceImp implements RoleService {

    @Autowired
    ShiroMapper shiroMapper;

    public String addRolePerm(SysRole role)
    {
        if(getRole(role.getName()).contains(role.getPerm()))
        {
            return "perm already exist";
        }
        shiroMapper.addRolePerm(role);
        return "perm add success";
    }

    public String deleteRolePerm(SysRole role)
    {
        if(getRole(role.getName()).contains(role.getPerm()))
        {
            shiroMapper.deleteRolePerm(role);
            return "perm delete success";
        }
        return "perm does not exist";
    }

    public String deleteRole(String name)
    {
        if(getRole(name).size()  == 0)
        {
            return "role does not exist";
        }
        shiroMapper.deleteRole(name);
        return "delete role success";
    }

    public String changeRolePerm(SysRole role, String newPerm){
        if(getRole(role.getName()).contains(role.getPerm()))
        {
            shiroMapper.changeRolePerm(role, newPerm);
            return "perm change success";
        }
        return "perm does not exist";
    }

    public ArrayList<String> getRole(String name){
        String roles = shiroMapper.getRole(name);
        String[] roleArray = roles.split(",");
        List<String> roleList = Arrays.asList(roleArray);
        return new ArrayList<>(roleList);
    }
}
