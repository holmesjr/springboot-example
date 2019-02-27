package hello.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class JWTGenerator implements TokenGenerator {

    private final String secretKey;

    public JWTGenerator(@Value("${RSAPRIVATEKEY}") String secretKey) {

        this.secretKey = secretKey;
    }

    @Override
    public String generate(String username) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.secretKey));
        RSAPrivateKey privKey = (RSAPrivateKey)kf.generatePrivate(keySpecPKCS8);

        Algorithm algorithm = Algorithm.RSA512(null, privKey);

        String token = JWT.create()
                .withIssuer("example-auth-service")
                .withSubject(username)
                .sign(algorithm);
        return token;


    }
}
