package org.example.verification_image;

import lombok.RequiredArgsConstructor;
import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.UserChallenge;
import org.example.challenge.repository.ChallengeRepository;
import org.example.challenge.repository.UserChallengeRepository;
import org.example.general.S3Uploader;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationImageService {
    private final VerificationImageRepository verificationImageRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final S3Uploader s3Uploader; // S3 업로드 유틸 (이미 구현했다고 가정)
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    // 인증 이미지 업로드
    public Long uploadVerification(Long userId, Long challengeId, MultipartFile image) throws IOException {
        UserChallenge userChallenge = userChallengeRepository
                .findByUser_UserIdAndChallenge_ChallengeId(userId, challengeId)
                .orElseThrow(() -> new RuntimeException("챌린지에 참가하지 않았습니다."));

        // S3 업로드
        String prefix = String.format("challenge/%d/user/%d", challengeId, userId);
        String imageUrl = s3Uploader.uploadImageUnderPrefix(image, prefix);

        VerificationImage vi = new VerificationImage(
                null,
                userChallenge,
                imageUrl,
                LocalDateTime.now()
        );
        verificationImageRepository.save(vi);

        userChallenge.increaseProgressDay();
        userChallengeRepository.save(userChallenge);

        return vi.getCertificationImageId();
    }
}
