package com.punch.shop.member.dto;

import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressResponseTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");
    }

    private Address createAddress(String addressDetail) {
        return Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", addressDetail, "집", false);
    }

    @Test
    @DisplayName("AddressResponse 변환 성공 - 모든 필드가 올바르게 매핑되고 전화번호가 포맷팅됨")
    void fromAllFields() {
        Address address = Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", "101동 1001호", "집", true);

        AddressResponse response = AddressResponse.from(address);

        assertThat(response.getLabel()).isEqualTo("집");
        assertThat(response.getRecipientName()).isEqualTo("홍길동");
        assertThat(response.getPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getZipCode()).isEqualTo("12345");
        assertThat(response.getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(response.getAddressDetail()).isEqualTo("101동 1001호");
        assertThat(response.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("AddressResponse 변환 성공 - 상세 주소가 null이면 null로 반환됨")
    void fromNullAddressDetail() {
        Address address = createAddress(null);

        AddressResponse response = AddressResponse.from(address);

        assertThat(response.getAddressDetail()).isNull();
    }

    @Test
    @DisplayName("AddressResponse 변환 성공 - 빈 문자열 상세 주소는 null로 정규화되어 반환됨")
    void fromEmptyAddressDetail() {
        Address address = createAddress("");

        AddressResponse response = AddressResponse.from(address);

        assertThat(response.getAddressDetail()).isNull();
    }

    @Test
    @DisplayName("AddressResponse 변환 성공 - 공백 문자열 상세 주소는 null로 정규화되어 반환됨")
    void fromBlankAddressDetail() {
        Address address = createAddress("   ");

        AddressResponse response = AddressResponse.from(address);

        assertThat(response.getAddressDetail()).isNull();
    }
}
