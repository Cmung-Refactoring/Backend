package com.minjae.cmungrebuilding.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import com.minjae.cmungrebuilding.Token.RefreshToken;
import com.minjae.cmungrebuilding.Token.RefreshTokenRepository;
import com.minjae.cmungrebuilding.Token.TokenDto;
import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.jwtutil.JwtUtil;
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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.secret}")
    private String naverClientSecret;

    // 카카오 회원가입 로직
    public GlobalResponseDto<?> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. 인가코드로 엑세스 토큰 발급받기
        String accessToken = getAccessToken(code);

        // 2 . 엑세스 토큰으로 사용자 정보 가져오기
        KakaoMemberInfoDto kakaoMemberInfo = getKakaoMemberInfo(accessToken);

        // 3. "카카오 사용자 정보"로 필요시 회원가입
        Member kakaoMember = registerKakaoMemberIfNeeded(kakaoMemberInfo);

        // 4. 강제 로그인 처리
        forceLogin(kakaoMember);

        // 토큰 발급
        TokenDto tokenDto = jwtUtil.createAllToken(kakaoMemberInfo.getEmail());

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByMemberEmail(kakaoMemberInfo.getEmail());


        // 로그아웃한 후 로그인을 다시 하는가?
        RefreshToken refreshToken1;
        if (refreshToken.isPresent()) {
            refreshToken1 = refreshToken.get().updateToken(tokenDto.getRefreshToken());
        } else {
            refreshToken1 = new RefreshToken(tokenDto.getRefreshToken(), kakaoMemberInfo.getEmail());
        }
        refreshTokenRepository.save(refreshToken1);


        //토큰을 header에 넣어서 클라이언트에게 전달하기
        setHeader(response, tokenDto);

        LoginResDto loginResDto = new LoginResDto(kakaoMember, kakaoMember.getUserImage());


        return GlobalResponseDto.ok(kakaoMember.getNickname() + "님 반갑습니다.", loginResDto);

    }


    private void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());
    }

    private void forceLogin(Member kakaoMember) {
        UserDetails userDetails = new UserDetailsImpl(kakaoMember);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    private Member registerKakaoMemberIfNeeded(KakaoMemberInfoDto kakaoMemberInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoMemberInfo.getId();

        Member kakaoMember = memberRepository.findByKakaoId(kakaoId).orElse(null);

        if (kakaoMember == null) {
            // 카카오 사용자 이메일과 동일한 이메일을 가진 회원이 있는지 확인
            String kakaoEmail = kakaoMemberInfo.getEmail();
            Member sameEmailMember = memberRepository.findByEmail(kakaoEmail).orElse(null);

            if (sameEmailMember != null) {
                kakaoMember = sameEmailMember;

                // 기존 회원정보에 카카오 Id 추가
                kakaoMember.setKakaoId(kakaoId);
            } else {
                // 신규 회원가입

                // username: kakao nickname
                String nickname = kakaoMemberInfo.getNickname();

                // password: random UUID
                String password = UUID.randomUUID().toString();
                String encodePassword = passwordEncoder.encode(password);

                // email: kakao email
                String email = kakaoMemberInfo.getEmail();

                // 프로필 사진 가져오기
                String userImage = kakaoMemberInfo.getUserImage();

                kakaoMember = new Member(nickname, encodePassword, email, userImage, kakaoId);
            }

            memberRepository.save(kakaoMember);
        }

        return kakaoMember;
    }

    private KakaoMemberInfoDto getKakaoMemberInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoMemberInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoMemberInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);


        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties").get("nickname").asText();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String userImage = jsonNode.get("kakao_account").get("profile").get("profile_image_url").asText();

        return new KakaoMemberInfoDto(id, nickname, email, userImage);
    }


    // 1. (카카오) 인가코드로 엑세스 토큰 발급받기
    private String getAccessToken(String code) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoRestApi); // REST API키
        body.add("redirect_uri", "https://크멍.com/auth/member/kakao/callback");
        body.add("code", code);

        //HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();

        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        //HTTP 응답 JSON으로 받음 -> 엑세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.get("access_token").asText();
    }


    // 네이버 회원가입 로직
    public GlobalResponseDto<Object> naverLogin(String code, String state, HttpServletResponse response) throws IOException {
        NaverMemberInfoDto naverMemberInfoDto = getNaverMemberInfo ( code, state );

        String naverId = naverMemberInfoDto.getNaverId ();
        Member naverMember = memberRepository.findByNaverId ( naverId ).orElse ( null );

        if (naverMember == null) {
            // 네이버 사용자 이메일과 동일한 이메일을 가진 회원이 있는지 확인
            String naverEmail = naverMemberInfoDto.getEmail ();
            Member sameEmailMember = memberRepository.findByEmail ( naverEmail ).orElse ( null );


            if (sameEmailMember != null) {
                naverMember = sameEmailMember;

                // 기존 회원정보에 네이버 Id 추가
                naverMember.setNaverId ( naverId );
            } else {
                // 신규 회원가입

                // username: naver nickname
                String nickname = naverMemberInfoDto.getNickname ();


                // password: random UUID
                String password = UUID.randomUUID ().toString ();
                String encodePassword = passwordEncoder.encode ( password );

                // email: naver email
                String email = naverMemberInfoDto.getEmail ();

                // 프로필 사진 가져오기
                String userImage = naverMemberInfoDto.getUserImage ();

                naverMember = new Member ( email, nickname, password, userImage, naverId );
            }

            memberRepository.save ( naverMember );
        }
        naverForceLogin ( naverMember );

        TokenDto tokenDto = jwtUtil.createAllToken ( naverMember.getEmail () );

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByMemberEmail ( naverMember.getEmail () );


        // 로그아웃한 후 로그인을 다시 하는가?
        RefreshToken refreshToken1;
        if (refreshToken.isPresent ()) {
            refreshToken1 = refreshToken.get ().updateToken ( tokenDto.getRefreshToken () );
        } else {
            refreshToken1 = new RefreshToken ( tokenDto.getRefreshToken (), naverMember.getEmail () );
        }
        refreshTokenRepository.save ( refreshToken1 );



        //토큰을 header에 넣어서 클라이언트에게 전달하기
        setHeader ( response, tokenDto );

        LoginResDto loginResDto = new LoginResDto ( naverMember, naverMember.getUserImage () );



        return GlobalResponseDto.ok ( naverMember.getNickname () + "님 반갑습니다.", loginResDto );
    }


        public NaverMemberInfoDto getNaverMemberInfo(String code, String state) throws IOException {

            String codeReqURL = "https://nid.naver.com/oauth2.0/token";
            String tokenReqURL = "https://openapi.naver.com/v1/nid/me";

            // 코드를 네이버에 전달하여 엑세스 토큰 가져옴
            JsonElement tokenElement = jsonElement(codeReqURL, null, code, state);

            String access_Token = tokenElement.getAsJsonObject().get("access_token").getAsString();
            String refresh_token = tokenElement.getAsJsonObject().get("refresh_token").getAsString();

            // 엑세스 토큰을 네이버에 전달하여 유저정보 가져옴
            JsonElement userInfoElement = jsonElement(tokenReqURL, access_Token, null, null);

            String naverId = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                    .getAsJsonObject().get("id"));
            String email = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                    .getAsJsonObject().get("email"));
            String nickname = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                    .getAsJsonObject().get("nickname"));
            String userImage = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                    .getAsJsonObject().get("profile_image"));


            naverId = naverId.substring(1, naverId.length() - 1);
            email = email.substring(1, email.length() - 1);
            nickname = nickname.substring(1, nickname.length() - 1);
            userImage = userImage.substring(1, userImage.length() - 1);

            return new NaverMemberInfoDto(naverId, nickname, email, userImage, access_Token, refresh_token);
        }

    public JsonElement jsonElement(String reqUrl, String token, String code, String state) throws IOException {

        URL url = new URL ( reqUrl );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection ();

        // POST 요청을 위해 기본값이 false인 setDoOutput을 true로
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // POST 요청에 필요한 데이터 저장 후 전송
        if (token == null) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            String sb = "grant_type=authorization_code" +
                    "&client_id=" + naverClientId +
                    "&client_secret=" + naverClientSecret +
                    "&redirect_uri= https://xn--922bn81b.com/auth/member/naver/callback" +
                    "&code=" + code +
                    "&state=" + state;
            bw.write(sb);
            bw.flush();
            bw.close();
        } else {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();

        while ((line = br.readLine()) != null) {
            result.append(line);
        }
        br.close();

        // Gson 라이브러리에 포함된 클래스로 JSON 파싱
        return JsonParser.parseString(result.toString());
    }
    private void naverForceLogin(Member naverMember) {
        UserDetails userDetails = new UserDetailsImpl(naverMember);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
