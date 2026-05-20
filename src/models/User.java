package models;

public class User {
    private int id;
    private String username;
    private String pin;

    public User(String username, String pin) {
        this.username = username;
        this.pin = pin;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPin() {
        return pin;
    }

    public void setId(int id) {
        this.id = id;
    }
}