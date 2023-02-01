package com.minjae.cmungrebuilding.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SocialService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${kakao.rest.api}")
    private String kakaoRestApi;

    // 카카오 회원가입 로직
    public GlobalResponseDto<?> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. 인가코드로 엑세스 토큰 발급받기
        String accessToken = getAccessToken ( code );

        // 2 . 엑세스 토큰으로 사용자 정보 가져오기
        KakaoMemberInfoDto kakaoMemberInfo = getKakaoMemberInfo(accessToken);

        // 3. "카카오 사용자 정보"로 필요시 회원가입
        Member kakaoMember = registerKakaoMemberIfNeeded ( kakaoMemberInfo );

        // 4. 강제 로그인 처리
        forceLogin ( kakaoMember );
        
        // 토큰 발급
        TokenDto tokenDto = jwtUtil.createAllToken ( kakaoMemberInfo.getEmail () );

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByMemberEmail ( kakaoMemberInfo.getEmail () );

        
        // 로그아웃한 후 로그인을 다시 하는가?
        RefreshToken refreshToken1;
        if (refreshToken.isPresent ()) {
            refreshToken1 = refreshToken.get ().updateToken ( tokenDto.getRefreshToken () );
        } else {
            refreshToken1 = new RefreshToken ( tokenDto.getRefreshToken (), kakaoMemberInfo.getEmail () );
        }
        refreshTokenRepository.save ( refreshToken1 );

       
        //토큰을 header에 넣어서 클라이언트에게 전달하기
        setHeader ( response, tokenDto );

        LoginResDto loginResDto = new LoginResDto ( kakaoMember, kakaoMember.getUserImage () );

        
        return GlobalResponseDto.ok ( kakaoMember.getNickname () + "님 반갑습니다.", loginResDto );

    }


    private void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader ( JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken () );
        response.addHeader ( JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken () );
    }

    private void forceLogin(Member kakaoMember) {
        UserDetails userDetails = new UserDetailsImpl( kakaoMember );
        Authentication authentication = new UsernamePasswordAuthenticationToken( userDetails, null, userDetails.getAuthorities () );
        SecurityContextHolder.getContext ().setAuthentication ( authentication );
    }


    private Member registerKakaoMemberIfNeeded(KakaoMemberInfoDto kakaoMemberInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoMemberInfo.getId ();

        Member kakaoMember = memberRepository.findByKakaoId ( kakaoId ).orElse ( null );

        if (kakaoMember == null) {
            // 카카오 사용자 이메일과 동일한 이메일을 가진 회원이 있는지 확인
            String kakaoEmail = kakaoMemberInfo.getEmail ();
            Member sameEmailMember = memberRepository.findByEmail ( kakaoEmail ).orElse ( null );

            if (sameEmailMember != null) {
                kakaoMember = sameEmailMember;

                // 기존 회원정보에 카카오 Id 추가
                kakaoMember.setKakaoId ( kakaoId );
            } else {
                // 신규 회원가입

                // username: kakao nickname
                String nickname = kakaoMemberInfo.getNickname ();

                // password: random UUID
                String password = UUID.randomUUID ().toString ();
                String encodePassword = passwordEncoder.encode ( password );

                // email: kakao email
                String email = kakaoMemberInfo.getEmail ();

                // 프로필 사진 가져오기
                String userImage = kakaoMemberInfo.getUserImage ();

                kakaoMember = new Member ( nickname, encodePassword, email, userImage, kakaoId );
            }

            memberRepository.save ( kakaoMember );
        }

        return kakaoMember;
    }

    private KakaoMemberInfoDto getKakaoMemberInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders ();
        headers.add ( "Authorization", "Bearer " + accessToken );
        headers.add ( "Content-type", "application/x-www-form-urlencoded;charset=utf-8" );

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoMemberInfoRequest = new HttpEntity<> ( headers );
        RestTemplate rt = new RestTemplate ();
        ResponseEntity<String> response = rt.exchange (
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoMemberInfoRequest,
                String.class
        );

        String responseBody = response.getBody ();
        ObjectMapper objectMapper = new ObjectMapper ();
        JsonNode jsonNode = objectMapper.readTree ( responseBody );


        Long id = jsonNode.get ( "id" ).asLong ();
        String nickname = jsonNode.get ( "properties" ).get ( "nickname" ).asText ();
        String email = jsonNode.get ( "kakao_account" ).get ( "email" ).asText ();
        String userImage = jsonNode.get ( "kakao_account" ).get ( "profile" ).get ( "profile_image_url" ).asText ();

        return new KakaoMemberInfoDto ( id, nickname, email, userImage );
    }



    // 1. (카카오) 인가코드로 엑세스 토큰 발급받기
    private String getAccessToken(String code) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders ();
        headers.add ("Content-type","application/x-www-form-urlencoded;charset=utf-8");

        // HTTP body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<> ();
        body.add ( "grant_type", "authorization_code" );
        body.add ( "client_id", kakaoRestApi ); // REST API키
        body.add ( "redirect_uri", "https://크멍.com/auth/member/kakao/callback" );
        body.add ( "code", code );

        //HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<> ( body, headers );
        RestTemplate rt = new RestTemplate ();

        ResponseEntity<String> response = rt.exchange (
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        //HTTP 응답 JSON으로 받음 -> 엑세스 토큰 파싱
        String responseBody = response.getBody ();
        ObjectMapper objectMapper = new ObjectMapper ();
        JsonNode jsonNode = objectMapper.readTree ( responseBody );

        return jsonNode.get ( "access_token" ).asText ();
    }

//    // 네이버 회원가입 로직
//    public GlobalResponseDto<?> naverLogin(String code, String state, HttpServletResponse response) {
//
//    }
}
