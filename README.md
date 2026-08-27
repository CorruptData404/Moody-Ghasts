# Moody Ghasts
 
A full overhaul of the Happy Ghast mob, transforming it from a simple building mount into a companion with emotions, aerial combat capabilities, and new items to interact with. This is my first mod!
 
For the full feature list, screenshots, and configuration details, see the mod page:
 
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/moody-ghasts)
- [Modrinth](https://modrinth.com/mod/moody-ghasts-mod)
---
 
## Requirements
 
Requires NeoForge. See the CurseForge/Modrinth links above for which Minecraft version this branch targets and the matching NeoForge version.
 
---
 
## Extending Moody Ghasts
 
Most of the mod's behavior is driven by datapacks, and the parts that aren't are built around open NeoForge registries so other mods can add to them without touching this mod's code.
 
### Datapacks
 
No companion mod needed for most customization. Datapacks under `data/moodyghasts/` can:
 
- Tune mood system values (base mood, regression speed, damage/healing rates, tantrum thresholds)
- Define new mood states, including custom textures and bar colours
- Configure ghast foods and projectiles (which item triggers what, mood-scaling curves, counts, remainders)
### Companion Mods
 
Two things require registering through code rather than JSON:
 
- **New projectile types** — implement a projectile factory and register it to `PROJECTILE_FACTORIES`
- **New firing patterns** — implement a firing pattern factory and register it to `FIRING_PATTERN_FACTORIES`

Both are open NeoForge registries. The easiest way to see the shape expected is to look at the existing factories in the mod's source (e.g. the ice charge and single-shot implementations) and follow the same pattern.
 
---
 
## Feedback & Bug Reports
 
Bug reports, balance feedback, and suggestions are very welcome. Please [open an issue](../../issues) with as much detail as you can (Minecraft/NeoForge version, steps to reproduce, logs if it's a crash).
