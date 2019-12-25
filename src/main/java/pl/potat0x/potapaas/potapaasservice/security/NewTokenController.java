package pl.potat0x.potapaas.potapaasservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/new-auth-token")
class NewTokenController {

    private final NewTokenFacade newTokenFacade;

    @Autowired
    NewTokenController(NewTokenFacade newTokenFacade) {
        this.newTokenFacade = newTokenFacade;
    }

    @GetMapping
    ResponseEntity generateNewAuthToken() {
        return newTokenFacade.generateNewJwtTokenForCurrentUser()
                .map(newToken -> ResponseEntity.ok().header("Authorization", newToken).build())
                .getOrElseGet(error -> ResponseEntity.status(403).build());
    }
}
