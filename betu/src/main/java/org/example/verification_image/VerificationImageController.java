package org.example.verification_image;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verifications")
public class VerificationImageController {

    private final VerificationImageService verificationImageService;
    private final UserService userService; // getUserIdFromToken(HttpServletRequest)

    @PostMapping(path = "/{challengeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "챌린지 인증 이미지 업로드")
    public ResponseEntity<Long> uploadVerification(
            HttpServletRequest request,
            @PathVariable Long challengeId,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        Long userId = userService.getUserIdFromToken(request);
        Long id = verificationImageService.uploadVerification(userId, challengeId, image);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/pending")
    @Operation(summary = "내가 처리 가능한 인증 대기 목록 조회(관리자/크루오너)")
    public ResponseEntity<List<VerificationImageResponse>> pending(HttpServletRequest request) {
        Long me = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(verificationImageService.getMyPendingImages(me));
    }

    @PostMapping("/{imageId}/approve")
    @Operation(summary = "인증 승인(관리자/크루오너)")
    public ResponseEntity<Void> approve(HttpServletRequest request, @PathVariable Long imageId) {
        Long me = userService.getUserIdFromToken(request);
        verificationImageService.approve(me, imageId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{imageId}/reject")
    @Operation(summary = "인증 거절(관리자/크루오너)")
    public ResponseEntity<Void> reject(HttpServletRequest request, @PathVariable Long imageId) {
        Long me = userService.getUserIdFromToken(request);
        verificationImageService.reject(me, imageId);
        return ResponseEntity.ok().build();
    }
}
