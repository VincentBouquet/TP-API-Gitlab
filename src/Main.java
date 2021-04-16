public class Main {
    public static void main(String[] args) {
        Auth auth = new Auth();
        auth.openAuthorizationPage();
        auth.runHttpServer();
    }
}
