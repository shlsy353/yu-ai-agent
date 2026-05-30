package com.tripo.yuaiagent.rag;


import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.rag.advisor.*;
import org.springframework.ai.rag.retrieval.search.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.ai.vectorstore.filter.*;

/**
 * 自定义 RAG 检索增强顾问的工厂
 */
public class LoveAppRagCustomAdvisorFactory {

    /**
     * 创建 RAG 检索增强顾问
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor createAdvisor(VectorStore vectorStore,String status) {
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.5)
                .topK(3)
                .build();


        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
              //  .queryAugmenter()
                .build();
    }
}
