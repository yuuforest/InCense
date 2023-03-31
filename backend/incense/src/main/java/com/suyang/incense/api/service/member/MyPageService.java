package com.suyang.incense.api.service.member;

import com.suyang.incense.api.request.member.mypage.PerfumeModifyReq;
import com.suyang.incense.api.request.member.mypage.PerfumeRegisterReq;
import com.suyang.incense.api.request.member.mypage.ReviewModifyReq;
import com.suyang.incense.api.response.member.mypage.PerfumeRes;
import com.suyang.incense.api.response.member.mypage.ReviewRes;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MyPageService {

    // [향수]
    List<PerfumeRes> getMyPerfume(String type, Authentication Authentication);
    void registerPerfume(PerfumeRegisterReq perfumeRegisterReq, Authentication authentication);
    void modifyPerfume(PerfumeModifyReq perfumeModifyReq);
    void removePerfume(Long myPerfumeId);

    // [후기]
    List<ReviewRes> getMyReview(Authentication Authentication);
    void modifyMyReview(ReviewModifyReq reviewModifyReq);
}
