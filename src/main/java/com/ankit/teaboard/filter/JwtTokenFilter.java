package com.ankit.teaboard.filter;

import com.ankit.teaboard.config.RouteValidator;
import com.ankit.teaboard.repository.UserLoginRepository;
import com.ankit.teaboard.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserLoginRepository userLoginRepository;
    @Autowired
    private RouteValidator routeValidator;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!routeValidator.isAuthenticated(request.getRequestURI())){
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            unAuthResponse(request,response);
            return;
        }
        final String token = header.split(" ")[1].trim();
        if (!jwtService.validateToken(token)) {
            //filterChain.doFilter(request, response);
            unAuthResponse(request,response);
            return;
        }

        UserDetails userDetails = userLoginRepository
                .getUserLoginByLoginId(jwtService.getLoginIdFromToken(token));

        UsernamePasswordAuthenticationToken
                authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails == null ?
                        List.of() : userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
        else{
            filterChain.doFilter(request, response);
        }
    }

    private void unAuthResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append("\"responseCode\": \"401\", ");
        sb.append("\"message\": \"Un-Authorized\"");
        sb.append("} ");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(sb.toString());
        return;
    }
}
