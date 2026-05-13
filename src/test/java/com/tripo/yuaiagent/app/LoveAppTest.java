package com.tripo.yuaiagent.app;

import jakarta.annotation.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString(); // 创建一个会话ID
        String answer1 = loveApp.doChat("nihao", chatId);

        String answer2 = loveApp.doChat("我叫小王，你叫什么名字？", chatId);

        String answer3 = loveApp.doChat("我想让我的另一半更爱我？", chatId);
        Assertions.assertNotNull(answer3);
        String answer4 = loveApp.doChat("我叫什么名字呢，刚刚给你说过！", chatId);
        Assertions.assertNotNull(answer4);
    }


    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString(); // 创建一个会话ID
        String answer1 = String.valueOf(loveApp.doChatWithReport("nihao,我是人鱼，我想让我的另一半（小星），" +
                "更加爱我，更加听我的话，但我不知道该怎么做", chatId));
        Assertions.assertNotNull(answer1);


    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString(); // 创建一个会话ID
        String answer1 = String.valueOf(loveApp.doChatWithRag("nihao,我是人鱼，我已经结婚了" +
                "，但婚后关系不太亲密怎么办？", chatId));
        Assertions.assertNotNull(answer1);
    }

    @Test
    void recommendPartner() {
        String answer = String.valueOf(loveApp.recommendPartner("请你给我推荐一个25岁的程序员，我不考虑星座"));
        Assertions.assertNotNull(answer);
        System.out.println("======== 推荐结果 ========");
        System.out.println(answer);
        System.out.println("==========================");
    }
}