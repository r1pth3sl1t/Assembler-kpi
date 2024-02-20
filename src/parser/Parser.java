package parser;

import lexer.Token;
import lexer.TokenType;
import lexer.TokenizedLine;
import parser.unit.Identifier;
import parser.unit.Mnemonic;
import parser.unit.operand.*;
import util.UtilTables;


import java.util.LinkedList;
import java.util.List;

/*
*
*
*
* */

public final class Parser {

    public Parser(){

    }

    private boolean stopParsing = false;

    private Sentence startingWithIdentifier(Sentence sentence) throws ParserException{

        if(sentence.getTokenizedLine().getTokens().size() >= 2) {
            Identifier identifier = new Identifier(sentence.getTokenizedLine().getTokens().get(0));
            if(sentence.getTokenizedLine().getTokens().get(1).getType() == TokenType.T_COLON) {
                identifier.setFirstTokenIdx(0);
                identifier.setTokensNumber(2);
                identifier.setType(UtilTables.IdentifierType.LABEL);
                sentence.setIdentifier(identifier);
                UtilTables.identifiersTable.put(identifier.getName(), identifier);

                if(sentence.getTokenizedLine().getTokens().size() > 2) {
                    if(sentence.getTokenizedLine().getTokens().get(2).getType() == TokenType.T_INSTRUCTION) {
                        this.processInstruction(sentence, 2);
                    }
                    else if(sentence.getTokenizedLine().getTokens().get(2).getType() == TokenType.T_DIRECTIVE) {
                        this.processDirective(sentence, 2);
                    }
                    else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Instruction | Directive expected");
                }

            }
            else if(sentence.getTokenizedLine().getTokens().get(1).getType() == TokenType.T_DIRECTIVE){
                identifier.setFirstTokenIdx(0);
                identifier.setTokensNumber(1);
                sentence.setIdentifier(identifier);
                this.processDirective(sentence, 1);
            }
            else throw  new ParserException(sentence.getTokenizedLine().getLineNumber(), "Directive expected");
        }
        else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Sentence must start of label, identifier or mnemonic");
        return sentence;
    }

    private Sentence processDirective(Sentence sentence, int index) throws ParserException {
        switch(sentence.getTokenizedLine().getTokens().get(index).getContent().toUpperCase()) {
            case "END" -> {

                if(index != 0) {
                    throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "END should be only one token in line");
                }
                else {
                    Mnemonic mnemonic = new Mnemonic(sentence.getTokenizedLine().getTokens().get(index));
                    mnemonic.setFirstTokenIdx(index);
                    mnemonic.setTokensNumber(1);
                    sentence.setMnemonic(mnemonic);
                    stopParsing = true;
                }
            }
            case "DB", "DW", "DD" -> {
                Mnemonic mnemonic = new Mnemonic(sentence.getTokenizedLine().getTokens().get(index));
                mnemonic.setFirstTokenIdx(index);
                mnemonic.setTokensNumber(1);
                sentence.setMnemonic(mnemonic);
                index++;
                this.processDXDirective(sentence, index);
            }
            case "EQU" -> {
                Mnemonic mnemonic = new Mnemonic(sentence.getTokenizedLine().getTokens().get(index));
                mnemonic.setFirstTokenIdx(index);
                mnemonic.setTokensNumber(1);
                sentence.setMnemonic(mnemonic);
                index++;
                this.processEquDirective(sentence, index);
            }
            case "SEGMENT","ENDS" -> {
                if(index == 0) {
                    throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Segment should have name");
                }
                if(index != sentence.getTokenizedLine().getTokens().size() - 1) {
                    throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "End of line expected");
                }
                else {
                    Mnemonic mnemonic = new Mnemonic(sentence.getTokenizedLine().getTokens().get(index));
                    mnemonic.setTokensNumber(1);
                    mnemonic.setFirstTokenIdx(index);
                    sentence.setMnemonic(mnemonic);
                }
            }
            default -> throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Couldn't process directive");
        }
        return sentence;
    }

    private void processDXDirective(Sentence sentence, int index) throws ParserException {
        if(sentence.getTokenizedLine().getTokens().size() - 2 > index) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Too many tokens for define operand");
        Operand operand;
        boolean onlyDB = false;
        if(sentence.getTokenizedLine().getTokens().get(index).getType() == TokenType.T_STRING) {
            operand = new StringLiteralOperand(sentence.getTokenizedLine().getTokens().get(index));
            operand.setFirstTokenIdx(index);
            operand.setTokensNumber(1);

            if(index != sentence.getTokenizedLine().getTokens().size() - 1)
                throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Too many tokens for string declaration");

            sentence.setOperands(List.of(operand));
            onlyDB = true;
        }
        else {
            List<Token> tokens = new LinkedList<>();
            int startIndex = index;
            if (sentence.getTokenizedLine().getTokens().get(index).getType() == TokenType.T_MINUS) {
                tokens.add(sentence.getTokenizedLine().getTokens().get(index++));
            }
            switch (sentence.getTokenizedLine().getTokens().get(index).getType()) {
                case T_BIN, T_DEC, T_HEX -> {
                    tokens.add(sentence.getTokenizedLine().getTokens().get(index));
                    operand = new ImmOperand(tokens);
                    operand.setFirstTokenIdx(startIndex);
                    operand.setTokensNumber(index - startIndex + 1);
                }
                default ->
                        throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Token is not allowed as " + sentence.getMnemonic().getName() + " operand");

            }
            if(index != sentence.getTokenizedLine().getTokens().size() - 1)
                throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Too many tokens for define operand");
        }
        switch(sentence.getMnemonic().getName()) {
            case "DB" -> {
                if(!onlyDB && operand.getSize() > 8) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Size is too big");
                operand.setSize(8);
                if(sentence.getIdentifier() != null) sentence.getIdentifier().setType(UtilTables.IdentifierType.BYTE);
                sentence.setOperands(List.of(operand));
            }
            case "DW"  -> {
                if(onlyDB) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "String literals are allowed only for DB");
                if(operand.getSize() > 16) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Size is too big");
                operand.setSize(16);
                if(sentence.getIdentifier() != null) sentence.getIdentifier().setType(UtilTables.IdentifierType.WORD);
                sentence.setOperands(List.of(operand));
            }
            case "DD" -> {
                if(onlyDB) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "String literals are allowed only for DB");
                if(operand.getSize() > 32) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Size is too big");
                operand.setSize(32);
                if(sentence.getIdentifier() != null) sentence.getIdentifier().setType(UtilTables.IdentifierType.DWORD);
                sentence.setOperands(List.of(operand));
            }
            default -> throw new ParserException(sentence.getTokenizedLine().getLineNumber(),"Unexpected token");
        }
        if(sentence.getIdentifier() != null) UtilTables.identifiersTable.put(sentence.getIdentifier().getName(), sentence.getIdentifier());
    }

    /*
    *
    */
    private void processEquDirective(Sentence sentence, int index) {
        EquDirectiveOperand equOperand;
        Token equSub;
        if(sentence.getTokenizedLine().getTokens().size() > index) {
            equSub = sentence.getTokenizedLine().getTokens().get(index);
            equOperand = new EquDirectiveOperand(equSub);
            equOperand.setTokensNumber(1);
            equOperand.setFirstTokenIdx(index);
            sentence.setOperands(List.of(equOperand));
            if(sentence.getIdentifier() != null) {
                UtilTables.equReplacements.put(sentence.getIdentifier().getName(), equSub.getContent());
            }
        }
        else {
            if(sentence.getIdentifier() != null) {
                UtilTables.equReplacements.put(sentence.getIdentifier().getName(), "");
            }
        }
    }

    private int processOperand(Sentence sentence, int index) throws ParserException {
        List<Token> tokens = sentence.getTokenizedLine().getTokens();
        switch(tokens.get(index).getType()){
            case T_GENERAL_REG08, T_GENERAL_REG32 -> {
                RegisterOperand operand = new RegisterOperand(tokens.get(index));
                operand.setFirstTokenIdx(index);
                operand.setTokensNumber(1);
                sentence.pushOperand(operand);
                index++;
            }
            case T_OPEN_BRACKET, T_SEGMENT_REG, T_TYPE, T_IDENTIFIER ->
                    index = this.processMemoryReference(sentence, index);
            case T_OPEN_PARENTHESIS, T_HEX, T_BIN, T_DEC, T_MINUS ->
                index = this.processImmOperand(sentence, index);
            default -> throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Operand expected but " + tokens.get(index).getContent() + " found");
        }
        return index;
    }

    private int processDirectAddressing(Sentence sentence, int index, int sizeOfOperators, String segmentSubstitution, int size)
            throws ParserException
    {
        if(sentence.getTokenizedLine().getTokens().get(index).getType() != TokenType.T_IDENTIFIER)
            throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Identifier | address expression expected");
        DirectMemReferenceOperand operand = new DirectMemReferenceOperand(sentence.getTokenizedLine().getTokens().get(index));
        operand.setSize(size);
        operand.setSegmentOverride(segmentSubstitution);
        operand.setFirstTokenIdx(index - sizeOfOperators);
        operand.setTokensNumber(sizeOfOperators + 1);
        sentence.pushOperand(operand);
        return index + 1;
    }

    private Sentence processInstruction(Sentence sentence, int index) throws ParserException{
        List<Token> tokens = sentence.getTokenizedLine().getTokens();
        Mnemonic mnemonic = new Mnemonic(tokens.get(index));
        mnemonic.setTokensNumber(1);
        mnemonic.setFirstTokenIdx(index);
        sentence.setMnemonic(mnemonic);
        if(tokens.size() > index + 1) {
            int startOpIndex = index + 1;
            while(startOpIndex < tokens.size()){
                startOpIndex = this.processOperand(sentence, startOpIndex);
                if(startOpIndex == tokens.size()) return sentence;
                if(tokens.get(startOpIndex).getType() == TokenType.T_COMMA) {
                    startOpIndex++;
                }
                else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Unexpected token: '" + tokens.get(startOpIndex).getContent() +"'");
            }
        }
        return sentence;
    }

    private int processImmOperand(Sentence sentence, int index) throws ParserException {
        List<Token> absoluteExpression = new LinkedList<>();
        int startIndex = index;
        while(index < sentence.getTokenizedLine().getTokens().size()){
            absoluteExpression.add(sentence.getTokenizedLine().getTokens().get(index++));
        }
        try {
            ImmOperand immOperand = new ImmOperand(absoluteExpression);
            immOperand.setFirstTokenIdx(startIndex);
            immOperand.setTokensNumber(absoluteExpression.size());
            sentence.pushOperand(immOperand);
            return index;
        } catch(ParserException e) {
            throw new ParserException(sentence.getTokenizedLine().getLineNumber(), e.getMessage());
        }
    }

    private int processMemoryReference(Sentence sentence, int index) throws ParserException {
        List<Token> tokens = sentence.getTokenizedLine().getTokens();
        MemReferenceOperand memReferenceOperand = new MemReferenceOperand();
        memReferenceOperand.setFirstTokenIdx(index);
        String segmentOverride = "DS";
        int size = 32;
        int sizeOfOperators = 0;
        boolean overridden = false;
        if(tokens.get(index).getType() == TokenType.T_SEGMENT_REG) {
            if (tokens.get(index + 1).getType() == TokenType.T_COLON) {
                segmentOverride = tokens.get(index).getContent();
                index += 2;
                sizeOfOperators += 2;
                overridden = true;
            }
            else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Segment register is not allowed in this context");
        }
        if(tokens.get(index).getType() == TokenType.T_TYPE) {
            if(tokens.get(index + 1).getType() == TokenType.T_PTR) {
                size = switch (sentence.getTokenizedLine().getTokens().get(index).getContent()) {
                    case "BYTE" -> 8;
                    case "DWORD" -> 32;
                    default -> throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Illegal type");
                };
                index += 2;
                sizeOfOperators += 2;
            }
            else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "'PTR' expected");
        }
        if(tokens.get(index).getType() == TokenType.T_SEGMENT_REG) {
            if(overridden) throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Multiple segment override");
            if (tokens.get(index + 1).getType() == TokenType.T_COLON) {
                segmentOverride = tokens.get(index).getContent();
                index += 2;
                sizeOfOperators += 2;
            }
            else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "Segment register is not allowed in this context");
        }

        if(tokens.get(index).getType() == TokenType.T_OPEN_BRACKET) {
            index++;

            if(tokens.get(index).getType() == TokenType.T_GENERAL_REG32) {
                if(segmentOverride.isEmpty() && tokens.get(index).getContent().equals("EBP")) segmentOverride = "SS";

                memReferenceOperand.setBase(sentence.getTokenizedLine().getTokens().get(index).getContent());
            }
            else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "General 32-bit register expected");

            index++;
            if(tokens.get(index).getType() == TokenType.T_PLUS) {
                index++;
                if(sentence.getTokenizedLine().getTokens().get(index).getContent().equals("ESP"))
                    throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "ESP is not allowed as index register");
                if(tokens.get(index).getType() == TokenType.T_GENERAL_REG32) {
                    memReferenceOperand.setIndex(sentence.getTokenizedLine().getTokens().get(index).getContent());
                }
                else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "General 32-bit register expected");
            }
            else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "'+' expected");

            index++;

            if(tokens.get(index).getType() == TokenType.T_PLUS || tokens.get(index).getType() == TokenType.T_MINUS) {
                    int sign = 1;
                    if(tokens.get(index).getType() == TokenType.T_MINUS) {
                        sign = -1;
                    }
                    index++;
                    int disp = 0;
                    if(!(tokens.get(index).getType() == TokenType.T_BIN || tokens.get(index).getType() == TokenType.T_HEX ||
                        tokens.get(index).getType() == TokenType.T_DEC))
                        throw new ParserException(sentence.getTokenizedLine().getLineNumber(),"Imm expected");
                    else disp = UtilTables.immPool.get(tokens.get(index++).getContent()) * sign;

                    if(index >= tokens.size() || tokens.get(index).getType() != TokenType.T_CLOSE_BRACKET)
                        throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "']' expected");
                    memReferenceOperand.setDisplacement(disp);
            }
            else throw new ParserException(sentence.getTokenizedLine().getLineNumber(), "'+' | '-' expected");

        }
        else {
            return this.processDirectAddressing(sentence, index, sizeOfOperators, segmentOverride, size);
        }
        memReferenceOperand.setSize(size);
        memReferenceOperand.setSegmentOverride(segmentOverride);
        memReferenceOperand.setName();
        memReferenceOperand.setTokensNumber(index - memReferenceOperand.getFirstTokenIdx() + 2);
        sentence.pushOperand(memReferenceOperand);
        return index + 1;
    }

    public Sentence parseLine(TokenizedLine inputLine) throws ParserException {
        if(stopParsing) return null;

        if(inputLine.getTokens().isEmpty()) {
            return new Sentence(inputLine.getLineNumber());
        }

           return switch (inputLine.getTokens().get(0).getType()) {
                case T_IDENTIFIER -> this.startingWithIdentifier(new Sentence(inputLine));

                case T_INSTRUCTION -> this.processInstruction(new Sentence(inputLine), 0);

                case T_DIRECTIVE -> this.processDirective(new Sentence(inputLine), 0);

                default -> throw new ParserException(inputLine.getLineNumber(), "This token is not allowed at the beginning of line: '" + inputLine.getTokens().get(0).getContent() + "'");

           };
    }
}