package com.tripo.yuaiagent.tools;

import cn.hutool.http.*;
import cn.hutool.json.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.ai.tool.annotation.*;

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

            // try multiple field names that different engines might use
            JSONArray results = jsonObject.getJSONArray("organic_results");
            if (results == null) results = jsonObject.getJSONArray("results");
            if (results == null) results = jsonObject.getJSONArray("organic");
            if (results == null) results = jsonObject.getJSONArray("items");
            if (results == null) {
                // log the full response for debugging and return it
                return "搜索失败：API返回格式异常，响应内容: " + response;
            }

            int limit = Math.min(results.size(), 5);
            List<Object> objects = results.subList(0, limit);

            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}