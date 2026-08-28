# BBCL — Variable Brightness and Color Light (Minecraft 1.7.10)

Forge 1.7.10 mod for independently adjustable RGB/color-temperature lighting. Each block stores its settings in a `TileEntity`; entity lights store the same data in entity NBT. A runtime shader adapter uploads the nearest 256 active lights when OptiFine/ShadersMod or Angelica binds a supported GLSL program.

## プロジェクト概要

- **名称:** BBCL (Variable Brightness and Color Light)
- **製作者:** Yuzuringo
- **用途:** 光量とRGB／色温度を光源ごとに設定できるブロック光源・Entity光源を追加します。建築照明、街灯、鉄道設備、舞台照明、景観演出などに利用できます。
- **対応Minecraft:** 1.7.10
- **Modローダー:** Forge 1.7.10（Mohistを含むForge互換ハイブリッドサーバーに対応）
- **シェーダー環境:** OptiFine/ShadersModおよびAngelicaの両方に対応
- **対応シェーダー:** Complementary Shaders v4.6系、SEUS Renewed 1.0.1、SEUS v10.2
- **ライセンス:** MIT License

BBCLはシェーダーパックを直接書き換えず、対応するGLSLソースの読み込み時にメモリ上で処理を挿入します。派生版や変更版では互換性が異なる場合があります。

## Project summary

- **Author:** Yuzuringo
- **Purpose:** Per-light brightness and RGB/Kelvin control for architectural, street, railway, stage and scenic lighting.
- **Minecraft version:** 1.7.10
- **Loader/server:** Forge 1.7.10 and Forge-compatible hybrid servers such as Mohist
- **Shader loaders:** OptiFine/ShadersMod and Angelica
- **Recognized shader families:** Complementary Shaders v4.6 family, SEUS Renewed 1.0.1 and SEUS v10.2
- **License:** MIT

## BBCL block and GUI

- BBCL is its own registered Forge block (`variablecolorlight:bbcl`) and stores settings in its own TileEntity NBT.
- Right-click BBCL to open the standard Minecraft GUI. Choose Kelvin or direct RGB mode, enter brightness 0–15 and color values, preview the result, then press Save.
- Like a barrier block, placed BBCL blocks are invisible unless the local player is holding a BBCL item (or currently editing one). Collision, selection, saved settings, and light emission remain active while invisible.
- Forge numeric commands (look at a lamp): `/vcl kelvin 4200`, `/vcl rgb 255 80 20`, `/vcl mode kelvin`, `/vcl mode rgb`, `/vcl level 12`.

## Without a shader pack

The lamp has a client-side emissive core rendered in its configured Kelvin color, so temperature changes remain plainly visible with Minecraft's standard renderer. Vanilla light propagation still uses brightness 0–15 and is monochrome because Minecraft 1.7.10 stores no RGB channel in its block-light data. With a patched shader pack, nearby surfaces additionally receive the per-lamp RGB contribution.

## Shader integration

The stock Minecraft lightmap contains intensity only, so changing a pack's global torch color cannot create multiple simultaneous temperatures. BBCL intercepts recognized shader sources in memory and injects its lighting pass without modifying the shader-pack archive on disk.

Runtime adapters currently cover the tested Complementary v4.6 family, SEUS Renewed 1.0.1 and SEUS v10.2 paths under OptiFine/ShadersMod and Angelica. The selected shader pack on disk is not changed, and already-patched sources are detected to prevent duplicate insertion. Exact compatibility can vary between shader-pack releases.

Adapters must reconstruct camera-relative world position from depth and use the pack's world-space normal. Typical insertion families:

- SEUS Renewed: `composite.fsh` (before tonemapping).
- BSL legacy: the deferred/composite lighting pass; exact filename changes by release.
- Complementary: the deferred/composite program selected by that release; modern releases use split files under `shaders/program` and `shaders/lib`.

Do not redistribute patched SEUS or Complementary files unless their license permits it. Distribute the small patch and instructions, and let users patch their own legally obtained copy.

## Build

Use a Java 8 JDK and Gradle 2.14.1; ForgeGradle 1.2 is too old for Java 21. Run `gradle setupDecompWorkspace build`. Both the Forge mod and Spigot plugin have been compiled successfully with OpenJDK 8 in this workspace. ForgeGradle's retired Mojang S3 URLs require the official 1.7.10 metadata/client/server artifacts to be pre-seeded in its cache; the build disables only those two obsolete download tasks.

The Forge dedicated server may load the main jar safely: all Minecraft client and OpenGL references are behind a sided proxy. Install the Forge jar on the dedicated server and every participating client.

## Spigot/Cauldron-compatible multiplayer bridge

Build `spigot-plugin` with Java 7/8 and Maven, then put its jar in the server's `plugins` directory. Clients still require the Forge client mod because a Bukkit server cannot execute or configure a player's GLSL shader.

- A redstone lamp is the server-side carrier block.
- Hold a blaze rod and right-click a redstone lamp to change temperature; sneak-right-click changes shader intensity.
- Permission: `variablecolorlight.edit` (operator by default).
- Spigot uses the same `/vcl kelvin`, `/vcl rgb`, `/vcl mode`, and `/vcl level` commands while looking at a redstone lamp.
- Settings persist in the plugin config and are sent on join, world change, and edits over `VCL|SYNC`.
- Breaking or exploding a configured lamp deletes its saved entry. Sticky/normal piston movement transfers settings to the destination coordinate.
- Stale entries left by WorldEdit or other direct world editors are removed during the next synchronization when the stored coordinate is no longer a redstone lamp.
- When WorldEdit is installed, ordinary `//copy` followed by `//paste` (including `//paste -o`) clones every variable-light setting in the selection using WorldEdit's player-relative clipboard origin. The integration is optional and uses no hard WorldEdit dependency. Rotated or flipped clipboards are not transformed in this initial adapter.
- One legacy plugin message carries at most 1,400 lights, keeping the RGB-capable packet below the 32 KiB custom-payload limit; the client renders the nearest 256 active lights.
- Vanilla clients may join and simply see ordinary redstone lamps; only modded clients receive colored shader lighting.
- The Spigot bridge can represent intermediate shader intensity, but vanilla block lighting itself remains off or level 15 because Bukkit 1.7.10 cannot assign arbitrary emission to a vanilla block.

## Current limits

- Shader compatibility is version-specific because pack layouts and variable names differ by release.
- The 256 nearest loaded active lamps are rendered. Raising this limit further may exceed older GPU uniform limits or significantly reduce shader performance.
- A placeholder block texture is intentionally not bundled.
- Mixed OptiFine/ShadersMod/Angelica configurations should be tested against the exact jars and shader-pack versions being deployed.
