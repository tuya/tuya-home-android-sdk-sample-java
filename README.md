Tuya Android Smart Life App SDK Sample
===
[中文版](README_zh.md)|[English](README.md)

This sample demonstrates the use of Tuya Android Smart Life App SDK to build an IoT App from scratch. It divides into several function groups to give developers a clear insight into the implementation for different features, includes the user registration process, home management for different users, device network configuration, and controls. For device network configuration, EZ mode and AP mode are implemented, which let developers pair devices over Wi-Fi, as well as control them via LAN and MQTT. For device control, it supplies a common panel for sending and receiving any kind types of data points.

## Self-developed Smart Life App Service
Self-Developed Smart Life App is one of Tuya’s IoT app development solutions. This solution provides the services that enable connections between the app and the cloud. It also supports a full range of services and capabilities that customers can use to independently develop mobile apps. The Smart Life App SDK used in this sample is included in the Self-developed Smart Life App Service.

Self-Developed Smart Life App is classified into the **Trial** and **Official** editions:

- **Self-Developed App Trial**: provided for a free trial. It supports up to 100,000 cloud API calls per month and up to 20 registered end users in total.

- **Self-Developed App Official**: provided for commercial use and costs $5,000/year (¥33,500/year) for the initial subscription and $2,000/year (¥13,500/year) for subsequent annual renewal. It is supplied with the Custom Domain Name service and up to 100 million cloud API calls per month.

For more information, please check the [Pricing](https://developer.tuya.com/en/docs/app-development/app-sdk-price?id=Kbu0tcr2cbx3o).

## Get Started

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
Tuya Android Smart Life App SDK Sample is available under the MIT license. Please see the [LICENSE](LICENSE) file for more info.
