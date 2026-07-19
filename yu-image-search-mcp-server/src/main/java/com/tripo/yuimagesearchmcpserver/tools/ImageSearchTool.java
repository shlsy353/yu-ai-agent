package com.tripo.yuimagesearchmcpserver.tools;

import cn.hutool.core.util.*;
import cn.hutool.http.*;
import cn.hutool.json.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.*;

@Service
public class ImageSearchTool {


    private static final String API_KEY_ENV = "PEXELS_API_KEY";


    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "Search images from web, returns markdown formatted image links")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            List<String> urls = searchMediumImages(query);
            if (urls.isEmpty()) {
                return "No images found for query: " + query;
            }
            StringBuilder result = new StringBuilder();
            result.append("Found ").append(urls.size()).append(" images:\n\n");
            for (int i = 0; i < urls.size(); i++) {
                result.append("![").append(query).append("-").append(i + 1).append("](")
                      .append(urls.get(i)).append(")\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }


    public List<String> searchMediumImages(String query) {

        Map<String, String> headers = new HashMap<>();
        String apiKey = System.getenv(API_KEY_ENV);
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalStateException("Missing " + API_KEY_ENV + " environment variable");
        }
        headers.put("Authorization", apiKey);


        Map<String, Object> params = new HashMap<>();
        params.put("query", query);


        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();


        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .limit(5)
                .collect(Collectors.toList());
    }
}

