package com.asistencia.backend.security;

import com.asistencia.backend.web.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthorizationService {
    public void requireAdmin(AuthenticatedUser user) {
        if (user == null || !user.admin()) {
            throw new ForbiddenException("Operacion permitida solo para administradores");
        }
    }
}
