package bj.salledereunion.security;

import java.security.Principal;

public final class RolePrincipal implements Principal {
    private final String role;

    public RolePrincipal(String role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return role;
    }

    @Override
    public boolean equals(Object autre) {
        return autre instanceof RolePrincipal principal && role.equals(principal.role);
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }
}
