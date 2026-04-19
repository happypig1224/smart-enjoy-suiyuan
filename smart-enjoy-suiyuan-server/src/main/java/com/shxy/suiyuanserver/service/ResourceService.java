package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ResourceCreateDTO;
import com.shxy.suiyuanentity.entity.Resource;
import com.shxy.suiyuanentity.vo.ResourceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author huang qi long
* @description 针对表【resource】的数据库操作 Service
* @createDate 2026-04-04 21:30:08
*/
public interface ResourceService extends IService<Resource> {

    /**
     * 查询资源列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param type 类型
     * @param sort 排序字段
     * @param order 排序方式
     * @return 分页结果
     */
    Result<PageResult> queryList(Integer page, Integer pageSize, String type, String sort, String order);

    /**
     * 上传资源
     * @param userId 用户 ID
     * @param file 文件
     * @param resourceCreateDTO 资源信息
     * @return 资源 ID
     */
    Result<Long> uploadResource(MultipartFile file, ResourceCreateDTO resourceCreateDTO);

    /**
     * 删除资源
     * @param userId 用户 ID
     * @param id 资源 ID
     * @return 操作结果
     */
    Result<String> deleteResource(Long userId, Long id);

    /**
     * 获取用户发布的资源列表
     * @param userId 用户 ID
     * @return 资源列表
     */
    Result<List<ResourceVO>> getUserPublishedResources(Long userId);

    /**
     * 收藏资源
     * @param userId 用户 ID
     * @param id 资源 ID
     * @return 操作结果
     */
    Result<String> favoriteResource(Long userId, Long id);

    /**
     * 取消收藏资源
     * @param userId 用户 ID
     * @param id 资源 ID
     * @return 操作结果
     */
    Result<String> cancelFavoriteResource(Long userId, Long id);

    /**
     * 获取资源详情
     * @param id 资源 ID
     * @param userId 用户 ID
     * @return 资源详情
     */
    Result<ResourceVO> getResourceDetail(Long id, Long userId);
}
