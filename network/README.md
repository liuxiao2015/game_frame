# Network Module - Netty TCP Server

## Overview

The Network module provides a high-performance TCP server based on Netty framework, designed as a reusable component that integrates with the existing Component and Config system. This module demonstrates a "line-delimited" text protocol with Echo functionality for easy testing with telnet or nc.

## Features

- **High-performance TCP server** - Built on Netty framework
- **Line-delimited text protocol** - Easy to test with telnet/nc commands
- **Graceful shutdown** - Proper resource cleanup and thread pool shutdown
- **TraceId injection** - Automatic MDC traceId generation for each connection
- **Comprehensive logging** - Connection and message level logging
- **Component integration** - Implements Component interface for SPI auto-discovery
- **Configurable port** - Server port configurable via Config system (default: 7001)

## Module Structure

```
network/
├── pom.xml                                    # Module dependencies (Netty, common, slf4j)
└── src/main/java/com/game/network/
    ├── NettyServerComponent.java              # Main server component
    ├── NettyServerInitializer.java            # Channel pipeline configuration
    ├── EchoHandler.java                       # Message handler with Echo functionality
    └── package-info.java                      # Module documentation
└── src/main/resources/META-INF/services/
    └── com.game.common.component.Component    # SPI configuration file
```

## Key Components

### NettyServerComponent
- Implements the Component interface
- Handles server lifecycle (init, start, stop)
- Reads configuration for server port
- Manages Netty EventLoopGroups for Boss and Worker threads

### NettyServerInitializer
- Configures the Netty ChannelPipeline
- Sets up line-based frame decoder (max 8KB)
- Configures UTF-8 string encoder/decoder
- Attaches the EchoHandler

### EchoHandler
- Handles incoming connections and messages
- Generates unique traceId for each connection
- Provides Echo functionality (returns messages with "Echo: " prefix)
- Supports graceful disconnection with 'quit' or 'exit' commands
- Logs all connection and message events

## Configuration

The server reads its configuration from the Config system:

- `server.port` - TCP port to bind (default: 7001)

Current configuration files already include:
- `application.properties`: `server.port=7000`
- `application-dev.properties`: `server.port=7001` (overrides default)

## Running the Server

### Using SPI Auto-discovery (Recommended)

```bash
# Build the project
mvn clean install -DskipTests

# Run the SPI demo launcher
mvn -q exec:java -Dexec.mainClass="com.game.launcher.LauncherSpiDemo" -pl launcher
```

The server will automatically start and display:
```
Netty 服务器启动成功，监听端口: 7001
可使用以下命令进行测试:
  telnet localhost 7001
  nc localhost 7001
```

### Manual Registration

```java
ComponentManager manager = new ComponentManager();
manager.register(new NettyServerComponent());
manager.initAll();
manager.startAll();
```

## Testing and Verification

### Using netcat (nc)

```bash
# Connect to the server
nc localhost 7001

# You'll see a welcome message with traceId
# Type messages and they will be echoed back
# Type 'quit' or 'exit' to disconnect
```

### Using telnet

```bash
# Connect to the server
telnet localhost 7001

# Type messages and see them echoed back
# Type 'quit' or 'exit' to disconnect gracefully
```

### Example Session

```bash
$ nc localhost 7001
欢迎连接到 Game Frame 服务器! (traceId: ce090079f44f4263)
您可以输入任何文本，服务器将原样回显。
输入 'quit' 或 'exit' 可断开连接。
hello world
Echo: hello world
test message  
Echo: test message
quit
再见!
```

## Logging Features

The network module provides comprehensive logging with traceId support:

### Connection Logs
```
[nioEventLoopGroup-3-1] INFO [ce090079f44f4263] com.game.network.EchoHandler - 新连接建立 - 客户端地址: 127.0.0.1:54321
[nioEventLoopGroup-3-1] INFO [ce090079f44f4263] com.game.network.EchoHandler - 连接断开
```

### Message Logs
```
[nioEventLoopGroup-3-1] INFO [ce090079f44f4263] com.game.network.EchoHandler - 接收消息: hello world
[nioEventLoopGroup-3-1] INFO [ce090079f44f4263] com.game.network.EchoHandler - 发送响应: Echo: hello world
```

### Server Lifecycle Logs
```
INFO c.game.network.NettyServerComponent - 正在初始化 Netty 服务器组件...
INFO c.game.network.NettyServerComponent - 服务器端口配置: 7001
INFO c.game.network.NettyServerComponent - Netty 服务器启动成功，监听端口: 7001
INFO c.game.network.NettyServerComponent - 正在停止 Netty 服务器...
INFO c.game.network.NettyServerComponent - Netty 服务器停止完成
```

## Protocol Specification

The server implements a simple line-delimited text protocol:

- **Frame format**: Messages are delimited by newline characters (`\n` or `\r\n`)
- **Encoding**: UTF-8 character encoding
- **Max frame size**: 8192 bytes per message
- **Echo functionality**: Server responds with "Echo: " prefix
- **Disconnect commands**: 'quit' or 'exit' (case-insensitive) trigger graceful disconnection

## Dependencies

- **Netty 4.1.104.Final** - High-performance network framework
- **SLF4J 2.0.13** - Logging facade
- **Common module** - Component interface and configuration system

## Architecture Integration

The Network module integrates seamlessly with the existing architecture:

1. **Component System**: Implements Component interface for lifecycle management
2. **Configuration System**: Uses Config interface to read server port
3. **Logging System**: Uses SLF4J with MDC for traceId injection
4. **SPI Discovery**: Automatically discovered and loaded by ComponentManager

This design ensures the network module is a reusable component that follows the established patterns and can be easily extended for more complex protocols in the future.