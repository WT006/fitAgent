package org.example.fitaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.constant.FileConstant;
import org.example.fitaiagent.manager.CosManager;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF 生成工具：本地生成后上传 COS，供登录用户下载
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PDFGenerationTool {

    private final CosManager cosManager;

    @Tool(description = "生成 PDF 文件并上传云存储。面向中文用户：文件名与正文 content 必须使用简体中文撰写（如锻炼计划、健康建议），禁止整篇英文。成功后返回 downloadKey 供用户下载。")
    public String generatePDF(
            @ToolParam(description = "PDF 文件名，使用中文，并以 .pdf 结尾，例如：太原居民健康锻炼计划.pdf") String fileName,
            @ToolParam(description = "写入 PDF 的正文内容，必须为简体中文，可含标题、分段、条目；不要用英文写全文") String content) {
        String displayName = sanitizeDisplayName(fileName);
        // 默认中文文件名，避免模型传 English 名
        if (isMostlyAsciiName(displayName)) {
            displayName = "健康锻炼计划.pdf";
        }
        String storageName = IdUtil.simpleUUID() + "_" + displayName;
        String localDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String localPath = localDir + "/" + storageName;
        File localFile = new File(localPath);

        try {
            FileUtil.mkdir(localDir);
            try (PdfWriter writer = new PdfWriter(localPath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                PdfFont font = createChineseFont();
                document.setFont(font);
                // 按换行拆段，便于阅读
                for (String line : content.split("\\R")) {
                    document.add(new Paragraph(StrUtil.blankToDefault(line, " ")));
                }
            }

            String cosKey = FileConstant.COS_PDF_PREFIX + storageName;
            String uploaded = cosManager.uploadFile(cosKey, localFile);
            if (StrUtil.isBlank(uploaded)) {
                return "Error generating PDF: upload to COS failed";
            }

            FileUtil.del(localFile);
            return "PDF generated successfully. downloadKey=" + cosKey + " displayName=" + displayName;
        } catch (Exception e) {
            log.error("生成 PDF 失败", e);
            return "Error generating PDF: " + e.getMessage();
        } finally {
            if (localFile.exists()) {
                FileUtil.del(localFile);
            }
        }
    }

    /**
     * 优先使用 font-asian 内置字体，失败则回退系统中文字体
     */
    private PdfFont createChineseFont() throws IOException {
        try {
            return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
        } catch (Exception e) {
            log.warn("内置中文字体不可用，尝试系统字体: {}", e.getMessage());
        }
        String[] candidates = {
                "C:/Windows/Fonts/simsun.ttc,0",
                "C:/Windows/Fonts/msyh.ttc,0",
                "C:/Windows/Fonts/simhei.ttf",
                "/System/Library/Fonts/PingFang.ttc,0",
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"
        };
        for (String candidate : candidates) {
            String path = candidate.contains(",") ? candidate.substring(0, candidate.indexOf(',')) : candidate;
            if (!Files.exists(Path.of(path))) {
                continue;
            }
            try {
                return PdfFontFactory.createFont(candidate, PdfEncodings.IDENTITY_H);
            } catch (Exception ex) {
                log.warn("加载字体失败 {}: {}", candidate, ex.getMessage());
            }
        }
        // 最后回退默认字体（中文可能显示为方框，但至少不中断）
        return PdfFontFactory.createFont();
    }

    private boolean isMostlyAsciiName(String name) {
        if (StrUtil.isBlank(name)) {
            return true;
        }
        String base = name.toLowerCase().endsWith(".pdf")
                ? name.substring(0, name.length() - 4)
                : name;
        return base.chars().allMatch(c -> c < 128);
    }

    private String sanitizeDisplayName(String fileName) {
        String name = StrUtil.blankToDefault(fileName, "plan.pdf").trim();
        name = name.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        if (!name.toLowerCase().endsWith(".pdf")) {
            name = name + ".pdf";
        }
        if (name.length() > 80) {
            String ext = ".pdf";
            name = name.substring(0, 80 - ext.length()) + ext;
        }
        return name;
    }
}
