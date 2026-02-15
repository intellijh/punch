package com.punch.shop.member.controller;

import com.punch.shop.member.dto.MemberRegisterRequest;
import com.punch.shop.member.dto.MemberRegisterResponse;
import com.punch.shop.member.exception.DuplicateEmailException;
import com.punch.shop.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
