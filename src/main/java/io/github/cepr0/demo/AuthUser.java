package io.github.cepr0.demo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Value
@EqualsAndHashCode(callSuper = false)
public class AuthUser extends User {

	private String email;

	@Builder(builderMethodName = "with")
	public AuthUser(final String username, final String password, @Singular final Collection<? extends GrantedAuthority> authorities, final String email) {
		super(username, password, authorities);
		this.email = email;
	}

	public enum Role implements GrantedAuthority {
		USER, ADMIN;

		@Override
		public String getAuthority() {
			return this.name();
		}
	}
}
