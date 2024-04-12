package codegen.java;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import utils.Box;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static magic.MagicImpls.getLiteral;

/*record Fi
record Output(List<Parser> files) {}

public class JavaFilesCodegen{
  JavaCodegen gen;
  
  public class JavaCodegen implements MIRVisitor<String> {
    // ...

    public void generateFiles() {
        // Create the "userCode" package directory
        String userCodeDir = "userCode";
        createDirectory(userCodeDir);

        // Generate the FearlessError and FAux classes
        String fearlessErrorContent = """
            package userCode;
            class FearlessError extends RuntimeException {
                // ...
            }
            class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
            """;
        writeFile(userCodeDir, "FearlessError.java", fearlessErrorContent);
        writeFile(userCodeDir, "FAux.java", fearlessErrorContent);

        // Generate files for each package
        for (MIR.Package pkg : p.pkgs()) {
            String pkgDir = getPkgName(pkg.name());
            createDirectory(pkgDir);

            // Generate files for each type definition
            for (MIR.TypeDef def : pkg.defs().values()) {
                String typeDefContent = visitTypeDef(pkg.name(), def, pkg.funs());
                String fileName = getBase(def.name().shortName()) + "_" + def.name().gen() + ".java";
                writeFile(pkgDir, fileName, typeDefContent);
            }
        }

        // Generate the main entry point file
        String entryContent = generateEntryPoint();
        writeFile("", "Main.java", entryContent);
    }

    private String generateEntryPoint() {
        // Generate the content of the main entry point file
        // ...
    }

    private void createDirectory(String dirName) {
        // Create the directory if it doesn't exist
        // ...
    }

    private void writeFile(String dirName, String fileName, String content) {
        // Write the content to the specified file in the given directory
        // ...
    }

    // ...
}
}*/