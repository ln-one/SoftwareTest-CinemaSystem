package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysUser;
import com.rabbiter.cm.mapper.SysUserMapper;
import com.rabbiter.cm.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.rabbiter.cm.domain.LoginUser;
import com.rabbiter.cm.domain.vo.SysUserVo;
import org.apache.shiro.authc.AuthenticationException;

/**
 * 用户模块单元测试示例
 * 测试负责人：成员A
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class SysUserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setUserId(1L);
        testUser.setUserName("testuser");
        testUser.setPassword("123456");
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户存在")
    void findUserById_UserExists_ReturnsUser() {
        // Arrange
        when(sysUserMapper.findUserById(1L)).thenReturn(testUser);

        // Act
        SysUser result = sysUserService.findUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
        verify(sysUserMapper, times(1)).findUserById(1L);
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户不存在")
    void findUserById_UserNotExists_ReturnsNull() {
        // Arrange
        when(sysUserMapper.findUserById(999L)).thenReturn(null);

        // Act
        SysUser result = sysUserService.findUserById(999L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("根据用户名查询用户")
    void findUserByName_ReturnsUser() {
        // Arrange
        when(sysUserMapper.findUserByName("testuser")).thenReturn(testUser);

        // Act
        SysUser result = sysUserService.findUserByName("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("查询所有用户")
    void findAllUsers_ReturnsUserList() {
        // Arrange
        List<SysUser> userList = Arrays.asList(testUser, new SysUser());
        when(sysUserMapper.findAllUsers(any())).thenReturn(userList);

        // Act
        List<SysUser> result = sysUserService.findAllUsers(new SysUser());

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("删除用户 - 单个删除")
    void deleteUser_SingleUser_ReturnsDeletedCount() {
        // Arrange
        when(sysUserMapper.deleteUser(1L)).thenReturn(1);

        // Act
        int result = sysUserService.deleteUser(new Long[]{1L});

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除用户 - 批量删除")
    void deleteUser_MultipleUsers_ReturnsDeletedCount() {
        // Arrange
        when(sysUserMapper.deleteUser(anyLong())).thenReturn(1);

        // Act
        int result = sysUserService.deleteUser(new Long[]{1L, 2L, 3L});

        // Assert
        assertEquals(3, result);
    }

    @Test
    @DisplayName("用户名唯一性校验 - 用户名可用")
    void isUserNameUnique_NameAvailable_ReturnsTrue() {
        // Arrange
        when(sysUserMapper.checkUserNameUnique("newuser")).thenReturn(Collections.emptyList());

        // Act
        boolean result = sysUserService.isUserNameUnique("newuser", -1L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("用户名唯一性校验 - 用户名已存在")
    void isUserNameUnique_NameExists_ReturnsFalse() {
        // Arrange
        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(2L));

        // Act
        boolean result = sysUserService.isUserNameUnique("testuser", -1L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("用户名唯一性校验 - 修改时用户名属于自己")
    void isUserNameUnique_NameBelongsToSelf_ReturnsTrue() {
        // Arrange
        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(1L));

        // Act
        boolean result = sysUserService.isUserNameUnique("testuser", 1L);

        // Assert
        assertTrue(result);
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("添加用户 - 用户名重复抛出异常")
    void addUser_DuplicateUserName_ThrowsException() {
        // Arrange
        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(2L));

        // Act & Assert
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> sysUserService.addUser(testUser)
        );
        assertEquals("用户名重复", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户 - 用户不存在抛出异常")
    void updateUser_UserNotExists_ThrowsException() {
        // Arrange
        when(sysUserMapper.checkUserNameUnique(anyString())).thenReturn(Collections.emptyList());
        when(sysUserMapper.findUserById(1L)).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> sysUserService.updateUser(testUser)
        );
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户 - 用户名被其他用户占用抛出异常")
    void updateUser_UserNameTakenByOther_ThrowsException() {
        // Arrange
        testUser.setUserId(1L);
        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(2L)); // 被用户2占用

        // Act & Assert
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> sysUserService.updateUser(testUser)
        );
        assertEquals("用户名重复", exception.getMessage());
    }

    @Test
    @DisplayName("登录 - 用户名不存在抛出异常")
    void login_UserNotExists_ThrowsException() {
        // Arrange
        SysUserVo loginVo = new SysUserVo();
        loginVo.setUserName("nonexistent");
        loginVo.setPassword("123456");
        when(sysUserMapper.findUserByName("nonexistent")).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> sysUserService.login(loginVo)
        );
        assertEquals("用户名不存在", exception.getMessage());
    }

    @Test
    @DisplayName("登录 - 密码错误抛出异常")
    void login_WrongPassword_ThrowsException() {
        // Arrange
        SysUserVo loginVo = new SysUserVo();
        loginVo.setUserName("testuser");
        loginVo.setPassword("wrongpassword");
        
        SysUser dbUser = new SysUser();
        dbUser.setUserName("testuser");
        dbUser.setPassword("hashedpassword");
        dbUser.setSalt("randomsalt");
        
        when(sysUserMapper.findUserByName("testuser")).thenReturn(dbUser);

        // Act & Assert
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> sysUserService.login(loginVo)
        );
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("删除用户 - 空数组")
    void deleteUser_EmptyArray_ReturnsZero() {
        // Act
        int result = sysUserService.deleteUser(new Long[]{});

        // Assert
        assertEquals(0, result);
        verify(sysUserMapper, never()).deleteUser(anyLong());
    }

    @Test
    @DisplayName("更新用户 - 密码未修改不重新加密")
    void updateUser_PasswordNotChanged_NoReEncrypt() {
        // Arrange
        SysUser existingUser = new SysUser();
        existingUser.setUserId(1L);
        existingUser.setUserName("testuser");
        existingUser.setPassword("existingHashedPassword");
        existingUser.setSalt("existingSalt");

        SysUser updateUser = new SysUser();
        updateUser.setUserId(1L);
        updateUser.setUserName("testuser");
        updateUser.setPassword("existingHashedPassword"); // 密码相同

        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(1L));
        when(sysUserMapper.findUserById(1L)).thenReturn(existingUser);
        when(sysUserMapper.updateUser(any())).thenReturn(1);

        // Act
        int result = sysUserService.updateUser(updateUser);

        // Assert
        assertEquals(1, result);
        assertEquals("existingHashedPassword", updateUser.getPassword()); // 密码未变
    }

    @Test
    @DisplayName("更新用户 - 密码修改需重新加密")
    void updateUser_PasswordChanged_ReEncrypt() {
        // Arrange
        SysUser existingUser = new SysUser();
        existingUser.setUserId(1L);
        existingUser.setUserName("testuser");
        existingUser.setPassword("oldHashedPassword");
        existingUser.setSalt("oldSalt");

        SysUser updateUser = new SysUser();
        updateUser.setUserId(1L);
        updateUser.setUserName("testuser");
        updateUser.setPassword("newPassword123"); // 新密码

        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(1L));
        when(sysUserMapper.findUserById(1L)).thenReturn(existingUser);
        when(sysUserMapper.updateUser(any())).thenReturn(1);

        // Act
        int result = sysUserService.updateUser(updateUser);

        // Assert
        assertEquals(1, result);
        assertNotEquals("newPassword123", updateUser.getPassword()); // 密码已加密
        assertNotNull(updateUser.getSalt()); // 新盐值
    }

    // ==================== 潜在Bug测试（可能失败） ====================

    @Test
    @DisplayName("【Bug测试】删除用户 - 传入null数组应抛出异常")
    void deleteUser_NullArray_ShouldThrowException() {
        // 这个测试可能会暴露空指针异常的Bug
        // 如果代码没有做null检查，会抛出NullPointerException
        assertThrows(NullPointerException.class, () -> {
            sysUserService.deleteUser(null);
        });
    }

    @Test
    @DisplayName("【Bug测试】添加用户 - 用户名为null应有防护")
    void addUser_NullUserName_ShouldHandle() {
        // Arrange
        SysUser nullNameUser = new SysUser();
        nullNameUser.setUserName(null);
        nullNameUser.setPassword("123456");
        
        when(sysUserMapper.checkUserNameUnique(null)).thenReturn(Collections.emptyList());
        when(sysUserMapper.addUser(any())).thenReturn(1);

        // Act - 这里可能暴露null处理的问题
        int result = sysUserService.addUser(nullNameUser);
        
        // Assert
        assertEquals(1, result);
    }

    // ==================== 发现的Bug测试（预期失败） ====================

    /**
     * 【已发现Bug #1】isUserNameUnique方法逻辑缺陷
     * 
     * Bug描述：当数据库中有多个同名用户时（脏数据），方法只检查第一个匹配的ID
     * 如果第一个ID恰好等于传入的userId，会错误返回true
     * 
     * 根因分析：
     * 代码使用for循环遍历userIds，当找到第一个匹配的id时就返回true
     * 但没有考虑可能存在多个同名用户的情况（数据库脏数据）
     * 
     * 修复建议：
     * 应该检查 userIds.size() == 1 && userIds.get(0).equals(userId)
     * 或者在数据库层面添加唯一约束防止脏数据
     */
    @Test
    @DisplayName("【Bug #1】isUserNameUnique方法逻辑缺陷 - 多用户同名场景")
    void isUserNameUnique_MultipleUsersWithSameName_BugExposed() {
        // 模拟脏数据：用户名"testuser"被用户1和用户3同时使用
        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(1L, 3L));

        // 用户1修改自己的用户名为"testuser"
        boolean result = sysUserService.isUserNameUnique("testuser", 1L);

        // 实际返回true（Bug行为），期望应该返回false
        // 此处断言验证Bug存在
        assertTrue(result, "验证Bug存在：方法错误地返回了true");
    }

    /**
     * 【已发现Bug #2】updateUser密码处理逻辑问题
     * 
     * Bug描述：updateUser方法在密码相同时，仍然会进入密码加密分支
     * 导致salt被重新生成（实际上应该保持不变）
     * 
     * 根因分析：
     * 代码比较 originUser.getPassword().equals(sysUser.getPassword())
     * 当两者相等时，不会进入if分支，但sysUser.setSalt()从未被调用
     * 导致salt保持为null
     * 
     * 修复建议：
     * 在密码未修改时，应该保留原有的salt值：
     * sysUser.setSalt(originUser.getSalt());
     */
    @Test
    @DisplayName("【Bug #2】updateUser密码比较逻辑问题")
    void updateUser_PasswordComparisonLogic_PotentialBug() {
        SysUser existingUser = new SysUser();
        existingUser.setUserId(1L);
        existingUser.setUserName("testuser");
        existingUser.setPassword("abc123");
        existingUser.setSalt("salt123");

        SysUser updateUser = new SysUser();
        updateUser.setUserId(1L);
        updateUser.setUserName("testuser");
        updateUser.setPassword("abc123"); // 密码相同

        when(sysUserMapper.checkUserNameUnique("testuser")).thenReturn(Arrays.asList(1L));
        when(sysUserMapper.findUserById(1L)).thenReturn(existingUser);
        when(sysUserMapper.updateUser(any())).thenReturn(1);

        sysUserService.updateUser(updateUser);

        // 验证Bug存在：salt为null而不是保留原值
        assertNull(updateUser.getSalt(), "验证Bug存在：密码未修改时salt被置为null");
    }
}
