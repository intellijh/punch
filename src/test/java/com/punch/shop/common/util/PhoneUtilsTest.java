package com.punch.shop.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneUtilsTest {

    @Test
    @DisplayName("전화번호 정규화 성공 - 하이픈 포함 번호에서 숫자만 추출")
    void normalizeWithHyphen() {
        assertThat(PhoneUtils.normalize("010-1234-5678")).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("전화번호 정규화 성공 - 이미 숫자만 있는 번호는 그대로 반환")
    void normalizeDigitsOnly() {
        assertThat(PhoneUtils.normalize("01012345678")).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("전화번호 정규화 성공 - null 입력 시 null 반환")
    void normalizeNull() {
        assertThat(PhoneUtils.normalize(null)).isNull();
    }

    @Test
    @DisplayName("전화번호 포맷 성공 - 11자리 숫자를 XXX-XXXX-XXXX 형식으로 변환")
    void formatElevenDigits() {
        assertThat(PhoneUtils.format("01012345678")).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("전화번호 포맷 성공 - 하이픈 포함 번호도 올바르게 변환")
    void formatWithHyphen() {
        assertThat(PhoneUtils.format("010-1234-5678")).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("전화번호 포맷 실패 - 11자리가 아닌 번호는 원본 그대로 반환")
    void formatNonElevenDigits() {
        assertThat(PhoneUtils.format("0101234567")).isEqualTo("0101234567");
    }

    @Test
    @DisplayName("전화번호 포맷 성공 - null 입력 시 null 반환")
    void formatNull() {
        assertThat(PhoneUtils.format(null)).isNull();
    }

    @Test
    @DisplayName("전화번호 유효성 검증 성공 - 유효한 010 번호는 정규화된 숫자 반환")
    void validateAndNormalizeSuccess() {
        assertThat(PhoneUtils.validateAndNormalize("010-1234-5678")).isEqualTo("01012345678");
        assertThat(PhoneUtils.validateAndNormalize("01012345678")).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("전화번호 유효성 검증 실패 - 010으로 시작하지 않는 번호는 예외 발생")
    void validateAndNormalizeNon010() {
        assertThatThrownBy(() -> PhoneUtils.validateAndNormalize("011-1234-5678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }

    @Test
    @DisplayName("전화번호 유효성 검증 실패 - 자릿수가 부족한 번호는 예외 발생")
    void validateAndNormalizeShortNumber() {
        assertThatThrownBy(() -> PhoneUtils.validateAndNormalize("010-1234-567"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }

    @Test
    @DisplayName("전화번호 유효성 검증 실패 - null 입력 시 예외 발생")
    void validateAndNormalizeNull() {
        assertThatThrownBy(() -> PhoneUtils.validateAndNormalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }
}
