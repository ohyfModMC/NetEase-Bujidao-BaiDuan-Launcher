# NetEase-Bujidao-BaiDuan-Launcher(网易布吉岛白端启动器)
**该项目可加Forge1.20.1模组，由于网易魔改的文件干扰，可能部分模组失效**

# 警告
*首次启动需要安装.NET Runtime 10.0普通版*，测试阶段可能出现Bug或者进不去游戏，如进不去请重新启动，如情况依旧请联系作者（下方联系方式）

# 联系方式：QQ号：3369682582 名称：锋锋

**前言**：*https://api.4399pc.cc/convert_cookies.php* 获取4399账号或者Cookie。
*下载成品链接2：123云盘：*

**介绍**：网易我的世界 Java 版整合包。通过魔改代理 `FantnelCli` 实现网易账号登录、联机代理、皮肤/头像显示，任何目录下双击 `start.cmd` 即可启动游戏并自动进服。**布吉岛脱盒整合包 —— 解压即玩，一键进服**
专为 `pc.bjdmc.net` 打造的网易 Java 版第三方整合包，双击 `start.cmd` 即可直接进入布吉岛服务器，彻底告别官方启动器限制。

**核心机制**：内置魔改代理 `FantnelCli`，单进程同时承担账号登录、联机转发（端口25565）与皮肤服务（端口9876），零端口冲突。支持密码/Cookie 登录、多账号管理、历史记录与账号删除，操作便捷。

**皮肤系统**：几乎完整还原网易体验，真人玩家显示当前装备皮肤，Java/基岩版各自呈现对应皮肤，人机 NPC 直接读取服务器贴图。独创进服预热机制，解析 Tab 列表提前下载全员皮肤，进服瞬间头像皮肤即时显示。支持 `skin_me.png` 自定义皮肤，粗细手臂自由切换。就是需要等待一会儿才可以加载皮肤

**游戏侧**：注入网易 mod 补丁，修复人机皮肤、自动连服及崩溃问题。整合 JDK 17 与全部模组，配置全相对路径，拷盘即走，换电脑无需重装。

开袋即食，进服即玩。

## 快速开始

1. 双击 [start.cmd](start.cmd)
2. 首次启动按提示登录网易账号（支持账号密码 / Cookie 登录）
3. 游戏自动连接服务器（布吉岛 pc.bjdmc.net）

## 启动流程（start.cmd）

```
检查 Java → 启动 fantnelcli(代理+皮肤服务器) → 等待 9876 端口就绪
→ java -Druntime_path=... @gameargs.txt 启动游戏 → 自动进服
```

- `-Druntime_path` 必须在 `@gameargs.txt` **之前**（JVM 参数顺序敏感）
- gameargs.txt 全部为相对路径，`--server 127.0.0.1 --port 25565` 自动连服

## 目录结构

```
├── start.cmd              一键启动
├── launcher.log           启动器+游戏输出日志
├── Java\                  JDK 17 运行时
├── authproxy\
│   ├── gameargs.txt       游戏启动参数(69行)
│   ├── skins_local\       皮肤缓存目录(见下)
│   └── NetEaseProxy.java  旧代理源码(已废弃, 勿运行——会引发 token 冲突 code=22)
├── .minecraft\            游戏本体(主 mod 含皮肤补丁)
├── server_proxy\          fantnelcli 运行时(FantnelCli.dll/exe + Nirvana.Heypixel.dll)
├── _build\                C# 源码工程(dotnet build)
└── _decompile\            Java 反编译/注入工具(cfr.jar, compile.ps1, install.ps1)
```

## 皮肤系统

### 显示逻辑

| 玩家类型   | UUID         | 皮肤来源                            |
| ------ | ------------ | ------------------------------- |
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
| ------ | ---------------------------------------------------------------------------------- |
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

- 源码入口：`_build\Nirvana.Cli\Program.cs`（登录/账号管理）、`SkinSocketServer.cs`（皮肤服务器）、`_build\Nirvana.Heypixel\Play\`（协议包解析）
- 只复制 exe 不更新 DLL 会跑旧逻辑——两个都要替换

### Java 侧（mod 补丁）

- 主 mod：`.minecraft\mods\4681704866889354274@3@0.jar`（原始版备份为 .bak）
- 修改流程：`_decompile` 反编译 → 改 `src\com\netease\...` → `compile.ps1` 编译 → `install.ps1` 注入
- 注入后必须重启游戏（JVM 不会热加载 class）

## 常见问题

| 现象                       | 原因/解决                                                        |
| ------------------------ | ------------------------------------------------------------ |
| code=22 帐号在另一处登录         | 同时运行了旧 NetEaseProxy Java 进程，关闭它                              |
| 皮肤/逻辑改动没生效               | fantnelcli 未重启（DLL 热加载无效）或 jar 注入后未重启游戏                      |
| 启动报 UnsatisfiedLinkError | `-Druntime_path` 不在 @gameargs.txt 之前                         |
| API 返回 dataCount=0       | 皮肤查询缺 game\_id 上下文（需 await LoginStartAndGameStart）或玩家确实没装备皮肤 |

