package lexer;

import java.util.List;

public class TokenizedLine {
    private final List<Token> tokens;

    private final int lineNumber;

    public int getLineNumber() {
        return lineNumber;
    }

    private final String initialLine;

    public TokenizedLine(List<Token> tokens, String line, int lineNumber){
        this.tokens = tokens;
        this.initialLine = line;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        if(tokens.isEmpty()) return this.lineNumber + ")";
        StringBuilder result = new StringBuilder();
        result.append(this.lineNumber).append(") ").append(initialLine.trim()).append('\n');
        for(int i = 0; i < tokens.size(); i++){
            String tokenDefinition = String.format("%" + tokens.stream().mapToInt(map -> map.getContent().length()).max().getAsInt() + "s", tokens.get(i).getContent()) + " - " + tokens.get(i).getType().toString();
            result.append('\t').append(String.format("%2d", (i + 1)).replace(' ', '0')).append(") ").append(tokenDefinition).append("\n");
        }
        return result.toString();
    }

    public List<Token> getTokens(){
        return this.tokens;
    }
}
