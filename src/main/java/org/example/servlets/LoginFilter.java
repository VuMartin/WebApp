package main.java.org.example.servlets;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final List<String> allowedURIs = new ArrayList<>();

    public void init(FilterConfig config) {
        allowedURIs.add("login.html");
        allowedURIs.add("js/login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("css/styles.css");
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Set up your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        System.out.println("LoginFilter: " + req.getRequestURI());

        if (this.isUrlAllowedWithoutLogin(req.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
        // Redirect to login page if the "user" attribute doesn't exist in session
        if (session == null || session.getAttribute("email") == null) {
            // Not logged in → redirect to login page
            res.sendRedirect(req.getContextPath() + "/login.html");
        } else {
            // Logged in → continue to requested page
            chain.doFilter(request, response);
        }
    }
}