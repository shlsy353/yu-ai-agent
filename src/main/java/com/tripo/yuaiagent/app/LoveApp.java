package com.tripo.yuaiagent.app;

import com.tripo.yuaiagent.advisor.*;
import com.tripo.yuaiagent.rag.*;
import jakarta.annotation.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.client.advisor.vectorstore.*;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.tool.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一位专业恋爱情感顾问，" +
            "精通情感沟通、恋爱心理与聊天话术，擅长共情倾听、高情商回应，" +
            "能自然引导聊天氛围，" +
            "给出贴心、走心、贴合情侣相处模式的回复。";

    /**
     * 构造函数
     *
     * @param dashscopeChatModel myLoggerAdvisor
     */
    public LoveApp(ChatModel dashscopeChatModel, MyLoggerAdvisor myLoggerAdvisor) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // 1. 你的自定义日志拦截器
                        myLoggerAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build()  // 使用 Builder 模式
                )
                .build();
    }

    /**
     * 聊天
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        // 绑定会话ID，实现多用户隔离
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                        // 动态指定本次请求读取最近10条历史
                        .param("chat_memory_retrieve_size", 10)
                )
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * AI 恋爱报告功能
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "你必须严格按照要求生成恋爱报告！\n" +
                        "只返回 JSON 格式数据，绝对不能有聊天内容！\n" +
                        "不要解释，不要打招呼，不要多余文字！\n" +
                        "标题格式：xxx的恋爱报告，xxx是用户的名字！\n" +
                        "返回格式：{\"title\":\"标题\",\"suggestions\":[\"建议1\",\"建议2\"]}")
                .user(message)
                .advisors(spec -> spec
                        // 绑定会话ID，实现多用户隔离
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                        // 动态指定本次请求读取最近10条历史
                        .param("chat_memory_retrieve_size", 10)
                )
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    // AI 恋爱知识库问答
    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    @Resource
    private VectorStore pgVectorVectorStore;
    @Resource
    private QueryRewriter queryRewriter;


    /**
     * AI 恋爱知识库问答功能---RAG
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewritenMessage = queryRewriter.doQueryRewrite(message);


        String content = chatClient
                .prompt()
                .user(rewritenMessage)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                        .param("chat_memory_retrieve_size", 10))

                //.advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                // 使用自定义的 RAG Advisor
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        log.info("rag content: {}", content);
        return content;
    }
    public String recommendPartner(String message) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .call()
                .content();
    }

    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                        .param("chat_memory_retrieve_size", 10))

                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}

