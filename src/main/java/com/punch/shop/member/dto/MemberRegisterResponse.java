package com.punch.shop.member.dto;

import com.punch.shop.member.model.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRegisterResponse {

    private Long memberId;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    public static MemberRegisterResponse from(Member member) {
        return MemberRegisterResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
