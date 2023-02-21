package com.baeldung.lss.web.model;

import com.baeldung.lss.validation.PasswordMatches;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.Calendar;
import java.util.Objects;

@Entity
@PasswordMatches
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotEmpty(message = "Email is required.")
    private String email;

    @NotEmpty(message = "Password is required.")
    private String password;

    public String getPassword() {
        return password;
    }

    @Transient
    @NotEmpty(message = "Password confirmation is required.")
    private String passwordConfirmation;

    private Calendar created = Calendar.getInstance();

    @Column
    private Boolean enabled ;
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.REMOVE)
    private VerificationToken verificationToken;

//    @Override
//    public String toString() {
//        return "User{" + "id=" + id + ", email='" + email + '\'' + ", password='" + password + '\'' + ", passwordConfirmation='" + passwordConfirmation + '\'' + ", created=" + created + '}';
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
