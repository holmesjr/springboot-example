package hello.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

@Service
public class LdapAuthenticator implements Authenticator {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public LdapAuthenticator(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public boolean authenticate(String username, String password){
        return ldapTemplate.authenticate("", "(uid=" + username + ")", password);
    }

    @Bean
    public static LdapTemplate ldapTemplate(@Autowired LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);

    }

    @Bean
    public static LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:8389/");
        contextSource.setBase("dc=springframework,dc=org");

        return contextSource;
    }
}
