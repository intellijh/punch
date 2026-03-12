package com.punch.shop.member.repository;

import com.punch.shop.common.config.JpaAuditingConfig;
import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678"));
    }

    private Address createAddress(String label, boolean isDefault) {
        return Address.create(member, "홍길동", "01012345678", "12345",
                "서울시 강남구 테헤란로 123", null, label, isDefault);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("배송지 저장 - ID 및 Auditing 적용 확인")
    void save() {
        Address address = addressRepository.save(createAddress("집", true));

        assertThat(address.getId()).isNotNull();
        assertThat(address.getLabel()).isEqualTo("집");
        assertThat(address.isDefaultAddress()).isTrue();
        assertThat(address.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 ID로 배송지 목록 조회 - 기본 배송지가 최상단에 위치")
    void findByMemberId() {
        addressRepository.save(createAddress("회사", false));
        addressRepository.save(createAddress("집", true));
        flushAndClear();

        List<Address> result = addressRepository.findByMemberIdOrderByDefaultAddressDesc(member.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLabel()).isEqualTo("집");
        assertThat(result.get(0).isDefaultAddress()).isTrue();
        assertThat(result.get(1).getLabel()).isEqualTo("회사");
    }

    @Test
    @DisplayName("다른 회원의 배송지는 조회되지 않음")
    void findByMemberId_otherMember() {
        addressRepository.save(createAddress("집", true));
        addressRepository.save(createAddress("회사", false));

        Member other = memberRepository.save(
                Member.create("other@example.com", "encodedPassword", "김철수", "010-9876-5432"));
        addressRepository.save(Address.create(other, "김철수", "01098765432", "54321",
                "서울시 마포구 상암로 1", null, "부모님댁", true));
        flushAndClear();

        List<Address> memberResult = addressRepository.findByMemberIdOrderByDefaultAddressDesc(member.getId());
        List<Address> otherResult = addressRepository.findByMemberIdOrderByDefaultAddressDesc(other.getId());

        assertThat(memberResult).hasSize(2);
        assertThat(memberResult).extracting(Address::getLabel)
                .containsExactlyInAnyOrder("집", "회사")
                .doesNotContain("부모님댁");

        assertThat(otherResult).hasSize(1);
        assertThat(otherResult.get(0).getLabel()).isEqualTo("부모님댁");
    }

    @Test
    @DisplayName("배송지 ID와 회원 ID로 단건 조회 성공")
    void findByIdAndMemberId() {
        Address saved = addressRepository.save(createAddress("집", true));
        flushAndClear();

        Optional<Address> result = addressRepository.findByIdAndMemberId(saved.getId(), member.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getLabel()).isEqualTo("집");
    }

    @Test
    @DisplayName("배송지 ID와 회원 ID가 일치하지 않으면 빈 결과 반환")
    void findByIdAndMemberId_mismatch() {
        Address saved = addressRepository.save(createAddress("집", true));

        Member other = memberRepository.save(
                Member.create("other@example.com", "encodedPassword", "김철수", "010-9876-5432"));
        flushAndClear();

        Optional<Address> result = addressRepository.findByIdAndMemberId(saved.getId(), other.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회원 ID로 배송지 개수 조회")
    void countByMemberId() {
        addressRepository.save(createAddress("집", true));
        addressRepository.save(createAddress("회사", false));
        flushAndClear();

        long count = addressRepository.countByMemberId(member.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("기본 배송지 조회 성공")
    void findByMemberIdAndDefaultAddressTrue() {
        addressRepository.save(createAddress("집", true));
        addressRepository.save(createAddress("회사", false));
        flushAndClear();

        Optional<Address> result = addressRepository.findByMemberIdAndDefaultAddressTrue(member.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getLabel()).isEqualTo("집");
        assertThat(result.get().isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지가 없으면 빈 결과 반환")
    void findByMemberIdAndDefaultAddressTrue_notExists() {
        Optional<Address> result = addressRepository.findByMemberIdAndDefaultAddressTrue(member.getId());

        assertThat(result).isEmpty();
    }
}
