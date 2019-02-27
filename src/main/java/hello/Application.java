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
    public String authenticate(@RequestParam("username") String username, @RequestParam("password") String password, @Value("${RSAPRIVATEKEY}") String privateKeyContent) {

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:8389/");
        contextSource.setBase("dc=springframework,dc=org");
        contextSource.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        try {
            ldapTemplate.afterPropertiesSet();
            boolean success = ldapTemplate.authenticate("", "(uid=" + username + ")", password);
            if(success) {

                KeyFactory kf = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
                RSAPrivateKey privKey = (RSAPrivateKey)kf.generatePrivate(keySpecPKCS8);

                Algorithm algorithm = Algorithm.RSA512(null, privKey);

                String token = JWT.create()
                        .withIssuer("example-auth-service")
                        .withSubject(username)
                        .sign(algorithm);
                return token;
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
