package com.tripo.yuaiagent.tools;

import cn.hutool.core.io.*;
import com.tripo.yuaiagent.constant.*;
import org.springframework.ai.tool.annotation.*;

/**
 * 文件操作工具
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.File_SAVE_DIR + "/file";


    @Tool(description = "读取文件内容")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName){
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e){
            return "读取文件失败";
        }

    }
    @Tool(description = "写入文件内容")
    public String writeFile(@ToolParam(description = "Name of the file to write")String fileName,
                            @ToolParam(description = "Content to write to the file")String content){
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "写入文件成功到："+filePath;
        } catch (Exception e) {
            return "写入文件失败:"+e.getMessage();
        }

    }
}
