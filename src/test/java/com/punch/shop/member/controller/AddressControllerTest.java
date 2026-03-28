package com.punch.shop.member.controller;

import com.punch.shop.member.dto.AddressCreateRequest;
import com.punch.shop.member.dto.AddressResponse;
import com.punch.shop.member.dto.AddressUpdateRequest;
import com.punch.shop.member.exception.AddressNotFoundException;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.AddressService;
import com.punch.shop.member.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MemberPrincipal principal;
    private AddressResponse addressResponse;

    @BeforeEach
    void setUp() {
        principal = new MemberPrincipal(1L, "test@example.com", "홍길동", "encodedPassword", List.of());
        addressResponse = AddressResponse.builder()
                .id(1L).label("집").recipientName("홍길동")
                .phone("010-1234-5678").zipCode("12345")
                .address("서울시 강남구 테헤란로 123").defaultAddress(false)
                .build();
    }

    @Test
    @DisplayName("배송지 목록 페이지 요청")
    void addressList() throws Exception {
        given(addressService.getAddresses(1L)).willReturn(List.of(addressResponse));

        mockMvc.perform(get("/member/address").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("member/address"))
                .andExpect(model().attributeExists("addresses"));
    }

    @Test
    @DisplayName("배송지 추가 폼 페이지 요청")
    void addForm() throws Exception {
        mockMvc.perform(get("/member/address/new").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("member/address-form"))
                .andExpect(model().attributeExists("addressCreateRequest"));
    }

    @Test
    @DisplayName("배송지 추가 성공")
    void addSuccess() throws Exception {
        given(addressService.addAddress(eq(1L), any(AddressCreateRequest.class))).willReturn(addressResponse);

        mockMvc.perform(post("/member/address/new").with(user(principal)).with(csrf())
                        .param("label", "집")
                        .param("recipientName", "홍길동")
                        .param("phone", "010-1234-5678")
                        .param("zipCode", "12345")
                        .param("address", "서울시 강남구 테헤란로 123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/address"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("배송지 추가 실패 - validation 오류")
    void addValidationFail() throws Exception {
        mockMvc.perform(post("/member/address/new").with(user(principal)).with(csrf())
                        .param("label", "")
                        .param("recipientName", "")
                        .param("phone", "invalid")
                        .param("zipCode", "abc")
                        .param("address", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("member/address-form"))
                .andExpect(model().attributeHasFieldErrors("addressCreateRequest",
                        "label", "recipientName", "phone", "zipCode", "address"));
    }

    @Test
    @DisplayName("배송지 수정 폼 페이지 요청")
    void editForm() throws Exception {
        given(addressService.getAddress(1L, 1L)).willReturn(addressResponse);

        mockMvc.perform(get("/member/address/1/edit").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("member/address-form"))
                .andExpect(model().attributeExists("addressId", "addressUpdateRequest"));
    }

    @Test
    @DisplayName("배송지 수정 폼 요청 실패 - 존재하지 않는 배송지")
    void editFormNotFound() throws Exception {
        given(addressService.getAddress(1L, 99L)).willThrow(new AddressNotFoundException(99L));

        mockMvc.perform(get("/member/address/99/edit").with(user(principal)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("배송지 수정 성공")
    void editSuccess() throws Exception {
        given(addressService.updateAddress(eq(1L), eq(1L), any(AddressUpdateRequest.class))).willReturn(addressResponse);

        mockMvc.perform(post("/member/address/1/edit").with(user(principal)).with(csrf())
                        .param("label", "집")
                        .param("recipientName", "홍길동")
                        .param("phone", "010-1234-5678")
                        .param("zipCode", "12345")
                        .param("address", "서울시 강남구 테헤란로 123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/address"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("배송지 수정 실패 - validation 오류")
    void editValidationFail() throws Exception {
        mockMvc.perform(post("/member/address/1/edit").with(user(principal)).with(csrf())
                        .param("label", "")
                        .param("recipientName", "")
                        .param("phone", "invalid")
                        .param("zipCode", "abc")
                        .param("address", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("member/address-form"))
                .andExpect(model().attribute("addressId", 1L))
                .andExpect(model().attributeHasFieldErrors("addressUpdateRequest",
                        "label", "recipientName", "phone", "zipCode", "address"));
    }

    @Test
    @DisplayName("배송지 수정 실패 - 존재하지 않는 배송지")
    void editNotFound() throws Exception {
        willThrow(new AddressNotFoundException(99L)).given(addressService).updateAddress(eq(1L), eq(99L), any(AddressUpdateRequest.class));

        mockMvc.perform(post("/member/address/99/edit").with(user(principal)).with(csrf())
                        .param("label", "집")
                        .param("recipientName", "홍길동")
                        .param("phone", "010-1234-5678")
                        .param("zipCode", "12345")
                        .param("address", "서울시 강남구 테헤란로 123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteSuccess() throws Exception {
        willDoNothing().given(addressService).deleteAddress(1L, 1L);

        mockMvc.perform(post("/member/address/1/delete").with(user(principal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/address"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 존재하지 않는 배송지")
    void deleteNotFound() throws Exception {
        willThrow(new AddressNotFoundException(99L)).given(addressService).deleteAddress(1L, 99L);

        mockMvc.perform(post("/member/address/99/delete").with(user(principal)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("기본 배송지 변경 성공")
    void setDefaultSuccess() throws Exception {
        given(addressService.setDefaultAddress(1L, 1L)).willReturn(addressResponse);

        mockMvc.perform(post("/member/address/1/default").with(user(principal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/address"))
                .andExpect(flash().attributeExists("message"));
    }
}
