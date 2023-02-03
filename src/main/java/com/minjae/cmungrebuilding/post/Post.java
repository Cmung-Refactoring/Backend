package com.minjae.cmungrebuilding.post;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.minjae.cmungrebuilding.member.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Post extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String local;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String category;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;
    // nickname을 원래 따로 컬럼값으로 가지고 있었는데, member를 테이블 조인하면 언제든 원할 때 사용할 수 있으니 nickname column은 제외했습니다.

    @JsonBackReference
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Image> imageList;

    public Post(PostRequestDto postRequestDto, Member member) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.price = postRequestDto.getPrice();
        this.state = postRequestDto.getState();
        this.local = postRequestDto.getLocal();
        this.date = postRequestDto.getDate();
        this.category = postRequestDto.getCategory();
        this.member = member;
    }
}
