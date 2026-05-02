package com.xigua.baseAPI.api.playerInfo;

import com.xigua.baseAPI.api.InputMode;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PlayerInfo {
    @Builder.Default
    private String gameId = "";
    @Builder.Default
    private String gameKey = "";
    @Builder.Default
    private String ShopServerUrl = "";
    @Builder.Default
    private String webServerUrl = "";
    @Builder.Default
    private long proxyUid = 0L;
    @Builder.Default
    private InputMode inputMode = null;
    @Builder.Default
    private boolean isTestServer = true;
    private UUID uuid;
}