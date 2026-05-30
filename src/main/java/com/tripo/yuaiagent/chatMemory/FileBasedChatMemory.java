package com.tripo.yuaiagent.chatMemory;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.esotericsoftware.kryo.util.*;
import java.io.*;
import java.util.*;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.messages.*;

public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    static{
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String baseDir) {
        BASE_DIR = baseDir;
        File baseDIr = new File(baseDir);
        if (!baseDIr.exists()) {
            baseDIr.mkdirs();
        }
    }
    @Override
    public void add(String conversationId, Message message) {
        saveConversation(conversationId, List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = getOrcreateConversion(conversationId);
        messageList.addAll(messages);
        saveConversation(conversationId, messageList);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messageList = getOrcreateConversion(conversationId);

        return messageList.stream()
                .skip(messageList.size() - 10)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取或创建一个会话信息的列表
     * @param conversationId
     * @return
     */
    private List< Message> getOrcreateConversion(String conversationId) {
        File file = getConversationFile(conversationId);
        List< Message> message = new ArrayList<>();
        if (!file.exists()) {
            try(Input input = new Input(new FileInputStream(file))) {
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
      return message;
    }

    /**
     * 保存会话信息
     * @param conversationId
     * @param messages
     * @return
     */
    private void saveConversation(String conversationId, List< Message> messages) {
        File file = getConversationFile(conversationId);
        try(Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }





    /**
     * 每个回话文件单独保存
     * @param conversationId
     * @return
     */

    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
