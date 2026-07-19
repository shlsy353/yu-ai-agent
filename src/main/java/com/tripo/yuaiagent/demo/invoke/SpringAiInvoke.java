package com.tripo.yuaiagent.demo.invoke;

import jakarta.annotation.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

/**
 * 描述:SpringBoot调用阿里云百炼
 *
 * @author yuaiAgent
 * @create 2023-05-09 10:05
 */
@Component
@ConditionalOnProperty(prefix = "demo.invoke", name = "enabled", havingValue = "true")
public class SpringAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;


    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你是谁？"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
