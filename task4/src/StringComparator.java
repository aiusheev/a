import java.io.File;
import java.util.Scanner;

public class StringComparator {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("not found args");
        }
        Scanner sc = new Scanner(new File(args[0]));
        String first = sc.nextLine();
        String second = sc.nextLine();
        compare(first, second);
    }

    private static void compare(String first, String second) {
        second = second.replace(".", "\\.").replace("*", "(.*)");
        if (first.matches(second)) {
            System.out.println("OK");
        } else {
            System.out.println("KO");
        }
    }
}

