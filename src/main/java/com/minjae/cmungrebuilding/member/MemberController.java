package com.minjae.cmungrebuilding.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.security.UserDetailsImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController  // -> @Controller + @ResponseBody
@RequestMapping("/api/auth") // /api/auth
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final SocialService socialService;

    // 일반 회원가입
    @PostMapping("/signup")
    public GlobalResponseDto<?> signup(@RequestBody @Valid MemberRequestDto memberRequestDto) {
        return memberService.signup(memberRequestDto);
    }

    // 카카오 회원가입
    @GetMapping("/kakao/callback")
    public  GlobalResponseDto<?> kakaoLogin(@RequestParam String code,
                                            HttpServletResponse response) throws JsonProcessingException {
        return socialService.kakaoLogin(code, response);
    }

    // 네이버 회원가입
    @GetMapping("/naver/callback")
    public GlobalResponseDto<?> naverLogin(@RequestParam String code,
                                           @RequestParam String state,
                                           HttpServletResponse response){
        return socialService.naverLogin(code, state, response);
    }


    // 아이디 중복확인
    @PostMapping("/idcheck")
    public GlobalResponseDto<?> emailCheck (@RequestBody MemberRequestDto memberRequestDto) {
        return memberService.emailCheck(memberRequestDto);
    }

    // 닉네임 중복확인
    @PostMapping("/nicknamecheck")
    public GlobalResponseDto<?> nicknameCheck (@RequestBody MemberRequestDto memberRequestDto) {
        return memberService.nicknameCheck(memberRequestDto);
    }

    // 로그인

    @PostMapping("/login")
    public GlobalResponseDto<?> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response){

        return memberService.login(loginRequestDto,response);
    }

    // 로그아웃
    @GetMapping("/logout")
    public GlobalResponseDto<?> logOut(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return memberService.logout(userDetails);
    }

    // 회원 탈퇴 기능
    @GetMapping("/")
    public GlobalResponseDto<?> signOut(@AuthenticationPrincipal UserDetailsImpl userDetails){
        memberService.signOut(userDetails.getMember());
    }
}
