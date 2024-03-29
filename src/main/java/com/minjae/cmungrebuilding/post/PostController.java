package com.minjae.cmungrebuilding.post;

import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public GlobalResponseDto<?> createPost(@RequestPart(value = "postImg", required = false)List<MultipartFile> multipartFiles,
                                           @RequestPart PostRequestDto postRequestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.createPost(multipartFiles, postRequestDto, userDetails.getMember());
    }

    // 게시글 전체 목록 조회
    @GetMapping
    public GlobalResponseDto<?> getAllPosts() {
        return postService.getAllPosts();
    }

    // 게시글 상세 조회 로직
    @GetMapping("/{postId}")
    public GlobalResponseDto<?> getOnePost(@PathVariable Long postId){
        return postService.getOnePost(postId);
    }

    // 게시글 삭제 로직
    @DeleteMapping("/{postId}")
    public GlobalResponseDto<?> deletePost(@RequestParam Long postId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.deletePost(postId, userDetails.getMember());
    }

    // 게시글 수정 로직
    @PutMapping("/{postId}")
    public GlobalResponseDto<?> updatePost(@RequestPart(value = "postImg", required = false) List<MultipartFile> multipartFiles,
                                           @PathVariable Long postId,
                                           @RequestPart PostRequestDto postRequestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.updatePost(postId, multipartFiles, postRequestDto, userDetails.getMember());
    }
}
