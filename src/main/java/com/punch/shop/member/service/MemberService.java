package com.punch.shop.member.service;

import com.punch.shop.member.dto.MemberProfileResponse;
import com.punch.shop.member.dto.MemberProfileUpdateRequest;
import com.punch.shop.member.dto.MemberRegisterRequest;
import com.punch.shop.member.dto.MemberRegisterResponse;
import com.punch.shop.member.exception.DuplicateEmailException;
import com.punch.shop.member.exception.MemberNotFoundException;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberRegisterResponse register(MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
        }

        Member member = Member.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getPhone()
        );

        Member savedMember = memberRepository.save(member);

        return MemberRegisterResponse.from(savedMember);
    }

    public MemberProfileResponse getProfile(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberProfileResponse::from)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    @Transactional
    public MemberProfileResponse updateProfile(Long memberId, MemberProfileUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        member.updateProfile(request.getName(), request.getPhone());
        return MemberProfileResponse.from(member);
    }
}
