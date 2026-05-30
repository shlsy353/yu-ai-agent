package com.tripo.yuaiagent.rag;


import jakarta.annotation.*;
import org.springframework.ai.embedding.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.ai.vectorstore.pgvector.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.*;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.*;

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
        @Bean
        public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
            return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                    .dimensions(1024)                    // Optional: defaults to model dimensions or 1536
                    .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                    .indexType(HNSW)                     // Optional: defaults to HNSW
                    .initializeSchema(true)              // Optional: defaults to false
                    .schemaName("public")                // Optional: defaults to "public"
                    .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                    .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                    .build();
        }
    }

