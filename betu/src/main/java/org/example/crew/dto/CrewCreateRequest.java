package org.example.crew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrewCreateRequest {
    private String crewName;
    private Boolean isPublic;
}
