package com.tripo.yuaiagent.agent.model;


import lombok.*;

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
                return "Skipped action";
            }
            // 行动步骤
            return act();
        }catch (Exception e){
            return "Error: " + e.getMessage();
        }
    }
}
