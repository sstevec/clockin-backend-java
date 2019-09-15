package backend.clockin.shiro;

import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfiguration {

    // 注入Realm
    @Bean
    public CustomRealm customRealm() {
        CustomRealm realm = new CustomRealm();
        return realm;
    }

    // 注入生命周期
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    // 配置安全管理器
    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm());
        return securityManager;
    }

    // 过滤器
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        chainDefinition.addPathDefinition("/api/login", "anon");
        chainDefinition.addPathDefinition("/api/redlogin", "anon");
        chainDefinition.addPathDefinition("/faceClockIn", "anon");
        chainDefinition.addPathDefinition("/clockInAfterConfirm", "anon");
        chainDefinition.addPathDefinition("/api/staff/getVerifyCode", "anon");
        chainDefinition.addPathDefinition("/api/staff/reSetPassword", "anon");
        chainDefinition.addPathDefinition("/templates/**", "anon");
        chainDefinition.addPathDefinition("/static/**", "anon");
        chainDefinition.addPathDefinition("/index", "anon");
        chainDefinition.addPathDefinition("/api/*", "user");
        return chainDefinition;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }
}
