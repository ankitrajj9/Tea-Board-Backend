package com.ankit.teaboard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "userLogin")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLoginId;
    private int userTypeId;
    private String loginId;
    private String password;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "userLoginId"),
            inverseJoinColumns = @JoinColumn(name = "roleId")
    )
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Role role : roles) {
            String name = role.getRoleName().toUpperCase();
            System.out.println("****"+name);
            authorities.add(new SimpleGrantedAuthority(name));
        }

        return authorities;
    }

    public UserLogin(Long userLoginId){
        this.userLoginId=userLoginId;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        // TODO Auto-generated method stub
        return this.getLoginId();
    }
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return true;
    }
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }
}
