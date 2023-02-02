package com.minjae.cmungrebuilding.post;

import com.minjae.cmungrebuilding.global.GlobalResponseDto;
import com.minjae.cmungrebuilding.member.Member;
import com.minjae.cmungrebuilding.s3.Image;
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


//    @Transactional
//    public GlobalResponseDto<PostResponseDto> createPost(PostRequestDto postRequestDto,
//                                                         List<MultipartFile> file, Member member){
//
//        List<Image> imgs = new ArrayList<>();
//
//        // 카테고리 검색
//        //Todo : 포스트 카테고리 만들기
////        Optional<Category> categoryOpt = categoryRepository.findByName(postRequestDto.getCategory());
//
//        // 포스트 생성
////        Post post = null;
////        if(categoryOpt.isPresent()) {
////            log.info("카테고리 존재");
////            log.info("Boolean : " + String.valueOf(categoryOpt.isPresent()));
////            log.info("get() : " + String.valueOf(categoryOpt.get()));
////            log.info("Category Name : " + String.valueOf(categoryOpt.get().getName()));
////            post = new Post(postRequestDto, categoryOpt.get(), member);
////        } else {
////            log.info("카테고리 생성");
////            Category category = new Category(postRequestDto.getCategory());
////            categoryRepository.save(category);
////            post = new Post(postRequestDto, category, member);
////        }
//
//        // MultipartFile Null 체크
//        if(file != null) {
//            log.info("파일이 Null이 아닙니다.");
//            // 이미지 파일 처리
//            for (MultipartFile multipartFile : file) {
//                // 이미지 저장
//                Image img = imgRepository.save(new Image(s3Service.uploadFile(multipartFile), post));
//                log.info("이미지 저장 : " + img.getImage());
//                // 이미지 리스트에 추가
//                imgs.add(img);
//            }
//        } else {
//            log.info("파일이 Null 입니다.");
//            log.info("이미지 저장 과정을 생략합니다.");
//        }
//
//        // 포스트 DB 저장
//        postRepository.save(post);
//
//        // 반환 DTO 작성
//        PostResponseDto postResponseDto = new PostResponseDto(post);
//
//        // 이미지 가져오기
//        List<String> imgList = new ArrayList<>();
//        for (Image img : imgs) {
//            imgList.add(img.getImage());
//        }
//
//        // 반환 DTO 이미지 리스트 설정
//        postResponseDto.setImgs(imgList);
//
//        // DTO 반환
//        return GlobalResDto.success(postResponseDto,"게시글 작성이 완료되었습니다.");
//    }


    // 게시글 최신순으로 가져오기
    @Transactional(readOnly = true)
    public GlobalResponseDto<?> allPost(Pageable pageable){
        // 포스트 최신순으로 가져오기
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 포스트 반환 DTO 리스트 작성
        List<AllPostResponseDto> allPostResponseDtos = new ArrayList<>();

        for(Post p : posts){
            // 이미지 리스트 작성
            List<String> imgList = new ArrayList<>();

            // 이미지 리스트에 이미지 추가
            for(Image img : p.getImage()){
                imgList.add(img.getImage());
            }

            // DTO 리스트에 DTO 추가
            allPostResponseDtos.add(new AllPostResponseDto(p, imgList));
        }

        // DTO 반환
        return GlobalResponseDto.ok("조회를 성공하였습니다.",allPostResponseDtos);
    }
}
