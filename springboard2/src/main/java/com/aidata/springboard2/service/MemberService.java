package com.aidata.springboard2.service;

import com.aidata.springboard2.dao.MemberDao;
import com.aidata.springboard2.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
@Slf4j
public class MemberService {
    @Autowired
    private MemberDao mDao;

    //비밀번호 암호화 인코더
    private BCryptPasswordEncoder pEncoder =
            new BCryptPasswordEncoder();

    public String idCheck(String mid){
        log.info("idCheck()");
        String result = null;

        int mcnt = mDao.selectId(mid);
        if(mcnt == 0){
            result = "ok";
        } else {
            result = "fail";
        }

        return result;
    }

    public String memberJoin(MemberDto member,
                             RedirectAttributes rttr){
        log.info("memberJoin()");
        //가입 성공 시 첫페이지로, 실패 시 가입 페이지로 이동
        String view = null;
        String msg = null;//경고창으로 출력할 메시지 저장 변수

        //비밀번호 암호화 처리
        String encPwd = pEncoder.encode(member.getM_pwd());
        //암호화된 비밀번호를 다시 dto 객체에 저장
        member.setM_pwd(encPwd);

        try {
            mDao.insertMember(member);
            msg = "가입 성공";
            view = "redirect:/";//list 페이지 작성 시 변경할 예정.
        } catch (Exception e){
            e.printStackTrace();
            msg = "가입 실패";
            view = "redirect:joinForm";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    public String loginProc(MemberDto member,
                            HttpSession session,
                            RedirectAttributes rttr){
        log.info("loginProc()");
        String view = null;
        String msg = null;

        //DB에서 해당 id의 비밀번호(암호문) 가져오기.
        String encPwd = mDao.selectPassword(member.getM_id());
        //encPwd에 암호화된 비밀번호가 들어가거나, - 해당 아이디가 존재
        //비밀번호가 들어오지 않거나(null). - 해당 아이디가 X
        if(encPwd != null){
            //입력한 비번과 DB에서 가져온 비번 비교(matches)
            if(pEncoder.matches(member.getM_pwd(), encPwd)){
                //로그인 성공.
                //회원 정보(아이디, 이름, 포인트, 등급이름) - from DB
                member = mDao.selectMember(member.getM_id());
                //세션에 회원 정보 저장.
                session.setAttribute("member", member);
                //로그인 성공 후 게시판 목록 페이지로 이동.
                view = "redirect:boardList?pageNum=1";
                msg = "로그인 성공";
            } else {
                //로그인 실패. - 비번을 잘못 입력한 경우
                view = "redirect:loginForm";
                msg = "비밀번호가 틀립니다.";
            }
        } else {
            //아이디가 없는 경우
            view = "redirect:loginForm";
            msg = "존재하지 않는 아이디입니다.";
        }
        //화면으로 메시지 보내기
        rttr.addFlashAttribute("msg", msg);

        return view;
    }

    public String logout(HttpSession session) {
        log.info("logout()");
        session.invalidate();
        return "redirect:/";
    }
}//class end
