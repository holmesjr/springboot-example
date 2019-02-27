package hello;

import static org.junit.Assert.*;

import hello.services.Authenticator;
import hello.services.TokenGenerator;
import org.junit.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationTest {

    private Application app = new Application(new MockRepository(), new MockAuthenticator(), new MockGenerator());
    private Application failingApp = new Application(new MockRepository(), new MockFailingAuthenticator(), new MockGenerator());

    @Test
    public void outputsJSONToTheBrowser() {

        assertEquals("[{\"name\":\"Fred\",\"email\":\"fred@here.com\"},{\"name\":\"Jill\",\"email\":\"jill@here.com\"}]", app.home());
    }

    @Test
    public void authenticatesTheUserAndSendsAJWT(){
        assertEquals("token-here", app.authenticate("fred", "bill"));
    }

    @Test
    public void failsToAuthenticateTheUserIfLDAPFails(){
        assertEquals("FAILED TO AUTHENTICATE", failingApp.authenticate("fred", "bill"));
    }
}

class MockAuthenticator implements Authenticator{

    @Override
    public boolean authenticate(String username, String password) {
        return true;
    }
}

class MockFailingAuthenticator implements Authenticator{

    @Override
    public boolean authenticate(String username, String password) {
        return false;
    }
}

class MockGenerator implements TokenGenerator{

    @Override
    public String generate(String username) {
        return "token-here";
    }
}

class MockRepository implements UserRepository{

    public MockRepository() {
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        users.add(new User("Fred","fred@here.com"));
        users.add(new User("Jill","jill@here.com"));
        return users;
    }

    @Override
    public List<User> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<User> findAllById(Iterable<Long> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(User user) {

    }

    @Override
    public void deleteAll(Iterable<? extends User> iterable) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends User> S save(S s) {
        return null;
    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> iterable) {
        return null;
    }

    @Override
    public Optional<User> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends User> S saveAndFlush(S s) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<User> iterable) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public User getOne(Long aLong) {
        return null;
    }

    @Override
    public <S extends User> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends User> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends User> boolean exists(Example<S> example) {
        return false;
    }
}