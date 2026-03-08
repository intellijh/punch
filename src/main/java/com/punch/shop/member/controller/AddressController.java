package com.punch.shop.member.controller;

import com.punch.shop.member.dto.AddressCreateRequest;
import com.punch.shop.member.dto.AddressResponse;
import com.punch.shop.member.dto.AddressUpdateRequest;
import com.punch.shop.member.exception.MaxAddressCountException;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/member/address")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public String addressList(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        List<AddressResponse> addresses = addressService.getAddresses(principal.getMemberId());
        model.addAttribute("addresses", addresses);
        return "member/address";
    }

    @GetMapping("/new")
    public String addForm(Model model) {
        model.addAttribute("addressCreateRequest", new AddressCreateRequest());
        return "member/address-form";
    }

    @PostMapping("/new")
    public String add(@AuthenticationPrincipal MemberPrincipal principal,
                      @Valid @ModelAttribute AddressCreateRequest addressCreateRequest,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/address-form";
        }

        try {
            addressService.addAddress(principal.getMemberId(), addressCreateRequest);
            redirectAttributes.addFlashAttribute("message", "배송지가 추가되었습니다.");
        } catch (MaxAddressCountException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/member/address";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal MemberPrincipal principal,
                           @PathVariable Long id,
                           Model model) {
        AddressResponse address = addressService.getAddress(principal.getMemberId(), id);

        model.addAttribute("addressId", id);
        model.addAttribute("addressUpdateRequest", AddressUpdateRequest.builder()
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .zipCode(address.getZipCode())
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .build());
        return "member/address-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@AuthenticationPrincipal MemberPrincipal principal,
                       @PathVariable Long id,
                       @Valid @ModelAttribute AddressUpdateRequest addressUpdateRequest,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("addressId", id);
            return "member/address-form";
        }

        addressService.updateAddress(principal.getMemberId(), id, addressUpdateRequest);
        redirectAttributes.addFlashAttribute("message", "배송지가 수정되었습니다.");
        return "redirect:/member/address";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal MemberPrincipal principal,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        addressService.deleteAddress(principal.getMemberId(), id);
        redirectAttributes.addFlashAttribute("message", "배송지가 삭제되었습니다.");
        return "redirect:/member/address";
    }

    @PostMapping("/{id}/default")
    public String setDefault(@AuthenticationPrincipal MemberPrincipal principal,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        addressService.setDefaultAddress(principal.getMemberId(), id);
        redirectAttributes.addFlashAttribute("message", "기본 배송지가 변경되었습니다.");
        return "redirect:/member/address";
    }
}
