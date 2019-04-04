package hello.services;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.unboundid.LdapTestUtils;

import static org.junit.Assert.assertTrue;

public class LdapAuthenticatorTest {

    @Test
    public void authenticatesTheUser() throws Exception {

        LdapContextSource contextSource = LdapAuthenticator.contextSource();
        contextSource.afterPropertiesSet();

        LdapTemplate ldapTemplate = LdapAuthenticator.ldapTemplate(contextSource);
        ldapTemplate.afterPropertiesSet();


        LdapTestUtils.startEmbeddedServer(8389, "dc=springframework,dc=org","springframework");

        Resource ldif = new ClassPathResource("test.ldif");

        LdapTestUtils.loadLdif(contextSource, ldif);

        LdapAuthenticator ldapAuthenticator = new LdapAuthenticator(ldapTemplate);

        boolean worked = ldapAuthenticator.authenticate("bob", "bobspassword");

        LdapTestUtils.shutdownEmbeddedServer();

        assertTrue(worked);

    }
}
