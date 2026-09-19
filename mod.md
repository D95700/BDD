你是一名资深 Minecraft Forge 模组开发者，精通 ForgeGradle 构建系统、Forge 双事件总线（ModEventBus / ForgeEventBus）、Mixin 字节码注入、OpenGL 渲染管线、Java 音频系统、OBS WebSocket 协议。请为我编写一个完整的 Minecraft Forge 客户端模组项目。这个模组是一个心理恐怖体验作品，主题是“躯体变形障碍（BDD，俗称惧陋症/丑陋恐惧症）”。

## 一、项目目标

制作一个 Forge 客户端模组（Minecraft Java Edition 1.20.1），实现以下核心能力：

1. 检测玩家后台是否正在运行 OBS 录制。
2. 当 OBS 正在录制时，让 OBS 捕获到的画面与玩家自己看到的画面不同：玩家看到正常的自己，OBS 观众看到被扭曲、丑化、局部放大的角色模型，以及隐藏弹幕文字。
3. 通过虚拟音频设备播放只有 OBS 能捕获、玩家听不到的隐藏音频（低语、嘲笑、心跳、叹息）。
4. 融入 BDD 心理机制：选择性注意、局部放大、检查/回避循环、被注视恐惧、安全行为悖论、羞耻与自我厌恶、认知模糊。
5. 所有效果无须提供“恐怖模式”总开关，玩家不可随时关闭全部效果。

## 二、技术栈与版本

- Minecraft：1.20.1
- 模组加载器：Forge 47.x（1.20.1 对应 Forge 47.1.x 及以上）
- Java：17
- 构建系统：ForgeGradle 6.x，Gradle 8.x
- Mixin：Forge 原生支持 Mixin，需在 build.gradle 中配置 mixin 依赖
- OBS 通信：obs-websocket-java（OBS 28+ 内置 WebSocket，默认端口 4455）
- 音频：Java Sound API（javax.sound.sampled），枚举系统 Mixer，选择 VB-Cable / Voicemeeter 虚拟设备
- 画面分流：FBO（帧缓冲对象）切换，使用 com.mojang.blaze3d.pipeline.Framebuffer
- 着色器：Forge 的 RegisterShadersEvent 注册 ShaderInstance，创建自定义 RenderType
- 配置：ForgeConfigSpec + Cloth Config（配置界面）
- 可选：Mod Menu / Catalogue 集成

## 三、Forge 项目结构

请按以下结构组织项目：
src/main/java/com/yourname/bddmod/
├── BDDMod.java // 主模组类，@Mod 注解
├── Config.java // ForgeConfigSpec 配置
├── client/
│ ├── OBSMonitor.java // OBS WebSocket 连接与状态检测
│ ├── HiddenAudioPlayer.java // 虚拟音频设备播放
│ ├── LocalInfoProvider.java // 本地信息安全读取
│ ├── BDDSessionData.java // 运行时统计数据
│ ├── render/
│ │ ├── BDDRenderTypes.java // ShaderInstance 注册与 RenderType
│ │ ├── DistortionRenderer.java // FBO 分流渲染
│ │ └── HiddenCommentRenderer.java // 隐藏弹幕文字
│ └── event/
│ └── ClientEventHandler.java // Forge 事件订阅
├── mixin/
│ ├── GameRendererMixin.java
│ ├── PlayerRendererMixin.java
│ ├── InGameHudMixin.java
│ └── LookControlMixin.java // 实体注视行为
└── util/
└── ModConfigIntegration.java // Cloth Config 集成

src/main/resources/
├── META-INF/
│ └── mods.toml // Forge 模组声明文件
├── bddmod.mixins.json // Mixin 配置
├── assets/bddmod/
│ ├── shaders/
│ │ ├── distortion.json
│ │ ├── distortion.vsh
│ │ └── distortion.fsh
│ ├── sounds/
│ │ ├── whisper.ogg
│ │ ├── laugh.ogg
│ │ └── heartbeat.ogg
│ └── lang/
│ ├── en_us.json
│ └── zh_cn.json
└── pack.mcmeta

text

## 四、Forge 构建配置

请提供 build.gradle、settings.gradle、gradle.properties、mods.toml、bddmod.mixins.json、pack.mcmeta 的完整内容。关键点：

- 使用 `net.minecraftforge.gradle` 版本 `[6.0,6.2)`。
- 使用 `org.spongepowered.mixin` 版本 `0.7.+`。
- Minecraft 依赖：`net.minecraftforge:forge:1.20.1-47.1.0`。
- obs-websocket-java 依赖：`io.obs-websocket.community:client:2.0.0`。
- Cloth Config 依赖：`me.shedaniel.cloth:cloth-config-forge:11.1.106`，使用 `fg.deobf`。
- Mixin annotationProcessor：`org.spongepowered:mixin:0.8.5:processor`。
- mods.toml 中声明 `side="CLIENT"`，并声明对 cloth_config 的可选依赖。
- mixins.json 中列出所有客户端 Mixin 类。

## 五、核心模块与功能需求

### 模块 1：OBS 录制状态检测

- 使用 obs-websocket-java 连接 localhost:4455。
- 支持密码配置，通过 ForgeConfigSpec 读取。
- 注册 RecordStateChangedEvent 监听器，实时更新录制状态。
- 如果 OBS 未运行、未开启 WebSocket 或连接失败，必须静默失败，不影响游戏。
- 提供 OBSMonitor 单例，暴露 `isRecording()`。
- 在 `@Mod` 类的构造函数中初始化，确保在客户端专用线程上运行。
- 使用 DistExecutor 或 `@Mod.EventBusSubscriber(value = Dist.CLIENT)` 确保仅客户端加载。

### 模块 2：画面分流与 OBS 专用渲染

这是最核心也最复杂的模块。Forge 1.20.1 的渲染管线与 Fabric 有显著差异，请按以下方式实现：

**FBO 管理**：使用 `com.mojang.blaze3d.pipeline.Framebuffer` 创建独立的帧缓冲对象。
- 调用 `beginWrite(true)` 绑定 FBO 并设置视口。
- 调用 `blitToScreen(width, height, false)` 将 FBO 内容绘制到屏幕。
- 参考 Streamproof 的 FBO 切换逻辑。

**着色器注册**：通过 Forge 的 `RegisterShadersEvent` 注册自定义 `ShaderInstance`。
- 在 ModEventBus 上订阅 `RegisterShadersEvent`。
- 创建 `ShaderInstance`，指向 `assets/bddmod/shaders/` 下的 shader json。
- 使用 `ShaderStateShard` 包装 `ShaderInstance`，创建自定义 `RenderType`。

**顶点扭曲着色器**：
- 顶点着色器（distortion.vsh）接收位置和纹理坐标，对特定骨骼部位（头、鼻、下巴）应用局部放大、拉长、倾斜变换。
- 片段着色器（distortion.fsh）叠加噪点、色偏、涂抹效果。
- 通过 uniform 变量传递“缺陷部位”的位置和放大强度。

**Mixin 注入点**：
- `GameRendererMixin`：注入 `render` 方法的 HEAD 和 RETURN，管理 FBO 绑定/解绑。
- `PlayerRendererMixin`：注入 `render` 方法，在 OBS 录制时替换玩家模型渲染为扭曲版本。
- `InGameHudMixin`：注入 `render` 方法，在 cleanFramebuffer 上叠加隐藏弹幕文字。

**Forge 事件订阅**：
- 使用 `@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)` 注册着色器。
- 使用 `@SubscribeEvent` 监听 `RenderLevelStageEvent` 控制渲染时机。

### 模块 3：隐藏音频注入

- 使用 `AudioSystem.getMixerInfo()` 枚举系统混音器。
- 查找名称包含 “CABLE Input”、“VB-Audio”、“Voicemeeter” 的虚拟设备。
- 将音频直接播放到该虚拟设备，OBS 添加“音频输入捕获”源监听它。
- 玩家默认扬声器听不到。
- 如果未安装虚拟声卡，回退为不播放隐藏音频，并在日志中提示。
- 播放内容：低语、冷笑、叹息、心跳脉动，音量随“丑陋值”增加。
- 在客户端专用线程上播放音频，避免阻塞游戏主线程。
- 使用 ForgeConfigSpec 配置虚拟设备名称匹配规则和音量。

### 模块 4：BDD 心理机制

请实现以下机制，每个机制对应一个 BDD 病理特征：

1. **选择性放大镜**：玩家看向自己或镜子时，自认“缺陷”部位（如鼻子、下巴）被视觉放大，周围模糊。越聚焦放大越强。通过顶点着色器实现局部缩放。
2. **镜子的双面性**：OBS 未录制时镜子显示正常；OBS 录制时显示扭曲。长时间不照镜子，屏幕出现渐进模糊和噪点（通过后处理链实现）。
3. **被观看反馈**：OBS 录制时，村民/实体的视线更频繁朝向玩家，停留更久。通过 Mixin 注入 `LookControl#setLookAt` 方法，在 OBS 录制时增加注视玩家的概率和持续时间。
4. **安全行为循环**：玩家戴头盔、穿高领、换皮肤等“遮掩”行为短暂降低扭曲，但遮掩时长被统计（使用 BDDSessionData），停止遮掩后扭曲反弹更强。游戏内书籍出现：“你今天遮住了。你觉得他们看不出来吗？”
5. **共病情绪**：间歇性“脑雾”——屏幕边缘模糊、文字难读、物品栏交互延迟，OBS 录制时更频繁。通过客户端侧的后处理滤镜实现，不影响服务端游戏逻辑。
6. **观众弹幕**：仅在 OBS 捕获画面上显示，根据玩家行为触发，例如照镜子超过 3 秒、遮掩、长时间静止、快速转头。弹幕文字通过 HiddenCommentRenderer 渲染到 cleanFramebuffer 上。
7. **双视角结局**：当“检查次数 + 遮掩时长 + OBS 录制时长”达到阈值，玩家屏幕角色变模糊，OBS 画面角色正常，并显示：“她/他其实一直都很正常。是她的/他的眼睛在说谎。”（所有弹幕使用英文）

### 模块 5：本地信息安全读取

允许读取以下信息：
- `System.getProperty("user.name")` 系统用户名
- `System.getProperty("os.name")` 操作系统
- 当前时间
- 游戏内统计：死亡次数、游戏时长、挖掘方块数、上次死亡坐标等
- 读取 IP、MAC 地址、浏览器历史、文件内容、剪贴板、摄像头、麦克风
- 发送任何网络请求
- 将信息写入日志或持久化存储
- 修改系统设置、删除文件、干扰其他程序

### 模块 6：伦理与配置

- 使用 ForgeConfigSpec 提供配置项：
  - `terrorModeEnabled`（恐怖模式总开关）
  - `hiddenAudioVolume`（隐藏音频音量）
  - `obsWebSocketPassword`（OBS WebSocket 密码）
  - `virtualAudioDeviceName`（虚拟音频设备名称）
- 使用 Cloth Config 或 Forge 内置 ConfigurationScreen 创建配置界面。


## 六、Forge 特有注意事项

1. **双事件总线**：ModEventBus 用于生命周期事件（注册、设置、着色器），ForgeEventBus 用于游戏事件（渲染、tick、输入）。确保将事件处理器注册到正确的总线上。
2. **DistExecutor**：所有客户端专用代码必须通过 `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)` 或 `@Mod.EventBusSubscriber(value = Dist.CLIENT)` 确保仅在客户端加载。
3. **Mixin 重映射**：Forge 需要 refmap 来处理混淆映射。确保 annotationProcessor 正确配置，并在 build.gradle 中设置 `mixin.env.remapRefMap` 和 `mixin.env.refMapRemappingFile`。
4. **兼容性**：注意与 OptiFine、Iris/Oculus 着色器加载器的兼容性。在自定义着色器中避免使用与光影包冲突的 uniform 变量名。可以考虑检测 Iris 是否加载，若加载则禁用 FBO 分流，回退到简单的文字叠加方案。
5. **Forge 版本**：使用 Forge 47.1.0 或更高版本，确保与 1.20.1 完全兼容。
6. **Java 17 兼容**：确保所有代码使用 Java 17 语法，但避免使用 Java 21 的特性。
7. **渲染线程安全**：所有 OpenGL 调用必须在渲染线程上执行。使用 `Minecraft.getInstance().execute()` 将非渲染线程的操作调度到主线程。

## 七、交付要求

请按以下格式输出：

1. **项目架构说明**：包结构、主要类、Mixin 配置、数据流、Forge 事件总线分配。
2. **完整项目文件**：
   - build.gradle
   - settings.gradle
   - gradle.properties
   - mods.toml
   - bddmod.mixins.json
   - pack.mcmeta
   - 主要 Java 类：BDDMod、Config、OBSMonitor、HiddenAudioPlayer、LocalInfoProvider、BDDSessionData、BDDRenderTypes、DistortionRenderer、HiddenCommentRenderer、ClientEventHandler、ModConfigIntegration，以及所有 Mixin 类（GameRendererMixin、PlayerRendererMixin、InGameHudMixin、LookControlMixin）。
   - 着色器文件：distortion.json、distortion.vsh、distortion.fsh。
   - 语言文件：en_us.json、zh_cn.json。
3. **代码注释**：关键逻辑必须写中文注释，解释对应的 BDD 机制和 Forge 特有实现。
4. **构建说明**：如何编译出 jar，如何安装，如何配置 OBS 和 VB-Cable。
5. **测试用例**：如何验证 OBS 检测、画面分流、隐藏音频、恐怖模式开关。
6. **回退方案**：OBS 未连接、虚拟声卡未安装、着色器编译失败、Iris 加载时的处理。
7. **依赖版本表**：列出所有依赖的精确版本号。

## 八、参考项目

- Streamproof：FBO 切换隐藏 HUD，https://github.com/xNasuni/streamproof
- OBS Overlay：叠加层隐藏 HUD，https://github.com/zziger/obs-overlay
- OBS Recording Monitor：检测 OBS 录制状态，https://github.com/yoima-jp/OBS-Recording-Monitor
- obs-websocket-java：https://github.com/obs-websocket-community-projects/obs-websocket-java
- Gigaherz 的 Forge 自定义着色器示例：https://gist.github.com/gigaherz/b8756ff463541f07a644ef8f14cb10f5
- CustomPlayerModels（Forge 1.20.1 Mixin 玩家渲染参考）：https://github.com/tom5454/CustomPlayerModels

## 九、重要约束

- 代码必须可编译、可运行，针对 Forge 1.20.1 + Java 17。
- 优先保证玩家自己的屏幕体验正常，OBS 画面才出现恐怖效果。
- 所有效果必须可关闭。
- 不得包含任何恶意、侵犯隐私、上传数据的功能。
- 如果代码量过大，请先给出核心模块的完整实现（OBS 检测、FBO 分流、隐藏音频、恐怖模式配置），再说明扩展模块的实现思路。
- 请先输出架构设计，再逐文件输出代码。
- 注意 Forge 与 Fabric 的 API 差异，不要混用。