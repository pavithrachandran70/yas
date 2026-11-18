package com.yas.cart.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.cart.config.SecurityConfig;
import com.yas.cart.service.CartItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CartItemController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class CartItemControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartItemService cartItemService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void testAddFromWishlist_Unauthorized() throws Exception {
        mockMvc.perform(post("/storefront/items/from-wishlist")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wishlistItemId\":1}"))
            .andExpect(status().isUnauthorized());
    }
}
