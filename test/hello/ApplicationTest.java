package hello;

import static org.junit.Assert.*;

import hello.services.Authenticator;
import hello.services.TokenGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

    @Mock
    UserRepository repository;

    @Mock
    Authenticator authenticator;

    @Mock
    TokenGenerator tokenGenerator;

    @Test
    public void outputsJSONToTheBrowser() {

        List<User> users = new ArrayList<>();
        users.add(new User("Fred","fred@here.com"));
        users.add(new User("Jill","jill@here.com"));
        Mockito.when(repository.findAll()).thenReturn(users);
        Application app = new Application(repository, authenticator, tokenGenerator);

        assertEquals("[{\"name\":\"Fred\",\"email\":\"fred@here.com\"},{\"name\":\"Jill\",\"email\":\"jill@here.com\"}]", app.home());
    }

    @Test
    public void authenticatesTheUserAndSendsAJWT() throws InvalidKeySpecException, NoSuchAlgorithmException {

        Mockito.when(authenticator.authenticate("fred", "bill")).thenReturn(true);
        Mockito.when(tokenGenerator.generate("fred")).thenReturn("token-here");
        Application app = new Application(repository, authenticator, tokenGenerator);

        assertEquals("token-here", app.authenticate("fred", "bill"));
    }

    @Test
    public void failsToAuthenticateTheUserIfLDAPFails() throws InvalidKeySpecException, NoSuchAlgorithmException {

        Mockito.when(authenticator.authenticate("fred", "bill")).thenReturn(false);
        Application app = new Application(repository, authenticator, tokenGenerator);

        assertEquals("FAILED TO AUTHENTICATE", app.authenticate("fred", "bill"));
    }
}