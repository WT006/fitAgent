package org.example.fitaiagent.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ResponseHeaderOverrides;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.config.CosClientConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * COS 对象存储管理器
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回访问 URL（若桶为公有读可用；私有桶请用预签名）
     */
    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url = buildPublicUrl(key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        }
        log.error("文件上传COS失败，返回结果为空");
        return null;
    }

    /**
     * 生成带时效的下载预签名 URL
     */
    public String generatePresignedUrl(String key, long duration, TimeUnit unit, String downloadFileName) {
        Date expiration = new Date(System.currentTimeMillis() + unit.toMillis(duration));
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                cosClientConfig.getBucket(), key, HttpMethodName.GET);
        request.setExpiration(expiration);
        if (StringUtils.hasText(downloadFileName)) {
            ResponseHeaderOverrides headers = new ResponseHeaderOverrides();
            String encoded = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8).replace("+", "%20");
            headers.setContentDisposition("attachment; filename*=UTF-8''" + encoded);
            request.setResponseHeaders(headers);
        }
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 删除对象
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
        log.info("已删除 COS 对象: {}", key);
    }

    /**
     * 列出指定前缀下 lastModified 早于 cutoff 的对象键
     */
    public List<String> listExpiredObjectKeys(String prefix, Date cutoff) {
        List<String> expiredKeys = new ArrayList<>();
        String marker = null;
        ObjectListing listing;
        do {
            ListObjectsRequest listReq = new ListObjectsRequest();
            listReq.setBucketName(cosClientConfig.getBucket());
            listReq.setPrefix(prefix);
            listReq.setMarker(marker);
            listing = cosClient.listObjects(listReq);
            for (COSObjectSummary summary : listing.getObjectSummaries()) {
                if (summary.getLastModified() != null && summary.getLastModified().before(cutoff)) {
                    expiredKeys.add(summary.getKey());
                }
            }
            marker = listing.getNextMarker();
        } while (listing.isTruncated());
        return expiredKeys;
    }

    public String buildPublicUrl(String key) {
        String host = cosClientConfig.getHost();
        if (!StringUtils.hasText(host)) {
            return key;
        }
        String normalizedHost = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        String normalizedKey = key.startsWith("/") ? key.substring(1) : key;
        return normalizedHost + "/" + normalizedKey;
    }
}
