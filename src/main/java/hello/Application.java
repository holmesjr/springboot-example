package hello;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.node.ArrayNode;
import hello.services.Authenticator;
import hello.services.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    public Application(UserRepository userRepository, Authenticator authenticator, TokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.authenticator = authenticator;
        this.tokenGenerator = tokenGenerator;
    }

    private final UserRepository userRepository;
    private final Authenticator authenticator;
    private final TokenGenerator tokenGenerator;

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
    public String authenticate(@RequestParam("username") String username, @RequestParam("password") String password) {

        try {
            if(authenticator.authenticate(username, password)) {
                return tokenGenerator.generate(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //This would set an error code if it failed.
        return "FAILED TO AUTHENTICATE";
    }

    @RequestMapping("/verify/{token}")
    public String verify(@PathVariable("token") String token, @Value("${RSAPUBLICKEY}") String publicKeyContent) {

        String user = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

            Algorithm algorithm = Algorithm.RSA512(pubKey, null);
            user = JWT.require(algorithm)
                    .build()
                    .verify(token)
                    .getSubject();

            //This would set an error code if it failed.
        } catch (Exception e) {
            e.printStackTrace();
        }


        return (user != null) ? user : "FAILED TO VERIFY";
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
