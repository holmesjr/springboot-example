package hello;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.naming.directory.DirContext;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    public Application(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserRepository userRepository;

    @RequestMapping("/")
    public String home() {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode users = mapper.createArrayNode();

        userRepository.findAll().forEach(user -> {
            ObjectNode person = mapper.createObjectNode();
            person.put("name", user.getName());
            person.put("email", user.getEmail());
            users.add(person);
        });

        return users.toString();
    }

    @RequestMapping("/authenticate")
    public String authenticate(@RequestParam("username") String username, @RequestParam("password") String password, @Value("${JWTSECRET}") String secretForJWT) {

        DirContext ctx = null;
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:8389/");
        contextSource.setBase("dc=springframework,dc=org");
        contextSource.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        try {
            ldapTemplate.afterPropertiesSet();
            boolean success = ldapTemplate.authenticate("", "(uid=" + username + ")", password);
            if(success) {
                // We'd change this to asymmetric if you wanted to verify out in the client services
                Algorithm algorithmHS = Algorithm.HMAC256(secretForJWT);
                String token = JWT.create()
                        .withIssuer("example-auth-service")
                        .withSubject(username)
                        .sign(algorithmHS);
                return token;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //This would set an error code if it failed.
        return "FAILED TO AUTHENTICATE";
    }

    @RequestMapping("/verify/{token}")
    public String verify(@PathVariable("token") String token, @Value("${JWTSECRET}") String secretForJWT) {
        // We'd change this to asymmetric if you wanted to verify out in the client services
        Algorithm algorithmHS = Algorithm.HMAC256(secretForJWT);
        String user = JWT.require(algorithmHS)
                .build()
                .verify(token)
                .getSubject();

        //This would set an error code if it failed.
        return user != null ? user : "FAILED TO VERIFY";
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
