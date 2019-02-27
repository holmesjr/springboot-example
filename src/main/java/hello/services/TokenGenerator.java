package hello.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface TokenGenerator {
    String generate(String username) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
