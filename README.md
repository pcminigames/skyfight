# SkyFight

A Minecraft minigame where players fight above the void using selected kits.

** Requires [pythoncraft's GameLib](https://github.com/pcminigames/gamelib) plugin to run. **

## Installation

1. Make sure you are running the correct version of Minecraft server (for SkyFight v4.1 it's Paper 1.21.8)
2. Download the latest release from the [releases page](https://github.com/pcminigames/skyfight/releases). You should get a `.jar` file and two `.yml` files.
3. Download the [GameLib](https://github.com/pcminigames/gamelib/releases) plugin. There should be just one `.jar` file.
4. Put both `.jar` files in your server's `plugins` folder.
5. Make a folder named `skyfight` in your server's `plugins` folder.
6. Put the `config.yml` and `kits.yml` files in the `skyfight` folder.
7. Start/restart your server.

## Usage

1. Click with the SkyFight menu in your hand to open the team selection menu.
2. Choose a team by clicking on one of the colored wool blocks.
3. Choose a kit for the next game. You will be automatically teleported to the arena.
4. When all of the online players have chosen a team and kit, the game will start automatically.
5. The game ends when one team has no players left alive.

## Notes

- Make sure to try out other [pythoncraft's minigames](https://github.com/orgs/pcminigames/repositories)
- Feel free to suggest improvements or report issues ([here](https://github.com/pcminigames/skyfight/issues)).
- Originally made for me and my friends, so don't expect too much polish. However, if you behave how you are expected to behave, everything should work fine.

## Configuration

### config.yml

- `arena-fill`: List of sections to create the arena.
- `inventory`: Predefined inventories layouts for players.
    - `default`: The default inventory layout.

### kits.yml

- List of kits that players can choose from.
- Each kit has to have an id, which is a unique identifier for that kit. It can be anything, as long as it's unique.
- The player will receive all items defined in both the choosen kit and the `default` kit.
- Each kit contains:
    - `name`: The display name of the kit.
    - `icon`: The item used as the icon for the kit in the selection menu.
    - `items`: List of items given to players when they choose this kit. Each item can be specified using either short or detailed notation

1. Short notation
    - Is used for simple items without any special properties.
    - `'<id>': <item_name>` - Gives one of the specified item.
    - `'<id>': <item_name>*<count>` - Gives a specified number of the item. `<count>` must be a positive integer, but can be greater 64.
    - Example:

      ```yaml
      items:
        k1: dirt
        k2: oak_log*16
        k3: diamond_sword
      ```

2. Detailed notation
    - Is used for items that require special properties, like custom names, lore, or enchantments.
    - Has multiple properties. The only one required is `id`.
    - `'<id>':` - Starts the detailed item definition.
      - `id: <item_name>` - Specifies the item name.
      - `count: <count>` - Specifies the number of the item. Must be a positive integer, but can be greater 64. Defaults to 1 if not specified.
      - `custom-name: <custom_name>` - Specifies a custom name for the item.
      - `enchantments:` - Starts the enchantments definition.
        - `<enchantment_1>: <level>` - Specifies an enchantment and its level. `<level>` must be a positive integer.
        - `<enchantment_2>: <level>`
      - `effects:` - Starts the effects definition.
        - `<effect_1>:` - Specifies an effect.
          - `duration: <duration>` - Duration of the effect in ticks (1 tick = 1/20 seconds). Must be a positive integer.
          - `amplifier: <amplifier>` - Amplifier of the effect. Must be a non-negative integer (0 = level 1, 1 = level 2, etc.). Defaults to 0 if not specified.
        - `<effect_2>: ...`
      - `potion-color: '<color>'` - Specifies the color of the potion. Must be a valid hex color code (e.g. `#FF0000` for red). Must be enclosed in single quotes.
      - `durability: <durability>` - Specifies the durability of the item. Must be a non-negative integer. If set to 0, the item will be unbreakable.
      - `trim-pattern: <pattern>` - Specifies the trim pattern for leather armor. Must be a valid trim pattern (e.g. `sentry`, `dune`, `flow`, etc.).
      - `trim-material: <material>` - Specifies the trim material for leather armor. Must be a valid trim material (e.g. `iron`, `gold`, `diamond`, etc.).
      - `red:` / `yellow:` - Specifies team-specific item properties. If the item is given to a player on the red team, the properties under `red:` will be applied. If the player is on the yellow team, the properties under `yellow:` will be applied. This can be used to give team-colored items (e.g. leather armor).
    - Example:

      ```yaml
      items:
        k1:
          id: diamond_sword
          count: 1
          custom-name: "Epic Sword"
          enchantments:
            sharpness: 5
            unbreaking: 3
        k2:
          id: potion
          count: 2
          custom-name: "Speed Potion"
          effects:
            speed:
              duration: 600
              amplifier: 1
          potion-color: '#FF0000'
        head:
          id: diamond_helmet
          durability: 0
          trim-pattern: flow
          yellow:
            trim-material: gold
          red:
            trim-material: redstone
      ```

## Issues

If you find any issues or have suggestions for improvements, please report them on the [issues page](https://github.com/pcminigames/skyfight/issues).
