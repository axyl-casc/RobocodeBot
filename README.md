# Infinite Mind Pictures Robocode Bot
Simple and fun Robocode Bot Demo

## Building

Use `compile.bat` (requires Java 11 or newer) to compile the Java sources on Windows:

```bat
compile.bat
```

This script compiles `TankBot.java` and `TargetLocator.java` using the
libraries found in the `lib` folder one level above this project.


This command starts the bot with the compiled classes and included library.

Alternatively run `build.bat` to create a runnable `TankBot.jar`. When executed
the jar opens a small dialog asking for the server URL and secret before
connecting.


```cmd
copy Bot\.env.example Bot\.env
rem edit Bot\.env and adjust SERVER_SECRET if needed
TankBot.cmd
```

## Documentation

Generated API documentation can be found in the `docs` folder. If serving the project with GitHub Pages, navigate to `docs/` to view the Javadoc and additional information. The main entry point is [docs/index.html](docs/index.html).
