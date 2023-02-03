package com.minjae.cmungrebuilding.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String userImg;
    private String nickname;
    private int price;
    private String categoryName;
    private String state;
    private String local;
    private String date;
    private List<String> imgs;

    private LocalDateTime createdAt;


    public PostResponseDto(Post post, List<String> imgUrl) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
//        this.rating = post.getMember().getRating();
        this.userImg = post.getMember().getUserImage();
        this.nickname = post.getMember().getNickname();
        this.price = post.getPrice();
        this.categoryName = post.getCategory();
        this.state = post.getState();
        this.date = post.getDate();
        this.local = post.getLocal();
        this.createdAt = post.getCreatedAt();
    }
}