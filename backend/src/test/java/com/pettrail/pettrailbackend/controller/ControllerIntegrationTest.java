package com.pettrail.pettrailbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pettrail.pettrailbackend.dto.MembershipOrderDTO;
import com.pettrail.pettrailbackend.dto.PostCreateDTO;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    private String authToken;

    @BeforeEach
    void setUp() {
        authToken = "Bearer " + jwtUtil.generateToken(1L, "user");
    }

    @Test
    @DisplayName("GET /api/achievements - 未登录返回401")
    void listAchievements_unauthorized() throws Exception {
        mockMvc.perform(get("/api/achievements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/achievements - 登录后返回成就列表")
    void listAchievements_authorized() throws Exception {
        mockMvc.perform(get("/api/achievements")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/membership/info - 获取会员信息")
    void getMembershipInfo() throws Exception {
        mockMvc.perform(get("/api/membership/info")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/challenges - 获取挑战赛列表(无需登录)")
    void listChallenges_noAuth() throws Exception {
        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/products - 获取商品列表(无需登录)")
    void listProducts_noAuth() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/vet/clinics - 获取医院列表(无需登录)")
    void listClinics_noAuth() throws Exception {
        mockMvc.perform(get("/api/vet/clinics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/pets/my - 未登录返回401")
    void listMyPets_unauthorized() throws Exception {
        mockMvc.perform(get("/api/pets/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/profile - 登录后获取用户资料")
    void getUserProfile_authorized() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/checkin/items - 获取打卡项目(无需登录)")
    void getCheckinItems_noAuth() throws Exception {
        mockMvc.perform(get("/api/checkin/items"))
                .andExpect(status().isOk());
    }
}
