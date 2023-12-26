package com.aidata.springboard2.service;

import com.aidata.springboard2.dao.BoardDao;
import com.aidata.springboard2.dao.MemberDao;
import com.aidata.springboard2.dto.*;
import com.aidata.springboard2.util.PagingUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@Service
@Slf4j
public class BoardService {
    @Autowired
    private BoardDao bDao;
    @Autowired
    private MemberDao mDao;

    //트랜젝션 관련 객체 선언
    @Autowired
    private PlatformTransactionManager manager;
    @Autowired
    private TransactionDefinition definition;

    private int lcnt = 5;//한 화면(페이지)에 보여질 글 개수

    public ModelAndView getBoardList(SearchDto sdto,
                                     HttpSession session) {
        log.info("getBoardList()");
        ModelAndView mv = new ModelAndView();
        //DB에서 게시글 가져오기
        int num = sdto.getPageNum();
        if (sdto.getListCnt() == 0) {
            sdto.setListCnt(lcnt);
        }
        //pageNum을 LIMIT 시작 번호로 변경
        sdto.setPageNum((num - 1) * sdto.getListCnt());
        List<BoardDto> bList = bDao.selectBoardList(sdto);
        //DB에서 가져온 데이터를 mv에 담기.
        mv.addObject("bList", bList);

        //페이징 처리
        sdto.setPageNum(num);//원래 페이지 번호로 환원
        String pageHtml = getPaging(sdto);
        mv.addObject("paging", pageHtml);

        //페이지 번호와 검색 관련 내용을 세션에 저장
        if(sdto.getColname() != null){
            session.setAttribute("sdto", sdto);
        } else {
            //검색이 아닐 때는 제거
            session.removeAttribute("sdto");
        }
        //별개로 페이지번호도 저장.
        session.setAttribute("pageNum", num);

        mv.setViewName("boardList");
        return mv;
    }

    private String getPaging(SearchDto sdto) {
        String pageHtml = null;

        //전체 글개수 구하기(from DB)
        int maxNum = bDao.selectBoardCnt(sdto);
        //페이지에 보여질 번호 개수
        int pageCnt = 5;
        //링크용 uri : 기본 - "boardList?
        // 검색 - "boardList?colname=b_title&keyword=4&
        String listName = "boardList?";
        if (sdto.getColname() != null) {
            listName += "colname=" + sdto.getColname()
                    + "&keyword=" + sdto.getKeyword() + "&";
        }

        PagingUtil paging = new PagingUtil(
                maxNum,
                sdto.getPageNum(),
                sdto.getListCnt(),
                pageCnt,
                listName
        );

        pageHtml = paging.makePaging();

        return pageHtml;
    }

    //게시글, 파일 저장 및 회원 정보 변경 메소드
    public String boardWrite(List<MultipartFile> files,
                             BoardDto board,
                             HttpSession session,
                             RedirectAttributes rttr) {
        log.info("boardWrite()");
        
        //트랜젝션 상태 처리 객체
        TransactionStatus status =
                manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            //글 내용 저장.
            bDao.insertBoard(board);
            //log.info("게시글 번호 : " + board.getB_num());

            //파일 저장(파일 정보 저장)
            fileUpload(files, session, board.getB_num());

            //작성자 point 수정
            MemberDto member = (MemberDto) session.getAttribute("member");
            int point = member.getM_point() + 10;
            if (point > 100) {//point가 100을 넘지 않도록 필터링.
                point = 100;
            }
            member.setM_point(point);
            mDao.updateMemberPoint(member);
            //세션에 새 정보를 저장.
            member = mDao.selectMember(member.getM_id());
            session.setAttribute("member", member);
            //세션에 같은 이름으로 set을 하면 덮어쓰기가 된다.

            manager.commit(status);//최종 승인
            view = "redirect:boardList?pageNum=1";
            msg = "작성 성공";
        } catch (Exception e) {
            e.printStackTrace();
            manager.rollback(status);//취소
            view = "redirect:writeForm";
            msg = "작성 실패";
        }
        rttr.addFlashAttribute("msg", msg);

        return view;
    }

    private void fileUpload(List<MultipartFile> files,
                            HttpSession session,
                            int bNum) throws Exception {
        //이 메소드의 예외처리(파일 저장 실패, 파일 정보 저장 실패)를
        //호출한 메소드에서 처리하도록 throws를 사용.
        log.info("fileUpload()");
        //파일 저장(폴더에...)
        //파일 저장 위치 처리: 세션에서 위치(경로) 정보를 구함.
        String realPath = session.getServletContext()
                .getRealPath("/");
        log.info(realPath);
        realPath += "upload/";//파일 업로드용 폴더
        //업로드용 폴더가 없으면 자동으로 생성하자.
        File folder = new File(realPath);
        if (folder.isDirectory() == false) {
            //isDirectory() 폴더의 유무 확인 메소드
            //폴더가 있으면 true, 없거나 폴더가 아니면 false.
            folder.mkdir();//MaKe DIRectory(폴더)
        }

        for (MultipartFile mf : files) {
            //파일명(원래 이름) 추출
            String oriname = mf.getOriginalFilename();
            if (oriname.equals("")) {
                return;//업로드할 파일 없음. 파일 저장 작업 종료.
            }

            BoardFileDto bfd = new BoardFileDto();
            bfd.setBf_bnum(bNum);//게시글 번호 저장.
            bfd.setBf_oriname(oriname);//원래 파일명 저장.
            String sysname = System.currentTimeMillis()
                    + oriname.substring(oriname.lastIndexOf("."));
            //air.jpg -> 1212412413.jpg
            bfd.setBf_sysname(sysname);

            //파일 저장(upload폴더에...)
            File file = new File(realPath + sysname);
            //......./.../.../webapp/upload/1212412413.jpg
            mf.transferTo(file);//하드디스크에 저장.

            //파일 정보 저장(DB에...)
            bDao.insertFile(bfd);
        }
    }

    public ModelAndView getBoard(int b_num){
        log.info("getBoard");
        ModelAndView mv = new ModelAndView();

        //게시글 번호로 선택한 게시물 가져오기
        BoardDto board = bDao.selectBoard(b_num);
        mv.addObject("board", board);

        //게시글의 파일목록 가져오기
        List<BoardFileDto> bfList = bDao.selectFileList(b_num);
        mv.addObject("bfList", bfList);

        //게시글의 댓글목록 가져오기
        List<ReplyDto> rList = bDao.selectReplyList(b_num);
        mv.addObject("rList", rList);

        mv.setViewName("boardDetail");

        return mv;
    }

    public ResponseEntity<Resource> fileDownload(
            BoardFileDto bfile, HttpSession session)
            throws IOException {
        log.info("fileDownload()");
        //파일 저장 경로 및 경로와 파일명을 조합
        String realPath = session.getServletContext().
                getRealPath("/");
        realPath += "upload/" + bfile.getBf_sysname();

        //실제 파일이 저장된 하드디스크까지의 통로를 수립.
        InputStreamResource fResource =
                new InputStreamResource(
                        new FileInputStream(realPath));

        //파일명이 한글인 경우의 처리(UTF-8로 인코딩)
        String fileName = URLEncoder
                .encode(bfile.getBf_oriname(), "UTF-8");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .body(fResource);
    }

    //게시글 삭제(파일목록+파일, 댓글목록 함께 삭제)
    public String deleteBoard(int b_num,
                              HttpSession session,
                              RedirectAttributes rttr){
        log.info("deleteBoard()");

        //트랜젝션
        TransactionStatus status =
                manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            //0. 파일 삭제 목록 구하기
            List<String> fList = bDao.selectFnameList(b_num);
            
            //1. 파일목록 삭제
            bDao.deleteFiles(b_num);
            //1. 댓글목록 삭제
            bDao.deleteReplays(b_num);
            //2. 게시글 삭제
            bDao.deleteBoard(b_num);

            //파일 삭제 처리
            if(fList.size() != 0) {
                deleteFiles(fList, session);
            }
            
            manager.commit(status);

            view = "redirect:boardList?pageNum=1";
            msg = "삭제 성공";
        } catch (Exception e){
            e.printStackTrace();

            manager.rollback(status);

            view = "redirect:boardDetail?b_num=" + b_num;
            msg = "삭제 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    private void deleteFiles(List<String> fList,
                             HttpSession session)
            throws Exception {
        log.info("deleteFiles()");
        //파일 위치
        String realPath = session.getServletContext()
                .getRealPath("/");
        realPath += "upload/";

        for(String sn : fList){
            File file = new File(realPath + sn);
            if(file.exists() == true){//파일 존재 확인 후
                file.delete();//파일 삭제
            }
        }
    }

    public ModelAndView updateBoard(int b_num) {
        log.info("updateBoard()");
        ModelAndView mv = new ModelAndView();
        //게시글 내용 가져오기
        BoardDto board = bDao.selectBoard(b_num);
        //파일목록 가져오기
        List<BoardFileDto> fList = bDao.selectFileList(b_num);
        //mv에 담기
        mv.addObject("board", board);
        mv.addObject("fList", fList);
        //템플릿 지정.
        mv.setViewName("updateForm");
        return mv;
    }

    public List<BoardFileDto> delFile(BoardFileDto bFile,
                                      HttpSession session){
        log.info("delFile()");
        List<BoardFileDto> fList = null;

        //파일 경로 설정.
        String realPath = session.getServletContext()
                .getRealPath("/");
        realPath += "upload/" + bFile.getBf_sysname();

        try {
            //파일 삭제
            File file = new File(realPath);
            if(file.exists()){
                if(file.delete()){
                    //해당 파일 정보 삭제(DB)
                    bDao.deleteFile(bFile.getBf_sysname());
                    //나머지 파일 목록 다시 가져오기
                    fList = bDao.selectFileList(bFile.getBf_bnum());
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return fList;
    }

    public String updateBoard(List<MultipartFile> files,
                              BoardDto board,
                              HttpSession session,
                              RedirectAttributes rttr){
        log.info("updateBoard()");

        TransactionStatus status =
                manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try{
            bDao.updateBoard(board);
            fileUpload(files, session, board.getB_num());

            manager.commit(status);
            view = "redirect:boardDetail?b_num="
                    + board.getB_num();
            msg = "수정 성공";
        } catch (Exception e){
            e.printStackTrace();
            manager.rollback(status);
            view = "redirect:updateForm?b_num="
                    + board.getB_num();
            msg = "수정 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    public ReplyDto replyInsert(ReplyDto reply) {
        log.info("replyInsert()");

        try{
            bDao.insertReply(reply);
            reply = bDao.selectLastReply(reply.getR_num());
        } catch (Exception e){
            e.printStackTrace();
            reply = null;
        }
        return reply;
    }
}//class end





