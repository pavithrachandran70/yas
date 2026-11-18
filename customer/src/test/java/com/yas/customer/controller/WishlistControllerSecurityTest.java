package com.yas.customer.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.customer.config.SecurityConfig;
import com.yas.customer.service.WishlistService;
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

@WebMvcTest(controllers = WishlistController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class WishlistControllerSecurityTest {

    private static final String BASE_URL = "/storefront/wishlist/items";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void testAddProductToWishlist_Unauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":123}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetWishlistItems_Unauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveWishlistItem_Unauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{wishlistItemId}", 1L).with(csrf()))
            .andExpect(status().isUnauthorized());
    }
}
