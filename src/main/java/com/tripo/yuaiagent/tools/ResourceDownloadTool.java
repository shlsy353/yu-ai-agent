package com.tripo.yuaiagent.tools;

import cn.hutool.core.io.*;
import cn.hutool.http.*;
import com.tripo.yuaiagent.constant.*;
import java.io.*;
import org.springframework.ai.tool.annotation.*;

public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url, @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        String fileDir = FileConstant.File_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;
        try {

            FileUtil.mkdir(fileDir);

            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
