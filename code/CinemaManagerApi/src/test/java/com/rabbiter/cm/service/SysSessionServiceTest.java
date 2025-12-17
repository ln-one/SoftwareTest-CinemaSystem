package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysSession;
import com.rabbiter.cm.domain.vo.SysSessionVo;
import com.rabbiter.cm.mapper.SysSessionMapper;
import com.rabbiter.cm.service.impl.SysSessionServiceImpl;
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
 * 场次模块单元测试
 * 测试负责人：成员B
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("场次服务测试")
class SysSessionServiceTest {

    @Mock
    private SysSessionMapper sysSessionMapper;

    @InjectMocks
    private SysSessionServiceImpl sysSessionService;

    private SysSession testSession;

    @BeforeEach
    void setUp() {
        testSession = new SysSession();
        testSession.setSessionId(1L);
        testSession.setMovieId(1L);
        testSession.setHallId(1L);
    }

    @Test
    @DisplayName("根据条件查询场次")
    void findByVo_ReturnsSessionList() {
        List<SysSession> sessionList = Arrays.asList(testSession);
        when(sysSessionMapper.findByVo(any())).thenReturn(sessionList);

        List<SysSession> result = sysSessionService.findByVo(new SysSessionVo());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("根据电影ID或影厅ID查询场次")
    void findSessionByMovieIdOrHallId_ReturnsSessionList() {
        List<SysSession> sessionList = Arrays.asList(testSession);
        when(sysSessionMapper.findSessionByMovieIdOrHallId(any())).thenReturn(sessionList);

        List<SysSession> result = sysSessionService.findSessionByMovieIdOrHallId(new SysSession());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("根据ID查询场次 - 场次存在")
    void findSessionById_SessionExists_ReturnsSession() {
        when(sysSessionMapper.findSessionById(1L)).thenReturn(testSession);

        SysSession result = sysSessionService.findSessionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSessionId());
    }

    @Test
    @DisplayName("根据ID查询场次 - 场次不存在")
    void findSessionById_SessionNotExists_ReturnsNull() {
        when(sysSessionMapper.findSessionById(999L)).thenReturn(null);

        SysSession result = sysSessionService.findSessionById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("查询单个场次详情")
    void findOneSession_ReturnsSession() {
        when(sysSessionMapper.findOneSession(1L)).thenReturn(testSession);

        SysSession result = sysSessionService.findOneSession(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("添加场次")
    void addSession_ReturnsInsertedCount() {
        when(sysSessionMapper.addSession(any())).thenReturn(1);

        int result = sysSessionService.addSession(testSession);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("更新场次")
    void updateSession_ReturnsUpdatedCount() {
        when(sysSessionMapper.updateSession(any())).thenReturn(1);

        int result = sysSessionService.updateSession(testSession);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除场次 - 单个删除")
    void deleteSession_SingleSession_ReturnsDeletedCount() {
        when(sysSessionMapper.deleteSession(1L)).thenReturn(1);

        int result = sysSessionService.deleteSession(new Long[]{1L});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除场次 - 批量删除")
    void deleteSession_MultipleSessions_ReturnsDeletedCount() {
        when(sysSessionMapper.deleteSession(anyLong())).thenReturn(1);

        int result = sysSessionService.deleteSession(new Long[]{1L, 2L});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("根据电影ID查询场次")
    void findSessionByMovieId_ReturnsSessionList() {
        List<SysSession> sessionList = Arrays.asList(testSession);
        when(sysSessionMapper.findSessionByMovieId(1L)).thenReturn(sessionList);

        List<SysSession> result = sysSessionService.findSessionByMovieId(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("根据电影ID查询场次 - 无场次")
    void findSessionByMovieId_NoSessions_ReturnsEmptyList() {
        when(sysSessionMapper.findSessionByMovieId(999L)).thenReturn(Collections.emptyList());

        List<SysSession> result = sysSessionService.findSessionByMovieId(999L);

        assertTrue(result.isEmpty());
    }
}
