package pl.potat0x.potapaas.potapaasservice.user;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

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
        UserEntity userEntity = userRepository.findOneByUsername(username);
        if (userEntity == null) {
            return Either.left(userNotFoundMessage());
        } else {
            return Either.right(createResponseDto(userEntity));
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
        UserEntity userEntity = userRepository.findOneByUsername(username);
        if (userEntity == null) {
            return Either.left(userNotFoundMessage());
        }

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
        UserEntity userToDelete = userRepository.findOneByUsername(username);
        if (userToDelete == null) {
            return Either.left(userNotFoundMessage());
        }

        userRepository.delete(userToDelete);
        return Either.right(username);
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
