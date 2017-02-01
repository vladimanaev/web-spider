# web-spider

Server side web crawler allowing full page render crawl using HtmlUnit with many configurations and parallelism.

Dependencies
=============
```
<dependency>
    <groupId>com.vladimanaev</groupId>
    <artifactId>web-spider</artifactId>
    <version>1.0.0</version>
</dependency>
```

If your project is built with Gradle add following to your gradle setting file:
```
compile 'com.vladimanaev:web-spider:1.0.0'
```

Tips:
=====
HtmlUnit package can create a lot of unnecessary logs here is a way to prevent it in case you using SpringBoot logback.xml or any other log4j configuration xml.
```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <Logger name="org.springframework" level="INFO"/>
    <Logger name="com.vmsr" level="INFO"/>
    <Logger name="com.gargoylesoftware.htmlunit" level="OFF"/>
    <Logger name="org.apache.http.impl.execchain" level="OFF"/>
</configuration>
```