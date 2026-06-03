package com.tripo.yuaiagent.agent.model;


import com.itextpdf.styledxmlparser.jsoup.internal.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.messages.*;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程
 *
 * 提供状态转换，内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现具体代理逻辑
 */
@Data
@Slf4j
public abstract class BaseAgent {


    private String name;


    private String systemPrompt;
    private String nextStepPrompt;


    private AgentState state = AgentState.IDLE;


    private int maxSteps = 10;
    private int currentStep = 0;


    private ChatClient chatClient;


    private List<Message> messageList = new ArrayList<>();


    public String run(String userPrompt) {
        // 1. 状态校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 2. 状态转换
        state = AgentState.RUNNING;

        messageList.add(new UserMessage(userPrompt));

        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);

                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }

            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {

            // 3.清理资源
            this.cleanup();
        }
    }

    // 单个步骤
    public abstract String step();


    protected void cleanup() {

    }
}