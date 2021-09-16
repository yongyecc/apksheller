package cn.yongye.stub;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import dalvik.system.BaseDexClassLoader;
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

        // load(oApp, oDexClassLoader, "/data/local/tmp/classes2.dex");
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

    public void load(Context oApp, DexClassLoader dcl, String path) {
        try {
            // 已加载的dex
            Object dexPathList = getField(BaseDexClassLoader.class, "pathList", oApp.getClassLoader());
            Object dexElements = getField(dexPathList.getClass(), "dexElements", dexPathList);

            // patchdex
            String dexOptDir = oApp.getCacheDir().getAbsolutePath();
            Object patchDexPathList = getField(BaseDexClassLoader.class, "pathList", dcl);
            Object patchDexElements = getField(patchDexPathList.getClass(), "dexElements", patchDexPathList);

            // 将patchdex和已加载的dexes数组拼接连接
            Object concatDexElements = concatArray(patchDexElements, dexElements);

            // 重新给dexPathList#dexElements赋值
            setField(dexPathList.getClass(), "dexElements", dexPathList, concatDexElements);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param cls       被访问对象的class
     * @param fieldName 对象的成员变量名
     * @param object    被访问对象
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public Object getField(Class<?> cls, String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }


    /**
     * @param cls       被访问对象的class
     * @param fieldName 对象的成员变量名
     * @param object    被访问对象
     * @param value     赋值给成员变量
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void setField(Class<?> cls, String fieldName, Object object, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }


    /**
     * 连接两个数组（指定位置）
     *
     * @param left  连接后在新数组的左侧
     * @param right 连接后在新数组的右侧
     * @return
     */
    public Object concatArray(Object left, Object right) {
        int len1 = Array.getLength(left);
        int len2 = Array.getLength(right);
        int totalLen = len1 + len2;
        Object concatArray = Array.newInstance(left.getClass().getComponentType(), totalLen);
        for(int i = 0; i < len1; i++) {
            Array.set(concatArray, i, Array.get(left, i));
        }
        for(int j = 0; j < len2; j++) {
            Array.set(concatArray, len1 + j, Array.get(right, j));
        }
        return concatArray;
    }
}
