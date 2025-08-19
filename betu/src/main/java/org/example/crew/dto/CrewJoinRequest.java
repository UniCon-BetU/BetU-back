package org.example.crew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrewJoinRequest {
    private Long crewId;
    private String crewCode;
}
