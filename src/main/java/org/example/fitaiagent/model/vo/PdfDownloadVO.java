package org.example.fitaiagent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * PDF 下载信息（短时预签名 URL）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfDownloadVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * COS 预签名下载地址
     */
    private String url;

    /**
     * 建议保存的文件名
     */
    private String fileName;

    /**
     * 链接有效秒数
     */
    private Long expiresInSeconds;
}
