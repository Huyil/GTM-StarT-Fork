
<p align="center"><img src="https://raw.githubusercontent.com/GregTechCEu/Branding/refs/heads/master/gregtech_ceu_modern_logo_large_modern.png" alt="Logo"></p>
<h1 align="center">GregTech CEu: Modern - StarT Fork</h1>
<p align="center">A community fork of GregTech CEu: Modern maintained by the StarT Development Team</p>
<h1 align="center">
    <a href="https://github.com/StarT-Dev-Team/GTM-StarT-Fork/blob/main/LICENSE"><img src="https://img.shields.io/github/license/StarT-Dev-Team/GTM-StarT-Fork?style=for-the-badge" alt="License"></a>
    <a href="https://github.com/StarT-Dev-Team/GTM-StarT-Fork/releases"><img src="https://img.shields.io/github/v/release/StarT-Dev-Team/GTM-StarT-Fork?style=for-the-badge" alt="Release"></a>
</h1>

## About This Fork

This is a community-maintained fork of [GregTech CEu: Modern](https://github.com/GregTechCEu/GregTech-Modern) by the StarT Development Team. 

**Original Mod:** [GregTech CEu: Modern](https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern)  
**Original Repository:** [GregTechCEu/GregTech-Modern](https://github.com/GregTechCEu/GregTech-Modern)

### Fork Status

### Fork Goals

### Changes from Upstream

## Credits

### Fork Development Team

- **trulyno** - Fork Maintainer
- **KillLaAqua** - Developer
- **stellaurora** - Developer

### Original GregTech CEu: Modern Team

This fork is based on the excellent work of the GregTech CEu: Modern development team:

- **KilaBash** - Original GTCEu Modern Developer
- **screret** - Original GTCEu Modern Developer
- **serenibyss** - Original GTCEu Modern Developer
- **Tech22** - Original GTCEu Modern Developer
- **YoungOnion** - Original GTCEu Modern Developer
- **Mikerooni** - Original GTCEu Modern Developer
- **Ghostipedia** - Original GTCEu Modern Developer

## For Developers

To add this fork as a dependency to your project, add the following to your `build.gradle`:

```groovy
repositories {
    maven {
        name = 'GTCEu Maven'
        url = 'https://maven.gtceu.com'
        content {
            includeGroup 'com.gregtechceu.gtceu'
        }
    }
}
```

Then, you can add it as a dependency, with `${mc_version}` being your Minecraft version target and `${gtm_version}` being the version you want to use:

```groovy
dependencies {
    // Forge (see below block as well if you use Forge Gradle)
    implementation fg.deobf("com.gregtechceu.gtceu:gtceu-${mc_version}:${gtm_version}")

    // Architectury
    modImplementation "com.gregtechceu.gtceu:gtceu-${mc_version}:${gtm_version}"
}
```

### IDE Requirements (when using IntelliJ IDEA)

For contributing to this mod, the [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok) for IntelliJ IDEA is strictly required.  
Additionally, the [Minecraft Development plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) is recommended.

## Credited Works

- Most textures are originally from [Gregtech: Refreshed](https://modrinth.com/resourcepack/gregtech-refreshed) by @ULSTICK. With some consistency edits and additions by @Ghostipedia.
- Some textures are originally from the **[ZedTech GTCEu Resourcepack](https://github.com/brachy84/zedtech-ceu)**, with some changes made by the community.
- New material item textures by @TTFTCUTS and @Rosethorns.
- Wooden Forms, World Accelerators, and the Extreme Combustion Engine are from the **[GregTech: New Horizons Modpack](https://www.curseforge.com/minecraft/modpacks/gt-new-horizons)**.
- Primitive Water Pump is from the **[IMPACT: GREGTECH EDITION Modpack](https://gt-impact.github.io/#/)**.
- Ender Fluid Link Cover, Auto-Maintenance Hatch, Optical Fiber, and Data Bank Textures are from **[TecTech](https://github.com/Technus/TecTech)**.
- Steam Grinder is from **[GregTech++](https://www.curseforge.com/minecraft/mc-mods/gregtech-gt-gtplusplus)**.
- Certificate of Not Being a Noob Anymore is from **[Crops++](https://www.curseforge.com/minecraft/mc-mods/berries)**.

See something we forgot to credit? Reach out to us by opening an issue and ask for appropriate credit, we will happily mark it here.

## License

This project is licensed under the LGPL-3.0 license - see the [LICENSE](LICENSE) file for details.

This fork maintains the same license as the original GregTech CEu: Modern project.
