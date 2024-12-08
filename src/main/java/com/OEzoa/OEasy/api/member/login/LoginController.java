package com.OEzoa.OEasy.api.member.login;

import com.OEzoa.OEasy.application.member.KakaoMemberService;
import com.OEzoa.OEasy.application.member.MemberService;
import com.OEzoa.OEasy.application.member.dto.MemberLoginDTO;
import com.OEzoa.OEasy.application.member.dto.MemberLoginResponseDTO;
import com.OEzoa.OEasy.infra.api.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Login API", description = "일반 로그인 및 카카오 로그인을 제공합니다.")
@RequestMapping("/login")
public class LoginController {

    private final MemberService memberService;
    private final KakaoMemberService kakaoMemberService;
    private final KakaoService kakaoService;

    // 일반 로그인 (JWT 활용)
    @PostMapping("/oeasy")
    @Operation(
            summary = "일반 로그인",
            description = "일반 로그인을 처리하고 JWT 토큰을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공. JWT 토큰 반환."),
                    @ApiResponse(responseCode = "401", description = "로그인 실패.")
            }
    )
    public ResponseEntity<MemberLoginResponseDTO> login(@RequestBody MemberLoginDTO memberLoginDTO, HttpSession session) {
        MemberLoginResponseDTO responseDTO = memberService.login(memberLoginDTO, session);
        return ResponseEntity.ok(responseDTO);
    }

    /// 카카오 로그인 콜백 - POST 요청으로 인가 코드 전달받기 (쿼리 파라미터 사용)
    @PostMapping("/kakao/callback")
    @Operation(
            summary = "카카오 API 로그인",
            description = "카카오 API를 통해 사용자 정보를 받아 로그인을 처리하고 JWT를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공. JWT 토큰 반환."),
                    @ApiResponse(responseCode = "400", description = "알 수 없는 오류 발생.")
            }
    )
    public ResponseEntity<MemberLoginResponseDTO> kakaoCallback(@RequestParam("code") String code, HttpSession session) {
        try {
            MemberLoginResponseDTO responseDTO = kakaoMemberService.loginWithKakao(code, session);

            // 반환되는 정보 확인 로그 추가
            log.info("카카오 로그인 응답: accessToken={}, email={}, nickname={}, refreshToken={}",
                    responseDTO.getAccessToken(),
                    responseDTO.getRefreshToken(),
                    responseDTO.getEmail(),
                    responseDTO.getNickname());

            // 프론트로 응답
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            log.error("카카오 로그인 중 오류 발생:", e);
            return ResponseEntity.badRequest().body(null); // 오류 발생 시 ResponseEntity를 반환
        }
    }

    // 카카오 로그인 URL 제공
    @GetMapping("/kakao")
    @Operation(
            summary = "카카오 로그인 페이지 URL 제공",
            description = "카카오 로그인 URL링크를 제공합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 로그인 페이지 URL 반환.")
            }
    )
    public ResponseEntity<String> getKakaoLoginUrl() {
        return ResponseEntity.ok(kakaoService.getKakaoLogin());
    }
}
