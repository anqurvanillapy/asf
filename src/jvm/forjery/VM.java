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
    private LinkedList<Token> e;

    public Lexer(Reader r) {
        this.r = r;
        this.e = new LinkedList<Token>();
    }

    public LinkedList<Token> scan() throws IOException {
        int t,              // temp
            row, col;       // line and column
        char p, lp;         // source code position
        TkType ty; int val; // current token

        ty = TkType.Num; val = 0;
        for (row = col = 1; (t = r.read()) > -1; ++col) {
            p = (char) t;

            if      (p >= '0' && p <= '9') {
                ty = TkType.Num;
                val = Character.getNumericValue(p);
            }
            else if (p == '.') { ty = TkType.Pop; val = p; }
            else if (p == 'S') { ty = TkType.Sstk; val = p; }
            else if (p == ' ') continue;
            else if (p == '\n') { ++row; col = 1; continue; }
            else {
                System.err.println(row + ":" + col +
                                   " Invalid token \"" + p + "\"");
                System.exit(1);
            }

            e.push(new Token(ty, val));
        }

        return e;
    }
}

class Parser {}

public class VM {
    private static String usage = "Usage: forjery file";
    // TODO: Reserved words HashMap/HashSet.

    private static LinkedList<Token> e;   // emitted code
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

        // Lexer.
        Lexer lexer = new Lexer(br);
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
