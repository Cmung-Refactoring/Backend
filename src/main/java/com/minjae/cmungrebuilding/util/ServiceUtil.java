package com.minjae.cmungrebuilding.util;

import com.minjae.cmungrebuilding.post.Image;
import com.minjae.cmungrebuilding.post.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceUtil {

    //Post 에서 이미지 url 추출
    public static List<String> getImgUrl(Post post) {
        List<String> imageUrl = new ArrayList<>();
        for (Image img : post.getImageList()) {
            imageUrl.add(img.getImageUrl());
        }
        Collections.reverse(imageUrl);
        return imageUrl;
    }

}
