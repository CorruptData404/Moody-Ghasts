# Moody Ghasts

An overhaul mod for the Happy Ghast mob that adds new utility and mechanics!

## Mood System
Ghasts now have dynamic emotions! Monitor their current emotion through the new mood bar while riding or by looking at their face.

### Mood Influences:
* **Projectile Usage** — Different projectiles affect mood differently
* **Treats** — Feed snowballs or new cookies to intentionally calm or anger your ghast
* **Damage/Healing** — Recieving Damage or Healing can affect their mood
* **Time** — Mood neutralizes over time

### Mood Effects:
* Excited ghasts gain a speed boost
* Angry ghasts fire more powerful versions of projectiles
* Enraged ghasts cry and risk becoming hostile - calm them quickly!

## Aerial Ghast Combat
You can now shoot various projectiles while controlling your ghast companion:

* **Fire Charges** — Classic explosive ghast projectiles
* **Wind Charges** — More powerful than player-thrown variants
* **Ice Charges** — New freezing projectile
* **Powdered Snow** — Release a barrage of snowballs

## Ice Charge Mechanics
A new craftable projectile that can be:
* Thrown by players
* Shot from dispensers
* Fired from ridden ghasts

### Effects:
* **On Entity Impact**
  * Applies slowness and deals damage
  * Transforms Skeletons into Strays
* **Area Effects**
  * Converts water sources to frosted ice (when adjacent to air or non-source water)
  * Transforms lava into obsidian and cobblestone
  * Creates snow layers on blocks
  * Extinguishes fires

## Configuration
This mod is highly data-driven and can be configured entirely through datapacks. No code required for most customization.

What you can configure:

* **Mood system** — tune base mood, how quickly mood regresses, damage and healing rates, tantrum thresholds and conversion behaviour, and ghast tear drops
* **Mood states** — edit the thresholds and effects of existing moods (speed modifiers, tantrum timers, textures), or define entirely new mood states with custom textures and bar colours
* **Ghast foods** — add new items that affect mood, and configure how much each one changes it
* **Projectile system** — mix and match any projectile type with any shooting behaviour, and tune their properties (velocity, inaccuracy, strength, radius, count) with per-mood scaling

### What requires a companion mod:

* New projectile entity types (requires a GhastProjectileFactory implementation)
* New shooting behaviours (requires a ShootingBehaviourFactory implementation)

Both extension points use NeoForge's registry system, so other mods can register against them without modifying this mod's code.

## Post 1.0 Plans

* More rendering effects for tantrums
* New Ice Charge item texture, custom model, particles, and sounds
* More vanilla projectiles

## Dependencies
* Minecraft [1.21.8]
* Neoforge