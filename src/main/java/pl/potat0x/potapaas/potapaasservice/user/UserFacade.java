package pl.potat0x.potapaas.potapaasservice.user;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.security.Principal;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.List;
import java.util.UUID;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
public class UserFacade {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserFacade(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    Either<ErrorMessage, UserResponseDto> getUserDetails(String username) {
        if (!checkIfAuthorizedUserIsEqualToRequestedUser(username)) {
            return Either.left(userNotFoundMessage());
        }

        List<UserEntity> user = userRepository.findAllByUsername(username);
        if (user.isEmpty()) {
            return Either.left(userNotFoundMessage());
        } else {
            return Either.right(createResponseDto(user.get(0)));
        }
    }

    Either<ErrorMessage, UserResponseDto> createUser(UserRequestDto requestDto) {
        if (!isUsernameAvailable(requestDto.getUsername())) {
            return Either.left(message("Username \"" + requestDto.getUsername() + "\" is not available", 409));
        }

        UserEntity createdUser = userRepository.save(new UserEntity(UUID.randomUUID().toString(), requestDto.getUsername(), encodePassword(requestDto.getPassword()), requestDto.getEmail()));
        return Either.right(createResponseDto(createdUser));
    }

    Either<ErrorMessage, String> changePassword(String username, ChangePasswordRequestDto requestDto) {
        if (!checkIfAuthorizedUserIsEqualToRequestedUser(username)) {
            return Either.left(userNotFoundMessage());
        }

        List<UserEntity> user = userRepository.findAllByUsername(username);
        if (user.isEmpty()) {
            return Either.left(userNotFoundMessage());
        }

        UserEntity userEntity = user.get(0);
        if (userEntity.getPassword().equals(encodePassword(requestDto.getCurrentPassword()))) {
            String encodedNewPassword = encodePassword(requestDto.getNewPassword());
            userEntity.setPassword(encodedNewPassword);
            userRepository.save(userEntity);
            return Either.right("Password changed");
        } else {
            return Either.left(message("Invalid password", 401));
        }
    }

    Either<ErrorMessage, String> deleteUser(String username) {
        if (!checkIfAuthorizedUserIsEqualToRequestedUser(username)) {
            return Either.left(userNotFoundMessage());
        }

        List<UserEntity> userToDelete = userRepository.findAllByUsername(username);
        if (userToDelete.isEmpty()) {
            return Either.left(userNotFoundMessage());
        }

        userRepository.delete(userToDelete.get(0));
        return Either.right(username);
    }

    private boolean checkIfAuthorizedUserIsEqualToRequestedUser(String requestedUsername) {
        Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return requestedUsername.equals(principal.username);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private ErrorMessage userNotFoundMessage() {
        return message("User not found", 404);
    }

    private boolean isUsernameAvailable(String username) {
        return userRepository.countByUsername(username) == 0;
    }

    private UserResponseDto createResponseDto(UserEntity userEntity) {
        return new UserResponseDto(userEntity.getUsername(), userEntity.getEmail(), userEntity.getCreatedAt());
    }
}
