package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysRole;
import com.rabbiter.cm.mapper.SysRoleMapper;
import com.rabbiter.cm.service.impl.SysRoleServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 角色模块单元测试
 * 测试负责人：成员E
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("角色服务测试")
class SysRoleServiceTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @InjectMocks
    private SysRoleServiceImpl sysRoleService;

    private SysRole testRole;

    @BeforeEach
    void setUp() {
        testRole = new SysRole();
        testRole.setRoleId(1L);
        testRole.setRoleName("管理员");
        testRole.setRoleDesc("系统管理员");
    }

    @Test
    @DisplayName("查询所有角色")
    void findAllRoles_ReturnsRoleList() {
        List<SysRole> roleList = Arrays.asList(testRole, new SysRole());
        when(sysRoleMapper.findAllRoles()).thenReturn(roleList);

        List<SysRole> result = sysRoleService.findAllRoles();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询所有角色 - 无角色")
    void findAllRoles_NoRoles_ReturnsEmptyList() {
        when(sysRoleMapper.findAllRoles()).thenReturn(Collections.emptyList());

        List<SysRole> result = sysRoleService.findAllRoles();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("根据ID查询角色 - 角色存在")
    void findRoleById_RoleExists_ReturnsRole() {
        when(sysRoleMapper.findRoleById(1L)).thenReturn(testRole);

        SysRole result = sysRoleService.findRoleById(1L);

        assertNotNull(result);
        assertEquals("管理员", result.getRoleName());
    }

    @Test
    @DisplayName("根据ID查询角色 - 角色不存在")
    void findRoleById_RoleNotExists_ReturnsNull() {
        when(sysRoleMapper.findRoleById(999L)).thenReturn(null);

        SysRole result = sysRoleService.findRoleById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("添加角色")
    void addRole_ReturnsInsertedCount() {
        when(sysRoleMapper.addRole(any())).thenReturn(1);

        int result = sysRoleService.addRole(testRole);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("更新角色")
    void updateRole_ReturnsUpdatedCount() {
        when(sysRoleMapper.updateRole(any())).thenReturn(1);

        int result = sysRoleService.updateRole(testRole);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除角色 - 单个删除")
    void deleteRole_SingleRole_ReturnsDeletedCount() {
        when(sysRoleMapper.deleteRole(1L)).thenReturn(1);

        int result = sysRoleService.deleteRole(new Long[]{1L});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除角色 - 批量删除")
    void deleteRole_MultipleRoles_ReturnsDeletedCount() {
        when(sysRoleMapper.deleteRole(anyLong())).thenReturn(1);

        int result = sysRoleService.deleteRole(new Long[]{1L, 2L});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("分配权限 - 新增权限")
    void allotRight_AddNewRights_ReturnsCount() {
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Collections.emptyList());
        when(sysRoleMapper.addRight(anyLong(), anyLong())).thenReturn(1);

        int result = sysRoleService.allotRight(1L, new Long[]{101L, 102L});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("分配权限 - 删除权限")
    void allotRight_RemoveRights_ReturnsCount() {
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Arrays.asList(101L, 102L));
        when(sysRoleMapper.deleteRight(anyLong(), anyLong())).thenReturn(1);

        int result = sysRoleService.allotRight(1L, new Long[]{});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("分配权限 - 部分新增部分删除")
    void allotRight_MixedOperation_ReturnsCount() {
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Arrays.asList(101L));
        when(sysRoleMapper.addRight(anyLong(), anyLong())).thenReturn(1);
        when(sysRoleMapper.deleteRight(anyLong(), anyLong())).thenReturn(1);

        int result = sysRoleService.allotRight(1L, new Long[]{102L});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("分配权限 - 权限不变")
    void allotRight_NoChange_ReturnsZero() {
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Arrays.asList(101L));

        int result = sysRoleService.allotRight(1L, new Long[]{101L});

        assertEquals(0, result);
    }

    // ==================== 复杂场景测试 ====================

    @Test
    @DisplayName("分配权限 - 大量权限变更")
    void allotRight_MassiveChanges_ReturnsCorrectCount() {
        // 原有权限: 1,2,3,4,5
        // 新权限: 3,4,5,6,7
        // 应删除: 1,2  应新增: 6,7
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        when(sysRoleMapper.addRight(anyLong(), anyLong())).thenReturn(1);
        when(sysRoleMapper.deleteRight(anyLong(), anyLong())).thenReturn(1);

        int result = sysRoleService.allotRight(1L, new Long[]{3L, 4L, 5L, 6L, 7L});

        assertEquals(4, result); // 2删除 + 2新增
    }

    @Test
    @DisplayName("分配权限 - 清空所有权限")
    void allotRight_ClearAllRights_ReturnsDeletedCount() {
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(sysRoleMapper.deleteRight(anyLong(), anyLong())).thenReturn(1);

        int result = sysRoleService.allotRight(1L, new Long[]{});

        assertEquals(3, result);
        verify(sysRoleMapper, times(3)).deleteRight(eq(1L), anyLong());
    }

    @Test
    @DisplayName("删除角色 - 空数组返回0")
    void deleteRole_EmptyArray_ReturnsZero() {
        int result = sysRoleService.deleteRole(new Long[]{});
        assertEquals(0, result);
    }

    // ==================== 潜在Bug测试 ====================

    @Test
    @DisplayName("【Bug测试】分配权限 - null权限数组")
    void allotRight_NullKeys_ShouldThrowException() {
        when(sysRoleMapper.findAllRights(1L)).thenReturn(Arrays.asList(1L));
        
        assertThrows(NullPointerException.class, () -> {
            sysRoleService.allotRight(1L, null);
        });
    }

    @Test
    @DisplayName("【Bug测试】删除角色 - null数组")
    void deleteRole_NullArray_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            sysRoleService.deleteRole(null);
        });
    }
}
