package main;

import astFull.E;
import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import failure.CompileError;
import id.Id;
import program.TypeSystemFeatures;
import rt.NativeRuntime;
import utils.Box;
import utils.Bug;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

public class Main {
  private static CompilerFrontEnd frontEnd = null;
  public static boolean isUserInvoked() {
    return frontEnd != null;
  }

  public static void resetAll(){
    E.X.reset();
    Id.GX.reset();
  }

  public static void main(String[] args) {
    args = args.length > 0 ? args : new String[]{"--help"};
    var verbosity = new Box<>(new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None));
//This commented code was terrible anyway, please, divide in more methods and concepts....
     var cli = new CmdLineAppBuilder(args)
      .withJarName("fearless")
      .withDescription("The compiler for the Fearless programming language. See https://fearlang.org for more information.")
      .withArg("new", "Create a new package")
      .withArg("check", "c", "Check that the given Fearless program is valid")
      .withArg("run", "r", "Compile and run the given Fearless program")
        .withArg("entry-point", "The qualified name for the entry-trait that implements base.Main")
        .withDependencies("run", "entry-point")
      .withFlag("regenerate-aliases", "ra", "Print the default alias file for a new package to standard output")
      .withFlag("generate-docs", "d", "Generate documentation for the given Fearless program")
      .withFlag("imm-base", "Use a pure version of the Fearless standard library")
      .withFlag("show-internal-stack-traces", "Show stack traces within the compiler on errors for debugging purposes")
      .withFlag("print-codegen", "pc", "Print the output of the codegen stage to standard output")
      .withFlag("show-tasks", "sct", "Print progress messages showing the current task the compiler is performing.")
      .withFlag("show-full-progress", "sfp", "Print progress messages showing the current task and all sub-tasks the compiler is performing.")
      .withAtLeastOneRequiredOption("help", "new", "check", "run", "regenerate-aliases", "generate-docs")
      .withEntryPoint(res->{
        var bv = res.hasOption("imm-base") ? CompilerFrontEnd.BaseVariant.Imm : CompilerFrontEnd.BaseVariant.Std;

        CompilerFrontEnd.ProgressVerbosity pv = CompilerFrontEnd.ProgressVerbosity.None;
        if (res.hasOption("show-tasks")) { pv = CompilerFrontEnd.ProgressVerbosity.Tasks; }
        if (res.hasOption("show-full-progress")) { pv = CompilerFrontEnd.ProgressVerbosity.Full; }
        verbosity.set(new CompilerFrontEnd.Verbosity(
          res.hasOption("show-internal-stack-traces"),
          res.hasOption("print-codegen"),
          pv
        ));
        frontEnd = new CompilerFrontEnd(bv, verbosity.get(), new TypeSystemFeatures());

        if (res.hasOption("new")) {
          frontEnd.newPkg(res.getOptionValue("new"));
          return;
        }
        if (res.hasOption("check")) {
          frontEnd.check(res.getOptionValues("check"));
          return;
        }
        if (res.hasOption("run")) {
          frontEnd.run(res.getOptionValue("entry-point"), res.getOptionValues("run"), res.getArgList());
          return;
        }
        if (res.hasOption("generate-docs")) {
          frontEnd.generateDocs(res.getOptionValues("generate-docs"));
          return;
        }
        if (res.hasOption("regenerate-aliases")) {
          System.out.println(frontEnd.regenerateAliases());
          return;
        }
        throw Bug.unreachable();
      });


    try {
      cli.build().run();
    } catch (CompileError | IllegalStateException e) {
      if (verbosity.get().showInternalStackTraces()) {
        throw e;
      }
      System.err.println(e);
      System.exit(1);
    } catch (RuntimeException e) {
      if (verbosity.get().showInternalStackTraces()) {
        throw e;
      }
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      throw Bug.of(e);
    }
  }

//  private static Options buildCli() {
//    var res = new Options();
//    res.addOption(Option.builder()
//      .desc("Create a new Fearless package")
//      .longOpt("new")
//      .hasArg().numberOfArgs(1)
//      .argName("package name")
//      .optionalArg(false)
//      .build()
//    );
//    res.addOption(Option.builder()
//      .desc("Generate aliases for the standard library")
//      .longOpt("gen-aliases")
//      .optionalArg(true)
//      .argName("output path")
//      .build()
//    );
//    res.addOption(Option.builder()
//      .desc("Compile and run the provided fearless files")
//      .hasArgs()
//      .longOpt("run")
//    )
//
//    return res;
//  }
}
