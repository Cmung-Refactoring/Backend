package com.minjae.cmungrebuilding.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoMemberInfoDto {

    private Long id;
    private String nickname;
    private String email;
    private String userImage;
}
