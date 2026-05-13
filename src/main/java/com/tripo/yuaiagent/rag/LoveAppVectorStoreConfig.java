package com.tripo.yuaiagent.rag;

import jakarta.annotation.*;
import org.springframework.ai.embedding.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.context.annotation.*;


/**
 * 恋爱大师配置向量数据库
 */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        simpleVectorStore.add(loveAppDocumentLoader.loadMarkdowns());
        return simpleVectorStore;

    }

}
