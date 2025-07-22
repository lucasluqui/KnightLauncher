<p align="center">
    <img src="https://github.com/lucasluqui/KnightLauncher/blob/main/src/main/resources/img/icon-128.png?raw=true" height="128">
</p>
<h1 align="center">Knight Launcher</h1>
<p align="center">
    <a href="https://github.com/lucasluqui/KnightLauncher/blob/main/LICENSE"><img alt="GitHub license" src="https://img.shields.io/github/license/lucasluqui/KnightLauncher?style=flat-square"></a>
    <a href="https://github.com/lucasluqui/KnightLauncher/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/lucasluqui/KnightLauncher?style=flat-square"></a>
    <a href="https://github.com/lucasluqui/KnightLauncher/stargazers"><img alt="GitHub stars" src="https://img.shields.io/github/stars/lucasluqui/KnightLauncher?style=flat-square"></a>
</p>
<p align="center">
    <a href="https://GitHub.com/lucasluqui/KnightLauncher/releases/"><img alt="Total downloads" src="https://img.shields.io/github/downloads/lucasluqui/KnightLauncher/total.svg"></a>
    <a href="https://discord.gg/RAf499a"><img alt="Discord" src="https://img.shields.io/discord/653349356459786240" target="_blank"></a>
</p>

Open source game launcher for Spiral Knights. Supports automatic 64-bit Java VM installation, Discord integration, easier modding & much more.

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/W4W11S2JU)

-----

## Features
* Install and uninstall game mods in an easy and noob friendly way without worrying about game updates.
* Jump into several editors such as a model viewer and scene editor to create custom user-generated content.
* Automatically patch your game to use a 64-bit Java VM and heavily improve your performance.
* Reinstall your game without having to re-download anything, not a single file!
* Intuitive and user friendly GUI for configuring advanced settings including Extra.txt.
* Discord RPC integration that shows exactly what you're up to in game.
* Easily launch alt accounts with lesser resources assigned avoiding losing performance on your main instance.
* Ability to switch between official and third party servers.

-----

## Building

1. Prerequisites 
   - [Java 8 JDK](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html) installed.
   - [Maven](https://maven.apache.org/download.cgi) installed:
     - Windows: Download the ZIP from the link above, extract, then add `bin/` to your `PATH`.
     - macOS (Homebrew): `brew install maven`.
     - Linux (APT): `sudo apt install maven`.
2. Clone the repository
   - `git clone https://github.com/lucasluqui/KnightLauncher.git`
3. Build the project
   - `cd KnightLauncher`
   - `mvn clean package`
4. Copy the package created by maven to your Spiral Knights folder and run it.
   - `java -jar KnightLauncher.jar`

-----

## Discord
We've built an amazing community on Discord focused on both helping newcomers get along with the launcher and giving a hand to modders, come join us!

https://discord.gg/RAf499a

-----

## Credits

### Translators
Thank you all for helping Knight Launcher, making it usable for everyone worldwide!
- Arabic: asan_ploto.
- Chinese: [yihleego](https://github.com/yihleego).
- Deutsch: Biral, Airbee.
- Eesti: [Thyrux](https://github.com/Thyrux).
- Français: PtitKrugger.
- Italiano: [Lawn](https://github.com/Foyylaroni), Kaos.
- Japanese: Armin.
- Polski: [Crowfunder](https://github.com/Crowfunder).
- Português (Brasil): Stret, Gugaarleo.
- Русский: Milliath, [Puzovoz](https://github.com/Puzovoz), [quardbreak](https://github.com/quardbreak).

### Third Party Libraries
The following open source libraries are used by Knight Launcher:

- [Guava](https://github.com/google/guava)
- [Guice](https://github.com/google/guice)
- [Apache Commons IO](https://github.com/apache/commons-io)
- [Image4J](https://github.com/imcdonagh/image4j)
- [Zip4J](https://github.com/srikanth-lingala/zip4j)
- [flatlaf](https://github.com/JFormDesigner/FlatLaf)
- [discord-rpc](https://github.com/Vatuu/discord-rpc)
- [mslinks](https://github.com/DmitriiShamrikov/mslinks)
- [org.json](https://github.com/eskatos/org.json-java)
- [jIconFont](https://github.com/jIconFont)
- [samskivert](https://github.com/samskivert/samskivert)
- [JHLabs Filters](http://www.jhlabs.com/)
- [OSHI](https://github.com/oshi/oshi)
