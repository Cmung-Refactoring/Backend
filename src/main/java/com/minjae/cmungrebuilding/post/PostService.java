package com.minjae.cmungrebuilding.post;

import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.member.Member;
import com.minjae.cmungrebuilding.s3.S3Uploader;
import com.minjae.cmungrebuilding.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;




@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ImageRepository imageRepository;

    // 게시글 작성 로직
    @Transactional
    public GlobalResponseDto<PostResponseDto> createPost(List<MultipartFile> file,
                                                         PostRequestDto postRequestDto,
                                                         Member member) {
        Post post = new Post(postRequestDto, member);
        postRepository.save(post);

        //이미지 있다면
        createImageIfNotNull(file, post);

        return GlobalResponseDto.created("게시글이 등록되었습니다.", new PostResponseDto(post, ServiceUtil.getImgUrl(post)));

    }


    // 게시글 전체 조회 로직
    public GlobalResponseDto<?> getAllPosts() {
        // 포스트 최신순으로 가져오기
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        // 포스트 반환 DTO 리스트 작성
        List<AllPostResponseDto> allPostResponseDtos = new ArrayList<>();

        for(Post p : posts){
            // 이미지 리스트 작성
            List<String> imgList = new ArrayList<>();

            // 이미지 리스트에 이미지 추가
            for(Image img : p.getImageList()){
                imgList.add(img.getImageUrl());
            }

            // DTO 리스트에 DTO 추가
            allPostResponseDtos.add(new AllPostResponseDto(p, imgList));
        }
        return GlobalResponseDto.ok("조회 성공!", allPostResponseDtos);
    }

    //등록 할 이미지가 있다면 사용
    public void createImageIfNotNull(List<MultipartFile> multipartFile, Post post) {
        if (multipartFile != null && multipartFile.size() > 0) {
            List<Image> imageList = new ArrayList<>();
            for (MultipartFile imgFile : multipartFile) {
                Image image = new Image(post, S3Uploader.upload(imgFile, "dirdir"));
                imageList.add(image);
                imageRepository.save(image);
            }
            post.setImageList(imageList);
        }
    }


}