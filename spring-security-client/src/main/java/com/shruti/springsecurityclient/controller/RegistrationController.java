package com.shruti.springsecurityclient.controller;

import com.shruti.springsecurityclient.entity.PasswordResetToken;
import com.shruti.springsecurityclient.entity.Users;
import com.shruti.springsecurityclient.entity.VerificationToken;
import com.shruti.springsecurityclient.event.RegistrationCompleteEvent;
import com.shruti.springsecurityclient.model.PasswordModel;
import com.shruti.springsecurityclient.model.UserModel;
import com.shruti.springsecurityclient.repository.PasswordResetTokenRepository;
import com.shruti.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    UserService userService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        Users user = userService.registerUser(userModel);
        applicationEventPublisher.publishEvent(new RegistrationCompleteEvent(
                user, applicationUrl(request)
        ));
        return "Success";
    }

    public String applicationUrl(HttpServletRequest request){
        return "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")) {
            return "User Verified Successfully";
        }
        return "Bad User";
    }

    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken
                = userService.generateNewVerificationToken(oldToken);
        Users user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        Users user = userService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if(user!=null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request), token);
        }
        return url;
    }
    private void resendVerificationTokenMail(Users user, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }

    public void createPasswordResetTokenForUser(Users user, String token) {
        PasswordResetToken passwordResetToken
                = new PasswordResetToken(user,token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null) {
            return "invalid";
        }
        Users user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((passwordResetToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";
    }
    private String passwordResetTokenMail(Users user, String applicationUrl, String token) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to Reset your Password: {}",
                url);
        return url;
    }
    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel) {
        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }
        Optional<Users> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()) {
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }
    }
    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        Users user = userService.findUserByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save New Password
        userService.changePassword(user,passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }

}
