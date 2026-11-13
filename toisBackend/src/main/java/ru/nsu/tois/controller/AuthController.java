package ru.nsu.tois.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.tois.authentication.JwtService;
import ru.nsu.tois.dto.request.LoginRequest;
import ru.nsu.tois.dto.response.LoginResponse;
import ru.nsu.tois.entity.user.UserAccount;
import ru.nsu.tois.repository.user.UserAccountRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(authentication);
        UserAccount userAccount = userAccountRepository.findByLogin(authentication.getName())
                .orElseThrow();
        String role = userAccount.getRole() == null ? null : userAccount.getRole().getName();

        return new LoginResponse(token, role);
    }
}
