package com.tripo.yuaiagent.controller;

import com.tripo.yuaiagent.agent.model.*;
import com.tripo.yuaiagent.app.*;
import jakarta.annotation.*;
import java.io.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.tool.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.*;
import reactor.core.publisher.*;

@RestController
@RequestMapping("/ai")
public class AiContronller {



        @Resource
        private LoveApp loveApp;

        @Resource
        private ToolCallback[] allTools;

        @Resource
        private ChatModel dashscopeChatModel;



        /**
         * 同步调用 AI 恋爱大师应用
         *
         * @param message
         * @param chatId
         * @return
         */
        @GetMapping("/love_app/chat/sync")
        public String doChatWithLoveAppSync(String message, String chatId) {
            return loveApp.doChat(message, chatId);
        }

        /**
         * SSE流式调用 AI 恋爱大师应用
         *
         * @param message
         * @param chatId
         * @return
         */
        @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
            return loveApp.doChatByStream(message, chatId);
        }

        /**
         * SSE流式调用 AI 恋爱大师应用
         *
         * @param message
         * @param chatId
         * @return
         */
        @GetMapping("/love_app/chat/sse")
        public SseEmitter doChatWithLoveAppSse(String message, String chatId) {

            SseEmitter emitter = new SseEmitter(180000L);

            loveApp.doChatByStream(message, chatId)
                    .subscribe(

                            chunk -> {
                                try {
                                    emitter.send(chunk);
                                } catch (IOException e) {
                                    emitter.completeWithError(e);
                                }
                            },

                            emitter::completeWithError,

                            emitter::complete
                    );

            return emitter;
        }

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        YuManus yuManus = new YuManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }

    }

