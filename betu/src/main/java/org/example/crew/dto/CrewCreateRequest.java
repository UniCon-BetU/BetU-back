package org.example.crew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class CrewCreateRequest {
    private String crewName;
    private Boolean isPublic;
    private String crewDescription;

    private List<String> customTags;
}
