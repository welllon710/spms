package com.spms.channel.model;

public record CustomerPageFilter(
        String name,
        String code,
        String phone,
        Boolean isDisabled
) {
}
