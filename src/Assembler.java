
import codegen.Allocator;
import lexer.Lexer;
import lexer.TokenizedLine;
import parser.Parser;
import parser.Sentence;
import util.UtilTables;

import java.io.*;
import java.util.*;

public class Assembler {
    private final Lexer lexer;
    private final Parser parser;

    private List<String> lines;
    private final List<Sentence> sentences;

    private final Map<String, String> options;
    private Assembler(String parserOutputFile,
                      String firstRunCompiler,
                      String secondRunCompiler,
                      String sourceFilePath){
        this.options = new HashMap<>();
        this.options.put("Parser", parserOutputFile);
        this.options.put("FirstRun", firstRunCompiler);
        this.options.put("SecondRun", secondRunCompiler);
        this.sentences = new LinkedList<>();

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
        long timeStart = Calendar.getInstance().getTimeInMillis();
        for(String line : lines){
            TokenizedLine tokenizedLine = lexer.tokenizeString(line, lineNum++);
            Sentence sentence = parser.parseLine(tokenizedLine);
            if(sentence != null) sentences.add(sentence);
        }

        Allocator allocator = new Allocator(sentences);
        allocator.allocate();
        long timeStop = Calendar.getInstance().getTimeInMillis();
        if(this.options.get("Parser") != null) {
            try (PrintWriter writer = new PrintWriter(this.options.get("Parser"))) {
                for (Sentence sentence : sentences) {
                    writer.println(sentence.toString());
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        if(this.options.get("FirstRun") != null) {
            try (PrintWriter writer = new PrintWriter(this.options.get("FirstRun"))) {
                allocator.firstRunListing(writer);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        System.out.println("Errors found: " + UtilTables.errors);
        System.out.println("Time of compilation: " + (timeStop - timeStart) / 1000.0 + "s");
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
