package org.example.parser;

public class Token {
    public enum Type {
        IDENT, KEYWORD, NUMBER, STRING,
        LPAREN, RPAREN, COMMA, SEMI,STAR,
        EOF,EQUAL
    }

    public final Type type;
    public final String text;

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public String toString() {
        return type + "(" + text + ")";
    }

}
