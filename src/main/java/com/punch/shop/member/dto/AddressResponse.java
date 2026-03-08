package com.punch.shop.member.dto;

import com.punch.shop.common.util.PhoneUtils;
import com.punch.shop.member.model.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {

    private Long id;
    private String label;
    private String recipientName;
    private String phone;
    private String zipCode;
    private String address;
    private String addressDetail;
    private boolean defaultAddress;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phone(PhoneUtils.format(address.getPhone()))
                .zipCode(address.getZipCode())
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}
