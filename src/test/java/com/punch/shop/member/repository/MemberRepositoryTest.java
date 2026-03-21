package com.punch.shop.member.repository;

import com.punch.shop.common.annotation.RepositoryTest;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.model.MemberStatus;
import com.punch.shop.member.model.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    private Member createMember(String email, String name) {
        return Member.create(email, "encodedPassword", name, "010-1234-5678");
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("회원 저장 - 기본값 및 Auditing 적용 확인")
    void save() {
        Member member = createMember("test@example.com", "홍길동");

        Member saved = memberRepository.save(member);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 회원 조회")
    void findByEmail() {
        memberRepository.save(createMember("test@example.com", "홍길동"));
        flushAndClear();

        Optional<Member> found = memberRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 이메일 조회 시 빈 결과 반환")
    void findByEmail_notFound() {
        Optional<Member> found = memberRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail() {
        memberRepository.save(createMember("test@example.com", "홍길동"));
        flushAndClear();

        assertThat(memberRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("other@example.com")).isFalse();
    }
}
