package com.spms.personal.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolePageRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesCommonFilterPageRequest() throws Exception {
        RolePageRequest request = objectMapper.readValue("""
                {
                  "filter": {
                    "name": "admin",
                    "code": "ADMIN",
                    "isDisabled": false
                  },
                  "page": {
                    "pageNum": 1,
                    "pageSize": 20
                  }
                }
                """, RolePageRequest.class);

        assertThat(request.filter().name()).isEqualTo("admin");
        assertThat(request.filter().code()).isEqualTo("ADMIN");
        assertThat(request.filter().isDisabled()).isFalse();
        assertThat(request.pageNum()).isEqualTo(1);
        assertThat(request.pageSize()).isEqualTo(20);
    }
}
