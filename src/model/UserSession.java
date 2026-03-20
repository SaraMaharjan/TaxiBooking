package model;
 
/**
 * UserSession — Singleton holding the currently logged-in user.
 *
 * Fields stored:
 *   fullName, email, role, address, phone
 *
 * Call createInstance() after a successful login (LogIn model).
 * Call updateProfile()  after the user saves profile changes.
 * Call logout()         on log out.
 */
public class UserSession {
 
    private static UserSession instance;
 
    private String fullName;
    private String email;
    private String role;
    private String address;   // from DB or profile save
    private String phone;     // from DB or profile save
 

    private UserSession(String fullName, String email, String role,
                        String address, String phone) {
        this.fullName = fullName;
        this.email    = email;
        this.role     = role;
        this.address  = address != null ? address : "";
        this.phone    = phone    != null ? phone    : "";
    }
 
    // ── Factory methods ──────────────────────────────────────────────
 
    /**
     * Full creation — called after login when all DB fields are available.
     */
    public static void createInstance(String fullName, String email,
                                      String role, String address, String phone) {
        instance = new UserSession(fullName, email, role, address, phone);
    }
 
    /**
     * Minimal creation — called when only name/email/role are returned by DB.
     * Address and phone default to empty strings.
     */
    public static void createInstance(String fullName, String email, String role) {
        createInstance(fullName, email, role, "", "");
    }
 
    /** Alias kept for backward compatibility. */
    public static void login(String fullName, String email, String role) {
        createInstance(fullName, email, role);
    }
 
    /** Clears session on logout. */
    public static void logout() {
        instance = null;
    }
 
    /** Returns active session, or null if nobody is logged in. */
    public static UserSession getInstance() {
        return instance;
    }
 
    // ── Update after profile save ────────────────────────────────────
 
    /**
     * Called by ProfileController (or Main.java saveBtn handler) after
     * the user saves their profile.  Updates in-memory session values.
     */
    public void updateProfile(String firstName, String lastName,
                              String address, String phone) {
        this.fullName = (firstName + " " + lastName).trim();
        this.address  = address != null ? address : "";
        this.phone    = phone   != null ? phone   : "";
    }
 
    // ── Getters ──────────────────────────────────────────────────────
 
    public String getFullName() { return fullName; }
    public String getEmail()    { return email;    }
    public String getRole()     { return role;     }
    public String getAddress()  { return address;  }
    public String getPhone()    { return phone;    }
}