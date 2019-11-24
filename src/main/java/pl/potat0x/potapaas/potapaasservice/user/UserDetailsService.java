package pl.potat0x.potapaas.potapaasservice.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.security.ExtendedUserDetails;

import java.util.Collections;

@Service
class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ExtendedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findOneByUsername(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new ExtendedUserDetails(userEntity.getUsername(), userEntity.getPassword(), userEntity.getId(), Collections.emptyList());
    }
}
