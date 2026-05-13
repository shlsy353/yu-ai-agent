package com.tripo.yuaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.*;
import com.alibaba.cloud.ai.dashscope.rag.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.rag.advisor.*;
import org.springframework.ai.rag.retrieval.search.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;


/**
 * 恋爱大师 rag 云Advisor---基于阿里云
 */
@Configuration
public class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    @Bean
    public Advisor loveAppRagCloudAdvisor(){
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .build();
        final String KNOWLEDGE_INDEX = "恋爱大师";
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }
}
