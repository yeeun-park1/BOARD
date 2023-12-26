package com.aidata.springboard2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {
    private String m_id;
    private String m_pwd;
    private String m_name;
    private String m_birth;
    private String m_addr;
    private String m_phone;
    private int m_point;
    private String g_name;
}
