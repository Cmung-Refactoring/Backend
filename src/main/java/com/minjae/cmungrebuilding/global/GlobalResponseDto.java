package com.minjae.cmungrebuilding.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GlobalResponseDto<T> {


    private String status;

    private String msg;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> GlobalResponseDto <T> ok(String msg, T data){
        return new GlobalResponseDto<>(HttpStatus.OK.toString(), msg, data);
    }
}
