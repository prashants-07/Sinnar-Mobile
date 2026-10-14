package com.sinnarmobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Single-instance throttle. The edge proxy should additionally rate-limit these routes in production. */
@Component @Order(1)
public class RateLimitFilter extends OncePerRequestFilter {
    private record Window(long start,int count) {}
    private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
    private final ObjectMapper json;
    public RateLimitFilter(ObjectMapper json) { this.json=json; }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        String path=req.getRequestURI();
        boolean login=path.equals("/api/auth/login");
        if (!req.getMethod().equals("POST") || !(login || path.equals("/api/public/enquiries") || path.equals("/api/public/repair-enquiries"))) {chain.doFilter(req,res); return;}
        long now=System.currentTimeMillis();
        windows.entrySet().removeIf(e->now-e.getValue().start()>60_000);
        String key=req.getRemoteAddr()+":"+(login?"login":"enquiry");
        Window w=windows.compute(key,(k,old)->old==null||now-old.start()>60_000?new Window(now,1):new Window(old.start(),old.count()+1));
        if(w.count()>(login?10:5)) {
            res.setStatus(429); res.setHeader("Retry-After","60"); res.setContentType("application/json");
            json.writeValue(res.getWriter(),Api.error("Too many attempts. Please wait a minute.",null)); return;
        }
        chain.doFilter(req,res);
    }
}
