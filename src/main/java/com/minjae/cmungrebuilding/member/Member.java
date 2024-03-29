package com.minjae.cmungrebuilding.member;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.minjae.cmungrebuilding.pet.Pet;
import com.minjae.cmungrebuilding.post.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String userImage;

    @Column(nullable = true)
    private Long kakaoId;

    @Column(nullable = true)
    private String naverId;

    @JsonManagedReference
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Post> post = new ArrayList<>();
    // ToDo : new arraylist 왜 붙였지?

//    @JsonManagedReference
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
//    private List<Review> reviews = new ArrayList<>();
//    ToDo :
//    @JsonManagedReference
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
//    private List<Pet> pet = new ArrayList<>();

    // ToDo : 평균 점수
//    private long rating;

    // ToDo :합계 점수
//    private long sum;


    public Member(MemberRequestDto memberRequestDto, String encodedPassword) {
        this.email = memberRequestDto.getEmail();
        this.nickname = memberRequestDto.getNickname();
        this.password = encodedPassword;
        this.userImage = MemberEnum.BASIC.getUrlValue();
    }

    public Member(String nickname, String encodePassword, String email, String userImage, Long kakaoId) {
        this.nickname = nickname;
        this.password = encodePassword;
        this.email = email;
        this.userImage = userImage;
        this.kakaoId = kakaoId;
    }
    public Member(String email, String nickname, String password, String userImage, String naverId) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.userImage = userImage;
        this.naverId = naverId;
    }
}
