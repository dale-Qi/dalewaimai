package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping("upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);

        try {
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名后缀
            String extention = originalFilename.substring(originalFilename.lastIndexOf("."));
            //使用uuid构建新名称
            String objectname = UUID.randomUUID().toString() + extention;

            String filepath = aliOssUtil.upload(file.getBytes(), objectname);
            return Result.success(filepath);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
