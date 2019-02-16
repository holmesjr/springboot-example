package hello;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String authenticate(@RequestParam("username") String username, @RequestParam("password") String password) {

        DirContext ctx = null;
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:8389/");
        contextSource.setBase("dc=springframework,dc=org");
        contextSource.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        try {
            ldapTemplate.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean success = ldapTemplate.authenticate("", "(uid=" + username + ")", password);

        //this is where we need to respond with the JWT.
        return success ? "SUCCEEDED" : "FAILED";
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
