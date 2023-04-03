package ghaffarian.progex.cpp;

import ghaffarian.nanologger.Logger;
import ghaffarian.progex.cpp.paser.CPP14Lexer;
import ghaffarian.progex.cpp.paser.CPP14Parser;
import ghaffarian.progex.cpp.paser.CPP14ParserBaseVisitor;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.java.JavaASTBuilder;
import ghaffarian.progex.java.parser.JavaLexer;
import ghaffarian.progex.java.parser.JavaParser;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public class CPPASTBuilder {

    public static AbstractSyntaxTree build(String cppFile) throws IOException {
        return build(new File(cppFile));
    }
    public static AbstractSyntaxTree build(File cppFile) throws IOException {
        if (!cppFile.getName().endsWith(".cpp"))
            throw new IOException("Not a cpp File!");
        InputStream inFile = new FileInputStream(cppFile);

        CharStream stream = CharStreams.fromStream(inFile);
        CPP14Lexer lexer = new CPP14Lexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        ParseTree tree = parser.translationUnit();
        return build(cppFile.getPath(), tree);
    }
    public static AbstractSyntaxTree build(String filePath, ParseTree tree) {
        CPPASTBuilder.AbstractSyntaxVisitor visitor = new CPPASTBuilder.AbstractSyntaxVisitor(filePath);
        Logger.debug("Visitor building AST of: " + filePath);
        return visitor.build(tree);
    }

    public static class AbstractSyntaxVisitor extends CPP14ParserBaseVisitor<String> {

        private String typeModifier;
        private String memberModifier;
        private Deque<ASNode> parentStack;
        private final AbstractSyntaxTree AST;
        private Map<String, String> vars, fields, methods;
        private int varsCounter, fieldsCounter, methodsCounter;
        public AbstractSyntaxVisitor(String filePath) {
            parentStack = new ArrayDeque<>();
            AST = new AbstractSyntaxTree(filePath);
            vars = new LinkedHashMap<>();
            fields = new LinkedHashMap<>();
            methods = new LinkedHashMap<>();
            varsCounter = 0; fieldsCounter = 0; methodsCounter = 0;
        }

        public AbstractSyntaxTree build(ParseTree tree){
            CPP14Parser.TranslationUnitContext rootCntx = (CPP14Parser.TranslationUnitContext) tree;
            AST.root.setCode(new File(AST.filePath).getName());
            parentStack.push(AST.root);
            if(rootCntx.declarationseq()!= null){
                visit(rootCntx.declarationseq());
            }
            parentStack.pop();
            vars.clear();
            fields.clear();
            methods.clear();
            return AST;
        }

    }
}
