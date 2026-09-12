# NetEase-Bujidao-BaiDuan-Launcher(网易布吉岛白端启动器)

**该项目可加Forge1.20.1模组，由于网易魔改的文件干扰，可能部分模组失效**

# 警告

*首次启动需要安装.NET Runtime 10.0普通版*，测试阶段可能出现Bug或者进不去游戏，如进不去请重新启动，如情况依旧请联系作者（下方联系方式）

**前言**：*<https://www.4399pc.cc/>* 获取4399账号或者Cookie。
*下载成品链接2（包括历史版本）：123云盘：<https://1817811933.share.123pan.cn/123pan/BqIvjv-DpYUh>*

# 联系方式：QQ号：3369682582 名称：锋锋 QQ群：1098356278

**介绍**：网易我的世界 Java 版整合包。通过魔改代理 `FantnelCli` 实现网易账号登录、联机代理、皮肤/头像显示，任何目录下双击 `start.cmd` 即可启动游戏并自动进服。**布吉岛脱盒整合包 —— 解压即玩，一键进服**
专为 `pc.bjdmc.net` 打造的网易 Java 版第三方整合包，双击 `start.cmd` 即可直接进入布吉岛服务器，彻底告别官方启动器限制。**已进入正常匹配池（非黑屋），零注入、零修改官方 Native DLL，纯启动器侧协议还原。**

**核心机制**：内置魔改代理 `FantnelCli`，单进程同时承担账号登录、联机转发（端口25565）与皮肤/mod 服务（端口9876），零端口冲突。支持密码/Cookie 登录、多账号管理、历史记录与账号删除，操作便捷。

**Native 模式（默认）**：以与官方一致的完整认证链启动游戏（`GameType:2` + 官方 51 文件 runtime），游戏 Native 层认为运行在官方环境，可进入正常匹配大厅。代理在 TCP 层使用 fantnelcli 的合法会话对接真实服务器，服务器直接认可，无需破解 Native 票据协议（9877 认证端口实际不会被调用）。

**皮肤系统**：几乎完整还原网易体验，真人玩家显示当前装备皮肤，Java/基岩版各自呈现对应皮肤，人机 NPC 直接读取服务器贴图。独创进服预热机制，解析 Tab 列表提前下载全员皮肤，进服瞬间头像皮肤即时显示。支持 `skin_me.png` 自定义皮肤，粗细手臂自由切换。就是需要等待一会儿才可以加载皮肤

**游戏侧**：注入网易 mod 补丁，修复人机皮肤、自动连服及崩溃问题。整合 JDK 17 与全部模组，配置全相对路径，拷盘即走，换电脑无需重装。

开袋即食，进服即玩。

## 快速开始

1. 双击 [start.cmd](start.cmd)（默认 Native 模式；`start.cmd proxy` 为旧休眠模式回退）
2. 首次启动按提示登录网易账号（支持账号密码 / Cookie 登录）
3. **先在 fantnelcli 窗口选好角色，游戏随后才会自动启动**（与官方启动器"先选角色后启动"一致）
4. 游戏加载完自动连接服务器（布吉岛 pc.bjdmc.net，经本地代理 127.0.0.1:25565）

> 加载阶段不要点游戏窗口（避免误触关闭）。进服后建议先挂机 2 分钟确认稳定。

## 启动流程（start.cmd，默认 native）

```
检查 JavaAC(官方 Temurin17.0.2 + 51 文件认证 runtime)
→ 启动 fantnelcli(代理+皮肤/mod 服务 9876)
→ fantnelcli: 登录 → 选角色 → 渲染 gameargs_native.txt 为 gameargs_runtime.txt
   (真实 uid / 角色名 / role uuid / token / 32字节 filterkey，并重写自加密过滤词库)
→ launch_native.ps1 将参数展开为【内联命令行】启动 java（Native DLL 通过
   GetCommandLine 读取参数，@argfile 会读不到）→ quickPlayMultiplayer 自动进服
```

- `-Druntime_path` 必须在游戏参数**之前**（JVM 参数顺序敏感）
- Native 模式必须用内联参数，不能用 `@gameargs.txt`（否则 X19 DLL preInit 抛 C++ 异常）
- 旧 proxy 模式：`java -Druntime_path=... @gameargs.txt`，休眠离线参数，只能进黑屋，仅作回退
- Native 认证服务（9877，AuthLibProtocol）已内置但当前链路不会触发；TCP 代理层已携带合法会话，不需要 CrcSalt

## Native 模式关键约束（踩坑总结）

| 约束 | 原因 |
| :-- | :-- |
| **C 盘剩余空间必须充足（建议 >2GB）** | 磁盘满时 Native 写临时文件/词库失败会自毁，崩溃签名表现为 ntdll c0000005（伪"堆损坏"），多次误判为参数问题 |
| filterkey 必须恰好 32 字节 | 游戏 AESHelper 长度不符时【不报错、原样返回密文】，Native 把密文当正则词库加载即崩。fantnelcli 自动从 token 派生（纯小写字母）并用同款 AES-128-CBC 自加密 fltmp 词库 |
| `--username`=角色名、`--uuid`=32位无横线 role uuid | 由 Skip32（密钥 SaintSteve）按 角色名+uid 生成，与 mod 同源；写死假 uuid 会伴随确定性崩溃 |
| `userProperties.gameid` 必须是可 int 解析的 `[0,0]` | GameHost 用 Gson 按 int 解析，填真实 64 位服务器 id 会 JsonSyntaxException；真实服务器 id 走 `-DlauncherGameId` |
| 端口分置：9876=mod/皮肤 socket（读 userProperties.launcherport），9877=Native 进服认证（读 -DlauncherControlPort） | 两套协议帧格式不同，不可共用一个端口 |
| 游戏异常退出后重启电脑再开 | 崩溃可能留下反作弊驱动挂住句柄的僵尸 java（任务管理器杀不掉），会导致官方启动器触发 Themida 拦截 |

## 目录结构

```
├── start.cmd              一键启动(默认 native；start.cmd proxy 回退旧模式)
├── launcher.log           启动器+游戏输出日志
├── Java\                  JDK 17 运行时(proxy 模式用)
├── JavaAC\bin\            官方 Temurin17.0.2 + 51 文件认证 runtime(native 模式用,
│                          含 X19 api-ms-win-crt-utility / libenvsdk / SecureEngine 等,
│                          必须与官方逐文件一致,勿替换/打补丁)
├── authproxy\
│   ├── gameargs.txt          旧 proxy 模式参数(休眠离线值)
│   ├── gameargs_native.txt   native 模式参数模板(含 __NEL_*__ 占位符)
│   ├── gameargs_runtime.txt  运行时由 fantnelcli 渲染生成(勿手改)
│   ├── launch_native.ps1     内联命令行启动器(native 必需)
│   ├── skins_local\          皮肤缓存目录(见下)
│   └── NetEaseProxy.java     旧代理源码(已废弃, 勿运行——会引发 token 冲突 code=22)
├── .minecraft\            游戏本体(主 mod 含皮肤补丁)
├── server_proxy\          fantnelcli 运行时(FantnelCli.dll/exe + Nirvana.*.dll)
│                          注意 gameargs_native.txt 模板需与此处副本保持同步
├── _build\                C# 源码工程(dotnet build)
├── _diag\fltmp_official_2003\  官方原版过滤词库参照文件
└── _decompile\            Java 反编译/注入工具(cfr.jar, compile.ps1, install.ps1)
```

## 皮肤系统

### 显示逻辑

| 玩家类型   | UUID         | 皮肤来源                            |
| :----- | :----------- | :------------------------------ |
| 网易真人玩家 | v4           | 2050 路径 → 网易 API（联机场景当前装备皮肤）    |
| 人机/NPC | 非 v4 (v3/v2) | 服务器下发的 textures 属性 → 无则 2050 兜底 |

- 按端严格对应：Java 玩家显示 Java 皮肤，基岩(Cpp)玩家显示基岩皮肤，不混用
- Alex(细手臂)皮肤默认按 Steve(粗手臂)回复
- 进服瞬间 fantnelcli 解析 Tab 列表包(0x3E)，**全员预热**查询+下载，2050 请求到达时磁盘命中秒回
- Tab 头像与身体皮肤同源（头像即皮肤渲染）

### skins\_local 查找优先级

1. `skin_me.png` — 自己的皮肤（粗手臂 Steve，mode=0）
2. `skin_me_slim.png` — 自己的皮肤（细手臂 Alex，mode=1，优先于上面）
3. `<角色名>.png` — 指定角色皮肤
4. 网易在线 API（结果缓存落盘 `skin_<id>.png`）
5. `skin_10000.png` — 默认皮肤

> 换皮肤只需替换对应 PNG 文件，无需重启（自己除外，见下）

### skin\_me 注意

自己的皮肤结果**永久缓存**在内存，替换 `skin_me.png` 后需重启 start.cmd 生效。

## fantnelcli 账号菜单

| 命令     | 功能                                                                                 |
| :----- | :--------------------------------------------------------------------------------- |
| 编号     | 选择已有账号登录（历史账号显示 UID）                                                               |
| `c`    | Cookie 登录：粘贴完整 Cookie JSON（`EntityX19Cookie`），或分行输入 sdkuid/sessionid/udid/deviceid |
| `d+编号` | 删除账号（输 `y` 确认）                                                                     |
| 退出时    | 显示"按回车键关闭窗口"防止窗口闪退                                                                 |

## 开发与部署

### C# 侧（fantnelcli）

```powershell
dotnet build _build\Nirvana.Cli\Nirvana.Cli.csproj -c Release
# 部署以下文件到 server_proxy\:
#   bin\Release\net10.0\FantnelCli.dll / FantnelCli.exe / FantnelCli.pdb
#   bin\Release\net10.0\Nirvana.Heypixel.dll / Nirvana.Heypixel.pdb   # 改了 Heypixel 项目时必须一并复制!
```

- 源码入口：`_build\Nirvana.Cli\Program.cs`（登录/账号管理/native 参数渲染 `TryRenderNativeArgs`、过滤词库生成 `EnsureNativeFilterFiles`、9877 认证服务 `TryStartNativeAuthLib`）、`SkinSocketServer.cs`（皮肤+mod socket）、`_build\Nirvana.Heypixel\Play\`（协议包解析）
- 只复制 exe 不更新 DLL 会跑旧逻辑——FantnelCli 与所有 Nirvana.*.dll 一并替换
- 当前 start.cmd 的 `NEL_ARGS_TEMPLATE`/`NEL_ARGS_OUTPUT` 都指向 `authproxy\`（模板 gameargs_native.txt → 渲染产物 gameargs_runtime.txt）；改模板只需改 authproxy 副本（server_proxy 里的同名副本为历史遗留，保持同步即可）
- fltmp 词库由 fantnelcli 每次选角后自动重写（路径见 `%LOCALAPPDATA%\Netease\MCLauncher\config\fltmp`，可用环境变量 `NEL_FILTER_PATH` 覆盖）；官方启动器若运行过会恢复成官方词库，下次 native 启动会再覆盖回来，无需处理

### Java 侧（mod 补丁）

- 主 mod：`.minecraft\mods\4681704866889354274@3@0.jar`（原始版备份为 .bak）
- 修改流程：`_decompile` 反编译 → 改 `src\com\netease\...` → `compile.ps1` 编译 → `install.ps1` 注入
- 注入后必须重启游戏（JVM 不会热加载 class）

## 常见问题

| 现象                       | 原因/解决                                                        |
| :----------------------- | :----------------------------------------------------------- |
| 进黑屋/无法正常匹配              | 误用了 `start.cmd proxy` 旧休眠模式；直接双击 start.cmd 走默认 native 模式         |
| 游戏加载到模型/纹理阶段闪退（exit -1073741819 / ntdll c0000005） | **先查 C 盘剩余空间（≥2GB）**；其次确认 fantnelcli 日志有 `filter 词库已重写`（32位 key）。这是磁盘满或过滤词库密钥长度不对导致的 Native 自毁，非随机崩溃 |
| 双击 start.cmd 游戏迟迟不启动     | native 模式要先在 fantnelcli 窗口完成登录+选角，脚本检测到 gameargs_runtime.txt 后才启动（最长等待10分钟） |
| 官方启动器报 Themida/保护系统拦截   | 上轮崩溃残留僵尸 java 句柄，重启电脑后再开；排查期间不要安装/运行调试器、转储监控等分析工具 |
| 4610 白名单查询日志报 EOFException | 已知非致命异常（回复体偏短），不影响进服，可忽略                                     |
| code=22 帐号在另一处登录         | 同时运行了旧 NetEaseProxy Java 进程，关闭它                              |
| 皮肤/逻辑改动没生效               | fantnelcli 未重启（DLL 热加载无效）或 jar 注入后未重启游戏                      |
| 启动报 UnsatisfiedLinkError | `-Druntime_path` 不在游戏参数之前；或误删/替换了 JavaAC\bin 官方 runtime 文件      |
| API 返回 dataCount=0       | 皮肤查询缺 game\_id 上下文（需 await LoginStartAndGameStart）或玩家确实没装备皮肤 |
| 闪退复现排查                   | 看 `.minecraft\logs\latest.log`（搜 `filterkey`/`load filter re`）、fantnelcli 窗口、根目录 launcher.log；Native 自毁不产生 hs_err |

