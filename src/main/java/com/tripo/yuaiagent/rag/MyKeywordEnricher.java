package com.tripo.yuaiagent.rag;

import jakarta.annotation.*;
import java.util.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.document.*;
import org.springframework.ai.model.transformer.*;
import org.springframework.stereotype.*;

@Component
class MyKeywordEnricher {
    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 为文档添加关键词
     * @param documents
     * @return
     */
    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.dashscopeChatModel, 5);
        return enricher.apply(documents);
    }
}


