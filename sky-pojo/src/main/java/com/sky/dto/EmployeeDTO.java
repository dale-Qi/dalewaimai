package com.sky.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "用户名", example = "libai")
    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
