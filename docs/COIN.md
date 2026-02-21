# 货币API使用指南
## 核心 API 方法


## 1. 查询硬币数量
```CompletableFuture<Double> getCoins(Player player)```

```CompletableFuture<Double> getCoins(UUID uuid)```

```double getCoinsSync(Player player)```

说明：获取指定玩家的硬币余额。

参数：```player``` - 玩家对象；```uuid``` - 玩家UUID。

返回：异步方法返回一个包含余额的 ```CompletableFuture<Double>；```同步方法直接返回 ```double```。

## 2. 修改硬币数量
```CompletableFuture<Boolean> addCoins(Player player, double amount)```

说明：为玩家增加指定数量的硬币。

参数：```player``` - 玩家对象；```amount``` - 要增加的正数数量。

返回：操作是否成功。

```CompletableFuture<Boolean> removeCoins(Player player, double amount)```

说明：从玩家处扣除指定数量的硬币。内部会检查余额是否充足。

参数：```player``` - 玩家对象；```amount``` - 要扣除的正数数量。

返回：操作是否成功（余额不足也会返回 false）。

```CompletableFuture<Boolean> setCoins(Player player, double amount)```

说明：将玩家的硬币余额直接设置为指定数量。

参数：```player``` - 玩家对象；```amount``` - 要设置的新数量。

返回：操作是否成功。

## 3. 经济操作与检查
```CompletableFuture<Boolean> hasEnoughCoins(Player player, double amount)```

说明：检查玩家的硬币余额是否大于或等于指定数量。

参数：```player``` - 玩家对象；```amount``` - 需要检查的金额。

返回：true 表示余额充足，false 表示不足。

```CompletableFuture<Boolean> transferCoins(Player from, Player to, double amount)```

说明：在两名玩家之间转账。操作是原子性的（在一个数据库事务中完成）。

参数：

```from``` - 转出硬币的玩家。

```to``` - 转入硬币的玩家。

```amount``` - 转账金额。

返回：转账是否成功。如果 ```from``` 余额不足，则直接返回 ```false``` 且不会进行任何操作。

## 4. 充值相关功能
这些方法用于管理玩家的“充值总额”，通常用于记录真实货币消费或发放福利。

```CompletableFuture<Boolean> addRechargedAmount(Player player, double amount)```

```CompletableFuture<Boolean> removeRechargedAmount(Player player, double amount)```

```CompletableFuture<Double> getRechargedAmount(Player player)```

## 5. 工具方法
```String formatCoins(double coins)```

说明：将硬币数量格式化为美观的字符串（例如：“1,234 硬币”）。

参数：```coins``` - 硬币数量。

返回：格式化后的字符串。

```String getCoinName()```

说明：获取硬币的显示名称。

返回：当前硬币的名称（例如：“硬币”）。