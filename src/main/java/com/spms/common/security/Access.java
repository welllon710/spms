package com.spms.common.security;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Access {
    private boolean login = false;
    private boolean authorize = false;
}
