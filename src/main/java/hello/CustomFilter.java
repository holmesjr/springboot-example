package hello;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        System.out.println("FILTER RAN");

        final String authorizationHeader = request.getHeader("Authorization");

        if(authorizationHeader != null){
            final String uri = "http://localhost:8080/verify/{token}";
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", authorizationHeader.replace("Bearer ",""));

            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class, params);
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
}