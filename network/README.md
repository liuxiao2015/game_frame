# Network Module - Command Routing Framework

## Overview

The Network module provides a high-performance TCP server based on Netty framework with a lightweight command routing system. This module implements a "line-text protocol" that supports command routing, session management, business thread pools, and heartbeat detection.

## Features

- **Command Routing Framework** - Route commands to specific handlers based on command names
- **Line-Text Protocol** - Simple text-based protocol: `cmd param1=value1 param2=value2`
- **Session Management** - Connection-level context with traceId injection and attribute storage
- **Business Thread Pool** - Decouple command processing from IO threads
- **Heartbeat & Idle Detection** - Built-in ping/pong commands and configurable timeouts
- **TraceId Integration** - Automatic MDC traceId generation for request tracing
- **Comprehensive Logging** - Connection and command level logging with traceId context
- **Component Integration** - Implements Component interface for SPI auto-discovery
- **Configurable Parameters** - Thread pool size, idle timeouts, and other settings via Config system

## Command Protocol

### Protocol Format
```
cmd [param1=value1] [param2=value2] [seq=sequence_number]
```

### Built-in Commands

| Command | Format | Description | Example |
|---------|--------|-------------|---------|
| echo | `echo msg=<message> [seq=<seq>]` | Echo back the message | `echo msg=hello seq=1` |
| time | `time [seq=<seq>]` | Get server timestamp | `time seq=2` |
| sum | `sum a=<num1> b=<num2> [seq=<seq>]` | Calculate sum of two numbers | `sum a=10 b=20 seq=3` |
| ping | `ping [seq=<seq>]` | Heartbeat check (returns pong) | `ping seq=4` |

### Response Format
Commands return responses in the same line-text format:
```
echo msg=hello seq=1           # Echo response
time timestamp=1703123456789 datetime=2023-12-20T15:30:56 seq=2
sum a=10 b=20 result=30 seq=3  # Sum result
pong seq=4                     # Ping response
```

### Error Handling
```
error code=UNKNOWN_COMMAND message=unknown_command_invalid
error code=PARSE_ERROR message=invalid_format
error code=INVALID_PARAMETER message=invalid_parameter_format
```

## Architecture

### Key Components

#### CommandMessage
- Immutable command representation with parsing and building capabilities
- Supports parameter validation and seq number handling
- Thread-safe design for concurrent access

#### Session
- Connection-level context management
- TraceId injection and MDC integration
- Remote address information and attribute storage
- Message sending with automatic line-ending handling

#### CommandDispatcher
- Routes commands to registered handlers
- Manages business thread pool for async processing
- Handles unknown commands and execution errors
- Configurable thread pool parameters

#### CommandHandler Interface
- Standard interface for command processing
- Receives CommandMessage and Session for processing
- Supports async execution in business threads

#### CommandProtocolHandler
- Netty channel handler replacing the original EchoHandler
- Integrates protocol parsing, session management, and command routing
- Handles connection lifecycle and idle state events

### Network Pipeline
```
IdleStateHandler -> LineBasedFrameDecoder -> StringDecoder -> StringEncoder -> CommandProtocolHandler
```

1. **IdleStateHandler** - Monitors connection idle state for heartbeat
2. **LineBasedFrameDecoder** - Splits incoming data by line breaks
3. **StringDecoder/Encoder** - Converts between bytes and UTF-8 strings
4. **CommandProtocolHandler** - Processes commands and manages sessions

## Configuration

### Server Configuration
- `server.port` - TCP port to bind (default: 7001)
- `server.host` - Host address to bind (default: 0.0.0.0)

### Business Thread Pool
- `business.thread.core` - Core thread count (default: 4)
- `business.thread.max` - Maximum thread count (default: 16)
- `business.thread.keepalive` - Thread keep-alive seconds (default: 60)
- `business.thread.queue` - Queue size (default: 1000)

### Idle Detection
- Reader idle: 60 seconds (disconnect if no data received)
- Writer idle: 30 seconds (send ping if no data sent)

## Usage Example

### Starting the Server
```java
// Using the LauncherProtocolDemo
java -cp ... com.game.launcher.LauncherProtocolDemo
```

### Testing with Telnet/NC
```bash
# Connect to server
telnet localhost 7001

# Send commands
echo msg=hello seq=1
time seq=2
sum a=10 b=20 seq=3
ping seq=4
quit
```

### Adding Custom Commands

```java
// 1. Implement CommandHandler
public class CustomCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandMessage request, Session session) {
        String param = request.getParam("param");
        CommandMessage response = CommandMessage.builder("custom")
            .param("result", processParam(param))
            .seq(request.getSeq())
            .build();
        session.sendMessage(response);
    }
}

// 2. Register in CommandProtocolHandler
dispatcher.registerHandler("custom", new CustomCommandHandler());
```

## Module Structure

```
network/
├── pom.xml                                    # Module dependencies
└── src/main/java/com/game/network/
    ├── NettyServerComponent.java              # Main server component
    ├── NettyServerInitializer.java            # Channel pipeline configuration
    ├── CommandProtocolHandler.java            # Command protocol handler
    │
    ├── protocol/
    │   └── CommandMessage.java                # Command message parsing/building
    │
    ├── session/
    │   └── Session.java                       # Session management
    │
    ├── dispatcher/
    │   └── CommandDispatcher.java             # Command routing dispatcher
    │
    ├── handler/
    │   ├── CommandHandler.java                # Handler interface
    │   └── impl/
    │       ├── EchoCommandHandler.java        # Echo command implementation
    │       ├── TimeCommandHandler.java        # Time command implementation
    │       ├── SumCommandHandler.java         # Sum command implementation
    │       └── PingCommandHandler.java        # Ping/heartbeat handler
    │
    └── package-info.java                      # Module documentation
└── src/main/resources/META-INF/services/
    └── com.game.common.component.Component    # SPI configuration
```

## Integration Points

The Network module integrates seamlessly with the existing architecture:

1. **Component System** - Implements Component interface for lifecycle management
2. **Configuration System** - Uses Config interface for all configuration parameters
3. **Logging System** - Uses SLF4J with MDC for traceId injection
4. **SPI Discovery** - Automatically discovered and loaded by ComponentManager

## Testing

### Manual Testing
Use the provided `LauncherProtocolDemo` to start the server and test with telnet:

```bash
# Terminal 1: Start server
mvn -q -DskipTests package
java -cp ... com.game.launcher.LauncherProtocolDemo

# Terminal 2: Test commands
echo -e "echo msg=test\ntime\nsum a=5 b=3\nping\nquit" | nc localhost 7001
```

### Expected Output
```
欢迎连接到 Game Frame 指令服务器! (traceId: abc123)
支持的指令: echo, time, sum, ping
协议格式: cmd [k=v]...
示例: echo msg=hello seq=1
输入 'quit' 或 'exit' 可断开连接。
echo msg=test
time timestamp=1703123456789 datetime=2023-12-20T15:30:56
sum a=5 b=3 result=8
pong
再见!
```

This design ensures the network module provides a robust, extensible command routing framework while maintaining compatibility with the existing component architecture.