package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    public static void createFile(String fileName,String dir,String content) throws IOException {
        if(Empty4JUtil.stringIsEmpty(fileName) || Empty4JUtil.stringIsEmpty(dir)){
            throw new IOException("文件名或保存路径异常");
        }
        File file;
        String lastSlash = dir.substring(dir.length()-1,dir.length());
        String path;
        if("/".equals(lastSlash)){
            path = dir;
        }else{
            path = dir + "/";
        }
        file = new File(path + fileName + ".java");
        File mkdir = new File(path);
        if(!mkdir.exists()){
            mkdir.mkdirs();
        }
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileWriter fw = new FileWriter(path + fileName + ".java");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
        fw.close();
    }

    public static void writerTo(String fileName,String dir,String content) throws IOException {
        String lastSlash = dir.substring(dir.length()-1,dir.length());
        String path;
        if("/".equals(lastSlash)){
            path = dir;
        }else{
            path = dir + "/";
        }
        FileWriter fw = new FileWriter(path + fileName + ".java");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
        fw.close();
    }


}
