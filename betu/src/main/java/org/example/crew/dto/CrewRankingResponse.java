package org.example.crew.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrewRankingResponse {
    private Long userId;
    private String userName;
    private Long challengeCount;
    private int rank;
}
