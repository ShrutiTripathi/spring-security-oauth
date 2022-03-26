package com.shruti.springsecurityclient.service;

import com.shruti.springsecurityclient.entity.Users;
import com.shruti.springsecurityclient.entity.VerificationToken;
import com.shruti.springsecurityclient.model.UserModel;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

public interface UserService {
    public Users registerUser(@RequestBody UserModel userModel) ;
    public void saveVerificationTokenForUser(String token, Users user);
    public String validateVerificationToken(String token);
    public VerificationToken generateNewVerificationToken(String oldToken);
    public Users findUserByEmail(String email) ;
    public void createPasswordResetTokenForUser(Users user, String token) ;
    public String validatePasswordResetToken(String token);
    public Optional<Users> getUserByPasswordResetToken(String token);
    public void changePassword(Users user, String newPassword);
    public boolean checkIfValidOldPassword(Users user, String oldPassword);

    }
