package com.xigua.baseAPI.api.playerInfo;

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
    private String gasServerUrl = "";
    @Builder.Default
    private String webServerUrl = "";
    @Builder.Default
    private int reviewStage = 0;
    @Builder.Default
    private long proxyUid = 0L;
    @Builder.Default
    private boolean isTestServer = false;
    private UUID uuid;
}