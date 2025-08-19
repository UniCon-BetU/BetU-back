package org.example.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.general.jwt.JwtProvider;
import org.example.user.dto.ChangePasswordRequestDto;
import org.example.user.dto.UserLogInRequestDto;
import org.example.user.dto.UserSignUpRequestDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Long getUserIdFromToken(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").substring(7); // "Bearer "를 제외한 토큰
        return jwtProvider.extractUserId(accessToken).orElseThrow(() -> new RuntimeException("토큰에서 유저 아이디를 찾을 수 없습니다."));
    }

    public void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response) {
        // 아이디 중복 체크
        if (userRepository.findByUserName(requestDto.getUserName()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 이메일 중복 체크
        if (userRepository.findByUserEmail(requestDto.getUserEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 저장
        User user = User.builder()
                .userName(requestDto.getUserName())
                .userPassword(passwordEncoder.encode(requestDto.getUserPassword())) // 비밀번호 암호화
                .userEmail(requestDto.getUserEmail())
                .build();

        userRepository.save(user);

        // 가입하자마자 로그인 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getUserName(), user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public void login(UserLogInRequestDto requestDto, HttpServletResponse response) {
        User user = userRepository.findByUserName(requestDto.getUserName())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getUserPassword(), user.getUserPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserName(), user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequestDto requestDto) {
        Long userId = getCurrentUserId();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getUserPassword())) {
            throw new RuntimeException("기존 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
    }

    public Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userDetails.getUsername();
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("로그인 정보를 찾을 수 없습니다."));
        return user.getUserId();
    }
}
