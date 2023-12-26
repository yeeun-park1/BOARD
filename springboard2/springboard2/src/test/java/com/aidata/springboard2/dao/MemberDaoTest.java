package com.aidata.springboard2.dao;

import com.aidata.springboard2.dto.MemberDto;
import org.junit.jupiter.api.*;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberDaoTest {
    @Autowired
    private MemberDao mDao;

    @Test
    @DisplayName("1. MemberDao id check")
    @Order(1)
    public void selectIdTest() throws Exception{
        int cnt = mDao.selectId("test");
    }

    @Test
    @DisplayName("2. MemberDao insert")
    @Order(2)
    public void insertMemberTest() throws Exception{
        //화면이 없기 때문에 직접 Dto를 생성하여 데이터를 넣고
        //테스트를 수행
        MemberDto member = new MemberDto();
        member.setM_id("user01");
        member.setM_pwd("1111");
        member.setM_name("사용자01");
        member.setM_birth("2023-01-01");
        member.setM_addr("인천시");
        member.setM_phone("01012345678");

        mDao.insertMember(member);
    }

    @Test
    @DisplayName("3. Get Password")
    @Order(3)
    public void selectPasswordTest() throws Exception{
        String pwd = mDao.selectPassword("test01");
    }
}//class end