package com.aidata.springboard2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ReplyDto {
    private int r_num;//기본키(댓글번호)
    private int r_bnum;//외래키(게시글번호)
    private String r_contents;
    private String r_id;//작성자 id(로그인 id)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul")
    private Timestamp r_date;
}
//rest 방식으로 날짜 정보를 넘길 때 날짜 형식을 지정할 수 있음.