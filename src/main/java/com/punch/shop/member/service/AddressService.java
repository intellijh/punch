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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressService {

    private static final int MAX_ADDRESS_COUNT = 10;

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    public List<AddressResponse> getAddresses(Long memberId) {
        return addressRepository.findByMemberIdOrderByDefaultAddressDesc(memberId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    public AddressResponse getAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse addAddress(Long memberId, AddressCreateRequest request) {
        long currentCount = addressRepository.countByMemberId(memberId);

        if (currentCount >= MAX_ADDRESS_COUNT) {
            throw new MaxAddressCountException(MAX_ADDRESS_COUNT);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("memberId: " + memberId));

        boolean isFirstAddress = currentCount == 0;

        Address address = Address.create(
                member,
                request.getRecipientName(),
                request.getPhone(),
                request.getZipCode(),
                request.getAddress(),
                request.getAddressDetail(),
                request.getLabel(),
                isFirstAddress
        );

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long memberId, Long addressId, AddressUpdateRequest request) {
        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        address.update(
                request.getRecipientName(),
                request.getPhone(),
                request.getZipCode(),
                request.getAddress(),
                request.getAddressDetail(),
                request.getLabel()
        );

        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long memberId, Long addressId) {
        addressRepository.findByMemberIdAndDefaultAddressTrue(memberId)
                .ifPresent(Address::unsetDefault);

        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        address.setAsDefault();

        return AddressResponse.from(address);
    }
}
