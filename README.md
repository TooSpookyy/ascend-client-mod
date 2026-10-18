# ASCEND Client Mod — Minecraft 1.21.11 / Fabric

Client-only Fabric mod with a K-key menu, Discord shortcut, clan-info command helper, local livestream list, and a local auction screen.

## Important limits
- This is a **client-only** mod. It cannot publish a livestream list to other players or synchronize an auction across clients without a server plugin or shared backend.
- Clan counts and payment notices are best-effort chat parsing. HugoSMP's exact `/Clan info` and `/pay` message formats may differ, so the regexes may need to be adjusted after testing.
- The auction is a local UI/timer, not a server-enforced auction. A detected payment is not proof that it was a bid on the current item.
- Livestream URLs are saved only in `config/ascend-client.json` on this computer.

## Build
Requires JDK 21 and internet access for Gradle dependencies. Open this folder in IntelliJ IDEA with the Minecraft Development/Fabric support, or install Gradle and run:

```bash
gradle build
```

The jar should appear in `build/libs/` after a successful build. This source package does not include a precompiled jar.

## Controls
- Press **K** to open the main menu.
- Discord opens `https://discord.gg/Rg3MWpDYf7`.
- Clan Members can send `/Clan info` and display counts if the server's response matches the parser.
- Livestream entries are local to this client.
- Auction duration choices: 30 seconds, 1 minute, 1:30, 2 minutes.
