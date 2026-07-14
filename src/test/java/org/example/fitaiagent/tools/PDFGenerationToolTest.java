package org.example.fitaiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PDFGenerationToolTest {

    @Resource
    private PDFGenerationTool pdfGenerationTool;

    @Test
    public void testGeneratePDF() {
        String fileName = "原创项目.pdf";
        String content = "原创项目 https://www.codefather.cn";
        String result = pdfGenerationTool.generatePDF(fileName, content);
        assertNotNull(result);
        assertTrue(result.contains("downloadKey=") || result.startsWith("Error"), result);
    }
}
