package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysMovie;
import com.rabbiter.cm.domain.vo.SysMovieVo;
import com.rabbiter.cm.mapper.SysMovieMapper;
import com.rabbiter.cm.service.impl.SysMovieServiceImpl;
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
 * 电影模块单元测试
 * 测试负责人：成员C
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("电影服务测试")
class SysMovieServiceTest {

    @Mock
    private SysMovieMapper sysMovieMapper;

    @InjectMocks
    private SysMovieServiceImpl sysMovieService;

    private SysMovie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = new SysMovie();
        testMovie.setMovieId(1L);
        testMovie.setMovieName("测试电影");
        testMovie.setMovieLength(120);
        testMovie.setMovieArea("中国大陆");
        testMovie.setMovieBoxOffice(1000000.0);
    }

    @Test
    @DisplayName("查询所有电影")
    void findAllMovies_ReturnsMovieList() {
        List<SysMovie> movieList = Arrays.asList(testMovie, new SysMovie());
        when(sysMovieMapper.findAllMovies(any())).thenReturn(movieList);

        List<SysMovie> result = sysMovieService.findAllMovies(new SysMovieVo());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据ID查询电影 - 电影存在")
    void findMovieById_MovieExists_ReturnsMovie() {
        when(sysMovieMapper.findMovieById(1L)).thenReturn(testMovie);

        SysMovie result = sysMovieService.findMovieById(1L);

        assertNotNull(result);
        assertEquals("测试电影", result.getMovieName());
    }

    @Test
    @DisplayName("根据ID查询电影 - 电影不存在")
    void findMovieById_MovieNotExists_ReturnsNull() {
        when(sysMovieMapper.findMovieById(999L)).thenReturn(null);

        SysMovie result = sysMovieService.findMovieById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("查询单个电影详情")
    void findOneMovie_ReturnsMovie() {
        when(sysMovieMapper.findOneMovie(1L)).thenReturn(testMovie);

        SysMovie result = sysMovieService.findOneMovie(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("添加电影")
    void addMovie_ReturnsInsertedCount() {
        when(sysMovieMapper.addMovie(any())).thenReturn(1);

        int result = sysMovieService.addMovie(testMovie);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("更新电影")
    void updateMovie_ReturnsUpdatedCount() {
        when(sysMovieMapper.updateMovie(any())).thenReturn(1);

        int result = sysMovieService.updateMovie(testMovie);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除电影 - 单个删除")
    void deleteMovie_SingleMovie_ReturnsDeletedCount() {
        when(sysMovieMapper.deleteMovie(1L)).thenReturn(1);

        int result = sysMovieService.deleteMovie(new Long[]{1L});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除电影 - 批量删除")
    void deleteMovie_MultipleMovies_ReturnsDeletedCount() {
        when(sysMovieMapper.deleteMovie(anyLong())).thenReturn(1);

        int result = sysMovieService.deleteMovie(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("总票房榜")
    void totalBoxOfficeList_ReturnsMovieList() {
        List<SysMovie> movieList = Arrays.asList(testMovie);
        when(sysMovieMapper.totalBoxOfficeList()).thenReturn(movieList);

        List<SysMovie> result = sysMovieService.totalBoxOfficeList();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("国内票房榜")
    void domesticBoxOfficeList_ReturnsMovieList() {
        List<SysMovie> movieList = Arrays.asList(testMovie);
        when(sysMovieMapper.domesticBoxOfficeList()).thenReturn(movieList);

        List<SysMovie> result = sysMovieService.domesticBoxOfficeList();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("国外票房榜")
    void foreignBoxOfficeList_ReturnsMovieList() {
        when(sysMovieMapper.foreignBoxOfficeList()).thenReturn(Collections.emptyList());

        List<SysMovie> result = sysMovieService.foreignBoxOfficeList();

        assertTrue(result.isEmpty());
    }
}
