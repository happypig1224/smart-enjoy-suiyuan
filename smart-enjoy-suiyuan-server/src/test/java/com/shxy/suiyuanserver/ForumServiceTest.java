package com.shxy.suiyuanserver;

import com.shxy.suiyuanserver.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class ForumServiceTest {

    @Autowired
    private PostService postService;
/*
    @Test
    void testQueryPostList() {
        Result<PageResult> result = postService.queryList(1, 10, null, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        PageResult pageResult = result.getData();
        assertNotNull(pageResult.getRecords());
        log.info("帖子列表查询成功，总数：{}, 当前页：{}", pageResult.getTotal(), pageResult.getPage());
    }

    @Test
    void testQueryPostListByType() {
        Result<PageResult> result = postService.queryList(1, 10, 0, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());

        PageResult pageResult = result.getData();
        log.info("技术讨论类型查询成功，数量：{}", pageResult.getRecords().size());
    }

    @Test
    void testQueryPostListByHotSort() {
        Result<PageResult> result = postService.queryList(1, 10, null, "hot");

        assertNotNull(result);
        assertEquals(200, result.getCode());

        PageResult pageResult = result.getData();
        log.info("热门排序查询成功，数量：{}", pageResult.getRecords().size());
    }

    @Test
    void testGetPostDetail() {
        Post firstPost = postService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                        .last("LIMIT 1")
        );

        if (firstPost != null) {
            Result<PostVO> result = postService.getDetail(firstPost.getId());

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());

            PostVO vo = result.getData();
            assertEquals(firstPost.getId(), vo.getId());
            log.info("获取帖子详情成功：{}", vo.getTitle());
        } else {
            log.info("没有找到帖子数据，跳过详情测试");
        }
    }

    @Test
    void testCreatePost() {
        Long userId = 1L;

        PostCreateDTO dto = PostCreateDTO.builder()
                .title("测试发布帖子")
                .content("这是一条测试的帖子内容")
                .type(0)
                .images(Arrays.asList("http://example.com/image1.jpg"))
                .build();

        try {
            Result<PostVO> result = postService.createPost(userId, dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertNotNull(result.getData().getId());

            log.info("发布帖子成功，ID：{}", result.getData().getId());
        } catch (Exception e) {
            log.info("发布测试失败（可能是用户 ID 问题）: {}", e.getMessage());
        }
    }

    @Test
    void testPagination() {
        Result<PageResult> result1 = postService.queryList(1, 5, null, null);
        Result<PageResult> result2 = postService.queryList(2, 5, null, null);

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