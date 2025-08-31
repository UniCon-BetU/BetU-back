package org.example.crew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.crew.entity.UserCrewRole;

import java.util.List;

@Getter
@AllArgsConstructor
public class CrewResponse {
    private Long crewId;
    private String crewName;
    private String crewDescription;

    private String crewCode;
    private Boolean isPublic;

    private UserCrewRole myRole;

    private Long ownerId;
    private String ownerName;

    private Long memberCount;
    private Long challengeCount;

    private List<String> imageUrls;

    private List<String> customTags;
}
