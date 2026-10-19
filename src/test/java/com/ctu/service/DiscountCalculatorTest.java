package com.ctu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DiscountCalculator Test Suite")
class DiscountCalculatorTest {
    private DiscountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DiscountCalculator();
    }

    @Test
    @DisplayName("Giảm giá 20% cho sản phẩm 100000")
    void calculateDiscountPrice_success() {
        assertEquals(80000, calculator.calculateDiscountPrice(100000, 20), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "100000, 0, 100000",
            "200000, 50, 100000",
            "500000, 10, 450000",
            "100000, 100, 0"
    })
    @DisplayName("Kiểm thử nhiều mức giá và phần trăm")
    void calculateDiscountPrice_parameterized(double price, double percent, double expected) {
        assertEquals(expected, calculator.calculateDiscountPrice(price, percent), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "100, -1",
            "100, 101"
    })
    @DisplayName("Từ chối giá hoặc phần trăm chiết khấu không hợp lệ")
    void calculateDiscountPrice_invalidInput_throwsException(double price, double percent) {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateDiscountPrice(price, percent));
    }
}
