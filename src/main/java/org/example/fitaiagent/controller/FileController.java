package org.example.fitaiagent.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitaiagent.common.BaseResponse;
import org.example.fitaiagent.common.ResultUtils;
import org.example.fitaiagent.constant.FileConstant;
import org.example.fitaiagent.exception.ErrorCode;
import org.example.fitaiagent.exception.ThrowUtils;
import org.example.fitaiagent.manager.CosManager;
import org.example.fitaiagent.model.vo.PdfDownloadVO;
import org.example.fitaiagent.utils.RequestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 文件下载（需登录）
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final long PRESIGN_MINUTES = 10L;

    @Resource
    private CosManager cosManager;

    /**
     * 获取 PDF 短时下载链接（需登录）
     */
    @GetMapping("/pdf/url")
    public BaseResponse<PdfDownloadVO> getPdfDownloadUrl(@RequestParam String downloadKey,
                                                         HttpServletRequest request) {
        // 触发登录校验（拦截器已校验；显式取用户保证接口语义清晰）
        RequestUtils.getLoginUser(request);
        String key = normalizeAndValidatePdfKey(downloadKey);
        String fileName = extractDisplayName(key);
        String url = cosManager.generatePresignedUrl(key, PRESIGN_MINUTES, TimeUnit.MINUTES, fileName);
        ThrowUtils.throwIf(!StringUtils.hasText(url), ErrorCode.SYSTEM_ERROR, "生成下载链接失败");
        return ResultUtils.success(PdfDownloadVO.builder()
                .url(url)
                .fileName(fileName)
                .expiresInSeconds(TimeUnit.MINUTES.toSeconds(PRESIGN_MINUTES))
                .build());
    }

    private String normalizeAndValidatePdfKey(String downloadKey) {
        ThrowUtils.throwIf(!StringUtils.hasText(downloadKey), ErrorCode.PARAMS_ERROR, "downloadKey 不能为空");
        String key = downloadKey.trim();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        ThrowUtils.throwIf(key.contains("..") || key.contains("\\"),
                ErrorCode.PARAMS_ERROR, "非法的文件标识");
        ThrowUtils.throwIf(!key.startsWith(FileConstant.COS_PDF_PREFIX),
                ErrorCode.PARAMS_ERROR, "仅支持下载计划 PDF");
        ThrowUtils.throwIf(!key.toLowerCase().endsWith(".pdf"),
                ErrorCode.PARAMS_ERROR, "仅支持 PDF 文件");
        return key;
    }

    private String extractDisplayName(String key) {
        String name = key.substring(key.lastIndexOf('/') + 1);
        int underscore = name.indexOf('_');
        if (underscore > 0 && underscore < name.length() - 1) {
            // uuid_原名.pdf → 原名.pdf
            return name.substring(underscore + 1);
        }
        return name;
    }
}
