import java.io.*;
import java.nio.charset.*;
import java.util.*;

enum TkType {
    /* Number. */
    Num,
    /* String. */
    Sos, Str,
    /* Operations. */
    Pop,
    Sstk,
    Add, Sub, Mul, Div,
    Println
}

class Token {
    public TkType ty;
    public Token(TkType ty) { this.ty = ty; }
}

class TkNum extends Token {
    public int val;
    public TkNum(TkType ty, int val) { super(ty); this.val = val; }
}

class TkStr extends Token {
    public String val;
    public TkStr(TkType ty, String val) { super(ty); this.val = val; }
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
            val,            // current token value
            row, col;       // line and column
        String buf;         // identifier buffer
        char p;             // source current position
        Token tk;           // last token
        TkType ty;          // current token type

        buf = ""; ty = TkType.Num; val = 0; tk = null;
        for (row = col = 1; (t = r.read()) > -1; ++col) {
            p = (char) t;

            if (p > 31 && p < 127 || p == '\n') {
                // Sos: Start of string, means a string literal.
                if (tk != null && tk.ty == TkType.Sos) {
                    if (p != '"') { buf += p; continue; }
                    tk = new TkStr(TkType.Str, buf);
                    buf = ""; ty = TkType.Num;
                } else if (p == ' ' || p == '\n') {
                    if (buf.equals("")) continue;

                    if ( (ty = rw.get(buf)) == null) {
                        try {
                            // Number literal.
                            val = Integer.parseInt(buf);
                            ty = TkType.Num;
                            tk = new TkNum(ty, val);
                        } catch (NumberFormatException e) {
                            System.err.println(row + ":" + col +
                                    " Invalid identifier \"" + buf + "\"");
                            System.exit(1);
                        }
                    } else tk = new Token(ty);
                } else { buf += p; continue; }

                if (p == '\n') { ++row; col = 1; }
                buf = "";

                // Don't put Sos into emitted code.
                if (tk.ty == TkType.Sos) continue;
            } else {
                System.err.println(row + ":" + col +
                        " Invalid character \"" + p + "\"");
                System.exit(1);
            }

            e.push(tk);
        }

        return e;
    }
}

public class VM {
    private static String usage = "Usage: forjery file";
    private static HashMap<String, TkType> rw;  // reserved words
    private static LinkedList<Token> e;         // emitted code
    private static LinkedList<Token> s;         // stack

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
        rw.put(".\"", TkType.Sos);
        rw.put(".", TkType.Pop);
        rw.put(".s", TkType.Sstk);
        rw.put("+", TkType.Add);
        rw.put("-", TkType.Sub);
        rw.put("*", TkType.Mul);
        rw.put("/", TkType.Div);
        rw.put("println", TkType.Println);

        // Start lexical analysis.
        Lexer lexer = new Lexer(br, rw);
        e = lexer.scan();

        // Run.
        int a, b;                       // temps
        Token tk;                       // current token
        s = new LinkedList<Token>();  // init stack

        while (e.size() != 0) {
            tk = e.removeLast();
            switch (tk.ty) {
                case Num: s.push(new TkNum(TkType.Num, ((TkNum)tk).val)); break;
                case Str: s.push(new TkStr(TkType.Str, ((TkStr)tk).val)); break;
                case Pop: s.pop(); break;
                case Sstk:
                    System.out.print("<" + s.size() + "> ");
                    for (int i = 0; i < s.size(); ++i) {
                        if (s.get(i) instanceof TkNum) {
                            System.out.print(((TkNum) s.get(i)).val + " ");
                        } else if (s.get(i) instanceof TkStr) {
                            System.out.print(
                                "\"" + ((TkStr) s.get(i)).val + "\" "
                            );
                        } else {
                            System.out.print(tk.ty + " ");
                        }
                    }
                    System.out.println("ok");
                    break;
                case Add:
                    a = ((TkNum) s.pop()).val; b = ((TkNum) s.pop()).val;
                    s.push(new TkNum(TkType.Num, b + a));
                    break;
                case Mul:
                    a = ((TkNum) s.pop()).val; b = ((TkNum) s.pop()).val;
                    s.push(new TkNum(TkType.Num, b * a));
                    break;
                case Sub:
                    a = ((TkNum) s.pop()).val; b = ((TkNum) s.pop()).val;
                    s.push(new TkNum(TkType.Num, b - a));
                    break;
                case Div:
                    a = ((TkNum) s.pop()).val; b = ((TkNum) s.pop()).val;
                    s.push(new TkNum(TkType.Num, b / a));
                    break;
                case Println:
                    tk = s.pop();
                    if (tk instanceof TkNum) {
                        System.out.println(((TkNum) tk).val);
                    } else if (tk instanceof TkStr) {
                        System.out.println(((TkStr) tk).val);
                    } else System.out.println(tk.ty);
                    break;
                default:
                    System.err.println("Invalid token tk=" + tk.ty);
            }
        }
    }
}
