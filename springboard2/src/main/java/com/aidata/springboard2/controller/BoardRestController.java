package com.aidata.springboard2.controller;

import com.aidata.springboard2.dto.BoardFileDto;
import com.aidata.springboard2.dto.ReplyDto;
import com.aidata.springboard2.service.BoardService;
import com.aidata.springboard2.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class BoardRestController {
    @Autowired
    private MemberService mServ;
    @Autowired
    private BoardService bServ;

    @GetMapping("idCheck")
    public String idCheck(String mid){
        log.info("idCheck()");
        String result = mServ.idCheck(mid);
        return result;
    }

    @PostMapping("delFile")
    public List<BoardFileDto> delFile(BoardFileDto bFile,
                                      HttpSession session){
        log.info("delFile()");
        List<BoardFileDto> fList = bServ.delFile(bFile, session);
        return fList;
    }

    @PostMapping("replyInsert")
    public ReplyDto replyInsert(ReplyDto reply){
        log.info("replyInsert()");
        reply = bServ.replyInsert(reply);
        return reply;
    }
}//class end
