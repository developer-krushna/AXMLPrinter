# AXMLPrinter
An Advanced Axml Printer available with proper xml style/format feature
# What is the use of this tool
A tool for printing out Android binary XML files (It can be Android Manifest or other resources XML files) into normal raw AXML text. It is inspired by Android4ME's axmlprinter library.

## What's new on it.
There are several changes made to the code, and multiple bugs have been fixed. The following changes have been made in this AXMLPrinter library:
- [x] Added support for translating enum or flags based attributes (Customized function)
- [x] Fixed decoding some UTF-8.
- [x] Extraction of attribute name from id
- [x] Extraction of flag and enum values according to android resource
- [x] Fixed close tag error
- [x] Fixed handling unknown attributes
- [x] Fixed indentation/tab error
- [x] Improved speed in xml decompilation
- [x] Extraction of Attribute name according to resource id
- [x] Id2Name (Reading axml file according to the resource id name)
- [x] Extraction of manifest permissions short description (Customized function)
- [x] Reading pseudo-encrypted xml files

## Usage

### Basic Decompilation
Decompile a binary XML file from a file path:
```java
AXMLPrinter axmlPrinter = new AXMLPrinter();
String xml = axmlPrinter.readFromFile("/path/to/binary/AndroidManifest.xml");
```

### Decompile from APK
Directly extract and decompile `AndroidManifest.xml` from an APK file, with automatic pseudo-encryption bypass:
```java
AXMLPrinter axmlPrinter = new AXMLPrinter();
String xml = axmlPrinter.readFromApk("/path/to/app.apk");
```

### Advanced Features
You can enable advanced features like attribute value translation (e.g., converting hex to flag names) and permission descriptions:
```java
AXMLPrinter axmlPrinter = new AXMLPrinter();
axmlPrinter.setAttrValueTranslation(true); // Translate enum/flag attributes
axmlPrinter.setExtractPermissionDescription(true); // Add permission comments in manifest
axmlPrinter.setEnableID2Name(true); // Enable ID to name resolution (requires resources.arsc)

String xml = axmlPrinter.readFromFile(path);
```

### Custom Resource Mapping
To resolve custom resource IDs to names, place the `resources.arsc` file in the same directory as your binary XML file, or load it manually:
```java
InputStream arscStream = new FileInputStream("resources.arsc");
axmlPrinter.loadCustomResources(arscStream);
```

## Test the library
I injected my library in **MT Manager Mod** You can download the apk file from my Telegram Channel. [Download Now](https://t.me/Modder_Hub/906)
#### Sample
<img src="https://github.com/developer-krushna/AXMLPrinter/blob/main/IMG_20241223_122041_536.jpg" width="240" alt="Screenshot"/>
<img src="https://github.com/developer-krushna/AXMLPrinter/blob/main/IMG_20241223_122046_767.jpg" width="240" alt="Screenshot"/>

## License
+ Apache License V2.0 <http://www.apache.org/licenses/LICENSE-2.0>

## Contributing

1. Fork repository
2. Make changes
3. Ensure tests pass (or hopefully adding tests!)
4. Submit pull request/issue

## Thanks

+ XML Pull Parsing: <http://www.xmlpull.org/>
+ AXMLPrinter(Android4ME)
