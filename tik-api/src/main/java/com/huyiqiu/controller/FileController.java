package com.huyiqiu.controller;


import com.huyiqiu.MinIOConfig;
import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.utils.MinIOUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "FileController 文件上传测试接口")
@RestController
public class FileController {

    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping("upload")
    public GraceJSONResult upload(MultipartFile file) throws Exception{

        String filename = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());

        String imgUrl = minIOConfig.getFileHost()
                        + "/"
                        + minIOConfig.getBucketName()
                        + "/"
                        + filename;

        return GraceJSONResult.ok(imgUrl);
    }

}
