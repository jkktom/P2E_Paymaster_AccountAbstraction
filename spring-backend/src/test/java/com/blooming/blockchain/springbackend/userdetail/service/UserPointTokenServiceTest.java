package com.blooming.blockchain.springbackend.userdetail.service;

import com.blooming.blockchain.springbackend.userdetail.entity.UserPointToken;
import com.blooming.blockchain.springbackend.userdetail.repository.UserPointTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPointTokenServiceTest {

    @Mock
    private UserPointTokenRepository userPointTokenRepository;

    @InjectMocks
    private UserPointTokenService userPointTokenService;

    private String testUserGoogleId;
    private UserPointToken testUserBalance;

    @BeforeEach
    void setUp() {
        testUserGoogleId = "test-google-id-123";
        testUserBalance = new UserPointToken(testUserGoogleId, 100, 200, 50L);
    }

    @Test
    void getOrCreateUserBalance_WhenUserExists_ShouldReturnExistingBalance() {
        // Given
        when(userPointTokenRepository.findByUserGoogleId(testUserGoogleId))
                .thenReturn(Optional.of(testUserBalance));

        // When
        UserPointToken result = userPointTokenService.getOrCreateUserBalance(testUserGoogleId);

        // Then
        assertThat(result).isEqualTo(testUserBalance);
        verify(userPointTokenRepository, never()).save(any());
    }

    @Test
    void getOrCreateUserBalance_WhenUserNotExists_ShouldCreateNewBalance() {
        // Given
        when(userPointTokenRepository.findByUserGoogleId(testUserGoogleId))
                .thenReturn(Optional.empty());
        UserPointToken newBalance = new UserPointToken(testUserGoogleId, 0, 0, 0L);
        when(userPointTokenRepository.save(any(UserPointToken.class)))
                .thenReturn(newBalance);

        // When
        UserPointToken result = userPointTokenService.getOrCreateUserBalance(testUserGoogleId);

        // Then
        assertThat(result).isEqualTo(newBalance);
        verify(userPointTokenRepository).save(any(UserPointToken.class));
    }

    @Test
    void addMainPoints_WhenValidAmount_ShouldReturnTrue() {
        // Given
        int amount = 50;
        when(userPointTokenRepository.findByUserGoogleId(testUserGoogleId))
                .thenReturn(Optional.of(testUserBalance));
        when(userPointTokenRepository.addMainPoints(testUserGoogleId, amount))
                .thenReturn(1);

        // When
        boolean result = userPointTokenService.addMainPoints(testUserGoogleId, amount);

        // Then
        assertThat(result).isTrue();
        verify(userPointTokenRepository).addMainPoints(testUserGoogleId, amount);
    }

    @Test
    void addMainPoints_WhenInvalidAmount_ShouldReturnFalse() {
        // When
        boolean result = userPointTokenService.addMainPoints(testUserGoogleId, -10);

        // Then
        assertThat(result).isFalse();
        verify(userPointTokenRepository, never()).addMainPoints(anyString(), anyInt());
    }

    @Test
    void addMainPoints_WhenUpdateFails_ShouldReturnFalse() {
        // Given
        int amount = 50;
        when(userPointTokenRepository.findByUserGoogleId(testUserGoogleId))
                .thenReturn(Optional.of(testUserBalance));
        when(userPointTokenRepository.addMainPoints(testUserGoogleId, amount))
                .thenReturn(0);

        // When
        boolean result = userPointTokenService.addMainPoints(testUserGoogleId, amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void spendMainPoints_WhenSufficientBalance_ShouldReturnTrue() {
        // Given
        int amount = 50;
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(100));
        when(userPointTokenRepository.spendMainPoints(testUserGoogleId, amount))
                .thenReturn(1);

        // When
        boolean result = userPointTokenService.spendMainPoints(testUserGoogleId, amount);

        // Then
        assertThat(result).isTrue();
        verify(userPointTokenRepository).spendMainPoints(testUserGoogleId, amount);
    }

    @Test
    void spendMainPoints_WhenInsufficientBalance_ShouldReturnFalse() {
        // Given
        int amount = 150;
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(100));

        // When
        boolean result = userPointTokenService.spendMainPoints(testUserGoogleId, amount);

        // Then
        assertThat(result).isFalse();
        verify(userPointTokenRepository, never()).spendMainPoints(anyString(), anyInt());
    }

    @Test
    void convertSubToMainPoints_WhenSufficientSubPoints_ShouldReturnTrue() {
        // Given
        int subAmount = 100;
        int mainAmount = 10;
        when(userPointTokenRepository.getSubPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(200));
        when(userPointTokenRepository.convertSubToMainPoints(testUserGoogleId, subAmount, mainAmount))
                .thenReturn(1);

        // When
        boolean result = userPointTokenService.convertSubToMainPoints(testUserGoogleId, subAmount, mainAmount);

        // Then
        assertThat(result).isTrue();
        verify(userPointTokenRepository).convertSubToMainPoints(testUserGoogleId, subAmount, mainAmount);
    }

    @Test
    void convertSubToMainPoints_WhenInsufficientSubPoints_ShouldReturnFalse() {
        // Given
        int subAmount = 300;
        int mainAmount = 30;
        when(userPointTokenRepository.getSubPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(200));

        // When
        boolean result = userPointTokenService.convertSubToMainPoints(testUserGoogleId, subAmount, mainAmount);

        // Then
        assertThat(result).isFalse();
        verify(userPointTokenRepository, never()).convertSubToMainPoints(anyString(), anyInt(), anyInt());
    }

    @Test
    void exchangeMainPointsToTokens_WhenSufficientMainPoints_ShouldReturnTrue() {
        // Given
        int mainAmount = 100;
        long tokenAmount = 10L;
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(150));
        when(userPointTokenRepository.exchangeMainPointsToTokens(testUserGoogleId, mainAmount, tokenAmount))
                .thenReturn(1);

        // When
        boolean result = userPointTokenService.exchangeMainPointsToTokens(testUserGoogleId, mainAmount, tokenAmount);

        // Then
        assertThat(result).isTrue();
        verify(userPointTokenRepository).exchangeMainPointsToTokens(testUserGoogleId, mainAmount, tokenAmount);
    }

    @Test
    void exchangeMainPointsToTokens_WhenInsufficientMainPoints_ShouldReturnFalse() {
        // Given
        int mainAmount = 200;
        long tokenAmount = 20L;
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(150));

        // When
        boolean result = userPointTokenService.exchangeMainPointsToTokens(testUserGoogleId, mainAmount, tokenAmount);

        // Then
        assertThat(result).isFalse();
        verify(userPointTokenRepository, never()).exchangeMainPointsToTokens(anyString(), anyInt(), anyLong());
    }

    @Test
    void getMainPointBalance_WhenUserExists_ShouldReturnBalance() {
        // Given
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(100));

        // When
        Integer result = userPointTokenService.getMainPointBalance(testUserGoogleId);

        // Then
        assertThat(result).isEqualTo(100);
    }

    @Test
    void getMainPointBalance_WhenUserNotExists_ShouldReturnZero() {
        // Given
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.empty());

        // When
        Integer result = userPointTokenService.getMainPointBalance(testUserGoogleId);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void hasSufficientMainPoints_WhenSufficientBalance_ShouldReturnTrue() {
        // Given
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(100));

        // When
        boolean result = userPointTokenService.hasSufficientMainPoints(testUserGoogleId, 50);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasSufficientMainPoints_WhenInsufficientBalance_ShouldReturnFalse() {
        // Given
        when(userPointTokenRepository.getMainPointBalance(testUserGoogleId))
                .thenReturn(Optional.of(30));

        // When
        boolean result = userPointTokenService.hasSufficientMainPoints(testUserGoogleId, 50);

        // Then
        assertThat(result).isFalse();
    }
}