package pl.potat0x.potapaas.potapaasservice.security;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
final class NewTokenFacade {
    private final JwtSecretConfig jwtSecretConfig;
    private final UserDetailsService userDetailsService;

    @Autowired
    NewTokenFacade(JwtSecretConfig jwtSecretConfig, UserDetailsService userDetailsService) {
        this.jwtSecretConfig = jwtSecretConfig;
        this.userDetailsService = userDetailsService;
    }

    Either<ErrorMessage, String> generateNewJwtTokenForCurrentUser() {
        Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetailsService.loadUserByUsername(principal.username) != null) {
            String token = JwtTokenGenerator.generateJwtToken(principal.username, jwtSecretConfig.getJwtSecret());
            return Either.right("Bearer " + token);
        } else {
            return Either.left(message("", 403));
        }
    }
}
