package hello.services;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

@Service
public class LdapAuthenticator implements Authenticator {

    private LdapTemplate ldapTemplate;

    public LdapAuthenticator() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:8389/");
        contextSource.setBase("dc=springframework,dc=org");
        contextSource.afterPropertiesSet();
        this.ldapTemplate = new LdapTemplate(contextSource);
        try {
            ldapTemplate.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean authenticate(String username, String password){
        return ldapTemplate.authenticate("", "(uid=" + username + ")", password);
    }
}
