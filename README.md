Tuya Android Home SDK Sample
===
[中文版](README_zh.md)|[English](README.md)

This sample demonstrates the use of Tuya Android Home SDK to build an IoT App from scratch. It divides into several function groups to give developers a clear insight into the implementation for different features, includes the user registration process, home management for different users, device network configuration, and controls. For device network configuration, EZ mode and AP mode are implemented, which let developers pair devices over Wi-Fi, as well as control them via LAN and MQTT. For device control, it supplies a common panel for sending and receiving any kind types of data points.

Get Started
---






- [Preparation for Integration](https://developer.tuya.com/en/docs/app-development/android-app-sdk/preparation?id=Ka7mqlxh7vgi9)

> Note：From the 3.29.5 version,Smart Life App SDK has done the security checksum。You need to get SHA256 in[Tuya IoT platform](https://developer.tuya.com/en/docs/app-development/iot_app_sdk_core_sha1?id=Kao7c7b139vrh),then bind your SHA256,otherwise it will report an illegal client error. If you need a local dubug to run Sample, you need to configure your signature information in the app module under build.gradle, android closures at：
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

- [Integration](https://developer.tuya.com/en/docs/app-development/android-app-sdk/integration/integrated?id=Ka69nt96cw0uj)

- [SDK Features](https://developer.tuya.com/en/docs/app-development/android-app-sdk/featureoverview?id=Ka69nt97vtsfu)

App Images
---
<img src="https://images.tuyacn.com/app/liya/tuya-app-sdk-sample/app_sdk_sample_en.jpg" alt="main_page" width="30%" />

Issue Feedback
---

You can provide feedback on your issue via **Github Issue** or [Technical Ticket](https://service.console.tuya.com).

License
---
Tuya Android Home SDK Sample is available under the MIT license. Please see the [LICENSE](LICENSE) file for more info.
