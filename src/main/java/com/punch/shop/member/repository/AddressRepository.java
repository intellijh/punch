package com.punch.shop.member.repository;

import com.punch.shop.member.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByMemberIdOrderByDefaultAddressDesc(Long memberId);

    Optional<Address> findByMemberIdAndDefaultAddressTrue(Long memberId);

    long countByMemberId(Long memberId);

    Optional<Address> findByIdAndMemberId(Long id, Long memberId);
}
