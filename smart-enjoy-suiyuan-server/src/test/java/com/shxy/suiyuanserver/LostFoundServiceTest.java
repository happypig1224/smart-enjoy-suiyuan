package com.shxy.suiyuanserver;

import com.shxy.suiyuanentity.dto.LostFoundCreateDTO;
import com.shxy.suiyuanserver.mapper.LostFoundMapper;
import com.shxy.suiyuanserver.service.LostFoundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class LostFoundServiceTest {

    @Autowired
    private LostFoundService lostFoundService;

    @Autowired
    private LostFoundMapper lostFoundMapper;
/*
    @Test
    void testQueryLostFoundList() {
        Result<PageResult> result = lostFoundService.queryList(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        PageResult pageResult = result.getData();
        assertNotNull(pageResult.getRecords());
        log.info("失物招领列表查询成功，总数：{}, 当前页：{}", pageResult.getTotal(), pageResult.getPage());
    }

    @Test
    void testQueryLostFoundListByType() {
        Result<PageResult> result = lostFoundService.queryList(1, 10, 0, null, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());

        PageResult pageResult = result.getData();
        log.info("寻物启事类型查询成功，数量：{}", pageResult.getRecords().size());
    }

    @Test
    void testQueryLostFoundListByStatus() {
        Result<PageResult> result = lostFoundService.queryList(1, 10, null, 0, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());

        PageResult pageResult = result.getData();
        log.info("未解决状态查询成功，数量：{}", pageResult.getRecords().size());
    }

    @Test
    void testQueryLostFoundListByUrgent() {
        Result<PageResult> result = lostFoundService.queryList(1, 10, null, null, 1);

        assertNotNull(result);
        assertEquals(200, result.getCode());

        PageResult pageResult = result.getData();
        log.info("紧急状态查询成功，数量：{}", pageResult.getRecords().size());
    }

    @Test
    void testGetLostFoundDetail() {
        LostFound firstLostFound = lostFoundMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LostFound>()
                        .last("LIMIT 1")
        );

        if (firstLostFound != null) {
            Result<LostFoundVO> result = lostFoundService.getDetail(firstLostFound.getId());

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());

            LostFoundVO vo = result.getData();
            assertEquals(firstLostFound.getId(), vo.getId());
            log.info("获取失物招领详情成功：{}", vo.getTitle());
        } else {
            log.info("没有找到失物招领数据，跳过详情测试");
        }
    }

    @Test
    void testCreateLostFound() {
        Long userId = 1L;

        LostFoundCreateDTO dto = LostFoundCreateDTO.builder()
                .type(0)
                .title("测试发布失物招领")
                .description("这是一条测试的失物招领信息")
                .urgent(0)
                .location("图书馆一楼")
                .phoneContact("13800138000")
                .build();

        try {
            Result<LostFoundVO> result = lostFoundService.createLostFound(userId, dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertNotNull(result.getData().getId());

            log.info("发布失物招领成功，ID：{}", result.getData().getId());
        } catch (Exception e) {
            log.info("发布测试失败（可能是用户 ID 问题）: {}", e.getMessage());
        }
    }

    @Test
    void testPagination() {
        Result<PageResult> result1 = lostFoundService.queryList(1, 5, null, null, null);
        Result<PageResult> result2 = lostFoundService.queryList(2, 5, null, null, null);

        assertNotNull(result1);
        assertNotNull(result2);

        PageResult page1 = result1.getData();
        PageResult page2 = result2.getData();

        assertEquals(1, page1.getPage());
        assertEquals(2, page2.getPage());
        assertEquals(5, page1.getSize());

        log.info("分页测试成功，第 1 页：{}条，第 2 页：{}条",
                page1.getRecords().size(), page2.getRecords().size());
    }

    */

}
