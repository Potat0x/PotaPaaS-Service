package pl.potat0x.potapaas.potapaasservice.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.security.ExtendedUserDetails;

import java.util.Collections;
import java.util.List;

@Service
class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ExtendedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserEntity> user = userRepository.findAllByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        UserEntity userEntity = user.get(0);
        return new ExtendedUserDetails(userEntity.getUsername(), userEntity.getPassword(), userEntity.getId(), Collections.emptyList());
    }
}
