package fr.eni.enchere.bll;

public interface PasswordResetService {
    /**
     * Creates a password reset token for a user and sends a reset email
     * @param email The email address of the user requesting password reset
     */
    void createPasswordResetTokenForUser(String email);

    /**
     * Resets the password for a user
     * @param token The password reset token
     * @param newPassword The new password
     * @return true if the password was reset successfully, false otherwise
     */
    boolean resetPassword(String token, String newPassword);
}
