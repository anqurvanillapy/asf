import java.io.*;
import java.nio.charset.*;
import java.util.*;

enum TkType {
    Num, Pop, Sstk
}

class Token {
    private TkType ty;
    private int val;

    public Token(TkType ty, int val) {
        this.ty = ty;
        this.val = val;
    }

    public TkType getType() { return this.ty; }
    public int getValue() { return this.val; }
}

class Lexer {
    private Reader r;
    private HashMap<String, TkType> rw;
    private LinkedList<Token> e;

    public Lexer(Reader r, HashMap<String, TkType> rw) {
        this.r = r;
        this.rw = rw;
        this.e = new LinkedList<Token>();
    }

    public LinkedList<Token> scan() throws IOException {
        int t,              // temp
            row, col;       // line and column
        String buf;         // identifier buffer
        char p;             // source current position
        TkType ty; int val; // current token

        buf = ""; ty = TkType.Num; val = 0;
        for (row = col = 1; (t = r.read()) > -1; ++col) {
            p = (char) t;

            // Re-initialize for every character.
            ty = TkType.Num; val = 0;
                ty = TkType.Num;
                val = Character.getNumericValue(p);
            if (p > 32 && p < 127) {    // whitespace is 31
                buf += p;
                continue;
            } else if (p == ' ' || p == '\n') {
                if (buf.equals("")) continue;

                if ( (ty = rw.get(buf)) == null) {
                    try {
                        val = Integer.parseInt(buf);
                        ty = TkType.Num;
                    } catch (NumberFormatException e) {
                        System.err.println(row + ":" + col +
                                " Invalid identifier \"" + buf + "\"");
                        System.exit(1);
                    }
                }

                if (p == '\n') { ++row; col = 1; }
                buf = "";
            } else {
                System.err.println(row + ":" + col +
                        " Invalid token \"" + p + "\"");
                System.exit(1);
            }

            e.push(new Token(ty, val));
        }

        return e;
    }
}

public class VM {
    private static String usage = "Usage: forjery file";
    private static HashMap<String, TkType> rw;  // reserved words

    private static LinkedList<Token> e;         // emitted code
    // TODO: Heterogeneous stack, not only for Integers.
    private static LinkedList<Integer> s;       // stack

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println(usage);
            System.exit(1);
        }

        // Read the source file (BufferedReader for efficiency).
        Reader br = new BufferedReader(
                        new InputStreamReader(
                            new FileInputStream(
                                new File(args[0])),
                            Charset.defaultCharset()));

        // Initialize reserved words table.
        rw = new HashMap<String, TkType>();
        rw.put(".", TkType.Pop);
        rw.put(".s", TkType.Sstk);

        // Start lexical analysis.
        Lexer lexer = new Lexer(br, rw);
        e = lexer.scan();

        // Run.
        Token tk;
        s = new LinkedList<Integer>();

        while (e.size() != 0) {
            tk = e.removeLast();
            switch (tk.getType()) {
                case Num:
                    s.push(tk.getValue());
                    break;
                case Pop:
                    s.pop();
                    break;
                case Sstk:
                    System.out.println("HEAD" + Arrays.toString(s.toArray()));
                    break;
                default:
                    System.err.println("Invalid token tk=" + tk.getType() +
                            " val=" + tk.getValue());
            }
        }
    }
}
