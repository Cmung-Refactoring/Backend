package com.minjae.cmungrebuilding.member;

import com.minjae.cmungrebuilding.Token.RefreshToken;
import com.minjae.cmungrebuilding.Token.RefreshTokenRepository;
import com.minjae.cmungrebuilding.Token.TokenDto;
import com.minjae.cmungrebuilding.exception.CustomException;
import com.minjae.cmungrebuilding.exception.ErrorCode;
import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.jwtutil.JwtUtil;
import com.minjae.cmungrebuilding.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Http2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    //회원가입
    @Transactional
    public GlobalResponseDto<?> signup(MemberRequestDto memberRequestDto) {

        //이메일이나 닉네임이 중복되었다면 예외 발생시키기
        if (memberRepository.existsByEmail(memberRequestDto.getEmail()) || memberRepository.existsByNickname(memberRequestDto.getNickname())){
            //ToDo: 2023. 1 .30. 예외코드 정리하여 수정하기
            throw new IllegalArgumentException();
        }

        //비밀번호 확인여부 틀리면 바로 예외발생시키기
        if (!memberRequestDto.getPassword().equals(memberRequestDto.getPasswordCheck())) {
            //ToDo: 2023. 1 .30. 예외코드 정리하여 수정하기
            throw new IllegalArgumentException();
        }

        //넘겨받은 회원 정보를 바탕으로 새로운 회원 Entity 생성 (패스워드는 인코딩 된 패스워드를 저장)
        Member member = new Member(memberRequestDto, passwordEncoder.encode(memberRequestDto.getPassword()));
        memberRepository.save(member);

        return GlobalResponseDto.ok("회원가입 완료", null);
    }


    //아이디 중복확인
    public GlobalResponseDto<?> emailCheck(MemberRequestDto memberRequestDto) {
       if(memberRepository.existsByEmail(memberRequestDto.getEmail())){
           throw   new IllegalArgumentException();
           //ToDo : 0131 예외코드 정리하여 수정하기
       }
       return GlobalResponseDto.ok("중복되지 않는 이메일 입니다.",null);
    }

    // 닉네임 중복확인
    public GlobalResponseDto<?> nicknameCheck(MemberRequestDto memberRequestDto) {
        if(memberRepository.existsByNickname(memberRequestDto.getNickname())) {
            throw new IllegalArgumentException();
            //ToDo : 0131 예외코드 정리하여 수정하기
        }
        return GlobalResponseDto.ok("중복되지 않은 닉네임 입니다.", null);
    }

    public GlobalResponseDto<?> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundMember));

        if(!passwordEncoder.matches(member.getPassword(), loginRequestDto.getPassword())){
            throw new CustomException(ErrorCode.WrongPassword);
        }

        TokenDto tokenDto = jwtUtil.createAllToken(loginRequestDto.getEmail());

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByMemberEmail ( loginRequestDto.getEmail () );

        if (refreshToken.isPresent ()) {
            refreshTokenRepository.save ( refreshToken.get ().updateToken ( tokenDto.getRefreshToken () ) );
        } else {
            RefreshToken newToken = new RefreshToken ( tokenDto.getRefreshToken (), loginRequestDto.getEmail () );
            refreshTokenRepository.save ( newToken );
        }

        setHeader ( response, tokenDto );

        LoginResDto loginResDto = new LoginResDto ( member, member.getUserImage () );
        return GlobalResponseDto.ok (  member.getNickname () + "님 반갑습니다.",loginResDto );
    }

    private void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader ( JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken () );
        response.addHeader ( JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken () );
    }

    public GlobalResponseDto<?> logout(UserDetailsImpl userDetails) {
        refreshTokenRepository.deleteById(userDetails.getMember().getId());

        return GlobalResponseDto.ok("로그아웃 되었습니다",null);
    }

    public GlobalResponseDto<?> signOut(Member member) {
        memberRepository.deleteById(member.getId());

        return GlobalResponseDto.ok("회원탈퇴가 완료되었습니다", null);
    }

    // ToDo : 0131 중복되는 함수 정리하기 (클린코드)
}
