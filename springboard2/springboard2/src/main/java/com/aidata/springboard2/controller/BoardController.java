package com.aidata.springboard2.controller;

import com.aidata.springboard2.dto.BoardDto;
import com.aidata.springboard2.dto.BoardFileDto;
import com.aidata.springboard2.dto.SearchDto;
import com.aidata.springboard2.service.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class BoardController {
    @Autowired
    private BoardService bServ;

    @GetMapping("boardList")
    public ModelAndView boardList(SearchDto sdto,
                                  HttpSession session){
        log.info("boardList()");
        ModelAndView mv = bServ.getBoardList(sdto, session);

        return mv;
    }

    @GetMapping("writeForm")
    public String writeForm(){
        log.info("writeForm()");
        return "writeForm";
    }

    @PostMapping("writeProc")
    public String writeProc(@RequestPart List<MultipartFile> files,
                            BoardDto board,
                            HttpSession session,
                            RedirectAttributes rttr){
        log.info("writeProc()");
        String view = bServ.boardWrite(files, board, session, rttr);
        return view;
    }

    //글 상세 보기
    @GetMapping("boardDetail")
    public ModelAndView boardDetail(int b_num){
        log.info("boardDetail() : {}", b_num);
        ModelAndView mv = bServ.getBoard(b_num);
        return mv;
    }

    //파일 다운로드
    @GetMapping("download")
    public ResponseEntity<Resource> fileDownload (
            BoardFileDto bfile,
            HttpSession session) throws IOException {
        log.info("fileDownload()");
        ResponseEntity<Resource> resp =
                bServ.fileDownload(bfile, session);
        return resp;
    }

    @GetMapping("boardDelete")
    public String boardDelete(int b_num,
                              HttpSession session,
                              RedirectAttributes rttr){
        log.info("boardDelete()");
        String view = bServ.deleteBoard(b_num, session, rttr);
        return view;
    }

    @GetMapping("updateForm")
    public ModelAndView updateForm(int b_num) {
        log.info("updateForm()");
        ModelAndView mv = bServ.updateBoard(b_num);
        return mv;
    }

    @PostMapping("updateProc")
    public String updateProc(List<MultipartFile> files,
                             BoardDto board,
                             HttpSession session,
                             RedirectAttributes rttr){
        log.info("updateProc()");
        String view = bServ.updateBoard(files, board, session, rttr);
        return view;
    }
}//class end



