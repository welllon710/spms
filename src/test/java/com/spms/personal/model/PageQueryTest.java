package com.spms.personal.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spms.base.PageQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageQueryTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesPageQueryWithTopLevelPageFields() throws Exception {
        PageQuery<RolePageFilter> request = objectMapper.readValue("""
                {
                  "filter": {
                    "name": "admin",
                    "code": "ADMIN",
                    "isDisabled": false
                  },
                  "pageNum": 1,
                  "pageSize": 20
                }
                """, new TypeReference<>() {
        });

        assertThat(request.filter().name()).isEqualTo("admin");
        assertThat(request.filter().code()).isEqualTo("ADMIN");
        assertThat(request.filter().isDisabled()).isFalse();
        assertThat(request.pageNum()).isEqualTo(1);
        assertThat(request.pageSize()).isEqualTo(20);
    }

    @Test
    void deserializesPageQueryWithNestedPageFields() throws Exception {
        PageQuery<RolePageFilter> request = objectMapper.readValue("""
                {
                  "filter": {
                    "name": "admin",
                    "code": "ADMIN",
                    "isDisabled": false
                  },
                  "page": {
                    "pageNum": 2,
                    "pageSize": 30
                  }
                }
                """, new TypeReference<>() {
        });

        assertThat(request.filter().name()).isEqualTo("admin");
        assertThat(request.filter().code()).isEqualTo("ADMIN");
        assertThat(request.filter().isDisabled()).isFalse();
        assertThat(request.page().pageNum()).isEqualTo(2);
        assertThat(request.page().pageSize()).isEqualTo(30);
    }
}
