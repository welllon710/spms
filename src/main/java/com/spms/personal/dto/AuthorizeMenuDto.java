package com.spms.personal.dto;


import com.spms.personal.entity.MenuEntity;
import lombok.Data;

import java.util.List;

@Data
public class AuthorizeMenuDto {

    private Long id;
    private List<MenuEntity> menuList;
}
