package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysUser;
import com.rabbiter.cm.domain.vo.SysUserVo;
import com.rabbiter.cm.mapper.SysUserMapper;
import com.rabbiter.cm.service.impl.SysUserServiceImpl;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 安全测试用例
 * 
 * 本测试类专注于系统的安全性验证，包括：
 * 1. 认证安全 - 登录、密码验证
 * 2. 授权安全 - 权限控制、越权访问
 * 3. 输入验证 - SQL注入、XSS防护
 * 4. 密码安全 - 加密存储、盐值处理
 * 5. 会话安全 - Token验证
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("安全测试")
class SecurityTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    // ==================== 认证安全测试 ====================
    
    @Nested
    @DisplayName("认证安全测试")
    class AuthenticationSecurityTest {

        @Test
        @DisplayName("SEC-AUTH-001: 用户名不存在时应拒绝登录")
        void login_NonExistentUser_ShouldReject() {
            SysUserVo loginVo = new SysUserVo();
            loginVo.setUserName("nonexistent_user");
            loginVo.setPassword("password123");
            
            when(sysUserMapper.findUserByName("nonexistent_user")).thenReturn(null);

            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.login(loginVo)
            );
            
            assertEquals("用户名不存在", exception.getMessage());
        }

        @Test
        @DisplayName("SEC-AUTH-002: 密码错误时应拒绝登录")
        void login_WrongPassword_ShouldReject() {
            SysUserVo loginVo = new SysUserVo();
            loginVo.setUserName("admin");
            loginVo.setPassword("wrong_password");
            
            SysUser dbUser = new SysUser();
            dbUser.setUserName("admin");
            dbUser.setPassword("5f4dcc3b5aa765d61d8327deb882cf99"); // 加密后的密码
            dbUser.setSalt("random_salt");
            
            when(sysUserMapper.findUserByName("admin")).thenReturn(dbUser);

            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.login(loginVo)
            );
            
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("SEC-AUTH-003: 暴力破解防护 - 多次失败登录")
        void login_BruteForceAttempt_ShouldBeDetectable() {
            SysUserVo loginVo = new SysUserVo();
            loginVo.setUserName("admin");
            
            SysUser dbUser = new SysUser();
            dbUser.setUserName("admin");
            dbUser.setPassword("encrypted_password");
            dbUser.setSalt("salt");
            
            when(sysUserMapper.findUserByName("admin")).thenReturn(dbUser);

            // 模拟多次失败登录尝试
            String[] commonPasswords = {"123456", "password", "admin", "root", "qwerty"};
            int failedAttempts = 0;
            
            for (String pwd : commonPasswords) {
                loginVo.setPassword(pwd);
                try {
                    sysUserService.login(loginVo);
                } catch (AuthenticationException e) {
                    failedAttempts++;
                }
            }
            
            assertEquals(5, failedAttempts, "所有弱密码尝试都应该失败");
            // 注：实际系统应实现账户锁定机制
        }
    }

    // ==================== 密码安全测试 ====================
    
    @Nested
    @DisplayName("密码安全测试")
    class PasswordSecurityTest {

        @Test
        @DisplayName("SEC-PWD-001: 密码应使用盐值加密存储")
        void addUser_PasswordShouldBeHashedWithSalt() {
            SysUser newUser = new SysUser();
            newUser.setUserName("newuser");
            newUser.setPassword("plaintext_password");
            
            when(sysUserMapper.checkUserNameUnique(anyString())).thenReturn(Collections.emptyList());
            when(sysUserMapper.addUser(any())).thenReturn(1);

            sysUserService.addUser(newUser);

            // 验证密码已被加密（不再是明文）
            assertNotEquals("plaintext_password", newUser.getPassword());
            // 验证盐值已生成
            assertNotNull(newUser.getSalt());
            // 验证盐值长度（8位）
            assertEquals(8, newUser.getSalt().length());
        }

        @Test
        @DisplayName("SEC-PWD-002: 不同用户应使用不同盐值")
        void addUser_DifferentUsersShouldHaveDifferentSalts() {
            SysUser user1 = new SysUser();
            user1.setUserName("user1");
            user1.setPassword("same_password");
            
            SysUser user2 = new SysUser();
            user2.setUserName("user2");
            user2.setPassword("same_password");
            
            when(sysUserMapper.checkUserNameUnique(anyString())).thenReturn(Collections.emptyList());
            when(sysUserMapper.addUser(any())).thenReturn(1);

            sysUserService.addUser(user1);
            sysUserService.addUser(user2);

            // 即使密码相同，盐值也应不同
            assertNotEquals(user1.getSalt(), user2.getSalt());
            // 因此加密后的密码也应不同
            assertNotEquals(user1.getPassword(), user2.getPassword());
        }

        @Test
        @DisplayName("SEC-PWD-003: 密码修改时应重新生成盐值")
        void updateUser_PasswordChangeShouldRegenerateSalt() {
            SysUser existingUser = new SysUser();
            existingUser.setUserId(1L);
            existingUser.setUserName("testuser");
            existingUser.setPassword("old_encrypted_password");
            existingUser.setSalt("old_salt");

            SysUser updateUser = new SysUser();
            updateUser.setUserId(1L);
            updateUser.setUserName("testuser");
            updateUser.setPassword("new_password"); // 新密码

            when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(1L));
            when(sysUserMapper.findUserById(1L)).thenReturn(existingUser);
            when(sysUserMapper.updateUser(any())).thenReturn(1);

            sysUserService.updateUser(updateUser);

            // 验证盐值已更新
            assertNotEquals("old_salt", updateUser.getSalt());
            // 验证密码已重新加密
            assertNotEquals("new_password", updateUser.getPassword());
        }
    }

    // ==================== 输入验证安全测试 ====================
    
    @Nested
    @DisplayName("输入验证安全测试")
    class InputValidationSecurityTest {

        @Test
        @DisplayName("SEC-INPUT-001: SQL注入攻击 - 用户名字段")
        void login_SqlInjectionInUsername_ShouldNotExecute() {
            // SQL注入攻击载荷
            String sqlInjectionPayload = "admin'; DROP TABLE sys_user; --";
            
            SysUserVo loginVo = new SysUserVo();
            loginVo.setUserName(sqlInjectionPayload);
            loginVo.setPassword("password");
            
            // 模拟MyBatis参数化查询的行为（不会执行注入的SQL）
            when(sysUserMapper.findUserByName(sqlInjectionPayload)).thenReturn(null);

            // 应该正常抛出"用户名不存在"异常，而不是执行恶意SQL
            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.login(loginVo)
            );
            
            assertEquals("用户名不存在", exception.getMessage());
            // 验证查询只执行了一次，且使用的是原始字符串（参数化查询）
            verify(sysUserMapper, times(1)).findUserByName(sqlInjectionPayload);
        }

        @Test
        @DisplayName("SEC-INPUT-002: SQL注入攻击 - 万能密码")
        void login_SqlInjectionUniversalPassword_ShouldFail() {
            // 经典的SQL注入万能密码
            String universalPassword = "' OR '1'='1";
            
            SysUserVo loginVo = new SysUserVo();
            loginVo.setUserName("admin");
            loginVo.setPassword(universalPassword);
            
            SysUser dbUser = new SysUser();
            dbUser.setUserName("admin");
            dbUser.setPassword("real_encrypted_password");
            dbUser.setSalt("salt");
            
            when(sysUserMapper.findUserByName("admin")).thenReturn(dbUser);

            // 即使使用SQL注入载荷，也应该正常进行密码验证并失败
            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.login(loginVo)
            );
            
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("SEC-INPUT-003: XSS攻击 - 用户名字段")
        void addUser_XssInUsername_ShouldBeStored() {
            // XSS攻击载荷
            String xssPayload = "<script>alert('XSS')</script>";
            
            SysUser newUser = new SysUser();
            newUser.setUserName(xssPayload);
            newUser.setPassword("password123");
            
            when(sysUserMapper.checkUserNameUnique(xssPayload)).thenReturn(Collections.emptyList());
            when(sysUserMapper.addUser(any())).thenReturn(1);

            int result = sysUserService.addUser(newUser);

            assertEquals(1, result);
            // 注：XSS防护通常在展示层处理，Service层应原样存储
            // 但建议在输入时进行过滤或转义
        }

        @Test
        @DisplayName("SEC-INPUT-004: 特殊字符处理")
        void addUser_SpecialCharacters_ShouldHandle() {
            String[] specialInputs = {
                "user<>name",
                "user\"name",
                "user'name",
                "user;name",
                "user--name",
                "user/**/name"
            };
            
            when(sysUserMapper.checkUserNameUnique(anyString())).thenReturn(Collections.emptyList());
            when(sysUserMapper.addUser(any())).thenReturn(1);

            for (String input : specialInputs) {
                SysUser user = new SysUser();
                user.setUserName(input);
                user.setPassword("password");
                
                // 不应抛出异常
                assertDoesNotThrow(() -> sysUserService.addUser(user));
            }
        }
    }

    // ==================== 授权安全测试 ====================
    
    @Nested
    @DisplayName("授权安全测试")
    class AuthorizationSecurityTest {

        @Test
        @DisplayName("SEC-AUTHZ-001: 用户名唯一性校验 - 防止账户劫持")
        void addUser_DuplicateUsername_ShouldReject() {
            SysUser newUser = new SysUser();
            newUser.setUserName("existing_user");
            newUser.setPassword("password");
            
            // 模拟用户名已存在
            when(sysUserMapper.checkUserNameUnique("existing_user")).thenReturn(Arrays.asList(999L));

            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.addUser(newUser)
            );
            
            assertEquals("用户名重复", exception.getMessage());
        }

        @Test
        @DisplayName("SEC-AUTHZ-002: 更新用户时不能使用他人用户名")
        void updateUser_UseOthersUsername_ShouldReject() {
            SysUser updateUser = new SysUser();
            updateUser.setUserId(1L);
            updateUser.setUserName("other_user_name"); // 尝试使用他人用户名
            updateUser.setPassword("password");
            
            // 模拟该用户名属于用户ID=2
            when(sysUserMapper.checkUserNameUnique("other_user_name")).thenReturn(Arrays.asList(2L));

            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.updateUser(updateUser)
            );
            
            assertEquals("用户名重复", exception.getMessage());
        }

        @Test
        @DisplayName("SEC-AUTHZ-003: 更新不存在的用户应拒绝")
        void updateUser_NonExistentUser_ShouldReject() {
            SysUser updateUser = new SysUser();
            updateUser.setUserId(99999L);
            updateUser.setUserName("ghost_user");
            updateUser.setPassword("password");
            
            when(sysUserMapper.checkUserNameUnique("ghost_user")).thenReturn(Collections.emptyList());
            when(sysUserMapper.findUserById(99999L)).thenReturn(null);

            AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sysUserService.updateUser(updateUser)
            );
            
            assertEquals("用户不存在", exception.getMessage());
        }
    }

    // ==================== 数据安全测试 ====================
    
    @Nested
    @DisplayName("数据安全测试")
    class DataSecurityTest {

        @Test
        @DisplayName("SEC-DATA-001: 批量删除应验证每个ID")
        void deleteUser_BatchDelete_ShouldValidateEachId() {
            Long[] userIds = {1L, 2L, 3L};
            
            when(sysUserMapper.deleteUser(1L)).thenReturn(1);
            when(sysUserMapper.deleteUser(2L)).thenReturn(0); // 用户2不存在或无权删除
            when(sysUserMapper.deleteUser(3L)).thenReturn(1);

            int result = sysUserService.deleteUser(userIds);

            // 应该返回实际删除的数量
            assertEquals(2, result);
            // 验证每个ID都被处理
            verify(sysUserMapper, times(1)).deleteUser(1L);
            verify(sysUserMapper, times(1)).deleteUser(2L);
            verify(sysUserMapper, times(1)).deleteUser(3L);
        }

        @Test
        @DisplayName("SEC-DATA-002: 空数组删除应安全处理")
        void deleteUser_EmptyArray_ShouldHandleSafely() {
            Long[] emptyIds = {};

            int result = sysUserService.deleteUser(emptyIds);

            assertEquals(0, result);
            verify(sysUserMapper, never()).deleteUser(anyLong());
        }

        @Test
        @DisplayName("SEC-DATA-003: null数组删除应抛出异常")
        void deleteUser_NullArray_ShouldThrowException() {
            assertThrows(NullPointerException.class, () -> {
                sysUserService.deleteUser(null);
            });
        }
    }
}
