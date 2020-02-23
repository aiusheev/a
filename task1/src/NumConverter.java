import java.io.File;
import java.util.Scanner;

public class NumConverter {
    private static final String NUM_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("not found args");
        }
        try (Scanner sc = new Scanner(new File(args[0]));) {
            int nb = sc.nextInt();
            String base = sc.next();
            System.out.println(intToBase(nb, base));
        }
    }

    private static String intToBase(int nb, String base) {
        if (!base.equals("0") && NUM_ALPHABET.startsWith(base)) {
            int radix = NUM_ALPHABET.lastIndexOf(base.charAt(base.length() - 1)) + 1;
            return Integer.toString(nb, radix);
        } else if (base.equals("котики")) {
            return "у меня лапки";
        }
        return "usage";
    }
}
