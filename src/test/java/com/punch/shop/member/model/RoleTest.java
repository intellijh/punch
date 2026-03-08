package com.punch.shop.member.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    @DisplayName("모든 Role의 authority는 ROLE_ 접두사를 포함한 문자열 반환")
    void roleAuthority() {
        for (Role role : Role.values()) {
            assertThat(role.getAuthority()).isEqualTo("ROLE_" + role.name());
        }
    }
}
