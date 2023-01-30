package com.minjae.cmungrebuilding.member;

import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
}
