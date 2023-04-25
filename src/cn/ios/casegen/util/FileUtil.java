package cn.ios.casegen.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangmiaomiao
 * @description: TODO
 * @date: 2023/4/19 11:09
 */
public class FileUtil {
    public static boolean hasFile(String path){
        List<File> allFileList = new ArrayList<>();
        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            getAllFile(file, allFileList);
            return allFileList.size() > 0;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static void getAllFile(File input, List<File> allFileList){
        File[] fileList = input.listFiles();
        if (fileList == null) {
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                getAllFile(file, allFileList);
            } else {
                allFileList.add(file);
            }
        }
    }
}
