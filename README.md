# Invsee

`/invsee <player>` opens another player's inventory as a double chest, live. What you see is their
actual inventory, not a snapshot - items they pick up appear as they pick them up, and anything you
move lands on them immediately.

```
/invsee <target>                 open target's inventory
/invsee show <viewer> <target>   make someone else open it
```

`show` sits behind its own permission node, because forcing a screen onto another player is a
different power from looking at their bag.

## Layout

The 54 slots of a double chest are mapped onto the 41 an inventory actually has:

| Chest slots | Contents |
| --- | --- |
| 0-26 | main storage |
| 27-35 | hotbar |
| 36-40 | armour and offhand |
| 41-53 | inert filler panes |

Filler slots are always locked. Shift-click is disabled outright - it returns empty rather than
guessing, so items cannot be flung into filler or across compartments by accident.

## Permissions

Checked through [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api), so
LuckPerms nodes work if you have it and op level is the fallback if you don't.

| Node | Default | Grants |
| --- | --- | --- |
| `invsee.use` | op level 2 | `/invsee <target>` |
| `invsee.show` | op level 2 | `/invsee show <viewer> <target>` |

## Config (`config/invsee.json`)

```json
{
  "permissionNode": "invsee.use",
  "showPermissionNode": "invsee.show",
  "defaultOpLevel": 2,
  "allowEdit": true,
  "titleFormat": "&8%s's Inventory"
}
```

Set `allowEdit` to false for a read-only view: real slots stop accepting pickup and place, and the
window becomes a viewer rather than an editor.

## Chat integration

The `[inv]` token in [chat-extensions](https://github.com/Glance-Studios/chat-extensions) renders as
a clickable link that runs `/invsee <name>`. That is the whole integration - it runs the command, so
this mod's own permission check still applies and the two mods share no code.

## Server-side only - players install nothing

A vanilla double-chest screen, so any client renders it.

## Requirements

Drop these into your **server's** `mods/` folder:

| Mod | Version |
| --- | --- |
| `invsee-0.1.0.jar` | this mod |
| [Fabric API](https://modrinth.com/mod/fabric-api) | `0.155.2+26.2` (or compatible) |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | `1.13.12+kotlin.2.4.0` (or compatible) |

fabric-permissions-api is bundled inside the jar, so there is nothing extra to install for it.

Minecraft **26.2**, Fabric Loader **0.19.3+**, **Java 25**.

## Limits

Online players only - there is no offline playerdata reading. The window closes if the target
disconnects. Ender chests are not covered.
