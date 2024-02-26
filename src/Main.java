import util.UtilTables;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String path;
        if(args.length == 0) path = "resources/test1.asm";
        else path = args[0];

        Assembler.builder()
            .setSourceFilePath(path)
            .setParserOutputFile(path.substring(0, path.lastIndexOf("/") == -1 ? 0 : path.lastIndexOf("/")) + "/lexer.txt")
            .setFirstRunCompilerListing(path.replace(".asm", "") + ".lst")
            .setSecondRunCompilerListing(path.replace(".asm", "") + ".lst")
            .build()
            .compile();

        //System.out.println(UtilTables.identifiersTable);
        //System.out.println(UtilTables.equReplacements);
        //System.out.println(UtilTables.immPool);
    }
}