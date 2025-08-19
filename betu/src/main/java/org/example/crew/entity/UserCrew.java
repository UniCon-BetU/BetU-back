package org.example.crew.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.user.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCrew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userGroupId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "crewId")
    private Crew crew;

    @Enumerated(EnumType.STRING)
    private UserCrewRole userCrewRole;

    public UserCrew(User user, Crew crew, UserCrewRole userCrewRole) {
        this.user = user;
        this.crew = crew;
        this.userCrewRole = userCrewRole;
    }
}
