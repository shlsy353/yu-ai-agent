package com.tripo.yuaiagent.tools;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


class FileOperationToolTest {

    @Test
    void readFile() {
        // 创建 FileOperationTool 对象
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test.txt";
        String result = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull( result);

    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test.txt";
        String content = "This is a test.";
        String result = fileOperationTool.writeFile(fileName,content);
        assertNotNull(result);
    }
}