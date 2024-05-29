package com.DBAuthExample.AuthExample.Services;

import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder encoder;

    public boolean HaveUser(String username) {
        return userRepo.findByName(username).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByName(username);


         return user.map(MyUserDetails::new)
                .orElseThrow(()->new UsernameNotFoundException(username+"There is not such user in REPO"));
    }
    public List<User> allUsers() {
        return userRepo.findAll();
    }

    public boolean saveUser(User user) {
        Optional<User> userFromDB = userRepo.findByName(user.getName());

        if (userFromDB.isPresent()){
            return false;
        }

        user.setRole("ROLE_USER");
        user.setPassword(encoder.encode(user.getPassword()));
        user.setMaxLvlAccess(Byte.parseByte("0"));
        user.setCurrentLvlAccess(Byte.parseByte("0"));
        userRepo.save(user);
        return true;
    }
    public User loadUserByID(long id){
        return userRepo.findById(id).get();
    }
    public boolean deleteUser(Long userId) {
        if (userRepo.findById(userId).isPresent()) {
            userRepo.deleteById(userId);
            return true;
        }
        return false;
    }
    public void ChangeAccessLvl(String name, byte lvl){
        Optional<User> userOptional = userRepo.findByName(name);
        User user = userOptional.get();
        user.setMaxLvlAccess(lvl);
        user.setCurrentLvlAccess(lvl);
        userRepo.save(user);

    }
    public User UserByUsername(String UserName){
        Optional<User> user= userRepo.findByName(UserName);
        return user.get();

    }
    public void ChangeTempAccessLvl(String name, byte lvl){
        Optional<User> userOptional = userRepo.findByName(name);
        User user = userOptional.get();
        user.setCurrentLvlAccess(lvl);
        userRepo.save(user);

    }
    public boolean HaveRuleEdit(User user, byte lvl){
       return user.getMaxLvlAccess()>=lvl;
    }
    public boolean HaveRuleAppend(User user, byte lvl){
        return user.getCurrentLvlAccess()<=lvl;
    }

}
