package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @description 针对表【comment】的数据库操作Mapper
 * @createDate 2026-04-04 21:30:08
 * @Entity com.shxy.entity.Comment
 */
public interface CommentMapper extends BaseMapper<Comment> {

    int updateIsDelete(Long id);

    List<CommentVO> selectCommentListWithUser(@Param("postId") Long postId,
                                              @Param("lostItemId") Long lostItemId,
                                              @Param("offset") int offset,
                                              @Param("size") int size,
                                              @Param("orderBy") String orderBy);

    Long selectCommentCount(@Param("postId") Long postId,
                            @Param("lostItemId") Long lostItemId);
}
