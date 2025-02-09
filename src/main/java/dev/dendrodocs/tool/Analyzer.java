package dev.dendrodocs.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.TypeSolverBuilder;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.SourceRoot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import dev.dendrodocs.tool.descriptions.Description;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaException;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.Validator;

/**
 * Analyzer executes {@link AnalysisJob} instances. It walks the given source directory, parses each
 * Java file, then uses the {@link AnalysisVisitor} to combine the resulting parse trees into a JSON
 * output that follows LivingDocumentation conventions.
 */
public class Analyzer {
    private final ObjectMapper objectMapper;

    /** Constructs an Analyzer with the given Jackson ObjectMapper. */
    public Analyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a type solver that can resolve from neighboring source code, the classpath provided in
     * the {@link AnalysisJob}, and by reflection (which is required to resolve types like Object).
     */
    private TypeSolver typeSolverFor(AnalysisJob job) throws IOException {
        var typeSolverBuilder = new TypeSolverBuilder();

        for (String cp : job.classpath()) {
            if (cp.toLowerCase().endsWith(".jar")) {
                typeSolverBuilder = typeSolverBuilder.withJAR(cp);
            }
        }

        return typeSolverBuilder.withCurrentClassloader().withSourceCode(job.project()).build();
    }

    private Boolean validateJson(List<Description> descriptions) {
        try {
            Schema schema = new SchemaStore().loadSchema(
                    this.getClass().getResource("/schema.json"));
            new Validator().validate(schema, new ObjectMapper().convertValue(descriptions, Object.class));
        } catch (SchemaException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** Execute the given job. An exception is thrown if a file can not be read or parsed. */
    public void analyze(AnalysisJob job) throws IOException {
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolverFor(job)));
        CollectionStrategy strategy = new SymbolSolverCollectionStrategy(parserConfiguration);
        List<Description> descriptions = new ArrayList<>();

        for (SourceRoot sourceRoot : strategy.collect(job.project()).getSourceRoots()) {
            List<ParseResult<CompilationUnit>> results = sourceRoot.tryToParse();
            SymbolResolver solver = sourceRoot.getParserConfiguration().getSymbolResolver().orElseThrow();

            for (ParseResult<CompilationUnit> result : results) {
                CompilationUnit compilationUnit = result.getResult().orElseThrow();
                AnalysisVisitor visitor = new AnalysisVisitor(solver);

                List<Description> visited = compilationUnit.accept(visitor, this);
                descriptions.addAll(visited);
            }
        }

        ObjectWriter writer = job.pretty()
                ? objectMapper.writerWithDefaultPrettyPrinter()
                : objectMapper.writer();
        if (validateJson(descriptions)) {
            writer.writeValue(job.output().toFile(), descriptions);
        }
    }
}
