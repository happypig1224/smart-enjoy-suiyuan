package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ReportDTO;
import com.shxy.suiyuanentity.entity.Report;

public interface ReportService extends IService<Report> {

    Result<Report> submitReport(ReportDTO reportDTO);
}
