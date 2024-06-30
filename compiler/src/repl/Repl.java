package repl;

import program.Program;
import program.TypeSystemFeatures;
import parser.Parser;
import org.apache.commons.cli.*;

import failure.CompileError;

import java.util.*;
import java.nio.file.Path;

public class Repl {
	/**
	 * Tries to compile the given state.
	 */
	static Program compile(State state) throws CompileError {
		var ps = List.of(new Parser(Path.of("DummyREPL.fear"), state.asCode()));
		var res = Parser.parseAll(ps, TypeSystemFeatures.of());

		return res;
	}

	/**
	 * Return true if it compiles fine,
	 * false if CompileError
	 */
	static boolean isValid(State state) {
		try {
			compile(state);
			return true;
		} catch (CompileError e) {
			return false;
		}
	}

	static Options createOptions() {
		var options = new Options();

		options.addOption("h", "help", false, "Show this message");
		options.addOption("p", "package", true, "Set REPL's package name");
		options.addOption("r", "refresh", false, "Refresh program state after every input.");

		return options;
	}

	static CommandLine parseArgs(String[] args, Options options) throws ParseException {
		// Based on https://stackoverflow.com/a/367714
		var parser = new DefaultParser();
		CommandLine cmd = null;

		cmd = parser.parse(options, args);

		return cmd;
	}

	public static void main(String[] args) {
		CommandLine conf = null;
		HelpFormatter formatter = new HelpFormatter();
		Options options = createOptions();

		try {
			conf = parseArgs(args, options);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			formatter.printHelp("java -cp fearless.jar repl.Repl", createOptions());

			System.exit(1);
		}

		if (conf.hasOption("help")) {
			formatter.printHelp("java -cp fearless.jar repl.Repl", createOptions());
			System.exit(127);
		}

		State state = new State(conf.getOptionValue("package", "user"));
		State lastGoodState = new State(state);

		Scanner sc = new Scanner(System.in);
		String prompt = "fear> ";

		// Hold validity in variable to avoid recompiling every time.
		boolean valid = true;

		// TODO: Have some /commands
		while (true) {
			System.out.print(prompt);
			String line;

			try {
				line = sc.nextLine();
			} catch (NoSuchElementException e) {
				// User pressed Ctrl D. stdin is closed.
				break;
			}

			try {
				state.add(line, valid);
			} catch (InvalidAliasException e) {
				System.err.println("Error: " + e);
				state = new State(lastGoodState);
				continue;
			}

			valid = isValid(state);

			if (valid) {
				prompt = "fear> ";

				// TODO: Check if it ran successfully
				// if (success) {
				lastGoodState = new State(state);
				// }
			} else {
				prompt = "  ... ";
			}
		}

		// Technically not ideal to close stdin, but it brings up a leaked resource
		// warning otherwise.
		sc.close();
	}
}
