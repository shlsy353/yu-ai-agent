package com.tripo.yuaiagent.tools;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TerminalOperationToolTest {

    @Test
    public void testExecuteTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "ls-l";
        String result = tool.executeTerminalCommand(command);
        assertNotNull(result);
    }
}