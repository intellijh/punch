package com.punch.shop.member.model;

import com.punch.shop.common.model.BaseEntity;
import com.punch.shop.common.util.PhoneUtils;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import lombok.*;

@Entity
@Table(name = "address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 5)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    private String addressDetail;
    private String label;

    @Column(nullable = false, name = "is_default")
    @Builder.Default
    private boolean defaultAddress = false;

    public void update(String recipientName, String phone, String zipCode,
                       String address, String addressDetail, String label) {
        this.recipientName = recipientName;
        this.phone = PhoneUtils.validateAndNormalize(phone);
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = normalize(addressDetail);
        this.label = label;
    }

    public void setAsDefault() {
        this.defaultAddress = true;
    }

    public void unsetDefault() {
        this.defaultAddress = false;
    }

    public static Address create(Member member, String recipientName, String phone,
                                 String zipCode, String address, String addressDetail,
                                 String label, boolean defaultAddress) {
        return Address.builder()
                .member(member)
                .recipientName(recipientName)
                .phone(PhoneUtils.validateAndNormalize(phone))
                .zipCode(zipCode)
                .address(address)
                .addressDetail(normalize(addressDetail))
                .label(label)
                .defaultAddress(defaultAddress)
                .build();
    }

    @Nullable
    private static String normalize(String addressDetail) {
        return StringUtils.hasText(addressDetail) ? addressDetail : null;
    }
}
