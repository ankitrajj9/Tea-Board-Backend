package com.ankit.teaboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {
    private static List<String> unAuthURLs;
    @Bean
    public List<String> getUnAuthUrls(){
        this.unAuthURLs = List.of("/login","/saveuser","/socket","/chat","/app","/test","/broadcast");
        return this.unAuthURLs;
    }

    public boolean isAuthenticated(String url){
        boolean vbool = false;
        for(String unAuthUrl:unAuthURLs){
            if(url.startsWith(unAuthUrl)){
                vbool = true;
                break;
            }
        }
        return vbool;
    }
}
