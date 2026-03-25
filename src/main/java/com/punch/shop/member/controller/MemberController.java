package com.punch.shop.member.controller;

import com.punch.shop.member.dto.MemberProfileResponse;
import com.punch.shop.member.dto.MemberProfileUpdateRequest;
import com.punch.shop.member.dto.MemberRegisterRequest;
import com.punch.shop.member.dto.MemberRegisterResponse;
import com.punch.shop.member.exception.DuplicateEmailException;
import com.punch.shop.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.punch.shop.member.model.MemberPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model) {
        String lastUsername = (String) session.getAttribute("LAST_LOGIN_USERNAME");
        if (lastUsername != null) {
            model.addAttribute("lastUsername", lastUsername);
            session.removeAttribute("LAST_LOGIN_USERNAME");
        }
        return "member/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("memberRegisterRequest", new MemberRegisterRequest());
        return "member/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute MemberRegisterRequest request,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/register";
        }

        try {
            MemberRegisterResponse response = memberService.register(request);
            log.info("회원가입 완료: memberId={}", response.getMemberId());
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
            return "redirect:/member/login";
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            return "member/register";
        }
    }

    @GetMapping("/profile")
    public String profileForm(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        MemberProfileResponse profile = memberService.getProfile(principal.getMemberId());
        model.addAttribute("profile", profile);
        model.addAttribute("memberProfileUpdateRequest", MemberProfileUpdateRequest.builder()
                .name(profile.getName())
                .phone(profile.getPhone())
                .build());
        return "member/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal MemberPrincipal principal,
                                @Valid @ModelAttribute MemberProfileUpdateRequest request,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", memberService.getProfile(principal.getMemberId()));
            return "member/profile";
        }

        memberService.updateProfile(principal.getMemberId(), request);
        redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다.");
        return "redirect:/member/profile";
    }
}
