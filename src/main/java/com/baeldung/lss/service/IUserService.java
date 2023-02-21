package com.baeldung.lss.service;

import com.baeldung.lss.validation.EmailExistsException;
import com.baeldung.lss.web.model.PasswordResetToken;
import com.baeldung.lss.web.model.User;
import com.baeldung.lss.web.model.VerificationToken;

public interface IUserService {

    User registerNewUser(User user) throws EmailExistsException;

    User updateExistingUser(User user) throws EmailExistsException;

    void saveRegisteredUser(User user);
    VerificationToken getVerificationToken(String token);
    void createPasswordResetTokenForUser(User user, String token);
    User findUserByEmail(String email);
    PasswordResetToken getPasswordResetToken(String token);

    void changeUserPassword(User user, String password);
}
