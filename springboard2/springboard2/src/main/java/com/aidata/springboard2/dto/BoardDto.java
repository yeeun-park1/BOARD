package com.aidata.springboard2.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class BoardDto {
    private int b_num;
    private String b_title;
    private String b_contents;
    private String b_id;
    private String m_name;
    private Timestamp b_date;
    private int b_views;
}
