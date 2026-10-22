package com.spms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Login-required filter (US-4). Runs before every protected controller method.
 *  - No session / expired session  -> redirect to /login?required
 *  - /admin pages without ADMIN role -> HTTP 403
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }
        String path = request.getRequestURI();
        boolean adminPage = path.startsWith(request.getContextPath() + "/admin");
        if (adminPage && !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access only");
            return false;
        }
        return true;
    }
}
