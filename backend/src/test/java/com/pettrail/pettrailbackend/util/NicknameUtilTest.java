package com.pettrail.pettrailbackend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NicknameUtilTest {

    @Test
    void generateRandomNickname_shouldNotBeNull() {
        String nickname = NicknameUtil.generateRandomNickname();
        assertNotNull(nickname);
        assertFalse(nickname.isEmpty());
    }

    @Test
    void generateRandomNickname_shouldContainAdjectiveAndNoun() {
        String nickname = NicknameUtil.generateRandomNickname();
        assertTrue(nickname.length() >= 3, "Nickname should have at least 3 characters");
    }

    @Test
    void generateRandomNickname_shouldGenerateMultipleDifferentNicknames() {
        String nick1 = NicknameUtil.generateRandomNickname();
        String nick2 = NicknameUtil.generateRandomNickname();
        String nick3 = NicknameUtil.generateRandomNickname();
        assertFalse(nick1.equals(nick2) && nick2.equals(nick3),
                "At least one of three consecutive nicknames should be different");
    }
}
