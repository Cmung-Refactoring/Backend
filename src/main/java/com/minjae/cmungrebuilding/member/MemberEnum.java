package com.minjae.cmungrebuilding.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum MemberEnum {
    BASIC("basicURL");

    private String urlValue;
}
