package com.baeldung.lss.service;

import javax.transaction.Transactional;

import com.baeldung.lss.persistence.VerificationTokenRepository;
import com.baeldung.lss.web.model.PasswordResetToken;
import com.baeldung.lss.web.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baeldung.lss.persistence.UserRepository;
import com.baeldung.lss.validation.EmailExistsException;
import com.baeldung.lss.web.model.User;

@Service
@Transactional
class UserService implements IUserService {

    private final UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerNewUser(final User user) throws EmailExistsException {
        if (emailExist(user.getEmail())) {
            throw new EmailExistsException("There is an account with that email address: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private boolean emailExist(String email) {
        final User user = userRepository.findByEmail(email);
        return user != null;
    }

    @Override
    public User updateExistingUser(User user) throws EmailExistsException {
        final Long id = user.getId();
        final String email = user.getEmail();
        final User emailOwner = userRepository.findByEmail(email);
        if (emailOwner != null && !id.equals(emailOwner.getId())) {
            throw new EmailExistsException("Email not available.");
        }
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public void saveRegisteredUser(final User user) {
        userRepository.save(user);
    }

    @Override
    public VerificationToken getVerificationToken(final String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }

    @Override
    public User findUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public PasswordResetToken getPasswordResetToken(String token) {
        return passwordTokenRepository.findByToken(token);
    }

    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }


}
