# smp-dynmap

A custom, fully self-contained Dynmap implementation for **Minecraft 1.21.1 (Spigot API)**.

## Features

| Feature | Details |
|---|---|
| 🗺 **Top-down map rendering** | Chunks rendered to 128×128 PNG tiles with slope-based shading and per-block colour mapping for 300+ block types |
| 🔍 **Zoom levels** | Configurable zoom (default 4 levels; higher = more zoomed-out tiles auto-generated) |
| 👤 **Player tracking** | Real-time player positions, health & armour broadcast to web clients |
| 💬 **Chat relay** | In-game chat shown in the web viewer; web visitors can send messages in-game |
| 📍 **Markers** | Named waypoints with icon support, persisted to `markers.json`; full marker-set system |
| 🌍 **Multi-world** | Overworld, Nether & End all rendered independently |
| 🌐 **Built-in HTTP server** | JDK `HttpServer` on port `8123` (configurable); no external process needed |
| 🔌 **Tile invalidation** | Block place/break/explode/burn events automatically re-queue affected tile |
| ⚙️ **Commands** | `/dynmap` + `/dmarker` with tab-completion and permission nodes |

## Requirements

- Java 21 (Minecraft 1.21.1 requires Java 21 at runtime)
- Spigot 1.21.1 (or any fork that exposes the Spigot API 1.21.1)

## Building

```bash
# Install Spigot API into your local Maven repo first (BuildTools)
java -jar BuildTools.jar --rev 1.21.1

# Then build the plugin JAR
mvn clean package
# Output: target/smp-dynmap-1.0.0.jar
```

Copy `target/smp-dynmap-1.0.0.jar` to your server's `plugins/` directory.

## Web API

The built-in server exposes the following endpoints (default port `8123`):

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Web viewer (`index.html`) |
| `GET` | `/up/configuration` | Server/world configuration JSON |
| `GET` | `/up/world/{world}/{timestamp}` | Live update JSON (players, chat, tile changes) since `timestamp` (ms) |
| `GET` | `/up/markers/{world}` | Marker sets + markers JSON |
| `POST` | `/up/sendchat` | Send a web-chat message (`{"name":"…","message":"…"}`) |
| `GET` | `/tiles/{world}/{map}/{x}_{z}.png` | Rendered map tile (zoom 0) |
| `GET` | `/tiles/{world}/{map}/z/{x}_{z}.png` | Zoom-1 tile (2× zoom-out) |
| `GET` | `/tiles/{world}/{map}/zz/{x}_{z}.png` | Zoom-2 tile (4× zoom-out), etc. |

### Tile coordinate system

Tile `(tX, tZ)` covers world blocks `[tX×128 … tX×128+127]` × `[tZ×128 … tZ×128+127]`.  
At zoom level `z`, each tile covers `2^z × 128` blocks.

## Commands

### `/dynmap`
| Subcommand | Description |
|---|---|
| `render` | Re-render the tile under your feet |
| `fullrender` | Queue all loaded chunks for re-render |
| `cancelrender` | Cancel pending renders |
| `pause` / `resume` | Pause or resume background rendering |
| `reload` | Reload `config.yml` |
| `show` / `hide` | Show or hide yourself on the map |
| `stats` | Show render queue statistics |

### `/dmarker`
| Subcommand | Description |
|---|---|
| `add <id> [label] [icon]` | Add a marker at your position |
| `delete <id>` | Delete a marker |
| `list` | List all markers |
| `set <id> label\|icon <value>` | Update a marker field |
| `addset <id> [label]` | Create a new marker set |
| `deleteset <id>` | Delete a marker set |
| `listsets` | List all marker sets |

## Permissions

| Node | Default | Description |
|---|---|---|
| `smpdynmap.admin` | `op` | Full administration access |
| `smpdynmap.marker` | `op` | Manage markers |
| `smpdynmap.webchat` | `true` | Send chat from the web |
| `smpdynmap.show` | `true` | Show yourself on the map |
| `smpdynmap.hide` | `true` | Hide yourself from the map |

## Configuration

See `config.yml` for all options (HTTP port, render threads, zoom levels, worlds, chat, players, markers).

## Frontend development

The plugin ships a minimal `index.html` at `/` that demonstrates the full API.  
You can override any file by placing it in `plugins/SmpDynmap/web/` — files there take precedence over the bundled ones.
