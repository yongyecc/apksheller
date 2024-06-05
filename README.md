# 简介

Java层DEX一键加固脚本

# 使用说明

```shell
python -f xxx.apk
```

# 加固原理

准备一个壳DEX文件(源码位置：shellApplicationSourceCode)，将原APK的DEX文件加密保存到壳DEX尾部，然后将原APK文件中的原DEX文件替换为壳DEX，并修改原APK文件里AndroidManifest.xml的applicationandroid:name字段，实现从壳DEX启动。

解密和加载原DEX的任务交给壳DEX，这样就实现了**APK文件防编译保护**

# 一键加固脚本实现步骤

1. 准备原DEX加密算法以及隐藏位置（壳DEX尾部）

```python
        """
        1. 第一步：确定加密算法
        """
        inKey = 0xFF
        print("[*] 确定加密解密算法，异或: {}".format(str(inKey)))
```

2. 生成壳DEX。（壳Application动态加载原application中需要原application的name字段）

```python
        """
        2. 第二步：准备好壳App
        """
        # 反编译原apk
        decompAPK(fp)
        # print("[*] 反编译原的apk文件{}完成".format(fp))
        # 获取Applicaiton name并保存到壳App源码中
        stSrcDexAppName = getAppName(fp)
        # print("[*] 获取原apk文件的Application Android:name=\"{}\" 完成".format(stSrcDexAppName))
        save_appName(stSrcDexAppName)
        # print("[*] 保存原apk文件的Application Android:name=\"{}\" 到壳App源码的配置文件完成".format(stSrcDexAppName))
        # 编译出壳DEX
        compileShellDex()
        print("[*] 壳App的class字节码文件编译为:shell.dex完成")
```

3. 修改原APK文件中的AndroidManifest.xml文件的applicationandroid:name字段，实现从壳application启动

```python
		"""
        3. 第三步：修改原apk AndroidManifest.xml文件中的Application name字段为壳的Application name字段
        """
        # 替换壳Applicaiton name到原apk的AndroidManifest.xml内
        replaceTag(fp, "cn.yongye.stub.StubApp")
        print("[*] 原apk文件AndroidManifest.xml中application的name字段替换为壳application name字段完成")
```

4. 加密原DEX到壳DEX尾部并将壳DEX替换到原APK中

```python
		"""
        4. 替换原apk中的DEX文件为壳DEX
        """
        replaceSDexToShellDex(os.path.join(stCurrentPt, "result.apk"))
        print("[*] 壳DEX替换原apk包内的DEX文件完成")
```

5. 自动签名

```python
		"""
        5. apk签名
        """
        signApk(os.path.join(stCurrentPt, "result.apk"), os.path.join(stCurrentPt, "demo.keystore"))
        print("[*] 签名完成，生成apk文件: {}".format(os.path.join(stCurrentPt, "result.apk")))
```

# 问题

【1】**libpng error: Not a PNG file**：apktool.jar编译smali项目时，如果出现png结尾的GIF文件时，会编译失败，这里我的解决方法时将GIF强行转换成PNG，解决问题。

中途解决该问题时，曾尝试使用[AXMLEditor](https://github.com/fourbrother/AXMLEditor)修改二进制Androidmanifest.xml的开源工具直接修改，然后想绕过编译资源步骤，实际不能成功，因为更改过后的application name字段在编译资源过程中，会被记录下来，而直接修改导致没有被记录，android系统是识别不到修改后的这个字段值

# 定制版内容（付费）

[x] 多DEX加密

[x] APK包伪加密

[x] 部分资源加密

[x] 包名随机

[x] ...


# 参考

[1] [Dex简单保护](https://xz.aliyun.com/t/5789)

[2] [ DexShell](https://github.com/Herrrb/DexShell)

# 联系

vx: xcc1014885794

# 赞助

![支付宝](/images/zfb.png)

![微信](/images/wx.png)
