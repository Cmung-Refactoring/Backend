package com.minjae.cmungrebuilding;

import com.minjae.cmungrebuilding.member.MemberController;
import com.minjae.cmungrebuilding.member.MemberRepository;
import com.minjae.cmungrebuilding.member.MemberRequestDto;
import com.minjae.cmungrebuilding.member.MemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MemberTest {

    @InjectMocks
    private MemberController memberController;
    @InjectMocks
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Validator validator;


    @Test
    @DisplayName("회원가입 성공")
    void signUpSuccess() {
        //Given
        MemberRequestDto memberRequestDto = new MemberRequestDto();

        //When
        memberRequestDto.setEmail("cmung@gmail.com");
        memberRequestDto.setNickname("크멍");
        memberRequestDto.setPassword("qwerty123");
        memberRequestDto.setPasswordCheck("qwerty123");
//        memberService.signup(memberRequestDto);

        //Then
        assertThat(memberService.signup(memberRequestDto).getStatus()).isEqualTo(HttpStatus.OK.toString());
//        assertThat(member.getEmail()).isEqualTo(memberRequestDto.getEmail());

    }

    @Test
    @DisplayName("비밀번호 체크 불일치 시 회원가입 실패")
    void signUpTest() {
        //Given
        MemberRequestDto memberRequestDto = new MemberRequestDto();


        //When
        memberRequestDto.setEmail("cmung@gmail.com");
        memberRequestDto.setNickname("크멍");
        memberRequestDto.setPassword("qwerty123");
        memberRequestDto.setPasswordCheck("qwerty1234");

        //Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            memberService.signup(memberRequestDto);
        });
    }

    @Test
    @DisplayName("회원가입시 이메일 형식 검증 실패")
    void validateEmail() {
        //Given
        MemberRequestDto memberRequestDto = new MemberRequestDto();
        MemberController memberController1 = new MemberController(memberService);

        //When
        //set을 해서 valid가 안되는 걸까?
        memberRequestDto.setEmail("anfro@naver.com");
        memberRequestDto.setNickname("멍");
        memberRequestDto.setPassword("qwerty123");
        memberRequestDto.setPasswordCheck("qwerty123");

        Set<ConstraintViolation<MemberRequestDto>> validate = validator.validate(memberRequestDto);
        Iterator<ConstraintViolation<MemberRequestDto>> iterator = validate.iterator();
        List<String> messages = new ArrayList<>();
        while (iterator.hasNext()) {
            ConstraintViolation<MemberRequestDto> next = iterator.next();
            messages.add(next.getMessage());
            System.out.println("message = " + next.getMessage());
        }
        // Then
//        Assertions.assertThrows(MethodArgumentNotValidException.class, () -> {
//            memberController1.signup(memberRequestDto);
//        });
        assertThat(messages).contains("Nickname의 길이는 2 ~ 20자 입니다.");
    }


}
