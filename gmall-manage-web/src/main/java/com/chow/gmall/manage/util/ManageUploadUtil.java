package com.chow.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ManageUploadUtil {

    public static String imgUpload(MultipartFile multipartFile){

        String imgUrl = "http://192.168.109.109";// 常量类

        try {
            ClientGlobal.init("tracker.conf");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 创建tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建storage
        StorageClient storageClient = new StorageClient(connection, null);

        // 上传文件
        String originalFilename = multipartFile.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        String file_ext_name = originalFilename.substring(i + 1);
        String[] jpgs = new String[0];
        try {
            jpgs = storageClient.upload_file(multipartFile.getBytes(), file_ext_name, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        for (String jpg : jpgs) {
            imgUrl = imgUrl + "/" + jpg;
        }
        return imgUrl;
    }

}
