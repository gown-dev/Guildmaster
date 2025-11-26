package model.entities;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Account")
public class BaseAccount implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	protected UUID id;
	
	@Column(name = "username", unique = true, nullable = false)
    protected String username;
	
	@Column(name = "password", unique = false, nullable = false)
    protected String password;
	
	@Builder.Default
    protected boolean active = true;
	
	@Builder.Default
    protected boolean expired = false;
	
	@Builder.Default
    protected boolean locked = false;
	
	@Builder.Default
    protected boolean expiredCredentials = false;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "account_role",
	    joinColumns = @JoinColumn(name = "account_id"),
	    inverseJoinColumns = @JoinColumn(name = "role_id"))
	protected List<Role> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !expiredCredentials;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

}