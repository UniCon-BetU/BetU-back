package org.example.crew.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long crewId;

    private String crewName;
    private String crewDescription;

    private String crewCode;
    private Boolean crewIsPublic;

    @ElementCollection
    @CollectionTable(name = "crew_custom_tags",
            joinColumns = @JoinColumn(name = "crew_id"))
    @Column(name = "custom_tag", nullable = false, length = 50)
    private Set<String> customTags = new HashSet<>();

    public Crew(String groupName, String description, String code, boolean isPublic) {
        this.crewName = groupName;
        this.crewDescription = description;
        this.crewCode = code;
        this.crewIsPublic = isPublic;
    }

    public void addCustomTags(Iterable<String> tags) {
        if (tags == null) return;
        for (String t : tags) {
            if (t != null && !t.isBlank()) {
                this.customTags.add(t.trim());
            }
        }
    }
}
