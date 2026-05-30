package com.tripo.yuaiagent.app;

import jakarta.annotation.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotNull(answer3);
        String answer4 = loveApp.doChat("我叫什么名字呢，刚刚给你说过！", chatId);
        assertNotNull(answer4);
    }


    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString(); // 创建一个会话ID
        String answer1 = String.valueOf(loveApp.doChatWithReport("nihao,我是人鱼，我想让我的另一半（小星），" +
                "更加爱我，更加听我的话，但我不知道该怎么做", chatId));
        assertNotNull(answer1);


    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString(); // 创建一个会话ID
        String answer1 = String.valueOf(loveApp.doChatWithRag("nihao,我是人鱼，我已经结婚了" +
                "，但婚后关系不太亲密怎么办？", chatId));
        assertNotNull(answer1);
    }

    @Test
    void recommendPartner() {
        String answer = String.valueOf(loveApp.recommendPartner("请你给我推荐一个25岁的程序员，我不考虑星座"));
        assertNotNull(answer);
        System.out.println("======== 推荐结果 ========");
        System.out.println(answer);
        System.out.println("==========================");
    }

    @Test
    void doChatWithTools() {

        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");


        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");


        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");


        testMessage("执行 Python3 脚本来生成数据分析报告");


        testMessage("保存我的恋爱档案为文件");


        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        assertNotNull(answer);
    }

}