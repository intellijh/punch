package com.punch.shop.member.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("유효한 회원가입 요청")
    void validRequest() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일이 null이면 검증 실패")
    void emailIsNull() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email(null)
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("이메일은 필수입니다");
    }

    @Test
    @DisplayName("이메일이 빈 문자열이면 검증 실패")
    void emailIsBlank() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("이메일은 필수입니다");
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 검증 실패")
    void emailFormatInvalid() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("invalid-email")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("이메일이 100자를 초과하면 검증 실패")
    void emailTooLong() {
        String local = "a".repeat(64);
        String domain = "b".repeat(32) + ".com";
        String longEmail = local + "@" + domain;
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email(longEmail)
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("이메일은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("비밀번호가 null이면 검증 실패")
    void passwordIsNull() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password(null)
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 검증 실패")
    void passwordTooShort() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("pass123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("비밀번호는 최소 8자 이상이어야 합니다");
    }

    @Test
    @DisplayName("이름이 null이면 검증 실패")
    void nameIsNull() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name(null)
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("이름은 필수입니다");
    }

    @Test
    @DisplayName("전화번호가 null이면 검증 실패")
    void phoneNumberIsNull() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone(null)
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("전화번호는 필수입니다");
    }

    @Test
    @DisplayName("전화번호 형식이 올바르지 않으면 검증 실패")
    void phoneNumberFormatInvalid() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("01012345678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsOnly("전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)");
    }

    @Test
    @DisplayName("전화번호 형식 - 4자리 중간번호도 허용")
    void phoneNumberWith4DigitMiddle() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("전화번호 형식 - 3자리 중간번호도 허용")
    void phoneNumberWith3DigitMiddle() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-123-5678")
                .build();

        Set<ConstraintViolation<MemberRegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("전화번호 하이픈 제거 변환")
    void getPhoneWithoutHyphen() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        String phoneWithoutHyphen = request.getPhoneWithoutHyphen();

        assertThat(phoneWithoutHyphen).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("전화번호가 null이면 null 반환")
    void getPhoneWithoutHyphenWhenNull() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone(null)
                .build();

        String phoneWithoutHyphen = request.getPhoneWithoutHyphen();

        assertThat(phoneWithoutHyphen).isNull();
    }
}
