package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuancommon.utils.TencentCOSAvatarUtil;
import com.shxy.suiyuanentity.dto.PostDTO;
import com.shxy.suiyuanentity.dto.PostUpdateDTO;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.entity.PostLike;
import com.shxy.suiyuanentity.vo.PostLikeStatusVO;
import com.shxy.suiyuanentity.vo.PostVO;
import com.shxy.suiyuanserver.mapper.PostLikeMapper;
import com.shxy.suiyuanserver.mapper.PostMapper;
import com.shxy.suiyuanserver.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheUtil redisCacheUtil;
    private final TencentCOSAvatarUtil tencentCOSAvatarUtil;

    public Result<PageResult> listPost(Integer page, Integer size, String sort, Integer type) {
        // 参数验证
        int validatedPage = (page == null || page < 1) ? 1 : page;
        int validatedSize = (size == null || size < 1) ? 10 : Math.min(size, 50); // 限制每页最大数量
        String validatedSort = sort;
        if (validatedSort != null && !isValidSortField(validatedSort)) {
            validatedSort = "newest"; // 默认排序
        }
        Integer validatedType = type;

        // 创建final变量以供lambda表达式使用
        final String finalValidatedSort = validatedSort;
        final Integer finalValidatedType = validatedType;
        final int finalValidatedPage = validatedPage;
        final int finalValidatedSize = validatedSize;

        String cacheKey = RedisConstant.POST_LIST_KEY_PREFIX +
                finalValidatedPage + ":" + finalValidatedSize +
                ":" + (finalValidatedType != null ? finalValidatedType : "all") +
                ":" + (finalValidatedSort != null ? finalValidatedSort : "newest");

        PageResult pageResult = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                PageResult.class,
                key -> {
                    String orderBy = "newest";
                    if ("hottest".equals(finalValidatedSort)) {
                        orderBy = "hottest";
                    } else if ("mostLiked".equals(finalValidatedSort)) {
                        orderBy = "mostLiked";
                    }
                    int offset = (finalValidatedPage - 1) * finalValidatedSize;
                    List<PostVO> postVOList = postMapper.selectPostListWithUser(finalValidatedType, offset, finalValidatedSize, orderBy);
                    Long total = postMapper.selectPostCount(finalValidatedType);
                    return PageResult.builder()
                            .total(total != null ? total : 0)
                            .records(postVOList)
                            .page(finalValidatedPage)
                            .size(finalValidatedSize)
                            .build();
                },
                RedisConstant.POST_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (pageResult == null) {
            return Result.fail("获取帖子列表失败");
        }
        return Result.success(pageResult);
    }
    
    /**
     * 验证排序字段是否合法
     */
    private boolean isValidSortField(String sort) {
        return "newest".equals(sort) || "hottest".equals(sort) || "mostLiked".equals(sort);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> publishPost(PostDTO postDTO) {
        // 验证输入数据
        validatePostData(postDTO);
        
        Long currentUserId = BaseContext.getCurrentUserId();
        String imagesJson = null;
        if (postDTO.getImages() != null && !postDTO.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(postDTO.getImages());
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                throw new BaseException("图片数据格式错误");
            }
        }

        Post post = Post.builder()
                .userId(currentUserId)
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .type(postDTO.getType())
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .images(imagesJson)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        int insert = postMapper.insert(post);
        if (insert <= 0) {
            throw new BaseException("发布帖子失败");
        }

        clearPostListCache();

        return Result.success(post);
    }
    
    /**
     * 验证帖子数据的有效性
     */
    private void validatePostData(PostDTO postDTO) {
        if (postDTO.getTitle() == null || postDTO.getTitle().trim().length() == 0) {
            throw new BaseException("帖子标题不能为空");
        }
        if (postDTO.getTitle().length() > 100) {
            throw new BaseException("帖子标题长度不能超过100个字符");
        }
        if (postDTO.getContent() == null || postDTO.getContent().trim().length() == 0) {
            throw new BaseException("帖子内容不能为空");
        }
        if (postDTO.getContent().length() > 5000) {
            throw new BaseException("帖子内容长度不能超过5000个字符");
        }
        if (postDTO.getImages() != null && postDTO.getImages().size() > 10) {
            throw new BaseException("最多只能上传10张图片");
        }
        // 验证类型是否在有效范围内: 0-技术讨论, 1-课程问题, 2-校园生活, 3-其他
        if (postDTO.getType() == null || postDTO.getType() < 0 || postDTO.getType() > 3) {
            throw new BaseException("帖子类型不合法，有效范围为0-3");
        }
    }

    public Result<PostVO> getPostDetail(Long id) {
        if (id == null || id <= 0) {
            return Result.fail("ID不合法");
        }
        String cacheKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;

        PostVO postVO = redisCacheUtil.queryWithMutex(
                cacheKey,
                PostVO.class,
                key -> {
                    List<PostVO> postVOList = postMapper.selectPostWithUser(id);
                    if (postVOList == null || postVOList.isEmpty()) {
                        return null;
                    }
                    return postVOList.get(0);
                },
                RedisConstant.POST_DETAIL_TTL,
                TimeUnit.SECONDS
        );

        if (postVO == null) {
            throw new BaseException("帖子不存在");
        }

        postMapper.update(null, new LambdaUpdateWrapper<>(Post.class)
                .eq(Post::getId, id)
                .setSql("view_count = view_count + 1"));

        return Result.success(postVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> likePost(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();

        // 查询 post_like 表判断是否已点赞
        PostLike existingLike = postLikeMapper.selectByPostIdAndUserId(id, currentUserId);
        if (existingLike != null) {
            throw new BaseException("已经点赞过");
        }

        PostLike postLike = PostLike.builder()
                .postId(id)
                .userId(currentUserId)
                .createTime(new Date())
                .build();
        int insert = postLikeMapper.insert(postLike);
        if (insert <= 0) {
            throw new BaseException("点赞失败");
        }

        postMapper.update(null, new LambdaUpdateWrapper<>(Post.class)
                .eq(Post::getId, id)
                .setSql("like_count = like_count + 1"));

        // 清除帖子详情缓存,确保下次访问时重新查询点赞状态
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);

        post.setLikeCount(post.getLikeCount() + 1);
        return Result.success(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> cancelLikePost(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();

        // 查询 post_like 表判断是否已点赞
        PostLike existingLike = postLikeMapper.selectByPostIdAndUserId(id, currentUserId);
        if (existingLike == null) {
            throw new BaseException("还未点赞，无法取消");
        }

        // 删除点赞记录
        int delete = postLikeMapper.deleteById(existingLike.getId());
        if (delete <= 0) {
            throw new BaseException("取消点赞失败");
        }

        // 更新帖子点赞数
        postMapper.update(null, new LambdaUpdateWrapper<>(Post.class)
                .eq(Post::getId, id)
                .gt(Post::getLikeCount, 0)
                .setSql("like_count = like_count - 1"));

        // 只清除帖子详情缓存,列表缓存容忍短暂不一致
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);

        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        return Result.success(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> deletePost(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        if (!post.getUserId().equals(currentUserId)) {
            throw new BaseException("只能删除自己发布的帖子");
        }

        // 删除帖子关联的点赞记录
        postLikeMapper.delete(new LambdaQueryWrapper<>(PostLike.class)
                .eq(PostLike::getPostId, id));

        // 删除帖子
        int delete = postMapper.deleteById(id);
        if (delete <= 0) {
            throw new BaseException("删除帖子失败");
        }

        // 清除缓存
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + id;
        redisTemplate.delete(detailKey);
        clearPostListCache();

        log.info("用户{}删除帖子{}成功", currentUserId, id);
        return Result.success("删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Post> updatePost(PostUpdateDTO postUpdateDTO) {
        if (postUpdateDTO == null || postUpdateDTO.getId() == null) {
            throw new BaseException("帖子ID不能为空");
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        Post post = postMapper.selectById(postUpdateDTO.getId());
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        if (!post.getUserId().equals(currentUserId)) {
            throw new BaseException("只能编辑自己发布的帖子");
        }

        // 验证类型有效性
        if (postUpdateDTO.getType() != null && (postUpdateDTO.getType() < 0 || postUpdateDTO.getType() > 3)) {
            throw new BaseException("帖子类型不合法，有效范围为0-3");
        }

        // 验证标题长度
        if (postUpdateDTO.getTitle() != null && postUpdateDTO.getTitle().length() > 100) {
            throw new BaseException("帖子标题长度不能超过100个字符");
        }

        // 验证内容长度
        if (postUpdateDTO.getContent() != null && postUpdateDTO.getContent().length() > 5000) {
            throw new BaseException("帖子内容长度不能超过5000个字符");
        }

        // 验证图片数量
        if (postUpdateDTO.getImages() != null && postUpdateDTO.getImages().size() > 10) {
            throw new BaseException("最多只能上传10张图片");
        }

        // 构建更新条件
        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>(Post.class)
                .eq(Post::getId, postUpdateDTO.getId());

        if (postUpdateDTO.getTitle() != null) {
            updateWrapper.set(Post::getTitle, postUpdateDTO.getTitle());
        }
        if (postUpdateDTO.getContent() != null) {
            updateWrapper.set(Post::getContent, postUpdateDTO.getContent());
        }
        if (postUpdateDTO.getType() != null) {
            updateWrapper.set(Post::getType, postUpdateDTO.getType());
        }
        if (postUpdateDTO.getImages() != null) {
            try {
                String imagesJson = objectMapper.writeValueAsString(postUpdateDTO.getImages());
                updateWrapper.set(Post::getImages, imagesJson);
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                throw new BaseException("图片数据格式错误");
            }
        }
        updateWrapper.set(Post::getUpdateTime, new Date());

        int update = postMapper.update(null, updateWrapper);
        if (update <= 0) {
            throw new BaseException("更新帖子失败");
        }

        // 清除缓存
        String detailKey = RedisConstant.POST_DETAIL_KEY_PREFIX + postUpdateDTO.getId();
        redisTemplate.delete(detailKey);
        clearPostListCache();

        Post updatedPost = postMapper.selectById(postUpdateDTO.getId());
        log.info("用户{}更新帖子{}成功", currentUserId, postUpdateDTO.getId());
        return Result.success(updatedPost);
    }

    private void clearPostListCache() {
        Set<String> keys = new HashSet<>();
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(RedisConstant.POST_LIST_KEY_PREFIX + "*")
                .count(100)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("清除帖子列表缓存, 删除 {} 个key", keys.size());
    }

    public Result<List<PostVO>> getUserPublishedPosts(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException("用户ID不合法");
        }

        String cacheKey = RedisConstant.USER_POST_LIST_KEY_PREFIX + userId;

        List<PostVO> postVOList = redisCacheUtil.queryWithPassThrough(
                cacheKey,
                List.class,
                key -> postMapper.selectPostListByUserId(userId),
                RedisConstant.POST_LIST_TTL,
                TimeUnit.SECONDS
        );

        if (postVOList == null) {
            postVOList = Collections.emptyList();
        }
        return Result.success(postVOList);
    }

    public String uploadPostImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传图片不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BaseException("只能上传图片文件");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BaseException("图片大小不能超过5MB");
        }

        String imageUrl = tencentCOSAvatarUtil.uploadFile(file);
        log.info("用户 {} 上传了帖子图片：{}", BaseContext.getCurrentUserId(), imageUrl);
        return imageUrl;
    }

    public Result<PostLikeStatusVO> getPostLikeStatus(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BaseException("帖子ID不合法");
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        boolean isLiked = false;
        if (currentUserId != null && currentUserId > 0) {
            PostLike postLike = postLikeMapper.selectByPostIdAndUserId(postId, currentUserId);
            isLiked = postLike != null;
        }

        PostLikeStatusVO statusVO = PostLikeStatusVO.builder()
                .postId(postId)
                .isLiked(isLiked)
                .build();

        return Result.success(statusVO);
    }
}