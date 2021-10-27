package com.simple.democdadmin.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName(value = "sys_user")
public class User {
    private  Long userId;
    private Long deptId;
    private String username;
    private String nickName;
    private String gender;
    private String phone;
    private String email;
    private String password;
    private boolean isAdmin;
    private boolean enabled;
    private String createBy;
    private String updateBy;
    private Date createTime;
    private Date updateTime;
}
