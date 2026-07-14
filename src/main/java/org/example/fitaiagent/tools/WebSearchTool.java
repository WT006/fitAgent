package org.example.fitaiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索工具类
 */
public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "未找到相关搜索结果";
            }
            int limit = Math.min(5, organicResults.size());
            List<String> lines = new ArrayList<>(limit);
            for (int i = 0; i < limit; i++) {
                JSONObject item = organicResults.getJSONObject(i);
                String title = StrUtil.blankToDefault(item.getStr("title"), "");
                String link = StrUtil.blankToDefault(item.getStr("link"), "");
                String snippet = StrUtil.blankToDefault(item.getStr("snippet"), "");
                if (StrUtil.isBlank(title) && StrUtil.isBlank(link)) {
                    continue;
                }
                lines.add(String.format("%d. %s | %s | %s", i + 1, title, link, snippet));
            }
            if (lines.isEmpty()) {
                return "未找到有效搜索结果";
            }
            return String.join("\n", lines);
        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}
