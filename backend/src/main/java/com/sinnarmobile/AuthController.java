package com.sinnarmobile;

import java.security.Principal;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/csrf") Api<Map<String,String>> csrf(CsrfToken token) {
        return Api.ok(Map.of("token",token.getToken(),"headerName",token.getHeaderName()));
    }
    @GetMapping("/me") Api<Map<String,String>> me(Principal p) { return Api.ok(Map.of("username",p.getName(),"role","ADMIN")); }
}
