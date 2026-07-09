package com.taskflow.common.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void success_withData_setsSuccessTrueAndPopulatesData() {
        String payload = "hello";

        ApiResponse<String> response = ApiResponse.success(payload);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(payload);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_withMessageAndData_setsAllFields() {
        Map<String, String> data = Map.of("key", "value");

        ApiResponse<Map<String, String>> response = ApiResponse.success("Operation complete", data);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operation complete");
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    void success_withMessageOnly_hasNullData() {
        ApiResponse<Void> response = ApiResponse.success("Done");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Done");
        assertThat(response.getData()).isNull();
    }

    @Test
    void error_setsSuccessFalseAndMessage() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Something went wrong");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
}
