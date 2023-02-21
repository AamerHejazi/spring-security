package com.baeldung.lss.security;

import com.baeldung.lss.persistence.UserRepository;
import com.baeldung.lss.web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;

@Service
@Transactional
public class LssUserDetailsService implements UserDetailsService {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;

    @Autowired
    public LssUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("No user found with username: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                true,
                true,
                true,
                getAuthorities(ROLE_ADMIN,ROLE_USER)
        );
    }

    private Collection<? extends GrantedAuthority>
    getAuthorities(String role, String roleUser) {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}
