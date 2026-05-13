package com.tripo.yuaiagent.rag;

import jakarta.annotation.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppDocumentLoaderTest {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void loadMarkdowns() {
         loveAppDocumentLoader.loadMarkdowns();

    }

}