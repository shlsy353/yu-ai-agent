package com.tripo.yuaiagent.agent.model;


import cn.hutool.core.collection.*;
import cn.hutool.core.util.*;
import lombok.*;
import org.springframework.ai.chat.messages.*;

/**
 * ReAct代理类
 * 实现了思考-行动-思考的循环，并使用ReAct工具进行决策和执行
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent{

    /**
     * 思考步骤
     * @return true表示成功，false表示失败
     */
    public abstract boolean think();

    /**
     * 行动步骤
     * @return true表示成功，false表示失败
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            // 思考步骤
            boolean shouldAct = think();
            if (!shouldAct) {
                // 当不需要调用工具时，LLM已经生成了最终的文本回答
                // 从消息列表中获取最后的助手消息并返回其文本
                Message lastMsg = CollUtil.getLast(getMessageList());
                if (lastMsg instanceof AssistantMessage) {
                    String text = ((AssistantMessage) lastMsg).getText();
                    if (StrUtil.isNotBlank(text)) {
                        return text;
                    }
                }
                return "Skipped action";
            }
            // 行动步骤
            return act();
        }catch (Exception e){
            return "Error: " + e.getMessage();
        }
    }
}
