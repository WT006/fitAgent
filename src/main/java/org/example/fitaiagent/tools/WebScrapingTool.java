package org.example.fitaiagent.tools;

import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 抓取网页正文（返回精简纯文本，避免把整页 HTML 塞进上下文）
 */
public class WebScrapingTool {

    private static final int MAX_TEXT_LENGTH = 3000;

    @Tool(description = "Scrape main text content of a web page. Pass a full http/https URL.")
    public String scrapeWebPage(@ToolParam(description = "Full URL of the web page, e.g. https://example.com/article") String url) {
        if (StrUtil.isBlank(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return "Error scraping web page: url must start with http:// or https://";
        }
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; FitAiAgent/1.0)")
                    .timeout(15000)
                    .get();
            doc.select("script, style, noscript, iframe, svg").remove();
            String title = StrUtil.blankToDefault(doc.title(), "");
            String text = doc.body() != null ? doc.body().text() : doc.text();
            text = text.replaceAll("\\s+", " ").trim();
            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH) + "…(已截断)";
            }
            if (StrUtil.isBlank(text)) {
                return "Error scraping web page: empty page content";
            }
            return "title: " + title + "\ncontent: " + text;
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
