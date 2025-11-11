package org.example.parser;



import org.example.catalog.Catalog;
import org.example.catalog.TableSchema;
import org.example.commands.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final Catalog catalog;

    public Parser(Catalog catalog) {
        this.catalog = catalog;
    }

    public Command parse(String input) {
        SimpleTokenizer tk = new SimpleTokenizer(input);
        Token t = tk.next();
        if (t.type == Token.Type.KEYWORD && t.text.equals("CREATE")) {
            Token nxt = tk.next();
            if (nxt.text.equals("TABLE")) return parseCreateTable(tk);
            if (nxt.text.equals("INDEX")) return parseCreateIndex(tk);
        }
        if (t.type == Token.Type.KEYWORD && t.text.equals("INSERT")) {
            return parseInsert(tk);
        }
        if (t.type == Token.Type.KEYWORD && t.text.equals("SELECT")) {
            return parseSelect(tk);
        }
        if (t.type == Token.Type.KEYWORD && t.text.equals("UPDATE")) {
            return parseUpdate(tk);
        }
        if (t.type == Token.Type.KEYWORD && t.text.equals("DELETE")) {
            return parseDelete(tk);
        }

        throw new RuntimeException("Unsupported command");
    }
    private void expectKeyword(SimpleTokenizer tk, String keyword) {
        Token t = tk.next();
        if (t.type != Token.Type.KEYWORD || !t.text.equalsIgnoreCase(keyword)) {
            throw new RuntimeException("Expected keyword '" + keyword + "' but got: " + t.text);
        }
    }

    // Utility method to ensure the next token is a specific symbol (like '(' or ';')
    private void expect(SimpleTokenizer tk, Token.Type type) {
        Token t = tk.next();
        if (t.type != type) {
            throw new RuntimeException("Expected " + type + " but got: " + t.text);
        }
    }

    private Command parseCreateTable(SimpleTokenizer tk) {
        Token name = tk.next();
        if (name.type != Token.Type.IDENT) throw new RuntimeException("Table name expected");
        Token lpar = tk.next();
        if (lpar.type != Token.Type.LPAREN) throw new RuntimeException("Expected '('");
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        while (true) {
            Token col = tk.next();
            if (col.type != Token.Type.IDENT) throw new RuntimeException("Column name expected");
            Token typ = tk.next();
            if (typ.type != Token.Type.KEYWORD) throw new RuntimeException("Type expected");
            cols.put(col.text, typ.text);
            Token nxt = tk.next();
            if (nxt.type == Token.Type.COMMA) continue;
            if (nxt.type == Token.Type.RPAREN) break;
        }
        TableSchema s = new TableSchema();
        s.name = name.text;
        s.columns = cols;
        return new CreateTableCommand(catalog, s);
    }

    private Command parseInsert(SimpleTokenizer tk) {
        expectKeyword(tk,"INTO");
        Token name = tk.next();
        expectKeyword(tk,"VALUES");

        Token lpar = tk.next();
        if (lpar.type != Token.Type.LPAREN) throw new RuntimeException("Expected '('");

        List<String> values = new ArrayList<>();

        while (true) {
            Token val = tk.next();

            // Stop if unexpected end
            if (val.type == Token.Type.EOF)
                throw new RuntimeException("Unexpected end of input in VALUES list");

            // Add the value (string/number)
            if (val.type == Token.Type.STRING || val.type == Token.Type.NUMBER) {
                values.add(val.text);
            } else {
                throw new RuntimeException("Expected value but got: " + val.text);
            }

            Token next = tk.next();

            if (next.type == Token.Type.COMMA) {
                // continue parsing next value
                continue;
            } else if (next.type == Token.Type.RPAREN) {
                break;
            } else {
                throw new RuntimeException("Expected ',' or ')' but got: " + next.text);
            }
        }

        // Optional semicolon at end
        Token semi = tk.next();
        if (semi.type != Token.Type.SEMI && semi.type != Token.Type.EOF) {
            throw new RuntimeException("Expected ';' or end of statement");
        }

        return new InsertCommand(catalog, name.text, values);
    }


    private Command parseSelect(SimpleTokenizer tk) {
        // SELECT * FROM table
        Token star = tk.next();
        Token from = tk.next();
        if (!from.text.equals("FROM")) throw new RuntimeException("Expected FROM");
        Token tbl = tk.next();
        return new SelectCommand(catalog, tbl.text);
    }
    private Command parseUpdate(SimpleTokenizer tk){
        Token table = tk.next();
        Token tableName = tk.next();
        if(tableName.type != Token.Type.IDENT) throw new RuntimeException("Table name expected");
        expectKeyword(tk,"SET");

        Map<String,String> updates = new LinkedHashMap<>();

        while(true){
            Token col = tk.next();
            if(col.type != Token.Type.IDENT) throw new RuntimeException("Column name expected");
            Token eq = tk.next();
            if(eq.type != Token.Type.EQUAL) throw new RuntimeException("Expected '='");
            Token val = tk.next();
            if(val.type != Token.Type.STRING && val.type != Token.Type.NUMBER) throw new RuntimeException("Value expected");
            updates.put(col.text,val.text);
            Token nxt = tk.next();
            if(nxt.type == Token.Type.COMMA) continue;
            if(nxt.type == Token.Type.KEYWORD && nxt.text.equals("WHERE")) break;
            if (nxt.type == Token.Type.SEMI || nxt.type == Token.Type.EOF) break;
        }

        Token whereCol = tk.next();
        if (whereCol.type == Token.Type.SEMI || whereCol.type == Token.Type.EOF) {
            return new UpdateCommand(catalog,tableName.text,updates,"","");
        }
        Token eq = tk.next();
        if(!eq.text.equals("=")) throw new RuntimeException("Expected '='");
        Token whereVal = tk.next();

        Token semi = tk.next();
        if (semi.type != Token.Type.SEMI && semi.type != Token.Type.EOF) {
            throw new RuntimeException("Expected ';' or end of statement");
        }
        return new UpdateCommand(catalog,tableName.text,updates,whereCol.text,whereVal.text);
    }
    private Command parseDelete(SimpleTokenizer tk){
        expectKeyword(tk,"FROM");
        Token tableName = tk.next();
        if(tableName.type != Token.Type.IDENT) throw new RuntimeException("Table name expected");

        Token whereToken = tk.next();
        if (whereToken.type == Token.Type.SEMI || whereToken.type == Token.Type.EOF) {
            return new DeleteCommand(catalog,tableName.text,"","");
        }
        if(!whereToken.text.equals("WHERE")) throw new RuntimeException("Expected WHERE");

        Token whereCol = tk.next();
        Token eq = tk.next();
        if(!eq.text.equals("=")) throw new RuntimeException("Expected '='");
        Token whereVal = tk.next();

        Token semi = tk.next();
        if (semi.type != Token.Type.SEMI && semi.type != Token.Type.EOF) {
            throw new RuntimeException("Expected ';' or end of statement");
        }
        return new DeleteCommand(catalog,tableName.text,whereCol.text,whereVal.text);
    }
    private Command parseCreateIndex(SimpleTokenizer tk) {
        Token indexName = tk.next(); // optional index name, can ignore
        expectKeyword(tk, "ON");
        Token table = tk.next();
        Token lpar = tk.next();
        if (lpar.type != Token.Type.LPAREN) throw new RuntimeException("Expected '('");
        Token col = tk.next();
        if (col.type != Token.Type.IDENT) throw new RuntimeException("Column name expected");
        Token rpar = tk.next();
        if (rpar.type != Token.Type.RPAREN) throw new RuntimeException("Expected ')'");
        return new CreateIndexCommand(catalog, table.text, col.text);
    }


}
