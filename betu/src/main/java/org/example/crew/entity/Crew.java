package org.example.crew.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long crewId;

    private String crewName;
    private String crewCode;
    private Boolean crewIsPublic;

    public Crew(String groupName, String code, boolean isPublic) {
        this.crewName = groupName;
        this.crewCode = code;
        this.crewIsPublic = isPublic;
    }
}
