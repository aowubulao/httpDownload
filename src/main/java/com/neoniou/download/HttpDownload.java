package com.neoniou.download;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Scanner;

/**
 * @author Neo.Zzj
 */
public class HttpDownload {

    public static void main(String[] args) {
        //获取桌面地址
        File desktopDir = FileSystemView.getFileSystemView() .getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath();

        System.out.println("输入下载地址：");
        Scanner scanner = new Scanner(System.in);
        String url = scanner.next();



        Downloader downloader = new Downloader(url,desktopPath,1);
        try {
            downloader.download();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
