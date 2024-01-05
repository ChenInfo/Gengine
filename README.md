# ⚙ Gengine

[English Version](README.en.md)

> 赋能分布式内容管理与转换

## 概述

Gengine 是一个分布式的内容和文件调查及操作框架，旨在以高扩展性处理资源密集型任务。通过利用消息队列和轻量级节点，Gengine 高效地处理以下常见的底层任务：

- 内容变更处理
- 转码转换
- 元数据提取
- 哈希值/校验和计算

在用户生成内容（UGC）爆发的时代，以及数字化转型的浪潮中，管理和处理海量的多媒体内容对各行各业都至关重要，包括媒体、娱乐、社交平台和云服务等。Gengine 为高效应对这些挑战提供了强大的赋能平台。

## 主要特性

- **高扩展性**：架构设计支持横向扩展，高效处理大量数据和任务。
- **模块化设计**：由可混合搭配的模块化组件组成，适应不同需求。
- **可扩展性**：支持多种消息系统和路由机制，能与 S3、WebDAV 等不同的内容源集成。
- **分布式处理**：利用轻量级节点进行分布式处理，适合云计算和微服务架构。

## 应用场景与行业

Gengine 的功能使其在以下场景中大有可为：

- **UGC 平台的多媒体处理**：高效处理用户上传的媒体的转码、压缩和转换，轻松应对短视频、直播等业务需求。
- **数字资产管理**：为媒体和娱乐行业管理和处理数字内容，支持高清、超高清等媒资的处理。
- **云服务**：为基于云的内容服务提供后端处理，包括存储和检索优化，实现海量文件的秒传等功能。
- **电商平台**：优化产品图片和视频在网页和移动端的展示效果，提高用户体验，提升转化率。
- **大数据与分析**：为分析预处理大型数据集，包括元数据提取和内容转换，为 AI 和机器学习提供数据支撑。
- **医疗健康**：安全高效地处理和管理医学影像数据，支持远程会诊和 AI 辅助诊断。

## 架构设计

### 客户端

客户端生成并发送包含源内容引用和必要选项的任务消息。内容引用可以是：

- 共享卷上的文件
- Amazon S3 路径
- CMIS 文档 ID
- WebDAV URL，等等。

### 路由

消息路由系统将请求定向到适当的队列，由节点进行处理。

### 节点

任务节点引导一个或多个组件，监听相关队列上的消息，并调用工作者对内容执行任务。

### 工作者

工作者执行实际的处理任务，如内容转换、元数据提取或哈希计算。

![architecture](/architecture-trans-node.png "Example architecture of a simple image transform node")

消息也可以通过其他方式产生，可以插入不同的消息系统或路由，或者节点工作者可以本地运行，无需消息传递。

## 项目模块

### 公共模块

- **gengine-commons**：内容定义、处理和实用操作的最小基础库。

### 内容处理器

- **gengine-content-handler-s3**：处理从 Amazon S3 读取和写入文件。
- **gengine-content-handler-webdav**：处理从 WebDAV 服务器读取和写入文件。
- **gengine-content-handler-tempfile**：管理用于处理的临时文件。

### 消息模块

- **gengine-messaging-commons**：定义通用的消息生产者和消费者接口，支持 JSON 序列化。
- **gengine-messaging-camel**：基于 Apache Camel 的消息生产者实现。
- **gengine-messaging-amqp-direct**：基于 Qpid 的 AMQP 端点，用于生产和消费消息。
- **gengine-messaging-benchmark**：用于对消息代理进行基准测试的工具。

### 转码转换

- **gengine-transform-commons**：内容转换器的转换选项对象定义。
- **gengine-transform-messaging**：转换请求和回复对象的定义。
- **gengine-transform-worker-ffmpeg**：集成 FFmpeg 进行媒体转换。
- **gengine-transform-worker-imagemagick**：集成 ImageMagick 进行图像转换。
- **gengine-transform-component**：内容转换器节点的基础。

### 哈希计算

- **gengine-hash-commons**：内容哈希工作者的定义。
- **gengine-hash-messaging**：哈希请求和回复对象的定义。
- **gengine-hash-worker-javase**：Java SE 实现的文件哈希计算。
- **gengine-hash-component**：内容哈希组件的基础。

### 节点模块

- **gengine-node-simple**：引导组件形成可执行的节点来处理消息。

### 健康监测

- **gengine-health-commons**：用于监测系统组件健康状态的接口和实现。

### 消息代理

- **gengine-messaging-broker-activemq**：使用单个 Maven 命令启动 ActiveMQ 消息代理，方便测试和开发。

## 快速开始

要开始使用 Gengine，请按照以下步骤操作：

1. **克隆仓库**

   ```bash
   git clone https://github.com/ChenInfo/Gengine.git
   ```

2. **构建项目**

   进入根目录，使用 Maven 构建项目：

   ```bash
   mvn clean install
   ```

3. **启动消息代理**

   启动一个 AMQP 消息代理，例如 ActiveMQ：

   ```bash
   cd gengine-messaging/messaging-broker-activemq
   mvn activemq:run
   ```

4. **配置并运行节点**

   使用提供的配置文件配置节点并运行，例如，运行一个简单节点：

   ```bash
   cd gengine-node-simple
   java -jar target/gengine-node-simple-0.X-SNAPSHOT-jar-with-dependencies.jar path/to/config.properties
   ```

5. **使用您的客户端发送消息，或使用提供的基准测试工具**

## 贡献指南

感谢您对 Gengine 感兴趣并希望贡献力量！欢迎提交 Pull Request、报告问题或提出建议，以帮助改进项目。

### 报告问题

请在本仓库的 [GitHub Issues](https://github.com/ChenInfo/Gengine/issues) 部分报告问题。

## 许可证

本项目采用 LGPL-3.0 许可证。详见 [LICENSE](LICENSE) 文件。
