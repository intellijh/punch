package com.punch.shop.member.dto;

import com.punch.shop.common.util.PhoneUtils;
import com.punch.shop.member.model.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponse {

    private String email;
    private String name;
    private String phone;

    public static MemberProfileResponse from(Member member) {
        return MemberProfileResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .phone(PhoneUtils.format(member.getPhone()))
                .build();
    }
}
