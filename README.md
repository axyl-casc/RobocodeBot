# RobocodeBot
Simple and fun Robocode Bot Demo

## Building

Use `compile.bat` (requires Java 11 or newer) to compile the Java sources on Windows:

```bat
compile.bat
```

This script compiles `TankBot.java` and `TargetLocator.java` using the
libraries found in the `lib` folder one level above this project.


This command starts the bot with the compiled classes and included library.

### Connecting to the game server

The bot communicates with a Tank Royale game server via WebSocket. Ensure the
`SERVER_URL` and `SERVER_SECRET` environment variables match the server
configuration. `TankBot.cmd` provides default values of `ws://localhost:7654`
and `SUMu9ukWcUUteXLidcLHZOFZWg+R2u9VGUb7bVaA3K`, which match the secret from a
default Tank Royale server configuration.

If the server uses a different secret, either set the environment variable
directly or create a `.env` file. An example file is provided as `Bot/.env.example`.
Copy it to `Bot/.env` and edit the values:

```cmd
copy Bot\.env.example Bot\.env
rem edit Bot\.env and adjust SERVER_SECRET if needed
TankBot.cmd
```
