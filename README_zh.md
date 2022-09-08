Tuya Android Smart Life App SDK Sample
===
[中文版](README_zh.md) | [English](README.md)

## 收费标准

Tuya Self-Developed Smart Life App 是涂鸦 IoT App 开发解决方案的一种，以 App 云端连接服务为主，包含智能生活App SDK, 涉及收费标准详见[产品定价](https://developer.tuya.com/cn/docs/app-development/app-sdk-price?id=Kbu0tcr2cbx3o)

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

![app_sdk_sample1_zh](https://user-images.githubusercontent.com/907831/186592149-3576b3da-a06f-4d53-b16e-29386fd57f8e.jpeg)

问题反馈
---

您可以通过**Github Issue** 或通过[**工单**](https://service.console.tuya.com)来进行反馈您所碰到的问题

LICENSE
---
Tuya Android Smart Life App SDK Sample是在MIT许可下提供的。更多信息请参考[LICENSE](LICENSE)文件
