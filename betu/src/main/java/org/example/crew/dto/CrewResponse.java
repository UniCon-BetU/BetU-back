package org.example.crew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.crew.entity.UserCrewRole;

@Getter
@AllArgsConstructor
public class CrewResponse {
    private Long crewId;
    private String crewName;
    private String crewCode;
    private Boolean isPublic;
    private UserCrewRole myRole; // 목록에 내 역할도 같이 주면 편함(전체 조회는 null 가능)
}

