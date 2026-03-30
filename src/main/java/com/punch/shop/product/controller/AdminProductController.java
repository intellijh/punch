package com.punch.shop.product.controller;

import com.punch.shop.product.dto.ProductCreateRequest;
import com.punch.shop.product.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("productCreateRequest", new ProductCreateRequest());
        model.addAttribute("categories", productService.getCategories());
        return "admin/product/register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute ProductCreateRequest request,
                           BindingResult bindingResult,
                           @RequestPart("image") MultipartFile image,
                           Model model) {
        if (image.isEmpty()) {
            bindingResult.reject("required", "이미지를 업로드해주세요");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.getCategories());
            return "admin/product/register";
        }

        Long productId = productService.createProduct(request, image);
        log.info("상품 등록 완료: productId={}", productId);
        return "redirect:/products/" + productId;
    }
}
