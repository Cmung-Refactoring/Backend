package com.minjae.cmungrebuilding.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.awt.*;

@Entity
@NoArgsConstructor
@Getter
public class Post {

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

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false) // 카테고리를 테이블 분리해서 관리해야 할까?
    private Category category;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;
    // nickname을 원래 따로 컬럼값으로 가지고 있었는데, member를 테이블 조인하면 언제든 원할 때 사용할 수 있으니 nickname column은 제외했습니다.

    @JsonBackReference
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Image> image;

}