package lexer;

import java.util.Optional;

public class Token {

    private final TokenType type;
    private final String content;

    public Token(String content, TokenType type){
        this.content = content;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "content='" + content + '\'' + "type='" + type.toString() + '\'' +
                '}';
    }
    public String getContent(){ return content; }
    public TokenType getType(){ return type; }

}
