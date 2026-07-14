package org.example.fitaiagent.constant;

public interface FileConstant {
    /**
     * 本地临时文件目录
     */
    String FILE_SAVE_DIR = System.getProperty("user.dir") + "/tmp";

    /**
     * COS 上 PDF 对象键前缀
     */
    String COS_PDF_PREFIX = "fit-ai-agent/pdf/";

    /**
     * PDF 保留天数
     */
    int PDF_RETENTION_DAYS = 7;
}
