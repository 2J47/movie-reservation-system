package com.myproject.movie_ticket.domain.user;

import com.myproject.movie_ticket.domain.user.enums.UserRole;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import com.myproject.movie_ticket.global.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
public class JwtTokenExtractTest {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private ObjectMapper objectMapper;

    @Rollback(false)
    @Test
    @DisplayName("K6 부하 테스트용 유저 100명 생성 및 JWT 토큰 JSON 추출")
    void generate100Tokens() throws Exception {
        List<String> tokens = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            String uniquePhoneNumber = String.format("010-0000-%04d", i);
            User dummyUser = User.builder()
                    .email("testUser"+ i + "@test.com")
                    .password("password123!")
                    .name("회원" + i)
                    .phoneNumber(uniquePhoneNumber)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(dummyUser);

            String token = jwtProvider.createAccessToken(dummyUser.getId(), "USER");
            tokens.add(token);
        }

        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tokens);

        System.out.println("========== [아래 JSON 텍스트를 복사하세요] ==========");
        System.out.println(jsonOutput);
        System.out.println("=================================================");
    }

}
