package cn.yongye.stub;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import cn.yongye.stub.common.Config;
import cn.yongye.stub.common.FileUtils;
import cn.yongye.stub.common.RefInvoke;
import dalvik.system.DexClassLoader;


public class StubApp extends Application {

    private String TAG = "yongye";
    private String stSrcDexName = "yongye.jar";
    private Application oApp = this;
    private String stDmpDexPt = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        /**
         * 1.解密原始DEX到缓存目录下yongye
         */
        byte[] baSrcDex = null;
        stDmpDexPt = oApp.getCacheDir().getAbsolutePath() + "/yongye";
        File libs = oApp.getDir("libs", Context.MODE_PRIVATE);
        byte[] baDex = null;
        try {
            baDex = FileUtils.readDexFileFromApk(oApp);
            FileUtils.splitPayLoadFromDex(stDmpDexPt, baDex);
        } catch (IOException e) {
            Log.e(TAG, "解密原始DEX失败。");
            e.printStackTrace();
        }
        Log.i(TAG, "解密原始DEXcompleted");
//        InputStream oIn = null;
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(new File(stDmpDexName));
//            byte[] ba = new byte[1024];
//            int len;
//            while((len = oIn.read(ba)) != -1){
//                for (int i=0; i<len; i++)
//                    ba[i] ^= bKey;
//                fos.write(ba, 0, len);
//
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "not found " + stSrcDexName);
//            e.printStackTrace();
//        }
//        Log.i(TAG, "decrypt completed.");

        /**
         * 2.加载原始DEX
         */
        String stActivityThread = "android.app.ActivityThread";
        String stClassLoadedApk = "android.app.LoadedApk";
        String stCurrentPkgName = this.getPackageName();

        File nativeLib = new File(FileUtils.getParent(libs), "lib");

        Object obCurrentActivityThread = RefInvoke.invokeStaticMethod(stActivityThread,
                "currentActivityThread", new Class[]{}, new Object[]{});
        Map<?,?> obmPackage = (Map<?,?>)RefInvoke.getFieldObject(stActivityThread,
                obCurrentActivityThread, "mPackages");
        WeakReference<?> wr = (WeakReference<?>) obmPackage.get(stCurrentPkgName);
        DexClassLoader oDexClassLoader = new DexClassLoader(stDmpDexPt, oApp.getCacheDir().getAbsolutePath(),
                nativeLib.getAbsolutePath(), (ClassLoader)RefInvoke.getFieldObject(stClassLoadedApk,
                wr.get(), "mClassLoader"));
        RefInvoke.setFieldObject(stClassLoadedApk, "mClassLoader", wr.get(), oDexClassLoader);
        Log.i(TAG, "load source dex completed.");

        /**
         * 3.启用原始DEX文件的Application(更改环境值满足加载原始DEX的要求)
         */
        String stSrcAppName = Config.MAIN_APPLICATION;
        Object mBoundApplication = RefInvoke.getFieldObject(stActivityThread, obCurrentActivityThread, "mBoundApplication");
        Object loadedApkInfo = RefInvoke.getFieldObject(stActivityThread +"$AppBindData", mBoundApplication, "info");
        RefInvoke.setFieldObject(stClassLoadedApk, "mApplication", loadedApkInfo, null);
        Object mInitApplication = RefInvoke.getFieldObject(stActivityThread, obCurrentActivityThread, "mInitialApplication");
        List<Application> mAllApplications = (List<Application>) RefInvoke.getFieldObject(stActivityThread, obCurrentActivityThread, "mAllApplications");
        mAllApplications.remove(mInitApplication);
        ((ApplicationInfo) RefInvoke.getFieldObject(stClassLoadedApk, loadedApkInfo, "mApplicationInfo")).className = stSrcAppName;
        ((ApplicationInfo) RefInvoke.getFieldObject(stActivityThread+"$AppBindData", mBoundApplication, "appInfo")).className = stSrcAppName;
        Application makeApplication = (Application) RefInvoke.invokeMethod(stClassLoadedApk,
                "makeApplication", loadedApkInfo, new Class[]{boolean.class, Instrumentation.class}, new Object[]{false, null});
        RefInvoke.setFieldObject(stActivityThread, "mInitialApplication", obCurrentActivityThread,
                makeApplication);
        Map<?,?> mProviderMap = (Map<?,?>) RefInvoke.getFieldObject(stActivityThread, obCurrentActivityThread,
                "mProviderMap");
        for (Map.Entry<?, ?> entry : mProviderMap.entrySet()) {
            Object providerClientRecord = entry.getValue();
            Object mLocalProvider = RefInvoke.getFieldObject(stActivityThread+"$ProviderClientRecord", providerClientRecord, "mLocalProvider");
            RefInvoke.setFieldObject("android.content.ContentProvider", "mContext", mLocalProvider, makeApplication);
        }
        makeApplication.onCreate();
        Log.i(TAG, "unshell completed.");
    }
}
