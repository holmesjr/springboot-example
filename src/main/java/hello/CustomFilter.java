package hello;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

@Component
public class CustomFilter extends OncePerRequestFilter {

    @Value("${RSAPUBLICKEY}")
    String publicKeyContent;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        System.out.println("FILTER RAN");
        System.out.println("public key: " + publicKeyContent);


        final String authorizationHeader = request.getHeader("Authorization");

        System.out.println("header: " + authorizationHeader);
        System.out.println("lower auth header: " + request.getHeader("authorization"));

        if(authorizationHeader != null){

            String result = verify(authorizationHeader.replace("Bearer ",""), publicKeyContent);
            if(!result.equals("FAILED TO VERIFY")){
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(result, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    private String verify(String token, String publicKeyContent) {

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
}