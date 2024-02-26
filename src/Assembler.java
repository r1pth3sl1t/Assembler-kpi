

import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.TokenizedLine;
import parser.Parser;
import parser.ParserException;
import parser.Sentence;
import util.UtilTables;

import java.io.*;
import java.util.*;

public class Assembler {
    private Lexer lexer;
    private Parser parser;

    private List<String> lines;
    private List<Sentence> sentences;
    private List<TokenizedLine> tokenizedLines;

    private Map<String, String> options;
    private Assembler(String parserOutputFile,
                      String firstRunCompiler,
                      String secondRunCompiler,
                      String sourceFilePath){
        this.options = new HashMap<>();
        this.options.put("Parser", parserOutputFile);
        this.options.put("FirstRun", firstRunCompiler);
        this.options.put("SecondRun", secondRunCompiler);
        this.sentences = new LinkedList<>();
        this.tokenizedLines = new LinkedList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath))){
            String line;
            this.lines = new LinkedList<>();
            while((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.err.println("Source file not found");
            System.exit(1);
        }
        this.lexer = new Lexer();
        this.parser = new Parser();
    }

    public void compile(){
        int lineNum = 1;
        int errorCnt = 0;
        for(String line : lines){
            TokenizedLine tokenizedLine = lexer.tokenizeString(line, lineNum++);
            tokenizedLines.add(tokenizedLine);
            sentences.add(parser.parseLine(tokenizedLine));
        }

        List<String> lexemesTable = tokenizedLines.stream().map(TokenizedLine::toString).toList();
        if(this.options.get("Parser") != null) {
            try (PrintWriter writer = new PrintWriter(new File(this.options.get("Parser")))) {
                for (int i = 0; i < sentences.size(); i++ ) {
                    writer.println(sentences.get(i).toString());
                }
                System.out.println("Errors found: " + UtilTables.errors);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }


    }

    public static AssemblerBuilder builder() {
        return new AssemblerBuilder();
    }

    public static class AssemblerBuilder {

        private String sourceFilePath;
        private String parserOutputFile;
        private String firstRunCompiler;
        private String secondRunCompiler;

        public AssemblerBuilder setSourceFilePath(String sourceFilePath) {
            this.sourceFilePath = sourceFilePath;
            return this;
        }

        public AssemblerBuilder setParserOutputFile(String parserOutputFile) {
            this.parserOutputFile = parserOutputFile;
            return this;
        }

        public AssemblerBuilder setFirstRunCompilerListing(String firstRunCompiler) {
            this.firstRunCompiler = firstRunCompiler;
            return this;
        }

        public AssemblerBuilder setSecondRunCompilerListing(String secondRunCompiler) {
            this.secondRunCompiler = secondRunCompiler;
            return this;
        }

        public Assembler build(){
            return new Assembler(
                    parserOutputFile,
                    firstRunCompiler,
                    secondRunCompiler,
                    sourceFilePath);
        }

        protected AssemblerBuilder(){}
    }
}
