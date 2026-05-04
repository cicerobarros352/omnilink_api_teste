package com.cicerobarros.omnilink_api_teste.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CpfValidator - Testes Unitários")
class CpfValidatorTest {

    private CpfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfValidator();
    }

    @ParameterizedTest
    @DisplayName("CPFs válidos devem ser aceitos")
    @ValueSource(strings = {
            "529.982.247-25",
            "52998224725",
            "111.444.777-35",
            "123.456.789-09"
    })
    void shouldAcceptValidCpfs(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("CPFs inválidos devem ser rejeitados")
    @ValueSource(strings = {
            "111.111.111-11",
            "000.000.000-00",
            "123.456.789-00",
            "12345678901",
            "abc.def.ghi-jk",
            "529.982.247-26"
    })
    void shouldRejectInvalidCpfs(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }
}
