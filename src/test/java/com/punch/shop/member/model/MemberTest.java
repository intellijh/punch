package com.punch.shop.member.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    @DisplayName("회원 생성 성공 - 하이픈 포함 전화번호가 정규화되어 저장")
    void createNormalizesPhone() {
        Member member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");

        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getPhone()).isEqualTo("01012345678");
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 생성 실패 - 유효하지 않은 전화번호 입력 시 예외 발생")
    void createInvalidPhone() {
        assertThatThrownBy(() -> Member.create("test@example.com", "encodedPassword", "홍길동", "010-123-456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }

    @Test
    @DisplayName("프로필 수정 성공 - 이름과 전화번호가 변경되고 전화번호가 정규화되어 저장")
    void updateProfileSuccess() {
        Member member = Member.create("test@example.com", "encodedPassword", "홍길동", "01012345678");

        member.updateProfile("김철수", "010-9876-5432");

        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getPhone()).isEqualTo("01098765432");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 유효하지 않은 전화번호 입력 시 예외 발생")
    void updateProfileInvalidPhone() {
        Member member = Member.create("test@example.com", "encodedPassword", "홍길동", "01012345678");

        assertThatThrownBy(() -> member.updateProfile("김철수", "010-9876-543"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }

    @Test
    @DisplayName("비밀번호 변경 성공 - 새 비밀번호로 정상적으로 변경")
    void updatePasswordSuccess() {
        Member member = Member.create("test@example.com", "encodedPassword", "홍길동", "01012345678");

        member.updatePassword("newEncodedPassword");

        assertThat(member.getPassword()).isEqualTo("newEncodedPassword");
    }
}
