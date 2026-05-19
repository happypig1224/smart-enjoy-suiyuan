package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ReportDTO;
import com.shxy.suiyuanentity.entity.Report;
import com.shxy.suiyuanserver.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * 举报接口
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/5/19 16:58
 */
@RestController
@RequestMapping("/user/report")
@Tag(name = "举报模块")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/submit")
    @RequireLogin
    @Operation(summary = "提交举报", description = "用户举报违规内容（帖子/评论/用户）")
    public Result<Report> submitReport(@Valid @RequestBody ReportDTO reportDTO) {
        return reportService.submitReport(reportDTO);
    }
}
