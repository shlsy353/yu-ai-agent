package com.tripo.yuaiagent.rag;

import jakarta.annotation.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.ai.document.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.boot.test.context.*;

@SpringBootTest
public class PgVectorVectorStoreConfigTest {

    @Resource(name = "vectorStore")
    VectorStore pgVectorVectorStore;

    @Test
    void test() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

        pgVectorVectorStore.add(documents);

        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        Assertions.assertNotNull(results);

    }
}