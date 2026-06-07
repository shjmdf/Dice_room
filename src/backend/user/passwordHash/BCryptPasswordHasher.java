package backend.user.passwordHash;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptPasswordHasher implements PasswordHasher {
    private static final int COST = 12;

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null) {
            return false;
        }
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), passwordHash).verified;
    }
}
