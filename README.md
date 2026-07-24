# Dynamsoft Capture Vision samples for the Java Edition

This repository contains multiple samples that demonstrates how to use the [Dynamsoft Capture Vision](https://www.dynamsoft.com/capture-vision/docs/server/programming/java/) Java Edition.

## System Requirements

### Supported Platforms

**Windows**
- Supported Versions: Windows 8 and higher, or Windows Server 2012 and higher
- Architectures: x64, x86

**Linux**
- **x64**
	- Supported Distributions: Ubuntu 16.04+ LTS, Debian 8+, CentOS 7+
	- Dependencies: glibc 2.17
- **ARM64**
	- Supported Distributions: Ubuntu 18.04+ LTS, Debian 10+, CentOS/RHEL 8+
	- Dependencies: glibc 2.27

**macOS**
- Supported Versions: macOS 12 (Monterey) and higher
- Architectures: universal (x64, Apple Silicon)

### Runtime Environment

- JDK 1.8 and above

## Samples

| Sample | Description |
| --- | --- |
|[`MRZScanner`](Samples/MRZScanner)          | Capture and extract user's information from machine-readable travel documents with Dynamsoft Capture Vision SDK.            |
|[`DriverLicenseScanner`](Samples/DriverLicenseScanner)          | Capture and extract user's information from driver license/ID with Dynamsoft Capture Vision SDK.            |
|[`DocumentScanner`](Samples/DocumentScanner)          | The simplest way to detect and normalize a document from an image and save the result as a new image.            |
|[`GS1AIScanner`](Samples/GS1AIScanner) | Shows how to extract and interpret GS1 Application Identifiers (AIs) from GS1 barcodes. |

## License

The library requires a license to work, you use the API initLicense to initialize license key and activate the SDK.

These samples use a free public trial license which require network connection to function. You can request a 30-day free trial license key from <a href="https://www.dynamsoft.com/customer/license/trialLicense?product=dcv&utm_source=samples&package=java" target="_blank">Customer Portal</a> which works offline.


## Contact Us

https://www.dynamsoft.com/company/contact/
