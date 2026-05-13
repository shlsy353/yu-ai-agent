package com.tripo.yuaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 描述: 通过Http调用阿里云百炼接口
 *
 * @author yuaiAgent
 * @create 2023-05-09 10:05
 */
public class HttpAiInvoke {

    public static void main(String[] args) {
        // 你的 API Key
        String apiKey = "sk-679c5650ca48440c8dc6b73b77c99ef8";

        // 请求地址（阿里云百炼统一接口）
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 构建请求体（和你Python代码完全一致）
        JSONObject body = JSONUtil.createObj();
        // 模型
        body.set("model", "deepseek-v4-pro");

        // 消息
        JSONObject message = JSONUtil.createObj();
        message.set("role", "user");
        message.set("content", "9.9和9.11哪个大");
        body.set("messages", new JSONObject[]{message});

        // 思考参数
        body.set("reasoning_effort", "high");
        body.set("result_format", "message");

        // 发送 POST 请求（Hutool）
        String result = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .execute()
                .body();

        // 输出结果
        System.out.println(result);
    }
}
