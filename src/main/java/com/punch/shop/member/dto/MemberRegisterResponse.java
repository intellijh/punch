package com.punch.shop.member.dto;

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
}
