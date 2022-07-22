public class Application {

    public static void main(String[] args) {

        System.out.println("Main class");

        final Speaker speaker = new Speaker();

        System.out.println(speaker.sayHi());
    }
}
