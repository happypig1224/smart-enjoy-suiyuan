package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ResourceDTO;
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
     * @param subject 学科分类
     * @param sort 排序字段
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    Result<PageResult> queryList(Integer page, Integer pageSize, String type, Integer subject, String sort, String keyword);

    /**
     * 上传资源
     * @param file 文件
     * @param resourceDTO 资源信息
     * @return 资源 ID
     */
    Result<Long> uploadResource(MultipartFile file, ResourceDTO resourceDTO);

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
     * 获取资源详情
     * @param id 资源 ID
     * @param userId 用户 ID
     * @return 资源详情
     */
    Result<ResourceVO> getResourceDetail(Long id, Long userId);

    /**
     * 上传图片（通用）
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 下载资源（递增下载次数并返回资源URL）
     * @param id 资源 ID
     * @return 资源下载URL
     */
    Result<String> downloadResource(Long id);

    /**
     * 更新资源信息
     * @param id 资源 ID
     * @param userId 用户 ID
     * @param resourceDTO 资源更新信息
     * @return 操作结果
     */
    Result<String> updateResource(Long id, Long userId, ResourceDTO resourceDTO);
}
