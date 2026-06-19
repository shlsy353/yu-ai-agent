package com.tripo.yuaiagent.agent.model;


import com.itextpdf.styledxmlparser.jsoup.internal.*;
import cn.hutool.core.util.*;
import java.util.*;
import java.util.concurrent.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

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

    /**
     * 执行代理逻辑
     *
     * @param userPrompt 用户输入
     * @return 代理结果
     */

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

    /**
     * 流式执行代理逻辑
     *
     * @param userPrompt 用户输入
     * @return 代理结果
     */
    public SseEmitter runStream(String userPrompt) {

        SseEmitter emitter = new SseEmitter(300000L);// 设置超时时间为5分钟

        // 线程异步处理
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                if (StringUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }


                state = AgentState.RUNNING;

                messageList.add(new UserMessage(userPrompt));

                try {
                    String lastResult = "";
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step " + stepNumber + "/" + maxSteps);

                        // 发送进度提示，让用户知道正在处理中（不展示原始工具数据）
                        emitter.send("[STATUS]正在处理... (步骤 " + stepNumber + "/" + maxSteps + ")");

                        String stepResult = step();
                        lastResult = stepResult;
                    }

                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        if (StrUtil.isBlank(lastResult)) {
                            lastResult = "执行结束: 达到最大步骤 (" + maxSteps + ")";
                        }
                    }

                    // 发送最终结果
                    emitter.send("[RESULT]" + lastResult);
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {

                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });


        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }


    // 单个步骤
    public abstract String step();


    protected void cleanup() {

    }
}