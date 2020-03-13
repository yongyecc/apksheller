package cn.yongye.stub.common;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    /**
     * 获取父目录的绝对路径
     * @param f 文件对象
     * @return 父目录对象
     */
    public static File getParent(File f) {
        String path = f.getAbsolutePath();
        int separator = path.lastIndexOf(File.separator);
        return separator == -1 ? new File(path) : new File(path.substring(0, separator));
    }

    /**
     * 获取应用classes.dex字节数据
     * @param context
     * @return
     * @throws IOException
     */
    public static byte[] readDexFileFromApk(Context context) throws IOException {
        ByteArrayOutputStream dexByteArrayOutputStream = new ByteArrayOutputStream();
        ZipInputStream localZipInputStream = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(
                        context.getApplicationInfo().sourceDir)));
        while (true) {
            ZipEntry localZipEntry = localZipInputStream.getNextEntry();
            if (localZipEntry == null) {
                localZipInputStream.close();
                break;
            }
            if (localZipEntry.getName().equals("classes.dex")) {
                byte[] arrayOfByte = new byte[1024];
                while (true) {
                    int i = localZipInputStream.read(arrayOfByte);
                    if (i == -1)
                        break;
                    dexByteArrayOutputStream.write(arrayOfByte, 0, i);
                }
            }
            localZipInputStream.closeEntry();
        }
        localZipInputStream.close();
        return dexByteArrayOutputStream.toByteArray();
    }

    /**
     * 将原DEX从壳DEX上分离出来
     * @param stDexPt
     * @param apkdata   壳DEX数据
     * @throws IOException
     */
    public static void splitPayLoadFromDex(String stDexPt, byte[] apkdata) throws IOException {
        String TAG = "yongye";
        apkdata = decrypt(apkdata);
        int ablen = apkdata.length;
        byte[] dexlen = new byte[4];
        System.arraycopy(apkdata, ablen - 4, dexlen, 0, 4);
        ByteArrayInputStream bais = new ByteArrayInputStream(dexlen);
        DataInputStream in = new DataInputStream(bais);
        int readInt = in.readInt();
        Log.i(TAG, "原始DEX长度：" + String.valueOf(readInt));
        byte[] newdex = new byte[readInt];
        System.arraycopy(apkdata, ablen - 4 - readInt, newdex, 0, readInt);
        File file = new File(stDexPt);
        if(file.exists()){
            Log.i(TAG, file.getAbsolutePath() + "文件还存在");
        }
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(file);
            localFileOutputStream.write(newdex);
            localFileOutputStream.close();
        } catch (IOException localIOException) {
            Log.e(TAG, "dump 原始DEX失败");
            throw new RuntimeException(localIOException);
        }
    }

    private static byte[] decrypt(byte[] baEncryted){
        byte[] baRes = new byte[baEncryted.length];
        for(int i=0; i<baEncryted.length; i++){
            baRes[i] = (byte) (baEncryted[i] ^ 0xff);
        }
        return baRes;
    }
}
