package com.simple.democdadmin.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.democdadmin.vo.ResultVo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 鉴权失败的处理器
 */
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResultVo resultVo = new ResultVo(10010, "鉴权失败", null);
        String jsonMap = new ObjectMapper().writeValueAsString(resultVo);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(jsonMap);
    }
}
