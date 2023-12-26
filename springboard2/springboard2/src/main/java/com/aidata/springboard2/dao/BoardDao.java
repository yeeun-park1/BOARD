package com.aidata.springboard2.dao;

import com.aidata.springboard2.dto.BoardDto;
import com.aidata.springboard2.dto.BoardFileDto;
import com.aidata.springboard2.dto.ReplyDto;
import com.aidata.springboard2.dto.SearchDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardDao {
    List<BoardDto> selectBoardList(SearchDto sdto);
    //전체 개시글 개수 구하는 메소드
    int selectBoardCnt(SearchDto sdto);
    //게시글 저장 메소드
    void insertBoard(BoardDto board);
    //파일 정보 저장 메소드
    void insertFile(BoardFileDto bFile);
    //게시글 하나만 가져오는 메소드
    BoardDto selectBoard(int b_num);
    //게시글 번호에 해당하는 파일목록을 가져오는 메소드
    List<BoardFileDto> selectFileList(int b_num);
    //게시글 번호에 해당하는 댓글목록을 가져오는 메소드
    List<ReplyDto> selectReplyList(int b_num);
    //게시글 번호에 해당하는 게시글 삭제 메소드
    void deleteBoard(int b_num);
    //게시글 번호에 해당하는 파일목록 삭제 메소드
    void deleteFiles(int b_num);
    //게시글 번호에 해당하는 댓글목록 삭제 메소드
    void deleteReplays(int b_num);
    //파일의 저장 이름 목록 구하는 메소드
    List<String> selectFnameList(int b_num);
    //수정 시 단독 파일 삭제
    void deleteFile(String sysname);
    //게시글 수정 메소드
    void updateBoard(BoardDto board);
    //댓글 저장 메소드
    void insertReply(ReplyDto reply);
    //저장 시 생성된 댓글 번호로 댓글 정보 가져오는 메소드
    ReplyDto selectLastReply(int r_num);
}
