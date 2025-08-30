package org.example.user;

import jakarta.servlet.http.HttpServletRequest;
import org.example.user.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입 1단계")
    @SecurityRequirement(name = "")
    @PostMapping(value = "/signup/step1", consumes = "application/json")
    public ResponseEntity<Void> signUpStep1(
            @RequestBody UserSignUpStep1Request requestDto,
            HttpServletResponse response
    ) {
        userService.signUpStep1(requestDto, response);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "회원가입 2단계")
    @SecurityRequirement(name = "")
    @PostMapping(value = "/signup/step2", consumes = "application/json")
    public ResponseEntity<Void> completeSignUp(
            HttpServletRequest request,
            @RequestBody UserSignUpStep2Request requestDto
    ) {
        Long userId = userService.getUserIdFromToken(request);
        userService.completeSignUp(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인")
    @SecurityRequirement(name = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "로그인 실패 (아이디/비밀번호 불일치)")
    })
    @PostMapping("/login")
    public String login(@RequestBody UserLogInRequestDto requestDto, HttpServletResponse response) {
        userService.login(requestDto, response);
        return "로그인 성공!";
    }

    @Operation(summary = "비밀번호 변경")
    @SecurityRequirement(name = "accessToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 변경 실패 (기존 비밀번호 불일치)")
    })
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequestDto requestDto) {
        userService.changePassword(requestDto);
        return ResponseEntity.ok("비밀번호 변경 성공");
    }

    @GetMapping("/points")
    @Operation(summary = "유저 포인트 확인")
    public ResponseEntity<Long> getUserPoint(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        long point = userService.getUserPoint(userId);
        return ResponseEntity.ok(point);
    }

    @PostMapping("/points/grant")
    @Operation(summary = "(테스트/관리자) 유저에게 포인트 추가")
    public ResponseEntity<Long> grantTestPoint(
            HttpServletRequest request, @RequestParam long amount
    ) {
        Long userId = userService.getUserIdFromToken(request);
        long point = userService.grantTestPoint(userId, amount);
        return ResponseEntity.ok(point);
    }

    @PostMapping("/admin")
    public ResponseEntity<String> promoteToAdmin(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        userService.promoteToAdmin(userId);
        return ResponseEntity.ok("User " + userId + " is now an ADMIN.");
    }
}

