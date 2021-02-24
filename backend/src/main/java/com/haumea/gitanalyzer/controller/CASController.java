package com.haumea.gitanalyzer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
@RequestMapping(path = "/api/v1/cas")
@Slf4j
public class CASController {

    @Value("${cas.server-url-prefix}")
    private String CASUrl;

    @GetMapping("/login")
    public String getCASUserId(HttpServletRequest request){
        return request.getRemoteUser();

    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if(session != null || !request.isRequestedSessionIdValid()) {
            String id = session.getId();
            session.invalidate();
            log.info("JSESSIONID: " + id + " is valid: " + request.isRequestedSessionIdValid());
        }

        response.sendRedirect(CASUrl + "/logout");
    }
}
