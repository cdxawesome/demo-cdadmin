package com.simple.democdadmin.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName(value = "sys_role")
public class Role {
    private Long roleId;
    private String roleName;
    private String name;
    private int level;
    private String description;
    private String dataScope;
    private String createBy;
    private String updateBy;
    private Date createTime;
    private Date updateTime;
}
