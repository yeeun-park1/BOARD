package com.aidata.springboard2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchDto {
    private String colname;
    private String keyword;
    private int pageNum;
    private int listCnt;
}
