package com.aidata.springboard2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardFileDto {
    private int bf_num;//boardfile 기본키
    private int bf_bnum;//게시글 번호
    private String bf_oriname;//원래 파일명
    private String bf_sysname;//변경한 파일명
}
