package com.tripo.yuaiagent.rag;

import java.util.*;
import org.springframework.ai.document.*;
import org.springframework.ai.transformer.splitter.*;
import org.springframework.stereotype.*;

/**
 * 自定义分词器
 * @author yuai
 * @date 2023/10/26
 */
@Component
class MyTokenTextSplitter {
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(200, 100, 10, 5000, true);
        return splitter.apply(documents);
    }
}