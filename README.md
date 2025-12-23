# SimpleTpaRTM

## 项目简介

SimpleTpaRTM 是一个基于 Fabric 的 Minecraft 服务端模组，提供简单的玩家传送请求（/tpa）相关功能与配置管理。

## 特性

- 简单的传送请求（TPA）功能
- 可定制的全局配置（位于 `config/tpa_global_config.toml` 或运行目录下的 `config/` 文件夹）

## 快速开始（Windows PowerShell）

在项目根目录下使用 Gradle Wrapper：

```powershell
# 构建项目（生成 jar）
.\gradlew.bat build

## 配置（详细）

项目使用一个基于注解的配置加载器（参见 `src/main/java/fun/bm/simpletpartm/configs/ConfigManager.java`），初始化时会扫描包 `fun.bm.simpletpartm.configs.modules` 下的模块并将配置文件放在运行目录下的 `config/`（或 `run/config/`）目录。下面是从源码提取的可配置字段与默认值，字段名以代码中 `@ConfigInfo(name = "...")` 为准：

- 模块：`core`（对应 `CoreConfig`）
  - `enable-cooldown` (boolean) — 是否启用传送冷却（包括 /tpa 请求与 /back）：默认 `false`
  - `cooldown-time` (int) — 冷却时间（秒，除 tpHere 外）：默认 `5`
  - `cleanup-interval-ticks` (int) — 清理缓存的间隔（ticks），默认 `20 * 60 * 15 = 18000`（约 15 分钟）

- 模块：`tpa`（对应 `TpaConfig`）
  - `request-expire-time` (int) — TPA 请求过期时间（秒）：默认 `120`
  - `enable-stand-still` (boolean) — 是否启用“静止等待”机制（在接收方确认时要求请求方静止）：默认 `false`
  - `stand-still-time` (int) — 静止等待时长（秒）：默认 `3`

- 模块：`tphere`（对应 `TpHereConfig`）
  - `request-expire-time` (int) — TPHERE 请求过期时间（秒）：默认 `120`
  - `enable-stand-still` (boolean) — 是否启用静止等待：默认 `false`
  - `stand-still-time` (int) — 静止等待时长（秒）：默认 `3`

- 模块：`back`（对应 `BackConfig`）
  - `back-expire-time` (int) — /back 返回点过期时间（秒）：默认 `120`
  - `enable-stand-still`、`stand-still-time` — 与 TPA 相同的静止等待相关设置，默认 `false` / `3`

注意：配置加载由 `ConfigsInstance.of(new File("config"), "tpa", "fun.bm.simpletpartm.configs.modules")` 完成，因此请将配置文件放在运行目录的 `config/` 下，配置项通常以模块名为顶级命名空间或文件名（实际生成的 TOML/配置文件请以 `config/` 中生成的样例为准）。

示例（伪 TOML，供参考）：

```toml
[core]
enable-cooldown = false
cooldown-time = 5
cleanup-interval-ticks = 18000

[tpa]
request-expire-time = 120
enable-stand-still = false
stand-still-time = 3

[tphere]
request-expire-time = 120
enable-stand-still = false
stand-still-time = 3

[back]
back-expire-time = 120
enable-stand-still = false
stand-still-time = 3
```

## 命令参考（tpHere / TPHERE 相关）

- /tpHere
  - 语法：`/tpHere` 或 `/tpHere <player>`
  - 执行者（命令源）：目标玩家（被请求的目的地，代码中称为 `into`）
  - 行为：
    - 若不带参数：将目标玩家设置为接收来自所有在线玩家的请求（内部标记 `tpHereToAll` 为 true），并向服务器上所有其他在线玩家发送一个“请求传送到你”的通知。
    - 若带 `<player>` 参数：向指定玩家发送一个将其传送到目标玩家的请求；内部将 `tpHereToAll` 设为 false。
  - 消息：向请求方与目标方发送提示，提示可使用 `/tpHereCancel` 取消请求。
  - 适用场景：目标玩家想让其他玩家传送到自己（全体或指定玩家）。

- /tpHereAccept
  - 语法：`/tpHereAccept` 或 `/tpHereAccept <player>`
  - 执行者（命令源）：发出请求的玩家（代码中称为 `from`）用来接受传送
  - 行为：
    - 若无参数，则接受最近的对自己发出的 tpHere 请求（通过 `TeleportDataManager.getLastTpHereRequest` 查找）。
    - 校验请求是否存在且未过期（由 `TpHereConfig.request-expire-time` 控制，默认 120 秒）。
    - 在接受时会检查冷却（如果 `core.enable-cooldown = true`）；若启用静止等待（stand-still），会要求请求方静止若干秒（代码使用 `TpaConfig.getStandStillTime()` 来决定静止时长，默认 3 秒），并通过 `TeleportScheduler` 做倒计时与最终传送。
    - 传送成功后会调用 `TeleportDataManager.reportTeleportedData` 记录返回点并清理 pos 存储。
  - 注意：代码中对静止等待时间的读取位置有所混用（`TpaConfig.getStandStillTime()`），因此若想打开静止等待，请同时检查 `tpa`/`tphere` 的相关配置。

- /tpHereDeny
  - 语法：`/tpHereDeny` 或 `/tpHereDeny <player>`
  - 行为：拒绝最近或指定玩家的 tpHere 请求，移除请求数据并向双方发送通知。

- /tpHereDenyAll
  - 语法：`/tpHereDenyAll`
  - 行为：拒绝对该玩家的所有 tpHere 请求（遍历 `TeleportDataManager.getTpHereRequests(from)` 并对每个调用拒绝逻辑）。

- /tpHereCancel
  - 语法：`/tpHereCancel` 或 `/tpHereCancel <player>`
  - 执行者：目标玩家（被请求的目的地）用来取消之前发送的 tpHere 请求
  - 行为：
    - 若目标处于 `tpHereToAll` 模式（接受来自所有人的请求），则会尝试取消对所有请求方的请求。
    - 否则仅取消最近的那个请求或指定玩家的请求。
    - 向请求方与目标方发送对应的通知。

通用说明
- 请求超时：tpHere 与 tpa 的请求超时时间各自独立，默认均为 120 秒（`request-expire-time`）；代码在接收/接受命令时会根据时间戳判断是否过期。
- 冷却：如果在 `core.enable-cooldown` 打开时，部分接受命令（如 `/tpHereAccept`）会调用 `CoreConfig.checkCooldown` 检查上一次传送时间并返回剩余秒数以阻止立即再次传送。
- 清理：运行时会在服务器 tick 的 END_SERVER_TICK 回调中定期调用 `TeleportDataManager.cleanUpCache(server)`，并按 `CoreConfig.cleanupIntervalTicks`（默认 18000 ticks）触发一次清理：清理逻辑会移除不在线玩家对应的缓存键。

## 命令参考（tpa 系列 & /back 全量）

下面汇总项目中实现的与 TPA (/tpa) 和返回点 (/back) 相关的所有命令（从 `src/main/java/.../commands/command/` 提取）：

- /tpa
  - 语法：`/tpa <player>`
  - 描述：向指定玩家发送将自己传送到对方的请求。
  - 行为：
    - 检查参数和自传送禁止；如命令方处于冷却中，会提示剩余秒数（若 `core.enable-cooldown = true`）。
    - 调用 `TeleportDataManager.sendTpa(from, into)`：同一请求者最多只能存在一个未处理的 tpa 请求（若已有指向其他人的未处理请求则返回失败）。
    - 成功后向目标玩家发送提示并告知可使用 `/tpaAccept` 或 `/tpaDeny`。
    - 失败时提示已有未处理的请求，并建议使用 `/tpaCancel`。
  - 示例：`/tpa PlayerName`

- /tpaAccept
  - 语法：`/tpaAccept` 或 `/tpaAccept <player>`
  - 描述：接受某个玩家发来的 /tpa 请求，将该玩家传送到自己。
  - 行为：
    - 不带参数时接受最近一个针对自己的 tpa 请求（`TeleportDataManager.getLastTpaRequest`）；带参数时接受指定玩家的请求。
    - 检查请求是否过期（由 `tpa.request-expire-time` 控制，默认 120 秒）。
    - 检查发送方是否因冷却不能立即接受（`core.enable-cooldown`）。
    - 若启用静止等待（`tpa.enable-stand-still`），会要求请求方静止若干秒（`tpa.stand-still-time`），期间通过 `TeleportScheduler` 做倒计时提示并在静止条件满足时执行传送。
    - 传送时会调用 `TeleportDataManager.reportTeleportedData` 记录返回点，移除请求数据，并通知双方。
  - 示例：`/tpaAccept` 或 `/tpaAccept PlayerName`

- /tpaAcceptAll
  - 语法：`/tpaAcceptAll`
  - 描述：接受所有当前针对自己的 tpa 请求（批量接受）。
  - 行为：遍历 `TeleportDataManager.getTpaRequests(into)` 返回的请求集合，对每个请求调用接受逻辑（与 `/tpaAccept` 相同的校验与传送流程）。
  - 示例：`/tpaAcceptAll`

- /tpaDeny
  - 语法：`/tpaDeny` 或 `/tpaDeny <player>`
  - 描述：拒绝最近或指定玩家的 tpa 请求。
  - 行为：移除对应 `tpaData` 条目并向双方发送通知。
  - 示例：`/tpaDeny` 或 `/tpaDeny PlayerName`

- /tpaDenyAll
  - 语法：`/tpaDenyAll`
  - 描述：拒绝所有针对自己的 tpa 请求（批量拒绝）。
  - 行为：遍历所有请求并对每个调用拒绝逻辑（与 `/tpaDeny` 相同）。
  - 示例：`/tpaDenyAll`

- /tpaCancel
  - 语法：`/tpaCancel`
  - 描述：取消自己当前发出的 tpa 请求（不需要指定目标，因每个玩家最多一个未处理请求）。
  - 行为：检查请求是否存在且未过期（`TpaConfig.checkTpa`），若存在则移除 `tpaData` 中该条目并通知请求方。
  - 示例：`/tpaCancel`

- /tpaConfig (管理员)
  - 语法：`/tpaConfig reload` 或 `/tpaConfig set <key> <value>`
  - 描述：管理配置，用于重载或设置配置项（需要较高权限）。
  - 行为：
    - `reload`：调用 `ConfigManager.configfile.reload()` 重新加载配置文件。
    - `set <key> <value>`：设置某个配置项并保存后重载，命令中提供了自动补全与默认建议（基于 `ConfigManager` 的实现）。
  - 示例：`/tpaConfig reload`，`/tpaConfig set tpa.request-expire-time 90`

- /back
  - 语法：`/back`
  - 描述：返回最近的记录点（上一次被记录的传送或死亡位置）。
  - 行为：
    - 检查冷却（`core.enable-cooldown`）；若冷却中会提示剩余秒数。
    - 读取 `TeleportDataManager.backData` 中存储的位置，并根据 `BackConfig.back-expire-time` 判断是否过期（默认 120 秒）；过期则移除并提示。
    - 若启用静止等待（`back.enable-stand-still`），会要求玩家静止若干秒后再传送；否则立即传送并记录新的返回点。
    - 传送成功后会调用 `TeleportDataManager.reportTeleportedData(player, false)` 更新返回点数据并通知玩家。
  - 示例：`/back`

注意与实现细节
- 请求超时与有效性：TPA 与 TPHERE 的请求使用各自模块的 `request-expire-time` 作为有效期判断（单位秒，源码以毫秒比较）。过期后相关接受/拒绝命令会提示请求已过期。
- 冷却：当 `core.enable-cooldown = true` 时，某些接收/传送命令会调用 `CoreConfig.checkCooldown`，如果返回非 -1 表示仍在冷却并会提示剩余秒数（否则返回 -1 表示可立即操作）。
- 静止等待（stand-still）：若启用，接受或 /back 操作会要求请求方或执行方在指定秒数内保持位置不变（通过 `TeleportDataManager.posStore` 存放并检测）；任意移动将取消传送并给出提示。
- 数据清理与注销行为：
  - 服务器在 END_SERVER_TICK 回调内按 `CoreConfig.cleanupIntervalTicks` 间隔调用 `TeleportDataManager.cleanUpCache(server)`，清理不在线玩家的缓存数据。
  - 玩家离线时 `PlayerLogoutClearDataEvent` 会触发 `TeleportDataManager.clearAllData` 清理该玩家的所有传送相关缓存。
