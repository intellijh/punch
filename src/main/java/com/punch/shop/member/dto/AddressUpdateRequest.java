package com.punch.shop.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressUpdateRequest {

    @NotBlank(message = "배송지명은 필수입니다")
    private String label;

    @NotBlank(message = "수령인은 필수입니다")
    private String recipientName;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^[0-9 -]+$", message = "전화번호는 숫자, 하이픈(-), 공백만 입력 가능합니다")
    private String phone;

    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다")
    private String zipCode;

    @NotBlank(message = "주소는 필수입니다")
    private String address;

    private String addressDetail;
}
