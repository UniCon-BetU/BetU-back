package org.example.crew;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.crew.dto.CrewCreateRequest;
import org.example.crew.dto.CrewJoinRequest;
import org.example.crew.dto.CrewRankingResponse;
import org.example.crew.dto.CrewResponse;
import org.example.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews")
public class CrewController {

    private final CrewService crewService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "그룹 생성")
    public ResponseEntity<CrewResponse> createCrew(
            HttpServletRequest request,
            @Valid @RequestBody CrewCreateRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(crewService.createCrew(userId, req));
    }

    @PostMapping("/join")
    @Operation(summary = "그룹 참가")
    public ResponseEntity<CrewResponse> joinCrew(
            HttpServletRequest request,
            @Valid @RequestBody CrewJoinRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(crewService.joinCrew(userId, req));
    }

    @DeleteMapping("/{crewId}")
    @Operation(summary = "그룹 삭제")
    public ResponseEntity<Void> deleteCrew(
            HttpServletRequest request,
            @PathVariable Long crewId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        crewService.deleteCrew(userId, crewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "전체 그룹 조회")
    public ResponseEntity<List<CrewResponse>> getAllCrews() {
        return ResponseEntity.ok(crewService.getAllCrews());
    }

    @GetMapping("/me")
    @Operation(summary = "내가 참여한 그룹 조회")
    public ResponseEntity<List<CrewResponse>> getMyCrews(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(crewService.getMyCrews(userId));
    }

    @GetMapping("/search")
    @Operation(summary = "그룹 이름으로 검색")
    public ResponseEntity<List<CrewResponse>> searchCrews(@RequestParam String keyword) {
        return ResponseEntity.ok(crewService.searchCrewsByName(keyword));
    }


    @GetMapping("/{crewId}/ranking")
    @Operation(summary = "크루 내 랭킹", description = "크루에서 챌린지를 가장 많이 한 사람 순으로 반환")
    public ResponseEntity<List<CrewRankingResponse>> getCrewRanking(@PathVariable Long crewId) {
        return ResponseEntity.ok(crewService.getCrewRanking(crewId));
    }
}
