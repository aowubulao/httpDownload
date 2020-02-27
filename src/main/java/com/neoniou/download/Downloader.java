package com.neoniou.download;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * @author Neo.Zzj
 */
public class Downloader {

    private static String   downloadUrl;
    private static String   downloadPath;
    private static int      threadNum;

    private static int      totalDownload = 0;
    private static int      endThread = 0;

    private int fileSize;

    public Downloader(String downloadUrl, String downloadPath, int threadNum) {
        Downloader.downloadUrl = downloadUrl;
        Downloader.downloadPath = downloadPath;
        Downloader.threadNum = threadNum;
    }

    public void download() throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //设置参数
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Connection", "Keep-Alive");

        if (200 == connection.getResponseCode()) {
            System.out.println("============================");
            System.out.println("获取文件信息：");
            //获取下载文件大小和名称
            fileSize = connection.getContentLength();
            System.out.println("文件大小：" + changeUnit(fileSize));
            String fileName = getFileName(connection);

            //创建本地文件
            downloadPath += "/" + fileName;
            RandomAccessFile file = new RandomAccessFile(downloadPath, "rw");
            file.setLength(fileSize);
            file.close();

            System.out.println("============================");
            System.out.println("开始下载...");
            int partSize = fileSize / threadNum;
            for (int i = 0; i < threadNum; i++) {
                //补全除不尽的部分
                if (i == threadNum - 1) {
                    partSize += fileSize - (partSize * threadNum);
                }
                int startPos = i * partSize;
                RandomAccessFile partFile = new RandomAccessFile(downloadPath, "rw");
                partFile.seek(startPos);
                new MultiDownload(startPos, partSize, partFile).start();
            }
            //显示下载进度
            printDownloadProcess();
        }
    }

    public static class MultiDownload extends Thread {

        private int startPos;
        private int partSize;
        private RandomAccessFile partFile;
        public int length;

        public MultiDownload(int startPos, int partSize, RandomAccessFile partFile) {
            this.startPos = startPos;
            this.partSize = partSize;
            this.partFile = partFile;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //设置参数
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Connection", "Keep-Alive");

                if (200 == connection.getResponseCode()) {
                    InputStream is = connection.getInputStream();
                    //跳转到该线程应该下载的指针处
                    is.skip(this.startPos);

                    byte[] buffer = new byte[1024];
                    int hasDownload = 0;
                    while (length < partSize && (hasDownload = is.read(buffer)) != -1) {
                        partFile.write(buffer,0, hasDownload);
                        //计算该线程下载的大小
                        length += hasDownload;
                        totalDownload += hasDownload;
                    }
                    //记录该线程下载完成
                    endThread++;
                    //Close
                    partFile.close();
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示下载进度
     * @throws Exception e
     */
    private void printDownloadProcess() throws Exception {
        int before = 0;
        float percent;
        int time = 0;
        DecimalFormat format = new DecimalFormat("0.00");
        while (threadNum != endThread) {
            String speed = changeUnit(totalDownload - before);
            percent = (float) totalDownload / fileSize * 100;
            String rev = format.format(percent);

            System.out.println("已经下载:" + rev + "%" + "--速度：" + speed + "/s");
            before = totalDownload;
            time++;
            Thread.sleep(1000);
        }
        System.out.println("============================");
        System.out.println("下载完成！用时：" + time + "s");
    }

    /**
     * 转换文件大小单位
     */
    public String changeUnit(int b) {
        final String[] unit = {"B","KB","MB","GB","TB"};
        float rev = b;
        int i = 0;
        while (rev > 1024) {
            rev /= 1024;
            i++;
        }
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        return decimalFormat.format(rev) + unit[i];
    }

    /**
     * 获取下载文件名称
     * @param connection HttpURLConnection
     * @return fileName
     */
    public String getFileName(HttpURLConnection connection) {
        String fileName = connection.toString();
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        System.out.println("下载文件名称：" + fileName);
        return fileName;
    }
}
