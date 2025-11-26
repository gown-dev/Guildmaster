package model.entities;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {

	private static final long serialVersionUID = 1L;
	
	protected String authority;
	
	@Override
	public String getAuthority() {
		return authority;
	}

}
