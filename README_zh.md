Tuya Android Smart Life App SDK Sample
===
[中文版](README_zh.md) | [English](README.md)

开始
---

[准备工作](https://developer.tuya.com/zh/docs/app-development/android-app-sdk/preparation?id=Ka7mqlxh7vgi9)

> 注意：智能生活App SDK从3.29.5版本开始，做了安全校验的升级。您需要在[IoT平台根据说明文档](https://developer.tuya.com/cn/docs/app-development/iot_app_sdk_core_sha1?id=Kao7c7b139vrh)来获取SHA256，然后在IoT平台绑定您的SHA256，否则会报错非法客户端。如果您需要本地dubug运行Sample，您需要在app模块的build.gradle下，android闭包中配置您的签名信息：
```groovy
signingConfigs {
        debug {
            storeFile file('../xxx.jks')
            storePassword 'xxx'
            keyAlias 'xxx'
            keyPassword 'xxx'
        }
    }
```

[集成](https://developer.tuya.com/zh/docs/app-development/android-app-sdk/integration/integrated?id=Ka69nt96cw0uj)

[SDK功能](https://developer.tuya.com/zh/docs/app-development/android-app-sdk/featureoverview?id=Ka69nt97vtsfu)

App 图片
---
<img src="https://images.tuyacn.com/app/liya/tuya-app-sdk-sample/app_sdk_sample_zh.jpg" alt="main_page" style="zoom:30%;" />

问题反馈
---

您可以通过**Github Issue** 或通过[**工单**](https://service.console.tuya.com)来进行反馈您所碰到的问题

LICENSE
---
Tuya Android Smart Life App SDK Sample是在MIT许可下提供的。更多信息请参考[LICENSE](LICENSE)文件
