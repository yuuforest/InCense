package com.suyang.incense.api.service.member;

import com.suyang.incense.api.request.member.mypage.PerfumeModifyReq;
import com.suyang.incense.api.request.member.mypage.PerfumeRegisterReq;
import com.suyang.incense.api.request.member.mypage.ReviewModifyReq;
import com.suyang.incense.api.response.member.mypage.BookmarkRes;
import com.suyang.incense.api.response.member.mypage.DealRes;
import com.suyang.incense.api.response.member.mypage.PerfumeRes;
import com.suyang.incense.api.response.member.mypage.ReviewRes;
import com.suyang.incense.db.entity.member.Member;
import com.suyang.incense.db.entity.perfume.Perfume;
import com.suyang.incense.db.entity.relation.Category;
import com.suyang.incense.db.entity.relation.MemberPerfume;
import com.suyang.incense.db.entity.review.Review;
import com.suyang.incense.db.repository.deal.DealBookmarkRepository;
import com.suyang.incense.db.repository.deal.DealRepository;
import com.suyang.incense.db.repository.member.*;
import com.suyang.incense.db.repository.perfume.PerfumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService{

    private final MemberPerfumeCustomRepository memberPerfumeCustomRepository;
    private final DealBookmarkRepository dealBookmarkRepository;
    private final MemberPerfumeRepository memberPerfumeRepository;
    private final DealRepository dealRepository;
    private final ReviewCustomRepository reviewCustomRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final PerfumeRepository perfumeRepository;
    private final AuthService authService;

    @Override
    public List<PerfumeRes> getMyPerfume(String type, Authentication authentication) {
        Long memberId = authService.getIdByAuthentication(authentication);
        if(type.equals("WANT")) {
            return memberPerfumeCustomRepository.getMyWantPerfume(memberId);
        } else {
            return memberPerfumeCustomRepository.getMyHaveHadPerfume(type, memberId);
        }
    }

    @Override
    @Transactional
    public void registerPerfume(PerfumeRegisterReq perfumeRegisterReq, Authentication authentication) {
        String category = perfumeRegisterReq.getCategory();
        Perfume perfume = perfumeRepository.findById(perfumeRegisterReq.getPerfumeId()).get();
        Member member = authService.getMemberByAuthentication(authentication).get();
        // MemberPerfume
        MemberPerfume memberPerfume = new MemberPerfume();
        memberPerfume.setMember(member);
        memberPerfume.setPerfume(perfume);
        memberPerfume.setCategory(Category.valueOf(category));
        memberPerfumeRepository.save(memberPerfume);
        // review
        if(category.equals("WANT")) {
            // popular_cnt +1 : 향수 Service로 빼서 구성할지 미정
            perfume.setPopularCnt(perfume.getPopularCnt() + 1);
        } else {
            perfume.setCommentCnt(perfume.getCommentCnt() + 1);
            Review review = new Review();
            review.setPreference(perfumeRegisterReq.getPreference());
            review.setComment(perfumeRegisterReq.getComment());
            review.setMember(member);
            review.setPerfume(perfume);
            reviewRepository.save(review);
        }
    }

    @Override
    @Transactional
    public void modifyPerfume(PerfumeModifyReq perfumeModifyReq) {
        String category = perfumeModifyReq.getCategory();
        // memberPerfume
        MemberPerfume myPerfume = memberPerfumeRepository.findById(perfumeModifyReq.getMemberPerfumeId()).get();
        myPerfume.setCategory(Category.valueOf(category));
        // review
        if(!category.equals("WANT")) {
            Member member = memberRepository.findById(myPerfume.getMember().getId()).get();
            Perfume perfume = perfumeRepository.findById(myPerfume.getPerfume().getId()).get();
            Review review = reviewCustomRepository.getReviewByMemberAndPerfume(member, perfume);
            if(review == null) {
                Review newReview = new Review();
                newReview.setMember(member);
                newReview.setPerfume(perfume);
                newReview.setPreference(perfumeModifyReq.getPreference());
                newReview.setComment(perfumeModifyReq.getComment());
                reviewRepository.save(newReview);
            } else {
                review.setPreference(perfumeModifyReq.getPreference());
                review.setComment(perfumeModifyReq.getComment());
            }
        }
    }

    @Override
    public void removePerfume(Long myPerfumeId) {
        memberPerfumeRepository.deleteById(myPerfumeId);
    }

    @Override
    public List<ReviewRes> getMyReview(Authentication authentication) {
//        Member member = memberRepository.findById(authService.getIdByAuthentication(authentication)).get();
//        return reviewCustomRepository.getReviewByMember(member);
        return reviewCustomRepository.getReviewByMember(authService.getMemberByAuthentication(authentication).get());
    }

    @Override
    @Transactional
    public void modifyMyReview(ReviewModifyReq reviewModifyReq) {
        Review review = reviewRepository.findById(reviewModifyReq.getReviewId()).get();
        review.setComment(reviewModifyReq.getComment());
        review.setPreference(reviewModifyReq.getPreference());
    }

    @Override
    public List<DealRes> getMyDeal(Authentication authentication) {
        Member member = authService.getMemberByAuthentication(authentication).get();
        return dealRepository.getDealByMember(member);
    }

    @Override
    public List<BookmarkRes> getMyBookmark(Authentication authentication) {
        Long memberId = authService.getIdByAuthentication(authentication);
        return dealBookmarkRepository.getBookmarkByMember(memberId);
    }
}
