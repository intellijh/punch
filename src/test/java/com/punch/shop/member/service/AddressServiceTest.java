package com.punch.shop.member.service;

import com.punch.shop.member.dto.AddressCreateRequest;
import com.punch.shop.member.dto.AddressResponse;
import com.punch.shop.member.dto.AddressUpdateRequest;
import com.punch.shop.member.exception.AddressNotFoundException;
import com.punch.shop.member.exception.MaxAddressCountException;
import com.punch.shop.member.exception.MemberNotFoundException;
import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.AddressRepository;
import com.punch.shop.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AddressService addressService;

    private Member createMember() {
        return Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");
    }

    private Address createAddress(Member member, String label, boolean isDefault) {
        return Address.create(member, "홍길동", "010-1234-5678", "12345",
                "서울시 강남구 테헤란로 123", null, label, isDefault);
    }

    private AddressCreateRequest createRequest() {
        return AddressCreateRequest.builder()
                .label("집").recipientName("홍길동").phone("010-1234-5678")
                .zipCode("12345").address("서울시 강남구 테헤란로 123").build();
    }

    private AddressUpdateRequest updateRequest() {
        return AddressUpdateRequest.builder()
                .label("집").recipientName("홍길동").phone("010-1234-5678")
                .zipCode("12345").address("서울시 강남구 테헤란로 123").build();
    }

    @Test
    @DisplayName("배송지 목록 조회")
    void getAddresses() {
        Member member = createMember();
        Address address = createAddress(member, "집", true);
        given(addressRepository.findByMemberIdOrderByDefaultAddressDesc(1L)).willReturn(List.of(address));

        List<AddressResponse> result = addressService.getAddresses(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLabel()).isEqualTo("집");
    }

    @Test
    @DisplayName("배송지 단건 조회 성공")
    void getAddressSuccess() {
        Member member = createMember();
        Address address = createAddress(member, "집", true);
        given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

        AddressResponse result = addressService.getAddress(1L, 1L);

        assertThat(result.getLabel()).isEqualTo("집");
    }

    @Test
    @DisplayName("배송지 단건 조회 실패 - 존재하지 않는 배송지")
    void getAddressNotFound() {
        given(addressRepository.findByIdAndMemberId(99L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddress(1L, 99L))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("배송지 추가 성공 - 첫 번째 배송지는 자동으로 기본 배송지 설정")
    void addAddressFirstIsDefault() {
        Member member = createMember();
        AddressCreateRequest request = createRequest();

        given(addressRepository.countByMemberId(1L)).willReturn(0L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(addressRepository.save(any(Address.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AddressResponse result = addressService.addAddress(1L, request);

        assertThat(result.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("배송지 추가 성공 - 두 번째 배송지는 기본 배송지 아님")
    void addAddressSecondIsNotDefault() {
        Member member = createMember();
        AddressCreateRequest request = AddressCreateRequest.builder()
                .label("회사").recipientName("홍길동").phone("010-1234-5678")
                .zipCode("54321").address("서울시 마포구 상암로 1").build();

        given(addressRepository.countByMemberId(1L)).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(addressRepository.save(any(Address.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AddressResponse result = addressService.addAddress(1L, request);

        assertThat(result.isDefaultAddress()).isFalse();
    }

    @Test
    @DisplayName("배송지 추가 실패 - 최대 등록 개수 초과")
    void addAddressMaxCountExceeded() {
        AddressCreateRequest request = createRequest();
        given(addressRepository.countByMemberId(1L)).willReturn(10L);

        assertThatThrownBy(() -> addressService.addAddress(1L, request))
                .isInstanceOf(MaxAddressCountException.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("배송지 추가 실패 - 존재하지 않는 회원")
    void addAddressMemberNotFound() {
        AddressCreateRequest request = createRequest();
        given(addressRepository.countByMemberId(99L)).willReturn(0L);
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.addAddress(99L, request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("배송지 수정 성공")
    void updateAddressSuccess() {
        Member member = createMember();
        Address address = createAddress(member, "집", true);
        AddressUpdateRequest request = AddressUpdateRequest.builder()
                .label("집(수정)").recipientName("김철수").phone("010-9876-5432")
                .zipCode("54321").address("서울시 마포구 상암로 1").build();

        given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

        AddressResponse result = addressService.updateAddress(1L, 1L, request);

        assertThat(result.getLabel()).isEqualTo("집(수정)");
        assertThat(result.getRecipientName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("배송지 수정 실패 - 존재하지 않는 배송지")
    void updateAddressNotFound() {
        AddressUpdateRequest request = updateRequest();
        given(addressRepository.findByIdAndMemberId(99L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.updateAddress(1L, 99L, request))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteAddressSuccess() {
        Member member = createMember();
        Address address = createAddress(member, "집", false);
        given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

        addressService.deleteAddress(1L, 1L);

        verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 존재하지 않는 배송지")
    void deleteAddressNotFound() {
        given(addressRepository.findByIdAndMemberId(99L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(1L, 99L))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("기본 배송지 변경 성공 - 이전 기본 배송지 해제")
    void setDefaultAddressSuccess() {
        Member member = createMember();
        Address oldDefault = createAddress(member, "회사", true);
        Address newDefault = createAddress(member, "집", false);

        given(addressRepository.findByMemberIdAndDefaultAddressTrue(1L))
                .willReturn(Optional.of(oldDefault));
        given(addressRepository.findByIdAndMemberId(2L, 1L))
                .willReturn(Optional.of(newDefault));

        addressService.setDefaultAddress(1L, 2L);

        assertThat(oldDefault.isDefaultAddress()).isFalse();
        assertThat(newDefault.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 변경 실패 - 존재하지 않는 배송지")
    void setDefaultAddressNotFound() {
        given(addressRepository.findByMemberIdAndDefaultAddressTrue(1L))
                .willReturn(Optional.empty());
        given(addressRepository.findByIdAndMemberId(99L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.setDefaultAddress(1L, 99L))
                .isInstanceOf(AddressNotFoundException.class);
    }
}
