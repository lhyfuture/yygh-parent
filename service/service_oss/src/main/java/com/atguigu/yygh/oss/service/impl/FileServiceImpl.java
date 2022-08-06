package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.utils.ConstantPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file) {
        //1.获取参数
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantPropertiesUtil.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtil.ACCESS_KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantPropertiesUtil.BUCKET_NAME;

        //2.创建客户端对象
        OSS ossClient = new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);
        try {
            //3.准备文件参数
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            //3.2文件名不重复Uuid+fileName
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            fileName = uuid + fileName;
            //3.3根据日期创建路径2022/07/30/ +fileName 方便维护
            String path = new DateTime().toString("yyyy/MM/dd");
            fileName = path + "/" + fileName;
            //4、上传文件
            ossClient.putObject(bucketName,fileName,inputStream);
            //5、获取url
            String url = "https://"+ bucketName + "." + endpoint + "/" + fileName;
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            throw new GuliException(20001,"上传文件失败");
        } finally {
            //6、关闭客户端
            if(ossClient != null){
                ossClient.shutdown();
            }
        }

    }
}
