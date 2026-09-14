package com.asistencia.backend.security;

import com.asistencia.backend.web.UnauthorizedException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public class FirebaseAdminTokenVerifier implements TokenVerifier {
    @Override
    public AuthenticatedUser verify(String idToken) {
        try {
            FirebaseToken token = firebaseAuth().verifyIdToken(idToken);
            Object adminClaim = token.getClaims().get("admin");
            return new AuthenticatedUser(
                    token.getUid(),
                    token.getEmail(),
                    token.getClaims(),
                    Boolean.TRUE.equals(adminClaim)
            );
        } catch (FirebaseAuthException exception) {
            throw new UnauthorizedException("Token Firebase invalido");
        }
    }

    private FirebaseAuth firebaseAuth() {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp();
        }
        return FirebaseAuth.getInstance();
    }
}
