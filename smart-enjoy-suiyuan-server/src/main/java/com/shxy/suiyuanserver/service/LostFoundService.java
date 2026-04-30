package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.LostFoundDTO;
import com.shxy.suiyuanentity.entity.LostFound;
import com.shxy.suiyuanentity.vo.LostFoundVO;

import java.util.List;
import java.util.Map;

/**
* @author Wu, Hui Ming
* @description 针对表【lost_found】的数据库操作Service
* @createDate 2026-04-04 21:30:08
*/
public interface LostFoundService extends IService<LostFound> {

    Result<LostFound> createLostFound(LostFoundDTO lostFoundDTO);

    Result<PageResult> listLostFound(Integer page, Integer pageSize, Integer type, Integer status, Integer urgent);

    Result<LostFoundVO> detailLostFound(Long id);

    Result<String> deleteLostFound(Long id);

    Result<String> updateLostFound(LostFoundDTO lostFoundDTO);

    Result<String> updateLostFoundStatus(Long id, Integer status);

    /**
     * 获取用户发布的失物招领列表
     * @param userId 用户ID
     * @return 失物招领列表
     */
    Result<List<LostFoundVO>> getUserPublishedLostFound(Long userId);

    /**
     * 获取所有未解决的失物招领记录（用于同步到向量库）
     * @return 失物招领列表
     */
    Result<List<LostFoundVO>> getAllForSync();
}
