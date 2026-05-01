package ru.ivanov.diplom.inventory_system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> ALLOWED_PATHS = List.of(
            "/api/auth/me",
            "/api/auth/logout",
            "/api/account/profile",
            "/api/account/change-password"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isAllowedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = authentication.getName();

        AppUser user = appUserRepository.findByUsername(username).orElse(null);

        if (user != null && Boolean.TRUE.equals(user.getMustChangePassword())) {
            writeMustChangePasswordResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        return ALLOWED_PATHS.stream().anyMatch(path::equals);
    }

    private void writeMustChangePasswordResponse(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 403,
                "error", "Password change required",
                "messages", List.of("Необходимо сменить временный пароль перед продолжением работы")
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
