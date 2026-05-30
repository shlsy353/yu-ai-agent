package com.tripo.yuaiagent.rag;

import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.rag.*;
import org.springframework.ai.rag.preretrieval.query.transformation.*;
import org.springframework.stereotype.*;

/**
 * 恋爱大师查询重写器
 * @author yuai
 * @date 2023/10/26
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;
    /**
     * 构造函数
     * @param
     */
    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 获取重写后的查询
        return transformedQuery.text();
    }
}







