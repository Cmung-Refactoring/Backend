package com.minjae.cmungrebuilding.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickName);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByKakaoId(Long kakaoId);
}
