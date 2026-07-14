package org.example.fitaiagent.agent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 将工具原始结果压缩为适合前端展示的短文本。
 * 完整结果仍可在 Agent 上下文中使用（超长时会在 ToolCallAgent 中压缩）。
 */
public final class ToolResultSummarizer {

    private static final int MAX_DISPLAY_LENGTH = 180;

    private ToolResultSummarizer() {
    }

    public static String forClient(String toolName, String responseData) {
        if (StrUtil.isBlank(responseData)) {
            return "无返回内容";
        }
        if (responseData.contains("downloadKey=")) {
            return responseData;
        }
        if (responseData.startsWith("Error") || responseData.startsWith("error")) {
            return truncate(responseData, MAX_DISPLAY_LENGTH);
        }

        String name = StrUtil.blankToDefault(toolName, "").toLowerCase();
        if (name.contains("search")) {
            return summarizeSearch(responseData);
        }
        if (name.contains("map") || name.contains("geo")) {
            return summarizeGeo(responseData);
        }
        if (JSONUtil.isTypeJSON(responseData) || looksLikeJsonObjectSequence(responseData)) {
            return summarizeGenericJson(responseData);
        }
        return truncate(responseData.replaceAll("\\s+", " ").trim(), MAX_DISPLAY_LENGTH);
    }

    private static String summarizeSearch(String data) {
        try {
            JSONArray array = parseToArray(data);
            if (array != null && !array.isEmpty()) {
                StringBuilder titles = new StringBuilder();
                int shown = 0;
                for (int i = 0; i < array.size() && shown < 3; i++) {
                    Object item = array.get(i);
                    if (!(item instanceof JSONObject obj)) {
                        continue;
                    }
                    String title = extractTitle(obj);
                    if (StrUtil.isBlank(title)) {
                        continue;
                    }
                    if (shown > 0) {
                        titles.append("；");
                    }
                    titles.append(title);
                    shown++;
                }
                if (shown == 0) {
                    return "网络搜索完成，共 " + array.size() + " 条结果";
                }
                StringBuilder sb = new StringBuilder("找到 ")
                        .append(array.size())
                        .append(" 条结果：")
                        .append(titles);
                if (array.size() > shown) {
                    sb.append(" 等");
                }
                return truncate(sb.toString(), MAX_DISPLAY_LENGTH);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "网络搜索完成（结果已供智能体分析）";
    }

    private static String extractTitle(JSONObject obj) {
        String title = firstNonBlank(
                obj.getStr("title"),
                obj.getStr("name"),
                obj.getStr("displayed_link"),
                obj.getStr("link")
        );
        return title;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String summarizeGeo(String data) {
        try {
            String normalized = data;
            if (normalized.startsWith("\"") && normalized.endsWith("\"")) {
                normalized = JSONUtil.toBean(normalized, String.class);
            }
            if (JSONUtil.isTypeJSONObject(normalized)) {
                JSONObject root = JSONUtil.parseObj(normalized);
                JSONArray geocodes = root.getJSONArray("geocodes");
                if (geocodes == null && root.containsKey("data")) {
                    Object nested = root.get("data");
                    if (nested instanceof String nestedStr && JSONUtil.isTypeJSON(nestedStr)) {
                        geocodes = JSONUtil.parseObj(nestedStr).getJSONArray("geocodes");
                    } else if (nested instanceof JSONObject nestedObj) {
                        geocodes = nestedObj.getJSONArray("geocodes");
                    }
                }
                if (geocodes != null && !geocodes.isEmpty()) {
                    StringBuilder sb = new StringBuilder("地理编码成功，共 ").append(geocodes.size()).append(" 处：");
                    int shown = 0;
                    for (int i = 0; i < geocodes.size() && shown < 2; i++) {
                        JSONObject item = geocodes.getJSONObject(i);
                        String label = firstNonBlank(
                                item.getStr("formatted_address"),
                                item.getStr("district"),
                                item.getStr("city"),
                                item.getStr("province")
                        );
                        if (StrUtil.isBlank(label)) {
                            continue;
                        }
                        if (shown > 0) {
                            sb.append("；");
                        }
                        sb.append(label);
                        shown++;
                    }
                    if (shown == 0) {
                        return "地理编码成功，共 " + geocodes.size() + " 处";
                    }
                    return truncate(sb.toString(), MAX_DISPLAY_LENGTH);
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "地理信息查询完成（详情已供智能体分析）";
    }

    private static String summarizeGenericJson(String data) {
        try {
            JSONArray array = parseToArray(data);
            if (array != null) {
                return "返回 " + array.size() + " 条结构化数据（详情已供智能体分析）";
            }
            if (JSONUtil.isTypeJSONObject(data)) {
                return "结构化数据已返回（详情已供智能体分析）";
            }
        } catch (Exception ignored) {
            // fall through
        }
        return truncate(data.replaceAll("\\s+", " ").trim(), MAX_DISPLAY_LENGTH);
    }

    private static JSONArray parseToArray(String data) {
        if (JSONUtil.isTypeJSONArray(data)) {
            return JSONUtil.parseArray(data);
        }
        if (looksLikeJsonObjectSequence(data)) {
            return JSONUtil.parseArray("[" + data + "]");
        }
        return null;
    }

    private static boolean looksLikeJsonObjectSequence(String data) {
        String trimmed = data.trim();
        return trimmed.startsWith("{") && trimmed.contains("},{");
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "…";
    }
}
