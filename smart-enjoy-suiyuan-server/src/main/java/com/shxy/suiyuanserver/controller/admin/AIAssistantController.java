package com.shxy.suiyuanserver.controller.admin;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.AiChatDTO;
import com.shxy.suiyuanentity.dto.KbDocumentDTO;
import com.shxy.suiyuanentity.dto.KbInfoCreateDTO;
import com.shxy.suiyuanentity.dto.KbInfoUpdateDTO;
import com.shxy.suiyuanentity.vo.AiStatsVO;
import com.shxy.suiyuanentity.vo.KbDocumentVO;
import com.shxy.suiyuanentity.vo.KbInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("adminAIAssistantController")
@RequestMapping("/admin/ai")
@Tag(name = "管理端AI模块")
public class AIAssistantController {

    @GetMapping("/kb/list")
    @Operation(summary = "知识库列表", description = "获取所有知识库列表")
    public Result<List<KbInfoVO>> getKbList(
            @RequestParam(required = false) String kbCategory,
            @RequestParam(required = false) Integer status) {
        return Result.success("知识库列表功能待完善");
    }

    @PostMapping("/kb/create")
    @Operation(summary = "创建知识库", description = "创建一个新的知识库")
    public Result<String> createKb(@RequestBody KbInfoCreateDTO createDTO) {
        return Result.success("创建知识库功能待完善");
    }

    @GetMapping("/kb/{id}")
    @Operation(summary = "获取知识库详情", description = "获取指定知识库的详细信息")
    public Result<KbInfoVO> getKbDetail(@PathVariable Long id) {
        return Result.success("知识库详情功能待完善");
    }

    @PutMapping("/kb/{id}")
    @Operation(summary = "更新知识库", description = "更新知识库信息")
    public Result<String> updateKb(@PathVariable Long id, @RequestBody KbInfoUpdateDTO updateDTO) {
        return Result.success("更新知识库功能待完善");
    }

    @DeleteMapping("/kb/{id}")
    @Operation(summary = "删除知识库", description = "删除指定的知识库及关联文档")
    public Result<String> deleteKb(@PathVariable Long id) {
        return Result.success("删除知识库功能待完善");
    }

    @GetMapping("/kb/{kbId}/doc/list")
    @Operation(summary = "知识库文档列表", description = "获取指定知识库下的所有文档")
    public Result<List<KbDocumentVO>> getDocList(
            @PathVariable Long kbId,
            @RequestParam(required = false) Integer vectorStatus) {
        return Result.success("文档列表功能待完善");
    }

    @PostMapping("/kb/{kbId}/doc/upload")
    @Operation(summary = "上传文档", description = "上传文档到指定知识库")
    public Result<String> uploadDoc(
            @PathVariable Long kbId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docTitle", required = false) String docTitle) {
        return Result.success("上传文档功能待完善");
    }

    @GetMapping("/kb/{kbId}/doc/{docId}")
    @Operation(summary = "获取文档详情", description = "获取指定文档的详细信息")
    public Result<KbDocumentVO> getDocDetail(@PathVariable Long kbId, @PathVariable Long docId) {
        return Result.success("文档详情功能待完善");
    }

    @DeleteMapping("/kb/{kbId}/doc/{docId}")
    @Operation(summary = "删除文档", description = "删除指定的文档")
    public Result<String> deleteDoc(@PathVariable Long kbId, @PathVariable Long docId) {
        return Result.success("删除文档功能待完善");
    }

    @PostMapping("/kb/{kbId}/doc/{docId}/vectorize")
    @Operation(summary = "触发向量化", description = "触发文档向量化处理")
    public Result<String> vectorizeDoc(@PathVariable Long kbId, @PathVariable Long docId) {
        return Result.success("向量化功能待完善");
    }

    @GetMapping("/session/list")
    @Operation(summary = "AI会话列表", description = "获取所有用户的AI会话列表")
    public Result<List<Object>> getSessionList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.success("会话列表功能待完善");
    }

    @DeleteMapping("/session/{id}")
    @Operation(summary = "删除会话", description = "删除指定用户的AI会话")
    public Result<String> deleteSession(@PathVariable Long id) {
        return Result.success("删除会话功能待完善");
    }

    @GetMapping("/stats")
    @Operation(summary = "AI使用统计", description = "获取全局AI使用统计数据")
    public Result<AiStatsVO> getStats() {
        return Result.success("统计数据功能待完善");
    }

    @PostMapping("/chat")
    @Operation(summary = "RAG问答", description = "基于知识库进行RAG增强问答")
    public Result<String> ragChat(@RequestBody AiChatDTO aiChatDTO) {
        return Result.success("RAG问答功能待完善");
    }
}
