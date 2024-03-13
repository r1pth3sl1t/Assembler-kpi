package parser;

import lexer.Token;
import lexer.TokenType;
import lexer.TokenizedLine;
import parser.unit.Identifier;
import parser.unit.Mnemonic;
import parser.unit.operand.*;
import util.UtilTables;

import java.util.List;

public final class Parser {

    public Parser(){

    }

    private boolean stopParsing = false;

    private Sentence startingWithIdentifier(Sentence sentence) throws ParserException{

        if(sentence.getTokenizedLine().getTokens().size() >= 2) {
            Identifier identifier = new Identifier(sentence.getTokenizedLine().getTokens().get(0));
            if(sentence.getTokenizedLine().getTokens().get(1).type() == TokenType.T_COLON) {
                identifier.setFirstTokenIdx(0);
                identifier.setTokensNumber(2);
                identifier.setType(UtilTables.IdentifierType.NEAR);
                sentence.setIdentifier(identifier);

                if(sentence.getTokenizedLine().getTokens().size() > 2) {
                    if(sentence.getTokenizedLine().getTokens().get(2).type() == TokenType.T_INSTRUCTION) {
                        this.processInstruction(sentence, 2);
                    }
                    else if(sentence.getTokenizedLine().getTokens().get(2).type() == TokenType.T_DIRECTIVE) {
                        this.processDirective(sentence, 2);
                    }
                    else throw new ParserException("Instruction | Directive expected");
                }

            }
            else if(sentence.getTokenizedLine().getTokens().get(1).type() == TokenType.T_DIRECTIVE){
                identifier.setFirstTokenIdx(0);
                identifier.setTokensNumber(1);
                sentence.setIdentifier(identifier);
                this.processDirective(sentence, 1);
            }
            else throw  new ParserException("Directive expected");
        }
        else throw new ParserException("Sentence must start of label, identifier with mnemonic, or mnemonic");
        return sentence;
    }

    private Sentence processDirective(Sentence sentence, int index) throws ParserException {
        switch(sentence.getTokenizedLine().getTokens().get(index).content().toUpperCase()) {
            case "END" -> {

                if(index != 0) {
                    throw new ParserException("END should be only one token in line");
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
                if(index != sentence.getTokenizedLine().getTokens().size() - 1) {
                    throw new ParserException("End of line expected");
                }
                Mnemonic mnemonic = new Mnemonic(sentence.getTokenizedLine().getTokens().get(index));
                mnemonic.setTokensNumber(1);
                mnemonic.setFirstTokenIdx(index);
                sentence.setMnemonic(mnemonic);
                if(sentence.getIdentifier() != null) sentence.getIdentifier().setType(UtilTables.IdentifierType.SEGMENT);

            }
            default -> throw new ParserException("Directive not supported");
        }
        return sentence;
    }

    private void processDXDirective(Sentence sentence, int index) throws ParserException {
        if(sentence.getTokenizedLine().getTokens().size() - 2 > index) throw new ParserException("Too many tokens for define operand");
        Operand operand;
        boolean onlyDB = false;
        if(index == sentence.getTokenizedLine().getTokens().size())
            throw new ParserException("Expected operands");
        if(sentence.getTokenizedLine().getTokens().get(index).type() == TokenType.T_STRING) {
            operand = new StringLiteralOperand(sentence.getTokenizedLine().getTokens().get(index));
            operand.setFirstTokenIdx(index);
            operand.setTokensNumber(1);

            if(index != sentence.getTokenizedLine().getTokens().size() - 1)
                throw new ParserException("Too many tokens for string declaration");

            sentence.setOperands(List.of(operand));
            onlyDB = true;
        }
        else {
            int startIndex = index;
            boolean negative = false;
            if (sentence.getTokenizedLine().getTokens().get(index).type() == TokenType.T_MINUS) {
                negative = true;
                index++;
            }
            switch (sentence.getTokenizedLine().getTokens().get(index).type()) {
                case T_BIN, T_DEC, T_HEX -> {
                    operand = new ImmOperand(sentence.getTokenizedLine().getTokens().get(index), negative, true);
                    operand.setFirstTokenIdx(startIndex);
                    operand.setTokensNumber(index - startIndex + 1);
                }
                default ->
                        throw new ParserException("Token is not allowed as " + sentence.getMnemonic().getName() + " operand");

            }
            if(index != sentence.getTokenizedLine().getTokens().size() - 1)
                throw new ParserException("Too many tokens for define operand");
        }
        switch(sentence.getMnemonic().getName()) {
            case "DB" -> {
                if(!onlyDB && operand.getSize() > 1) throw new ParserException("Size out of range (0-0xff)");
                if(!onlyDB) operand.setSize(1);
                if(sentence.getIdentifier() != null && sentence.getIdentifier().getType() == null)
                    sentence.getIdentifier().setType(UtilTables.IdentifierType.BYTE);
                sentence.setOperands(List.of(operand));
            }
            case "DW"  -> {
                if(onlyDB) throw new ParserException("String literals are allowed only for DB");
                if(operand.getSize() > 2) throw new ParserException("Size out of range (0-0xffff)");
                operand.setSize(2);
                if(sentence.getIdentifier() != null && sentence.getIdentifier().getType() == null)
                    sentence.getIdentifier().setType(UtilTables.IdentifierType.WORD);
                sentence.setOperands(List.of(operand));
            }
            case "DD" -> {
                if(onlyDB) throw new ParserException("String literals are allowed only for DB");
                operand.setSize(4);
                if(sentence.getIdentifier() != null && sentence.getIdentifier().getType() == null)
                    sentence.getIdentifier().setType(UtilTables.IdentifierType.DWORD);
                sentence.setOperands(List.of(operand));
            }
            default -> throw new ParserException("Unexpected token");
        }
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
            if(sentence.getIdentifier() != null && sentence.getIdentifier().getType() == null) {
                sentence.getIdentifier().setType(UtilTables.IdentifierType.EQU);
                UtilTables.equReplacements.put(sentence.getIdentifier().getName(), equSub.content());
            }
        }
        else {
            if(sentence.getIdentifier() != null && sentence.getIdentifier().getType() == null) {
                sentence.getIdentifier().setType(UtilTables.IdentifierType.EQU);
                UtilTables.equReplacements.put(sentence.getIdentifier().getName(), "");
            }
        }
    }

    private int processOperand(Sentence sentence, int index) throws ParserException {
        List<Token> tokens = sentence.getTokenizedLine().getTokens();
        switch(tokens.get(index).type()){
            case T_GENERAL_REG08, T_GENERAL_REG32 -> {
                RegisterOperand operand = new RegisterOperand(tokens.get(index));
                operand.setFirstTokenIdx(index);
                operand.setTokensNumber(1);
                sentence.pushOperand(operand);
                index++;
            }
            case T_BANNED_REG ->
                throw new ParserException("Illegal register");
            case T_OPEN_BRACKET, T_SEGMENT_REG, T_TYPE, T_IDENTIFIER ->
                    index = this.processMemoryReference(sentence, index);
            case T_HEX, T_BIN, T_DEC, T_MINUS, T_STRING ->
                index = this.processImmOperand(sentence, index);
            default -> throw new ParserException("Operand expected but '" + tokens.get(index).content() + "' found");
        }
        return index;
    }

    private int processDirectAddressing(Sentence sentence, int index, int sizeOfOperators, String segmentOverride, int size)
            throws ParserException
    {
        if(sentence.getTokenizedLine().getTokens().get(index).type() != TokenType.T_IDENTIFIER)
            throw new ParserException("Identifier | address expression expected");
        DirectMemReferenceOperand operand = new DirectMemReferenceOperand(sentence.getTokenizedLine().getTokens().get(index));
        operand.setSize(size);
        operand.setSegmentOverride(segmentOverride);
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
                if(tokens.get(startOpIndex).type() == TokenType.T_COMMA) {
                    startOpIndex++;
                }
                else throw new ParserException("Unexpected token: '" + tokens.get(startOpIndex).content() +"'");
            }
        }
        return sentence;
    }

    private int processImmOperand(Sentence sentence, int index) throws ParserException {

        boolean negative = false;
        if(sentence.getTokenizedLine().getTokens().get(index).type() == TokenType.T_MINUS) {
            negative = true;
            index++;
        }
        switch (sentence.getTokenizedLine().getTokens().get(index).type()) {
            case T_BIN, T_DEC, T_HEX -> {
                ImmOperand immOperand = new ImmOperand(sentence.getTokenizedLine().getTokens().get(index), negative);
                immOperand.setFirstTokenIdx(index);
                immOperand.setTokensNumber(negative ? 2 : 1);
                sentence.pushOperand(immOperand);
                index++;
            }
            case T_STRING -> {
                ImmOperand operand = new ImmOperand(sentence.getTokenizedLine().getTokens().get(index).content(), negative);
                operand.setFirstTokenIdx(index);
                operand.setTokensNumber(negative ? 2 : 1);
                sentence.pushOperand(operand);
                index++;
            }
            default ->
                    throw new ParserException("Only bin, dec, hex, text constanta allowed");

        }
        return index;
    }

    private int processMemoryReference(Sentence sentence, int index) throws ParserException {
        List<Token> tokens = sentence.getTokenizedLine().getTokens();
        MemReferenceOperand memReferenceOperand = new MemReferenceOperand();
        memReferenceOperand.setFirstTokenIdx(index);
        String segmentOverride = "";
        int size = 0;
        int sizeOfOperators = 0;
        boolean overridden = false;
        try {
            if (tokens.get(index).type() == TokenType.T_SEGMENT_REG) {
                if (tokens.get(index + 1).type() == TokenType.T_COLON) {
                    segmentOverride = tokens.get(index).content();
                    index += 2;
                    sizeOfOperators += 2;
                    overridden = true;
                } else throw new ParserException("Segment register is not allowed in this context");
            }
            if (tokens.get(index).type() == TokenType.T_TYPE) {
                if (tokens.get(index + 1).type() == TokenType.T_PTR) {
                    size = switch (sentence.getTokenizedLine().getTokens().get(index).content()) {
                        case "BYTE" -> 8;
                        case "DWORD" -> 32;
                        default -> throw new ParserException("Illegal type");
                    };
                    index += 2;
                    sizeOfOperators += 2;
                } else throw new ParserException("'PTR' expected");
            }
            if (tokens.get(index).type() == TokenType.T_SEGMENT_REG) {
                if (overridden) throw new ParserException("Multiple segment override");
                if (tokens.get(index + 1).type() == TokenType.T_COLON) {
                    segmentOverride = tokens.get(index).content();
                    index += 2;
                    sizeOfOperators += 2;
                } else throw new ParserException("Segment register is not allowed in this context");
            }

            if (tokens.get(index).type() == TokenType.T_OPEN_BRACKET) {
                index++;

                if (tokens.get(index).type() == TokenType.T_GENERAL_REG32) {
                    memReferenceOperand.setBase(sentence.getTokenizedLine().getTokens().get(index).content());
                } else throw new ParserException("General 32-bit register expected");

                index++;
                if (tokens.get(index).type() == TokenType.T_PLUS) {
                    index++;
                    if (sentence.getTokenizedLine().getTokens().get(index).content().equals("ESP"))
                        throw new ParserException("ESP is not allowed as index register");
                    if (tokens.get(index).type() == TokenType.T_GENERAL_REG32) {
                        memReferenceOperand.setIndex(sentence.getTokenizedLine().getTokens().get(index).content());
                    } else throw new ParserException("General 32-bit register expected");
                } else throw new ParserException("'+' expected");

                index++;

                if (tokens.get(index).type() == TokenType.T_PLUS || tokens.get(index).type() == TokenType.T_MINUS) {
                    int sign = 1;
                    if (tokens.get(index).type() == TokenType.T_MINUS) {
                        sign = -1;
                    }
                    index++;
                    int displacement;
                    if (!(tokens.get(index).type() == TokenType.T_BIN || tokens.get(index).type() == TokenType.T_HEX ||
                            tokens.get(index).type() == TokenType.T_DEC))
                        throw new ParserException("Imm expected");
                    else displacement = UtilTables.immPool.get(tokens.get(index++).content()) * sign;

                    if (index >= tokens.size() || tokens.get(index).type() != TokenType.T_CLOSE_BRACKET)
                        throw new ParserException("']' expected");
                    memReferenceOperand.setDisplacement(displacement);
                } else throw new ParserException("'+' | '-' expected");

            } else {
                index = this.processDirectAddressing(sentence, index, sizeOfOperators, segmentOverride, size);
                return index;
            }
            memReferenceOperand.setSize(size);
            if (!segmentOverride.isEmpty()) memReferenceOperand.setSegmentOverride(segmentOverride);
            memReferenceOperand.setName();
            memReferenceOperand.setTokensNumber(index - memReferenceOperand.getFirstTokenIdx() + 2);
            sentence.pushOperand(memReferenceOperand);
            return index + 1;
        }
        catch (IndexOutOfBoundsException e){
            throw new ParserException("Unexpected end of line");
        }
    }

    public Sentence parseLine(TokenizedLine inputLine) {
        if(stopParsing) return null;
        Sentence sentence = new Sentence(inputLine);

        if(sentence.hasErrors()) return sentence;

        if (inputLine.getTokens().isEmpty()) {
            return sentence;
        }
        try {
            return switch (inputLine.getTokens().get(0).type()) {
                case T_IDENTIFIER -> this.startingWithIdentifier(sentence);
                case T_INSTRUCTION -> this.processInstruction(sentence, 0);
                case T_DIRECTIVE -> this.processDirective(sentence, 0);
                default ->
                        throw new ParserException("This token is not allowed at the beginning of line: '" + inputLine.getTokens().get(0).content() + "'");

            };
        }
        catch(ParserException e) {
            UtilTables.errors++;
            ParserException parserException = new ParserException(inputLine.getLineNumber(), e.getMessage());
            System.err.println(parserException.getMessage());
            sentence.setException(parserException);
        }
        return sentence;
    }


}