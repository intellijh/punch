package com.punch.shop.member.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");
    }

    @Test
    @DisplayName("배송지 생성 성공 - 모든 필드가 올바르게 저장되고 전화번호가 정규화됨")
    void createSuccess() {
        Address address = Address.create(member, "홍길동", "010-1234-5678", "12345",
                "서울시 강남구 테헤란로 123", "101동 1001호", "집", true);

        assertThat(address.getMember()).isSameAs(member);
        assertThat(address.getRecipientName()).isEqualTo("홍길동");
        assertThat(address.getPhone()).isEqualTo("01012345678");
        assertThat(address.getZipCode()).isEqualTo("12345");
        assertThat(address.getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(address.getAddressDetail()).isEqualTo("101동 1001호");
        assertThat(address.getLabel()).isEqualTo("집");
        assertThat(address.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("배송지 생성 실패 - 유효하지 않은 전화번호 입력 시 예외 발생")
    void createInvalidPhone() {
        assertThatThrownBy(() -> Address.create(member, "홍길동", "010-123-456", "12345",
                "서울시 강남구 테헤란로 123", null, "집", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }

    @Test
    @DisplayName("배송지 생성 - 빈 문자열 상세 주소는 null로 정규화")
    void createNormalizesEmptyAddressDetail() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", "", "집", false);

        assertThat(address.getAddressDetail()).isNull();
    }

    @Test
    @DisplayName("배송지 생성 - 공백 문자열 상세 주소는 null로 정규화")
    void createNormalizesBlankAddressDetail() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", "   ", "집", false);

        assertThat(address.getAddressDetail()).isNull();
    }

    @Test
    @DisplayName("배송지 수정 성공 - 변경된 값으로 업데이트되고 전화번호가 정규화됨, 기본 배송지 상태는 변경되지 않음")
    void updateSuccess() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", null, "집", true);

        address.update("김철수", "010-9876-5432", "54321", "서울시 마포구 상암로 1", "202동", "회사");

        assertThat(address.getRecipientName()).isEqualTo("김철수");
        assertThat(address.getPhone()).isEqualTo("01098765432");
        assertThat(address.getZipCode()).isEqualTo("54321");
        assertThat(address.getAddress()).isEqualTo("서울시 마포구 상암로 1");
        assertThat(address.getAddressDetail()).isEqualTo("202동");
        assertThat(address.getLabel()).isEqualTo("회사");
        assertThat(address.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("배송지 수정 - 상세 주소가 빈 문자열이면 null로 정규화됨")
    void updateNormalizesEmptyAddressDetail() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", "101동 1001호", "집", false);

        address.update("홍길동", "01012345678", "12345", "서울시 강남구 테헤란로 123", "", "집");

        assertThat(address.getAddressDetail()).isNull();
    }

    @Test
    @DisplayName("배송지 수정 실패 - 유효하지 않은 전화번호 입력 시 예외 발생")
    void updateInvalidPhone() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", null, "집", true);

        assertThatThrownBy(() -> address.update("김철수", "010-123-456", "54321",
                "서울시 마포구 상암로 1", null, "회사"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 휴대폰 번호입니다");
    }

    @Test
    @DisplayName("기본 배송지 설정 - setAsDefault 호출 시 기본 배송지로 변경")
    void setAsDefault() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", null, "집", false);

        assertThat(address.isDefaultAddress()).isFalse();

        address.setAsDefault();

        assertThat(address.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 해제 - unsetDefault 호출 시 기본 배송지 해제")
    void unsetDefault() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", null, "집", true);

        assertThat(address.isDefaultAddress()).isTrue();

        address.unsetDefault();

        assertThat(address.isDefaultAddress()).isFalse();
    }
}
