package com.punch.shop.member.service;

import com.punch.shop.member.model.Member;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.model.Role;
import com.punch.shop.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("이메일로 사용자 조회 성공 - UserDetails 반환")
    void loadUserByUsernameSuccess() {
        String email = "test@example.com";
        Member member = Member.builder()
                .email(email)
                .password("encodedPassword")
                .name("홍길동")
                .phone("01012345678")
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isInstanceOf(MemberPrincipal.class);
        MemberPrincipal principal = (MemberPrincipal) userDetails;
        assertThat(principal.getMemberId()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo(email);
        assertThat(principal.getPassword()).isEqualTo("encodedPassword");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(Role.USER.getAuthority());

        verify(memberRepository).findByEmail(email);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않는 이메일일 경우 UsernameNotFoundException 발생")
    void loadUserByUsernameFail() {
        String email = "notfound@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(email);

        verify(memberRepository).findByEmail(email);
    }
}
