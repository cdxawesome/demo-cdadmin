package com.simple.democdadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simple.democdadmin.mapper.RoleMapper;
import com.simple.democdadmin.service.RoleService;
import com.simple.democdadmin.vo.ResultVo;
import com.simple.democdadmin.vo.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Role findRoleById(Long id) {
        return roleMapper.selectOne(new QueryWrapper<Role>().eq("role_id", id));
    }
}
