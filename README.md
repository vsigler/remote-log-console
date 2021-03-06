# Remote log console

![Build](https://github.com/vsigler/remote-log-console/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/19431.svg)](https://plugins.jetbrains.com/plugin/19431)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/19431)](https://plugins.jetbrains.com/plugin/19431)

<!-- Plugin description -->
Simple plugin that adds a tool window with a console that can be connected to any simple logging service that provides
plaintext logs over TCP. For example, many embedded devices provide logs in such a way when set to debug mode.
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "remote-log-console"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/vsigler/remote-log-console/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
