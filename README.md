# 基础API使用指南

## 概述
包含一些基础API，方便开发服务器

提示，某些功能可能需要搭配这个[Geyser](https://github.com/zhiyunxigua/Geyser)才能正常使用

## 目录
1. [添加依赖](#添加依赖)
2. [API 获取与初始化](#API-获取与初始化)
3. [核心 API 方法详解](#核心-API-方法详解)
4. [监听方法详解](#监听方法详解)
5. [构造Form](docs/FORM.md)

## 添加依赖

### Maven
#### Repository:
```xml
<repositories>
    <repository>
        <id>xigua-maven-repo</id>
        <url>https://raw.githubusercontent.com/zhiyunxigua/maven-repo/master/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

#### Dependencies:
```xml
<dependencies>
    <dependency>
        <groupId>com.xigua</groupId>
        <artifactId>BaseAPI</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle
#### Repository:
```kts
repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/zhiyunxigua/maven-repo/master/")
} 
```

#### Dependencies:
```kts
dependencies {
    compileOnly("com.xigua:BaseAPI:1.0.0")
}
```

## API 获取与初始化

### 前置条件
确保您的插件 `depend` 或 `softdepend` 了 `BaseAPI` 插件。

### 初始化
在您插件的主类 `onEnable` 方法中，通过 BaseAPI 的实例调用API。

```java
import com.xigua.baseAPI.BaseAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {
    private BaseAPI baseAPI;

    @Override
    public void onEnable() {
        BaseAPI baseAPI = (BaseAPI) getServer().getPluginManager().getPlugin("BaseAPI");

        getLogger().info("baseAPI 已成功加载。");
    }
}
```
## 核心-API-方法详解

### notifyToClient
`给指定玩家发送服务端事件`

| 参数名       | 类型                  | 说明                                  |
|-----------|---------------------|-------------------------------------|
| player    | Player              | 接收事件的玩家                             |
| namespace | String              | 在客户端系统使用ListenForEvent监听的namespace  |
| system    | String              | 在客户端系统使用ListenForEvent监听的systemName |
| event     | String              | 事件名                                 |
| data      | Map<String, Object> | 事件参数。注意，要使用-2指代本地玩家的entityId。       |

### notifyToMultiClients
`给多个玩家发送服务端事件。 因为-2的entityId对于不同玩家来说都指代本机玩家，而非某个固定的实体，所以不要在多播中发送这种信息。`

| 参数名       | 类型                  | 说明        |
|-----------|---------------------|-----------|
| players   | List<Player>        | 接收事件的玩家列表 |
| namespace | String              | 同上        |
| system    | String              | 同上        |
| event     | String              | 同上        |
| data      | Map<String, Object> | 同上        |

### notifyToClientsNearby
`给某个位置附近一定半径内的所有玩家发送服务端事件。 因为-2的entityId对于不同玩家来说都指代本机玩家，而非某个固定的实体，所以不要在多播中发送这种信息。`

| 参数名       | 类型                  | 说明                        |
|-----------|---------------------|---------------------------|
| except    | Player              | 发送事件时排除掉这个玩家，可以为null表示不排除 |
| loc       | Location            | 圆心位置                      |
| dist      | double              | 半径                        |
| namespace | String              | 同上                        |
| system    | String              | 同上                        |
| event     | String              | 同上                        |
| data      | Map<String, Object> | 同上                        |

### broadcastToAllClient
`给某个world内的所有玩家发送服务端事件。 因为-2的entityId对于不同玩家来说都指代本机玩家，而非某个固定的实体，所以不要在多播中发送这种信息。`

| 参数名       | 类型                  | 说明      |
|-----------|---------------------|---------|
| except    | Player              | 同上      |
| world     | World               | 所在world |
| namespace | String              | 同上      |
| system    | String              | 同上      |
| event     | String              | 同上      |
| data      | Map<String, Object> | 同上      |

### broadcastToAllClient
`给服务器内的所有玩家发送服务端事件。 因为-2的entityId对于不同玩家来说都指代本机玩家，而非某个固定的实体，所以不要在多播中发送这种信息。`

| 参数名       | 类型                  | 说明      |
|-----------|---------------------|---------|
| except    | Player              | 同上      |
| namespace | String              | 同上      |
| system    | String              | 同上      |
| event     | String              | 同上      |
| data      | Map<String, Object> | 同上      |

### openShop
`打开指定玩家商城界面`

| 参数名    | 类型                  | 说明 |
|--------|---------------------|----|
| player | Player              | 玩家 |

### closeShop
`关闭指定玩家商城界面`

| 参数名    | 类型                  | 说明 |
|--------|---------------------|----|
| player | Player              | 玩家 |

### getPlayerOrderList
`获取玩家未发货订单列表`

| 参数名      | 类型                                  | 说明                 |
|----------|-------------------------------------|--------------------|
| player   | Player                              | 玩家                 |
| callback | FutureCallback<Map<String, Object>> | FutureCallBack回调函数 |

- 示例: 回调参数为Map<String,Object>, 目前值为


| Key         | Value    | 解释 |
|-------------|----------|----|
| player      | Player   | 玩家 |
| json_result | 订单json数据 | 玩家 |


### finPlayerOrder
`通知网易服务器完成指定玩家订单`

| 参数名       | 类型                                  | 说明     |
|-----------|-------------------------------------|--------|
| player    | Player                              | 玩家     |
| orderList | List<String>                        | 订单id列表 |
| callback  | FutureCallback<Map<String, Object>> | 同上     |

### sendForm
`发送formui给指定玩家`

| 参数名    | 类型   | 说明   |
|--------|------|------|
| uuid | UUID | 玩家   |
| form   | Form | 表单实例 |

### sendForm
`发送formui给指定玩家`

| 参数名         | 类型                   | 说明           |
|-------------|----------------------|--------------|
| uuid      | UUID               | 玩家           |
| formBuilder | FormBuilder<?, ?, ?> | 表单builder类实例 |

### transferPlayer
`将玩家传送到指定的服务器。`

| 参数名         | 类型     | 说明           |
|-------------|--------|--------------|
| uuid        | UUID   | 玩家           |
| address | String | 目标服务器IP地址 |
| port | int    | 目标服务器端口 |

## 监听方法详解

目前有4个事件可以监听，事件在`com.xigua.baseAPI.api.events`,4个事件分别是

| 事件名                          | 说明             |
|------------------------------|----------------|
| `NeteasePythonEvent`         | 客户端Python事件    |
| `ClientLoadAddonFinishEvent` | 玩家客户端加载Mod完成事件 |
| `PlayerBuyItemSuccessEvent`  | 玩家购买成功事件       |
| `PlayerUrgeShipEvent`        | 玩家催发货事件        |

使用示例
```java
import com.xigua.baseAPI.api.events.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class EventListener implements Listener {
    
  @EventHandler
  public void onNeteasePython(NeteasePythonEvent event) {
    Player player = event.player;
    String namespace = event.namespace;
    String systemName = event.systemName;
    String eventName = event.eventName;
    Map<String, Object> data = event.data;
  }

  @EventHandler
  public void onClientLoadFinish(ClientLoadAddonFinishEvent event) {
    Player player = event.player;
  }

  @EventHandler
  public void onPlayerBuySuc(PlayerBuyItemSuccessEvent event) {
    Player player = event.player;
  }

  @EventHandler
  public void onPlayerUrgeShip(PlayerUrgeShipEvent event) {
    Player player = event.player;
  }

}

```