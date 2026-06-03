package com.tripo.yuaiagent.tools;

import org.springframework.ai.tool.annotation.*;

/**
 * 终止工具 zuoyong:让自主规划智能体能够中断
 */
public class TerminateTool {

    @Tool(description = "description = \"\"\"  \n" +
            "            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  \n" +
            "            \"When you have finished all the tasks, call this tool to end the work.  \n" +
            "            \"\"\"")
    public String doTerminate() {
        return "Terminating...";
    }
}
