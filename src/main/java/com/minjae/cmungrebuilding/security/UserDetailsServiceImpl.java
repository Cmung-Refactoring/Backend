package com.minjae.cmungrebuilding.security;


import com.minjae.cmungrebuilding.member.Member;
import com.minjae.cmungrebuilding.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail ( email ).orElseThrow (
                () -> new RuntimeException ("Not found Account")
        );

        UserDetailsImpl userDetails = new UserDetailsImpl ();
        userDetails.setMember ( member );

        return userDetails;
    }
}
