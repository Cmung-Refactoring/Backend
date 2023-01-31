package com.minjae.cmungrebuilding.member;

import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController  // -> @Controller + @ResponseBody
@RequestMapping("/api/auth") // /api/auth
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 일반 회원가입
    @PostMapping("/signup")
    public GlobalResponseDto<?> signup(@RequestBody @Valid MemberRequestDto memberRequestDto) {
        return memberService.signup(memberRequestDto);
    }

    // 카카오 회원가입
//    @GetMapping("/kakao/callback")
//    public  GlobalResponseDto<?> kakaoLogin(@RequestParam String code,
//                                            HttpServletResponse response) throws JsonProcessingException {
//        return memberService.kakaoLogin(code, response);
//    }
//
//    // 네이버 회원가입
//    @GetMapping("/naver/callback")
//    public GlobalResponseDto<?> naverLogin(@RequestParam code, @RequestParam String state,
//                                           HttpServletResponse response){
//
//    }
//
//
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
}
