package org.example.parser;

import java.util.Set;


public class SimpleTokenizer {
    private final String input;
    private int pos = 0;
    private static final Set<String> KEYWORDS = Set.of(
            "CREATE", "TABLE", "DELETE", "INSERT", "INTO", "VALUES", "SELECT",
            "FROM", "INT", "STRING","UPDATE","SET","WHERE"
    );

    public SimpleTokenizer(String input) {
        this.input = input.trim();
    }

    public Token next() {
        skipWhitespace();
        if (pos >= input.length()) return new Token(Token.Type.EOF, "");

        char c = input.charAt(pos);

        // Parentheses and punctuation
        if (c == '(') {
            pos++;
            return new Token(Token.Type.LPAREN, "(");
        }
        if (c == ')') {
            pos++;
            return new Token(Token.Type.RPAREN, ")");
        }
        if (c == ',') {
            pos++;
            return new Token(Token.Type.COMMA, ",");
        }
        if (c == ';') {
            pos++;
            return new Token(Token.Type.SEMI, ";");
        }
        if (c == '*') {
            pos++;
            return new Token(Token.Type.STAR, "*");
        }
        if(c == '='){
            pos++;
            return new Token(Token.Type.EQUAL, "=");
        }
        if (c == '"') return string();

        // Identifiers or keywords
        if (Character.isLetter(c)) {
            String word = readWord().toUpperCase();
            if (KEYWORDS.contains(word))
                return new Token(Token.Type.KEYWORD, word);
            return new Token(Token.Type.IDENT, word);
        }

        // Numbers
        if (Character.isDigit(c)) {
            String num = readNumber();
            return new Token(Token.Type.NUMBER, num);
        }

        if (pos >= input.length()) {
            return new Token(Token.Type.EOF, "");
        }

        throw new RuntimeException("Unexpected character: " + c);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
    }

    private String readWord() {
        skipWhitespace();
        int start = pos;
        while (pos < input.length() &&
                (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            pos++;
        }
        return input.substring(start, pos);
    }

    private String readNumber() {
        skipWhitespace();
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
        return input.substring(start, pos);
    }

    private Token string() {
        pos++; // skip opening quote
        int start = pos;
        while (pos < input.length() && input.charAt(pos) != '"') pos++;
        if (pos >= input.length()) throw new RuntimeException("Unterminated string");
        String s = input.substring(start, pos);
        pos++; // skip closing quote
        return new Token(Token.Type.STRING, s);
    }

    public void debugTokens() {
        SimpleTokenizer copy = new SimpleTokenizer(this.input);
        Token t;
        do {
            t = copy.next();
            System.out.println(t);
        } while (t.type != Token.Type.EOF);
    }

}