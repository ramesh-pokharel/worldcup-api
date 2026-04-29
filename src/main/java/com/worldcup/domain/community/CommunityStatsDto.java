package com.worldcup.domain.community;

import java.util.List;

public record CommunityStatsDto(
        List<TeamFavStat>    topTeams,
        List<PlayerFavStat>  topPlayers,
        List<MatchConsensus> predictionConsensus
) {
    public interface TeamFavStat {
        Long   getTeamId();
        String getCountryName();
        String getFlagEmoji();
        String getCodeIso2();
        Long   getFavCount();
    }

    public interface PlayerFavStat {
        Long   getPlayerId();
        String getName();
        String getTeamName();
        String getCodeIso2();
        Long   getFavCount();
    }

    public interface MatchConsensus {
        Long getMatchId();
        int  getHomePct();
        int  getDrawPct();
        int  getAwayPct();
        int  getTotalPredictions();
    }
}
