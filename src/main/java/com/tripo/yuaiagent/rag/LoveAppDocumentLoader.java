package com.tripo.yuaiagent.rag;

import java.io.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.springframework.ai.document.*;
import org.springframework.ai.reader.markdown.*;
import org.springframework.ai.reader.markdown.config.*;
import org.springframework.core.io.*;
import org.springframework.core.io.support.*;
import org.springframework.stereotype.*;

/**
 * 恋爱大师应用文档加载器
 * @author yuai
 * @date 2023/10/26
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载文档
     * @return
     */
    public List<Document> loadMarkdowns(){
        List<Document> documents = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for(Resource resource: resources){
                String filename = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource,config);
                documents.addAll(markdownDocumentReader.get());
            }

        } catch (IOException e) {
            log.error("加载文档失败：{}", e);
        }
        return documents;
    }
}
