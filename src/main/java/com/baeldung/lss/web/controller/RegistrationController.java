package com.baeldung.lss.web.controller;

import com.baeldung.lss.persistence.VerificationTokenRepository;
import com.baeldung.lss.service.IUserService;
import com.baeldung.lss.validation.EmailExistsException;
import com.baeldung.lss.web.model.PasswordResetToken;
import com.baeldung.lss.web.model.User;
import com.baeldung.lss.web.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.google.common.collect.ImmutableMap;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

@Controller
public class RegistrationController {

    private final IUserService userService;
    private final VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private Environment env;
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public RegistrationController(IUserService userService, VerificationTokenRepository verificationTokenRepository) {
        this.userService = userService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @RequestMapping(value = "signup")
    public ModelAndView registrationForm() {
        return new ModelAndView("registrationPage", "user", new User());
    }

    @RequestMapping(value = "user/register")
    public ModelAndView registerUser(
            @Valid final User user,
            final BindingResult result,
            final HttpServletRequest request) {

        if (result.hasErrors()) {
            return new ModelAndView("registrationPage", "user", user);
        }
        try {
            user.setEnabled(false);
            userService.registerNewUser(user);
            final String token = UUID.randomUUID().toString();
            final VerificationToken myToken = new VerificationToken(token, user);
            verificationTokenRepository.save(myToken);

            final SimpleMailMessage email = sendEmail(request, token, user, "Registration Confirmation","/registrationConfirm?token=");
            mailSender.send(email);

        } catch (EmailExistsException e) {
            result.addError(new FieldError("user", "email", e.getMessage()));
            return new ModelAndView("registrationPage", "user", user);
        }
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/registrationConfirm")
    public ModelAndView confirmRegistration(
            final Model model,
            @RequestParam("token") final String token,
            final RedirectAttributes redirectAttributes) {
        final VerificationToken verificationToken = userService.getVerificationToken(token);

        if (verificationToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid account confirmation token.");
            return new ModelAndView("redirect:/login");
        }
        User user = verificationToken.getUser();
        //User user1 = new User(user.getEmail(), user.getPassword(), user.getPasswordConfirmation(), user.getEnabled(), user.getCreated(), user.getVerificationToken());
        //==================== This is added by me ===============================//TODO
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your registration token has expired. Please register again.");
            return new ModelAndView("redirect:/login");
        }
        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        redirectAttributes.addFlashAttribute("message", "Your account verified successfully");
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
    public ModelAndView resetPassword(final HttpServletRequest request,
                                      final RedirectAttributes redirectAttributes,
                                      @RequestParam("email") final String userEmail){
        final User user = userService.findUserByEmail(userEmail);
        System.out.println("I am here");
        System.out.println("This is an email "+userEmail);
        if (user != null){
            final String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            final SimpleMailMessage email = sendEmail(request, token, user, "Reset Password","/user/changePassword?id=" +user.getId()+ "&token=");
            mailSender.send(email);
        }
        redirectAttributes.addFlashAttribute("message", "You should receive an Password Reset Email shortly");
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/user/changePassword",method = RequestMethod.GET)
    public ModelAndView changePassword(
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes,
            @RequestParam("id") final long id,
            @RequestParam("token") final String token){

        final PasswordResetToken passToken = userService.getPasswordResetToken(token);


        if (passToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid password reset token");
            return new ModelAndView("redirect:/login");
        }
        final User user = passToken.getUser();
        if (user.getId() != id) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid password reset token");
            return new ModelAndView("redirect:/login");
        }

        final Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate()
                .getTime()
                - cal.getTime()
                .getTime()) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your password reset token has expired");
            return new ModelAndView("redirect:/login");
        }

        final ModelAndView view = new ModelAndView("resetPassword");
        view.addObject("token", token);
        return view;
    }

    @RequestMapping(value = "/user/savePassword")
    public ModelAndView savePassword(@RequestParam("password") final String password,
                                     @RequestParam("passwordConfirmation") final String passwordConfirmation,
                                     @RequestParam("token") final String token,
                                     final RedirectAttributes redirectAttributes) {

        if (!password.equals(passwordConfirmation)) {
            return new ModelAndView("resetPassword", ImmutableMap.of("errorMessage", "Passwords do not match"));
        }
        final PasswordResetToken p = userService.getPasswordResetToken(token);
        if (p == null) {
            redirectAttributes.addFlashAttribute("message", "Invalid token");
        } else {
            final User user = p.getUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("message", "Unknown user");
            } else {
                userService.changeUserPassword(user, password);
                redirectAttributes.addFlashAttribute("message", "Password reset successfully");
            }
        }
        return new ModelAndView("redirect:/login");
    }
    public final SimpleMailMessage sendEmail(final HttpServletRequest request, String token, User user, String subject, String path) {
        final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        System.out.println(request.getServerName());
        System.out.println(request.getServerPort());
        System.out.println("The Context Path is:"+request.getContextPath());
        final String recipientAddress = user.getEmail();
        //final String subject = "Registration Confirmation";
        final String confirmationUrl = appUrl + path + token;
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText("Please open the following URL to verify your account: \r\n" + confirmationUrl);
        email.setFrom(Objects.requireNonNull(env.getProperty("support.email")));
        return email;
    }
}
