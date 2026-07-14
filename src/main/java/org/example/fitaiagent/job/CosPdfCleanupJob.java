package org.example.fitaiagent.job;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.constant.FileConstant;
import org.example.fitaiagent.manager.CosManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 定期清理 COS 上超过保留期的 PDF
 */
@Slf4j
@Component
public class CosPdfCleanupJob {

    @Resource
    private CosManager cosManager;

    /**
     * 每天凌晨 3 点清理超过 7 天的 PDF
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredPdfs() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -FileConstant.PDF_RETENTION_DAYS);
        Date cutoff = calendar.getTime();
        try {
            List<String> expiredKeys = cosManager.listExpiredObjectKeys(FileConstant.COS_PDF_PREFIX, cutoff);
            if (expiredKeys.isEmpty()) {
                log.info("COS PDF 清理：无过期对象");
                return;
            }
            int success = 0;
            for (String key : expiredKeys) {
                try {
                    cosManager.deleteObject(key);
                    success++;
                } catch (Exception e) {
                    log.warn("删除过期 PDF 失败: {}", key, e);
                }
            }
            log.info("COS PDF 清理完成：扫描 {} 个，成功删除 {}", expiredKeys.size(), success);
        } catch (Exception e) {
            log.error("COS PDF 清理任务失败", e);
        }
    }
}
