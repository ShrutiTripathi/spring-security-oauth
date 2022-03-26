package com.shruti.springsecurityclient.entity.listener;

import com.shruti.springsecurityclient.entity.Users;
import com.shruti.springsecurityclient.event.RegistrationCompleteEvent;
import com.shruti.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;
/*
three thing are happening on user registration:
1. Data saved in Users table as it is coming from request; password saved with PasswordEncoder
2. Data saved in Verification_token table; token randomly generated; expiration time calculated
3. A link is created to verify the user account based upon the generated token.
 */
    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //Create the Verification Token for the User with Link
        Users user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token,user);
        //Send Mail to user
        String url =
                event.getApplicationUrl()
                        + "/verifyRegistration?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }
}
