package group1.com.MangaSystemAndManagement.security.service;

import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmailIgnoreCase(email == null ? null : email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
