<div align="center">

# BDD Local Client

### 为 Minecraft 设计的本地心理恐怖视觉体验模组

![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-3C8527?style=flat-square&logo=minecraft&logoColor=white)
![Forge 47.4.10](https://img.shields.io/badge/Forge-47.4.10-orange?style=flat-square)
![Version 1.2.0-test6](https://img.shields.io/badge/version-1.2.0--test6-4C9AFF?style=flat-square)
![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Client Only](https://img.shields.io/badge/side-client--only-6C63FF?style=flat-square)

**BDD Local Client 1.2.0-test6** 是一个面向 Minecraft Java Edition 1.20.1 的 Forge 客户端模组测试构建。
它通过简洁的 HUD、OBS 录制状态反馈和本地会话统计，营造“玩家所见”与“观众所见”之间逐渐产生偏差的心理恐怖氛围。

</div>

> [!WARNING]
> 本模组包含心理恐怖主题、身体形象焦虑、被注视感和录制状态反馈等内容，不适合对相关主题敏感的玩家。
> 请在知情、同意的前提下用于直播或录制，并提前告知观众。

## ✦ 项目简介

BDD Local Client 的设计重点不是传统的怪物或跳脸惊吓，而是把“检查、注视、遮掩和自我怀疑”转化为轻量的客户端反馈：

- **平时保持克制**：不改变服务端逻辑，不添加实体、方块或世界生成内容。
- **录制时产生差异感**：模组连接本机 OBS WebSocket，并在 HUD 中显示录制/待机状态。
- **将异常限制在本地**：只读取明确允许的少量系统属性，不读取文件、剪贴板、网络身份或设备标识。
- **优先稳定性**：OBS 未启动、端口不可访问或虚拟音频设备不存在时，功能会静默回退，不阻塞游戏。

## ✅ 当前功能

| 功能 | 状态 | 说明 |
| --- | :---: | --- |
| 客户端专用加载 | ✅ | `mods.toml` 声明为 `clientSideOnly=true`，不会加载到专用服务端 |
| BDD 状态 HUD | ✅ | 在快捷栏区域显示 `BDD // OBS RECORDING` 或 `BDD // OBS STANDBY` |
| OBS WebSocket 5 监控 | ✅ | 连接 `127.0.0.1:<obsPort>`，通过 OBS 事件实时同步录制状态 |
| OBS 密码认证与状态恢复 | ✅ | 支持密码认证，并在连接时读取 OBS 当前录制状态 |
| OBS 设置引导 | ✅ | 首次打开游戏时自动引导，也可通过主菜单底部的录制图标重复打开；密码框掩码显示 |
| 断线安全回退 | ✅ | OBS 关闭、认证失败或连接断开时回到待机状态并后台重连 |
| 录制视觉反馈 | ✅ | OBS 录制时显示低强度、呼吸式的屏幕边缘反馈 |
| 会话统计 | ✅ | 统计录制 tick、可操作/无遮罩 tick 和检查次数 |
| 虚拟音频输出 | 🧩 | 已实现安全的 Mixer 查找与静默回退，等待上层事件触发音频内容 |
| 本地信息快照 | 🧩 | 仅提供用户名、操作系统和当前时间的内存快照 |
| FBO 分流诊断面板 | 🧪 | 测试模式开启时通过置顶 GUI overlay 显示高对比度玩家/OBS 面板，并直接报告模组观测到的 OBS 状态与 WebSocket 连接状态；用于验证 FBO 生命周期，不等同于 OBS 捕获分流 |
| 画面 FBO 分流 / 模型扭曲 | 🚧 | 原生交换缓冲钩子和观众专用画面仍在规划中 |

### HUD 示例

```text
BDD // OBS RECORDING
checks 0  covered 00:42
```

录制状态下，屏幕四边会出现轻微的暗红色脉动边缘；停止录制、OBS 不可访问或连接断开时，该反馈会自动消失。

## 🧱 技术架构

```text
BDDMod
├─ Config                       ForgeConfigSpec 客户端配置
├─ OBSMonitor                   本机 OBS WebSocket 5 状态监控
├─ client/
│  ├─ BDDSessionData            当前游戏会话统计
│  ├─ HiddenAudioPlayer         虚拟声卡输出基础设施
│  ├─ LocalInfoProvider         最小化本地信息快照
│  └─ event/ClientEventHandler  客户端 Tick 与 HUD 渲染
└─ resources/
   └─ META-INF/mods.toml        Forge 模组元数据
```

### 数据流

1. `BDDMod` 在客户端注册 `ForgeConfigSpec`，并启动 `OBSMonitor`。
2. `OBSMonitor` 使用随模组打包的阻塞式 WebSocket 客户端连接本机 OBS WebSocket 5 服务，完成握手、认证和录制状态请求。
3. OBS 的 `RecordStateChanged` 事件实时更新内存中的录制状态；连接异常时自动回退并重试。
4. `ClientEventHandler` 在客户端 Tick 中更新 `BDDSessionData`。
5. HUD 渲染事件读取当前状态，在快捷栏上方绘制状态文本、计时和录制边缘反馈。

### 隐私边界

`LocalInfoProvider` 当前只允许读取以下信息：

- `user.name`：当前系统用户名；
- `os.name`：操作系统名称；
- 当前时间。

模组不会主动读取或持久化 IP、MAC 地址、浏览器历史、文件内容、剪贴板、摄像头、麦克风或其他设备标识，也不会修改系统设置或删除文件。

## 🎮 安装与运行

### 运行环境

- Minecraft Java Edition **1.20.1**
- Minecraft Forge **47.4.10**（Forge 47.x，1.20.1）
- Java **17**
- 客户端环境；不支持将本模组安装到专用服务端

### 安装发行版

1. 从 GitHub Releases 或本地构建产物中获取 `bddmod-1.2.0-all.jar`。
2. 安装 Minecraft 1.20.1 对应的 Forge 47.x 客户端。
3. 将 JAR 放入 Minecraft 的 `mods` 文件夹：
   - Windows：`%APPDATA%\\.minecraft\\mods`
   - Linux：`~/.minecraft/mods`
   - macOS：`~/Library/Application Support/minecraft/mods`
4. 使用 Forge 1.20.1 配置启动游戏。

## 🛠️ 开发与构建

仓库已包含 Gradle Wrapper，请使用项目自带脚本，不需要预装 Gradle。

### Windows PowerShell

```powershell
# 编译 Java 源码
.\gradlew.bat compileJava

# 检查资源模板
.\gradlew.bat processResources

# 完整构建；可安装产物为 build/libs/bddmod-<版本>-all.jar
.\gradlew.bat build

# 启动开发客户端
.\gradlew.bat runClient
```

### Linux / macOS

```bash
./gradlew compileJava
./gradlew processResources
./gradlew build
./gradlew runClient
```

开发环境生成命令：

```bash
./gradlew genIntellijRuns   # IntelliJ IDEA
./gradlew genEclipseRuns    # Eclipse
./gradlew genVSCodeRuns     # Visual Studio Code
```

构建参数：

| 项目 | 版本 |
| --- | --- |
| Minecraft | `1.20.1` |
| Forge | `47.4.10` |
| ForgeGradle | `[6.0,6.2)` |
| Java Toolchain | `17` |
| 映射 | Mojang Official `1.20.1` |
| OBS WebSocket 运行时依赖 | 内置 `nv-websocket-client` 2.14，无需用户额外安装 |

## ⚙️ 配置

配置文件由 Forge 自动生成于：

```text
.minecraft/config/bddmod-client.toml
```

当前配置项如下：

| 配置项 | 默认值 | 作用 |
| --- | ---: | --- |
| `terrorModeEnabled` | `true` | 启用/停用 BDD HUD 与录制边缘反馈 |
| `renderRouteTestEnabled` | `true` | 启用/停用录制时的 FBO 分流测试面板 |
| `hiddenAudioVolume` | `0.35` | 虚拟音频输出音量，范围 `0.0`–`1.0` |
| `obsWebSocketPassword` | `""` | OBS WebSocket 5 密码；留空表示 OBS 未启用密码 |
| `obsSetupCompleted` | `false` | 是否已经完成首次 OBS 设置引导；引导中选择“稍后设置”也会结束本次首次提示 |
| `virtualAudioDeviceName` | `"VB-Audio,Voicemeeter,CABLE Input"` | 虚拟音频 Mixer 名称匹配关键词 |
| `obsPort` | `4455` | 连接 `127.0.0.1` 上的 OBS WebSocket 5 端口 |

设置引导保存后会立即请求后台重连 OBS；手动修改配置文件后请完全重启客户端，以确保所有配置值重新加载。

## 📹 OBS 与虚拟音频说明

### OBS

默认端口为 `4455`。模组使用内置的阻塞式 WebSocket 客户端连接 OBS WebSocket 5，避免依赖主机的 Java NIO Selector：

1. 在 OBS 中启用 WebSocket 服务，并确认端口与引导界面中的端口一致。
2. 首次打开 Minecraft 时，在引导界面填写 OBS 端口和密码；之后可通过主菜单底部、无障碍按钮旁的录制图标随时重新打开，悬停会显示 `OBS 设置`。连接地址固定为 `127.0.0.1`。密码框使用掩码显示，保存到客户端配置后不会写入日志。
3. 首次引导可以选择“稍后设置”；之后可从主菜单重新进入，也可在 `bddmod-client.toml` 中修改 `obsPort` 和 `obsWebSocketPassword`。
4. 启动 Minecraft 后，模组会请求当前录制状态，因此即使 OBS 先于 Minecraft 开始录制，HUD 也能恢复正确状态。
5. 录制开始/停止由 OBS 事件驱动更新；OBS 关闭或连接断开时，模组会显示 `OBS STANDBY` 并在后台重连。密码错误时，测试面板会显示 `AUTH FAILED`，并降低重试频率。

监控始终限制在 `127.0.0.1`，不会向外部服务发送数据。

### 虚拟音频

`HiddenAudioPlayer` 会枚举 Java Sound Mixer，并只尝试匹配配置中的虚拟设备关键词。找不到设备时会静默放弃，不会改用玩家的默认扬声器。

要测试虚拟音频链路，可使用 VB-CABLE 或 Voicemeeter，并在 OBS 中添加对应的“音频输入捕获”源。当前仓库版本尚未接入具体的低语、心跳或环境音触发事件。

## 🧪 验证清单

启动开发客户端后，可以按以下步骤检查基础功能：

1. 进入任意单人世界，确认左上角显示 `BDD // OBS STANDBY`。
2. 在 OBS 中启用 WebSocket 5，并确认端口和密码配置正确；若启用了 OBS 密码，必须将相同密码写入 `obsWebSocketPassword`。
3. 开始 OBS 录制，确认状态切换为 `OBS RECORDING`；停止录制后确认恢复为 `OBS STANDBY`。
4. 在 OBS 已经录制时启动 Minecraft，确认连接后 HUD 能恢复录制状态。
5. 观察录制状态下屏幕四边的低强度脉动反馈。
6. 关闭 OBS 或断开 WebSocket，确认 HUD 安全回退到待机状态。
7. 打开菜单或暂停界面，确认会话统计只在玩家处于游戏世界时更新。
8. 将 `terrorModeEnabled=false` 写入配置并重启，确认 HUD 与边缘反馈关闭。
9. 移除虚拟声卡后触发音频播放路径，确认游戏不会崩溃，也不会输出到默认扬声器。
10. OBS 开始录制后，确认出现绿色 `PLAYER VIEW` 和红色 `OBS TEST BUFFER` 测试面板；该面板只验证独立 FBO 生命周期，不验证 OBS 已捕获不同画面。

## 🗺️ 开发路线

- [x] 增强 OBS WebSocket 断线重连、错误提示和连接状态诊断。
- [ ] 接入 OBS 专用 FBO / RenderTarget 渲染链路。
- [x] 建立独立 TextureTarget 的创建、尺寸同步、写入和安全回退诊断路径。
- [ ] 增加仅对观众可见的局部模型变形、噪点和隐藏文字。
- [ ] 加入检查、回避、遮掩等行为的可验证触发器。
- [ ] 完成虚拟音频素材管理与事件驱动播放。
- [ ] 增加兼容 Iris/Oculus 等渲染扩展的回退策略。
- [ ] 补充自动化测试、运行截图和发行版工作流。

构建会同时生成不含内置依赖的开发薄包和带 `-all` 后缀的可安装包。安装时必须选择 `-all.jar`。

## 🆕 1.2.0 更新摘要

- OBS WebSocket 改用随模组打包的阻塞式传输，避开部分 Windows 主机上的 Java NIO Selector 初始化故障。
- 支持 OBS 录制开始/停止事件、密码认证、连接时的当前状态恢复和断线重连。
- OBS 关闭、认证失败或断线时自动回到 `OBS STANDBY`，并在后台重连。
- `CONNECTED` 只在 OBS 完成身份确认后显示，避免认证失败时短暂误报。
- `nv-websocket-client` 已包含在发行 JAR 中，用户不需要额外安装依赖。
- 当前正式版：`build/libs/bddmod-1.2.0-all.jar`。

## 📁 许可证与致谢

- 本项目自主编写的代码与资源采用 [WTFPL v2](LICENSE) 许可。
- Forge/MDK、内置依赖及其他第三方内容仍遵循各自的许可；相关声明保留在 `THIRD_PARTY_LICENSES.txt`、`CREDITS.txt` 和依赖包中。
- Minecraft、Minecraft Forge、OBS 和 VB-CABLE/Voicemeeter 均为其各自权利人的项目或商标，本项目与 Mojang、Microsoft、OBS Project 或相关音频软件开发者没有隶属关系。

## 💬 反馈与贡献

提交 Issue 时请尽量附上：

- Minecraft、Forge、Java 版本；
- 操作系统和显卡/渲染器信息；
- `latest.log` 中与 `bddmod` 相关的片段；
- 是否启用了 OBS、Iris/Oculus 或虚拟音频设备；
- 可稳定复现问题的最小步骤。

涉及心理恐怖内容的改进建议，请同时说明预期的玩家体验和内容警告需求。
