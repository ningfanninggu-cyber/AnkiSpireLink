# Anki Spire Link

一个把《杀戮尖塔》和 Anki 复习连起来的 Mod。

打出一张普通牌之前，游戏会暂停并等待你在真正的 Anki 桌面软件里完成一张复习卡。你在 Anki 里点的 `重来 / 困难 / 良好 / 简单`，会决定这张牌获得什么加成。

这个 Mod 的目的很简单：让爬塔变成一个温和但有效的学习闸门。你不是先玩完再想起要学习，而是每打出一张关键牌，都要先真的复习一张卡。

Steam 创意工坊：[Anki Spire Link](https://steamcommunity.com/sharedfiles/filedetails/?id=3720354290)

更详细的中文使用教程：[使用说明.md](使用说明.md)

## 这个 Mod 到底做什么

Anki Spire Link 不会把 Anki 的题目、答案和按钮塞进游戏里。它使用的是你电脑上已经安装好的 Anki 桌面软件，并通过 AnkiConnect 读取真实的复习结果。

这样设计有几个好处：

- 你的复习记录仍然由 Anki 正常保存。
- 原来的牌组、模板、图片、音频、公式、插件都照常工作。
- 数学题、语言题、医学题、图片题、听力题这些复杂卡片，仍然在 Anki 自己的界面里完成。
- 你可以分屏使用：左边 Anki，右边《杀戮尖塔》。
- 如果你喜欢自动切屏，macOS 上也可以让 Mod 自动把焦点切到 Anki，再切回游戏。

## 主要功能

- 新增一个 `Anki Link` 遗物。
- 每局开局会询问：本局是否启用 Anki 联动。
- 启用后，打出非状态、非诅咒牌时会触发 Anki 复习。
- 游戏会等待你在真实 Anki 里完成一张复习卡。
- 根据 Anki 评分发放对应奖励。
- 支持填写固定 Anki 牌组名，也支持留空后手动在 Anki 里选牌组。
- 支持中英文本地化：英文、简体中文、繁体中文。
- 游戏内会提示你刚刚获得了什么奖励。

## 需要准备什么

你需要先准备这些东西：

- 《杀戮尖塔》
- ModTheSpire
- BaseMod
- Anki 桌面版
- AnkiConnect 插件

AnkiConnect 的插件代码是：

```text
2055492159
```

AnkiConnect 装好后，默认会在你自己的电脑上提供这个本地地址：

```text
http://127.0.0.1:8765
```

这是每个人电脑上都一样的本地地址，不是作者的网站，也不是需要单独注册的服务器。它只表示“你这台电脑上的 AnkiConnect 服务”。

## 游戏流程

正常使用时，大概是这样：

1. 打开 Anki，并确认 AnkiConnect 已安装。
2. 通过 ModTheSpire 启动《杀戮尖塔》。
3. 开始一局游戏。
4. 开局弹窗里选择 `本局使用`。
5. 打出一张非状态、非诅咒牌。
6. 游戏弹出 Anki 等待界面。
7. 切到 Anki，完成一张真实复习卡。
8. 在 Anki 里点击 `重来`、`困难`、`良好` 或 `简单`。
9. Mod 读取你的评分，回到游戏并结算这张牌。
10. 游戏内显示你获得的奖励。

这个 Mod 没有“5 秒后自动放行”的设计。只要 Anki 当前有可复习的卡片，游戏就会等你真的完成一次 Anki 复习。

## 固定奖励

每次完成 Anki 复习后，都会先根据你的评分给出固定奖励：

| Anki 评分 | 牌面倍率 | 固定奖励 |
| --- | ---: | --- |
| 重来 / Again | x1 | 永久最大生命值 +1 |
| 困难 / Hard | x1 | 获得 1 点能量和 3 点格挡 |
| 良好 / Good | x2 | 抽 1 张牌，并获得 1 点临时力量 |
| 简单 / Easy | x4 | 获得 1 点能量，抽 1 张牌，并获得 1 点临时力量和 1 点临时敏捷 |

倍率会作用在这张牌本身的正向数值上，包括：

- 伤害
- 格挡
- 正数 `magicNumber`
- 群体攻击的多目标伤害

结算完成后，牌的数值会恢复，不会把临时倍率永久写进卡牌。

## 随机额外奖励

除了固定奖励，每个评分档位还有机会触发一个小的随机奖励。它们是锦上添花，不是稳定刷资源的手段。

| Anki 评分 | 随机获得一件遗物 | 永久最大生命值 +3 到 +5 | 少量治疗 | 少量金币 |
| --- | ---: | ---: | ---: | ---: |
| 重来 / Again | 1% | 4% | 8% | 12% |
| 困难 / Hard | 3% | 6% | 10% | 14% |
| 良好 / Good | 8% | 8% | 12% | 15% |
| 简单 / Easy | 15% | 10% | 10% | 10% |

遗物概率故意做得比较低。它应该像一次小惊喜，而不是每局都能稳定规划进去的收益。

## 分屏和自动切屏

macOS 上，Mod 可以在触发复习时把 Anki 拉到前台，检测到你答完后再尽量把《杀戮尖塔》拉回前台。

如果你更喜欢自己分屏，可以在 Mod 设置里关掉：

```text
Switch focus to Anki
```

关掉后，Mod 不再主动切换软件焦点，只会在游戏里显示等待界面，并在后台轮询 AnkiConnect。你可以自己把 Anki 和游戏窗口摆成左右分屏。

Windows 和 Linux 目前默认就是手动切换。请提前打开 Anki，然后用自己的窗口布局或 `Alt+Tab` 在 Anki 和游戏之间切换。

## 安装方式

普通玩家推荐直接通过 Steam 创意工坊订阅：

[Steam Workshop: Anki Spire Link](https://steamcommunity.com/sharedfiles/filedetails/?id=3720354290)

手动安装时，把 `AnkiSpireLink.jar` 放进《杀戮尖塔》的 `mods` 文件夹，然后通过 ModTheSpire 启动，并确保 BaseMod 已启用。

不要同时加载本地 jar 和 Steam 创意工坊版本。两份 Mod 同时存在时，很容易出现重复加载或行为异常。

## 从源码构建

这个项目使用 Maven。由于《杀戮尖塔》、ModTheSpire 和 BaseMod 不是普通 Maven Central 依赖，项目会从本地 Steam 游戏目录读取相关 jar。

在项目根目录运行：

```bash
mvn package
```

如果你的 Steam 库不在默认位置，可以设置 `STEAMAPPS`：

```bash
STEAMAPPS="/path/to/steamapps" mvn package
```

macOS 默认路径通常是：

```text
~/Library/Application Support/Steam/steamapps
```

Windows 默认路径通常是：

```text
C:/Program Files (x86)/Steam/steamapps
```

Windows 如果安装在 `Program Files` 下，打包后复制 jar 可能需要管理员权限。

这个仓库本地开发时使用过下面这套命令：

```bash
JAVA_HOME="../SpireMod_CET46InSpire/.tools/java/jdk-25.0.3+9/Contents/Home" \
PATH="../SpireMod_CET46InSpire/.tools/java/jdk-25.0.3+9/Contents/Home/bin:../SpireMod_CET46InSpire/.tools/maven/bin:$PATH" \
mvn -s "../SpireMod_CET46InSpire/.tools/maven-settings-aliyun.xml" package
```

## 测试

运行单元测试：

```bash
mvn test
```

手动冒烟测试建议这样做：

1. 打开 Anki。
2. 确认 AnkiConnect 能响应 `http://127.0.0.1:8765`。
3. 用 ModTheSpire 启动游戏，并启用 BaseMod 和 Anki Spire Link。
4. 开始一局，选择 `本局使用`。
5. 打出一张非状态牌。
6. 在 Anki 中完成一张复习卡。
7. 确认原本那张牌正常结算，并且游戏内出现奖励提示。

## 当前限制

- AnkiConnect 地址固定为 `127.0.0.1:8765`。
- 牌组名需要填写 Anki 中的精确牌组名。留空则由你在 Anki 里手动选择。
- 自动切换软件焦点目前只支持 macOS。
- 不支持 AnkiMobile、AnkiDroid 或 AnkiWeb，因为它们不提供这个 Mod 需要的本地 AnkiConnect API。

## 项目结构

```text
src/main/java/com/ankispirelink
  actions/    自定义游戏动作
  anki/       AnkiConnect 客户端和外部复习流程
  game/       奖励策略、本局启用状态、卡牌数值辅助逻辑
  patches/    ModTheSpire 卡牌打出拦截补丁
  relics/     Anki Link 遗物实现
  screens/    开局选择界面和复习等待界面
  ui/         BaseMod 设置面板

src/main/resources
  ModTheSpire.json
  AnkiSpireLinkResources/

src/test/java
  Anki、奖励、卡牌数值和本局选择逻辑的单元测试
```

## 参与贡献

欢迎提交 issue 或 pull request。开始前可以先看 [CONTRIBUTING.md](CONTRIBUTING.md)。

安全相关问题请看 [SECURITY.md](SECURITY.md)。

## 许可证

本项目使用 MIT License。详情见 [LICENSE](LICENSE)。

本项目与 Mega Crit、Anki、AnkiConnect、ModTheSpire、BaseMod 没有官方关联。

---

# English

Anki Spire Link is a Slay the Spire mod that connects card plays to real Anki reviews through AnkiConnect.

Before a normal card resolves, the game waits for you to answer one review card in the real Anki desktop app. Your Anki rating then decides the reward applied to that card.

Steam Workshop: [Anki Spire Link](https://steamcommunity.com/sharedfiles/filedetails/?id=3720354290)

Detailed Chinese player guide: [使用说明.md](使用说明.md)

## What This Mod Does

This mod is for players who want Slay the Spire to nudge them into studying without replacing Anki.

It does not embed Anki inside Slay the Spire. It does not render Anki card fronts, backs, or answer buttons in-game. Instead, it uses your real Anki desktop app and reads real review results through AnkiConnect.

That means:

- Your reviews are still recorded by Anki normally.
- Your existing decks, templates, media, and add-ons still work.
- Long math, language, image, audio, or medical cards can be answered in Anki itself.
- Split-screen play works well: Anki on one side, Slay the Spire on the other.

## Features

- Adds the `Anki Link` relic.
- Asks at the start of each run whether to enable Anki Link for that run.
- Intercepts non-status, non-curse card plays.
- Waits for one real Anki review before the card resolves.
- Supports a configured Anki deck name, or manual deck selection in Anki.
- Supports macOS focus switching and manual split-screen play on all platforms.
- Shows an in-game message describing the reward you received.
- Includes English, Simplified Chinese, and Traditional Chinese localization.

## Requirements

- Slay the Spire
- ModTheSpire
- BaseMod
- Anki desktop running locally
- AnkiConnect installed and listening on `http://127.0.0.1:8765`

AnkiConnect add-on code:

```text
2055492159
```

The address `http://127.0.0.1:8765` is a local address on your own computer. It is not a website owned by this mod.

## Gameplay Flow

1. Open Anki.
2. Launch Slay the Spire through ModTheSpire.
3. Start a run.
4. Choose `Use this run`.
5. Play a non-status, non-curse card.
6. The game opens an Anki review waiting screen.
7. Answer one real review card in Anki.
8. Click Again, Hard, Good, or Easy in Anki.
9. The mod detects the rating through AnkiConnect.
10. The original card resolves with the matching reward.

There is no short auto-skip timer. If Anki has an active review card, the game waits until a real Anki answer is recorded.

## Rewards

| Anki rating | Card multiplier | Fixed reward |
| --- | ---: | --- |
| Again | x1 | Max HP +1 |
| Hard | x1 | Gain 1 Energy and 3 Block |
| Good | x2 | Draw 1 card and gain 1 temporary Strength |
| Easy | x4 | Gain 1 Energy, draw 1 card, and gain 1 temporary Strength and 1 temporary Dexterity |

The multiplier applies to the played card's damage, block, positive magic number, and multi-target damage. Card stats are restored after the play resolves.

Each rating can also roll one small random extra reward:

| Anki rating | Random relic | Max HP +3 to +5 | Small heal | Small gold |
| --- | ---: | ---: | ---: | ---: |
| Again | 1% | 4% | 8% | 12% |
| Hard | 3% | 6% | 10% | 14% |
| Good | 8% | 8% | 12% | 15% |
| Easy | 15% | 10% | 10% | 10% |

The relic chance is intentionally low. It is meant to feel like a pleasant surprise, not a reliable farming engine.

## Focus Switching

On macOS, the mod can focus Anki when a review starts and best-effort focus Slay the Spire after the review is detected.

You can disable this in the mod settings with `Switch focus to Anki`. When disabled, the mod only shows the waiting screen and polls AnkiConnect in the background. This is the recommended mode for manual split-screen play.

On Windows and Linux, focus switching is manual. Keep Anki open and use your normal window layout or Alt+Tab.

## Installation

For normal play, install through Steam Workshop:

[Steam Workshop: Anki Spire Link](https://steamcommunity.com/sharedfiles/filedetails/?id=3720354290)

For manual installation, place `AnkiSpireLink.jar` in the Slay the Spire `mods` folder and launch through ModTheSpire with BaseMod enabled.

Do not load both a local jar and a Steam Workshop copy at the same time.

## Building

This project uses Maven and system-scoped dependencies for Slay the Spire, ModTheSpire, and BaseMod.

From this directory:

```bash
mvn package
```

If your Steam library is not in the default location, set `STEAMAPPS`:

```bash
STEAMAPPS="/path/to/steamapps" mvn package
```

On macOS, the default Steam profile expects:

```text
~/Library/Application Support/Steam/steamapps
```

On Windows, the default Steam path is usually under:

```text
C:/Program Files (x86)/Steam/steamapps
```

That location may require an elevated terminal for the package step to copy the jar into the game `mods` folder.

## Testing

Run the unit tests:

```bash
mvn test
```

Manual smoke test:

1. Launch ModTheSpire with BaseMod and Anki Spire Link.
2. Open Anki and confirm AnkiConnect responds at `http://127.0.0.1:8765`.
3. Start a run and choose to enable Anki Link.
4. Play a non-status card.
5. Complete one Anki review card.
6. Confirm the original card resolves and an in-game reward message appears.

## Current Limitations

- The AnkiConnect endpoint is fixed to `127.0.0.1:8765`.
- The deck setting is an exact Anki deck name. Leave it empty to choose the deck manually in Anki.
- Automatic application focus switching is macOS-only.
- Mobile Anki clients and AnkiWeb are not supported because they do not expose the local AnkiConnect API used by this mod.

## Project Structure

```text
src/main/java/com/ankispirelink
  actions/    Small custom game actions.
  anki/       AnkiConnect client and external review session logic.
  game/       Reward policy, run-choice state, and card-stat helpers.
  patches/    ModTheSpire patches for card play interception.
  relics/     Anki Link relic implementation.
  screens/    Custom start-choice and review-wait screens.
  ui/         BaseMod settings panel.

src/main/resources
  ModTheSpire.json
  AnkiSpireLinkResources/

src/test/java
  Unit tests for Anki, reward, card-stat, and run-choice logic.
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

This project is not affiliated with Mega Crit, Anki, AnkiConnect, ModTheSpire, or BaseMod.
