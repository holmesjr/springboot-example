package hello.services;

public interface Authenticator {
    boolean authenticate(String username, String password);
}
