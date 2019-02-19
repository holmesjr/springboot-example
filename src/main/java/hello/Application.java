package hello;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.client.RestTemplate;

import javax.naming.directory.DirContext;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    public Application(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserRepository userRepository;

    @RequestMapping("/")
    public String home(@RequestHeader(value="Authorization") String authorizationHeader) {

        if(authorizationHeader != null){
            final String uri = "http://localhost:8080/verify/{token}";
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", authorizationHeader.replace("Bearer ",""));

            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class, params);
            if(result.equals("FAILED TO VERIFY")){
                return "DENIED";
            }
        }

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
    public String authenticate(@RequestParam("username") String username, @RequestParam("password") String password) {

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
                Algorithm algorithmHS = Algorithm.HMAC256("secret");
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
    public String verify(@PathVariable("token") String token) {
        // We'd change this to asymmetric if you wanted to verify out in the client services
        Algorithm algorithmHS = Algorithm.HMAC256("secret");
        String user = JWT.require(Algorithm.HMAC256("secret"))
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
