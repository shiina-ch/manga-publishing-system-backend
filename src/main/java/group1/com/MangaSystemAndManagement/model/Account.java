package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

@Entity
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<SystemRole> systemRole;

    @Column(name = "status")
    private String status = AccountStatus.PENDING.name();

    @Column(name = "requested_role")
    private String requestedRole;

    @Column(name = "approved_by_id")
    private Long approvedById;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "rejected_by_id")
    private Long rejectedById;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    public Account() {}

    public Account(long id, String firstName, String lastName, String phoneNumber,
                   String email, String password, List<SystemRole> systemRole) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.systemRole = systemRole;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public List<SystemRole> getSystemRole() { return systemRole; }
    public void setSystemRole(List<SystemRole> systemRole) { this.systemRole = systemRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setStatus(AccountStatus status) {
        this.status = status == null ? null : status.name();
    }

    public String getRequestedRole() { return requestedRole; }
    public void setRequestedRole(String requestedRole) { this.requestedRole = requestedRole; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getRejectedById() { return rejectedById; }
    public void setRejectedById(Long rejectedById) { this.rejectedById = rejectedById; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (systemRole == null) return List.of();
        return systemRole.stream()
                .filter(Objects::nonNull)
                .map(SystemRole::getRoleName)
                .map(SystemRoleName::tryFrom)
                .flatMap(java.util.Optional::stream)
                .distinct()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    public boolean hasRole(SystemRoleName roleName) {
        return roleName != null && getAuthorities().stream()
                .anyMatch(authority -> roleName.name().equals(authority.getAuthority()));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return AccountStatus.ACTIVE.matches(status); }
}
