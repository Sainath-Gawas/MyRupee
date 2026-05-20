import db.InitDB;
import screens.LoginScreen;

public class Main {
    public static void main(String[] args) {
        InitDB.init();
        new LoginScreen();
    }
}