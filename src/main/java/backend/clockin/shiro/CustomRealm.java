package backend.clockin.shiro;

import backend.clockin.pojo.shiro.SysUser;
import backend.clockin.service.RoleService;
import backend.clockin.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.TreeSet;

public class CustomRealm extends AuthorizingRealm {

    @Autowired
    RoleService roleService;
    @Autowired
    UserService userService;


    //定义如何获取用户的角色和权限的逻辑，给shiro做权限判断
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }
        SysUser user = (SysUser) getAvailablePrincipal(principals);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        System.out.println("获取角色信息：" + user.getRoles());
        System.out.println("获取权限信息：" + roleService.getRole(user.getRoles()));

        Set<String> roles = new TreeSet<>();
        roles.add(user.getRoles());
        info.setRoles(roles);

        TreeSet<String> perms = new TreeSet<>(roleService.getRole(user.getRoles()));
        info.setStringPermissions(perms);
        return info;
    }

    //定义如何获取用户信息的业务逻辑，给shiro做登录
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String account = upToken.getUsername();
        // Null username is invalid
        if (account == null) {
            throw new AccountException("请输入账户");
        }
        SysUser userDB = userService.getUser(account).get(0);
        // 这个地方需要验证密码




        //查询用户的角色和权限存到SimpleAuthenticationInfo中，这样在其它地方
        //SecurityUtils.getSubject().getPrincipal()就能拿出用户的所有信息，包括角色和权限
        Set<String> roles = new TreeSet<>();
        roles.add(userDB.getRoles());
        TreeSet<String> perms = new TreeSet<>(roleService.getRole(userDB.getRoles()));

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(userDB, userDB.getPassword(), getName());
        if (userDB.getSalt() != null) {
            info.setCredentialsSalt(ByteSource.Util.bytes(userDB.getSalt()));
        }
        return info;
    }
}
