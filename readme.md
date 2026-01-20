````markdown
<div align="center">

  <img src="logo.png" alt="MuCore Logo" width="150" height="150">

# MuCore (MuSentry)

**Enterprise Hybrid AntiCheat Solution for Modern Minecraft Servers**

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spigot](https://img.shields.io/badge/API-Spigot%201.20%2B-yellow?style=for-the-badge&logo=minecraft)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-Private-red?style=for-the-badge)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Maven-blue?style=for-the-badge&logo=apachemaven)](pom.xml)

  <p>
    <a href="#features">Features</a> •
    <a href="#installation">Installation</a> •
    <a href="#commands">Commands</a> •
    <a href="#configuration">Configuration</a>
  </p>
</div>

---

## 🛡️ About The Project

**MuCore** (codenamed _MuSentry_) is a lightweight, packet-based AntiCheat designed for hybrid server environments. Unlike traditional AntiCheats, MuCore natively understands **Geyser/Bedrock** players, preventing false positives caused by protocol differences.

Built with **performance** in mind, it utilizes asynchronous logging, connection pooling (HikariCP), and efficient packet listeners (ProtocolLib).

## 🚀 Key Features

### 🌐 Hybrid Intelligence

- **Geyser Support:** Automatically detects Bedrock players and adjusts check sensitivity or bypasses specific checks (like OmniSprint/Hitbox) to prevent false bans.
- **Version Agnostic:** Built on top of ProtocolLib to support multiple Minecraft versions (1.19 - 1.21+).

### ⚔️ Combat Analysis

- **KillAura Detection:** Analyzes attack packets, rotation consistency, and click patterns.
- **Reach Check:** Calculates precise distance validation based on gamemode.
- **Velocity/Anti-KB:** Verifies if a player responds correctly to server-side knockback packets.

### 🏃 Movement Checks

- **Fly & Hover:** Detects illegal flight and air-hovering.
- **Speed:** Horizontal and vertical speed limits with friction simulation.
- **OmniSprint:** Detects sprinting in impossible directions (backwards/sideways).

### 🛡️ Packet Security (Anti-Crasher)

- **Packet Limiter:** Prevents "Lag Machines" and crash clients by limiting packets per second (PPS).
- **Exploit Prevention:** Blocks invalid `WINDOW_CLICK` and `CUSTOM_PAYLOAD` spam often used to crash servers.

### 📊 Enterprise Logging

- **Database Support:**
  - **H2 (Default):** Zero-setup, high-performance local file database.
  - **MySQL:** Full support for network/BungeeCord environments.
- **Discord Webhooks:** Real-time alerts sent directly to your staff Discord channel with detailed embed stats.

---

## 📥 Installation

1.  **Requirements:**
    - Java 17 or newer.
    - Server based on Spigot/Paper (1.20+ recommended).
    - [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (Required).
    - _(Optional)_ Geyser/Floodgate (for Bedrock support).

2.  **Setup:**
    - Download `MuCore-1.0-SNAPSHOT.jar` from the `target` folder.
    - Place it in your server's `plugins` folder.
    - Restart the server.

3.  **First Run:**
    - The plugin will generate a `config.yml` and a database folder.
    - By default, it uses **H2** (Local). No database setup is required.

---

## 💻 Commands & Permissions

| Command                 | Permission     | Description                                               |
| :---------------------- | :------------- | :-------------------------------------------------------- |
| `/mucore alerts`        | `mucore.admin` | Toggle in-game cheat notifications for yourself.          |
| `/mucore info <player>` | `mucore.admin` | View player stats (Ping, Platform, VL).                   |
| `/mucore reload`        | `mucore.admin` | Reload `config.yml` and Webhook settings without restart. |
| `/mucore status`        | `mucore.admin` | View system health, database status, and memory usage.    |

**Bypass Permission:**

- `mucore.bypass` - Grants total immunity to all checks.

---

## ⚙️ Configuration

Located at `plugins/MuCore/config.yml`.

```yaml
# Database Configuration
database:
  storage-type: "H2" # Options: "H2" or "MYSQL"
  host: "localhost"
  port: 3306
  name: "mucore_db"
  password: "securepassword"

# Webhook Integration
webhook:
  enabled: true
  url: "[https://discord.com/api/webhooks/](https://discord.com/api/webhooks/)..."
  alert_frequency: 5 # Send alert every 5 VL to prevent spam

# Check Settings
checks:
  packet_limiter:
    global_limit:
      java: 300
      bedrock: 450 # Adjusted for Geyser overhead
```
````

---

## 🛠️ Building from Source

To build this project locally, you need **Maven** and **JDK 17+**.

```bash
# 1. Clone the repository
git clone [https://github.com/username/mucore.git](https://github.com/username/mucore.git)

# 2. Navigate to directory
cd mucore

# 3. Build with Maven
mvn clean package

```

The compiled jar will be in the `target/` directory:

> `MuCore-1.0-SNAPSHOT.jar` (Use this one!)

---

## 🤝 Credits

- **Core Logic:** Built with Spigot API & ProtocolLib.
- **Database:** Powered by HikariCP & H2/MySQL.
- **Networking:** Java HTTP Client (Async Webhooks).

---

<div align="center">
    <small>Developed by Muzone Team • &copy; 2026</small>
</div>

```