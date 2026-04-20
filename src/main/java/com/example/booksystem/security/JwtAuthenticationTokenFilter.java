package com.example.booksystem.security;

import com.example.booksystem.config.JwtConfig;
import com.example.booksystem.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT登录授权过滤器
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtConfig jwtConfig;

    public JwtAuthenticationTokenFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil, JwtConfig jwtConfig) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        System.out.println("JwtAuthenticationTokenFilter is executing");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request method: " + request.getMethod());
        
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("Authorization header: " + authHeader);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 截取"Bearer "之后的JWT令牌
                String authToken = authHeader.substring(7);
                System.out.println("Extracted token: " + authToken);
                
                try {
                    String username = jwtTokenUtil.getUsernameFromToken(authToken);
                    System.out.println("Extracted username: " + username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        System.out.println("Loading user details for: " + username);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        System.out.println("Loaded user details: " + userDetails);
                        
                        if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                            System.out.println("Token validation successful");
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            System.out.println("Authentication set successfully");
                        } else {
                            System.out.println("Token validation failed");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing token: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("No Authorization header or header does not start with Bearer");
            }
        } catch (Exception e) {
            System.out.println("Error in JwtAuthenticationTokenFilter: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Continuing filter chain");
        chain.doFilter(request, response);
    }
}