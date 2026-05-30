package com.tripo.yuaiagent.rag;

import jakarta.annotation.*;
import java.util.*;
import org.springframework.ai.document.*;
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

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscpeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscpeEmbeddingModel).build();
        // 加载文档
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();
        // 分割文档
        // List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        // 自动补充关键字（学习阶段跳过，省 API 费用）
            List<Document> enrichedDocuments = documentList;
            simpleVectorStore.add(enrichedDocuments);
            return simpleVectorStore;

    }

}
