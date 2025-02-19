package org.example.projectjava.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHasher {

    // Pieprz jako stała wartość
    private static final String PEPPER = "pepper342dfgewssss";

    // Metoda do generowania soli
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : saltBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    // Hashowanie hasła z solą i pieprzem
    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Połączenie hasła, soli i pieprzu
        String passwordWithSaltAndPepper = password + salt + PEPPER;

        // Hashowanie
        byte[] hashBytes = md.digest(passwordWithSaltAndPepper.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
