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
}
