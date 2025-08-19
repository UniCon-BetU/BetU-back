package org.example.general.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
@Transactional
public class JwtProvider {


    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String LOGIN_ID_CLAIM = "loginId";
    private static final String BEARER = "Bearer ";
    private static final String USER_ID_CLAIM = "userId";

    private final UserRepository userRepository;

    // accessToken 생성
    public String createAccessToken(String userName, Long userId) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                .withClaim(LOGIN_ID_CLAIM, userName)
                .withClaim(USER_ID_CLAIM, userId)
                .sign(Algorithm.HMAC512(secretKey));
    }

    // refreshToken 생성
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    // accessToken header 통해 전송
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader, accessToken);
        log.info("발급된 Access Token : {}", accessToken);
    }

    // accessToken & refreshToken header 통해 전송
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("accesstoken: " + accessToken + "refreshtoken" + refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    // header에서 refreshToken 추출
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, "")); // 'Bearer XXX' 형식에서 "Bearer"를 삭제
    }

    // header에서 accessToken 추출
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    // accessToken에서 userName 추출
    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(LOGIN_ID_CLAIM)
                    .asString());
        } catch (Exception e) {
            log.error("액세스 토큰이 유효하지 않습니다.as");
            return Optional.empty();
        }
    }

    // accessToken에서 userId 추출
    public Optional<Long> extractUserId(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(USER_ID_CLAIM)
                    .asLong());
        } catch (Exception e) {
            log.error("유효하지 않은 accessToken입니다.");
            return Optional.empty();
        }
    }

    // accessToken header 설정
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    // refreshToken header 설정
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    // refreshToken db에 업데이트
    public void updateRefreshToken(Long userId, String refreshToken) {
        userRepository.findByUserId(userId)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> {
                            throw new RuntimeException("일치하는 회원이 없습니다.");
                        });
    }

    // token 유효성 확인
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            log.info("유효한 토큰입니다.");
            return true;
        } catch (JWTVerificationException e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            throw new JWTVerificationException("토큰이 유효하지 않습니다.");
        }
    }

    // accesstoken 유효성 확인
    public boolean isAccessTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            log.info("유효한 엑세스 토큰입니다.");
            return true;
        } catch (JWTVerificationException e) {
            log.error("유효하지 않은 엑세스 토큰입니다. {}", e.getMessage());
            throw new JWTVerificationException("엑세스 토큰이 유효하지 않습니다.");
        }
    }

    // refreshtoken 유효성 확인
    public boolean isRefreshTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            log.info("유효한 리프레시 토큰입니다.");
            return true;
        } catch (JWTVerificationException e) {
            log.error("유효하지 않은 리프레시 토큰입니다. {}", e.getMessage());
            throw new JWTVerificationException("리프레시 토큰이 유효하지 않습니다.");
        }
    }
}




