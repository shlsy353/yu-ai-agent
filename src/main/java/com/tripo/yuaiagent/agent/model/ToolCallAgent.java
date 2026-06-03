package com.tripo.yuaiagent.agent.model;

import cn.hutool.core.collection.*;
import cn.hutool.core.util.*;
import com.alibaba.cloud.ai.dashscope.chat.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.model.tool.*;
import org.springframework.ai.tool.*;

/**
 * 工具调用代理类
 *
 * 继承自ReActAgent，实现了基于工具调用的代理逻辑
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 工具调用
    private final ToolCallback[] availableTools;

    // 保存工具调用信息
    private ChatResponse toolCallChatResponse;

    // 禁用 SpringAI 内置工具调用机制，使用自定义的
    private final ChatOptions chatOptions;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;



    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();

        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled (false)
                .build();
    }
    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 执行结果
     */

    @Override
    public boolean think() {
        // 1.校验提示词,拼接用户提示词
        if(StrUtil.isNotBlank(getNextStepPrompt())){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        // 2.调用AI LLM，获取工具调用结果
        List< Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList,this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下Act
            this.toolCallChatResponse = chatResponse;
            // 3.获取工具调用结果
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName()+"的思考:"+result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 如果不需要调用工具，返回false
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时添加助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }

        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        setMessageList(toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;
    }

}
