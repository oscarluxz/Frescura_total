package frescuratotal.erp.common;

/** Explicit role simulation for the academic console; this is not authentication. */
public class UserSession {
    public enum Role { OPERATOR, INSPECTOR }
    private Role role = Role.OPERATOR;
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = java.util.Objects.requireNonNull(role); }
    public void requireInspector() {
        if (role != Role.INSPECTOR) {
            throw new IllegalArgumentException("Solo una sesión con rol Inspector puede aprobar o rechazar lotes.");
        }
    }
}
