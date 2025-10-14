package main.java.org.example.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class LoginFilter implements Filter {

    public void init(FilterConfig config) {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        // Check if user is logged in
        String path = req.getRequestURI();
        if (path.endsWith("login.html")) {
            chain.doFilter(request, response);
            return;
        }
        if (session == null || session.getAttribute("email") == null) {
            // Not logged in → redirect to login page
            res.sendRedirect(req.getContextPath() + "/login.html");
        } else {
            // Logged in → continue to requested page
            chain.doFilter(request, response);
        }
    }

    public void destroy() {}
}