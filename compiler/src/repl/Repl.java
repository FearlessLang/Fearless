package repl;

import program.Program;
import org.apache.commons.cli.*;

import java.util.*;

public class Repl {
	static Options createOptions() {
		var options = new Options();

		options.addOption("h", "help", false, "Show this message");
		options.addOption("p", "package", true, "Set REPL's package name");
		options.addOption("r", "refresh", false, "Refresh program state after every input.");

		return options;
	}

	static CommandLine parseArgs(String[] args, Options options) throws ParseException {
		// Based on https://stackoverflow.com/a/367714
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		cmd = parser.parse(options, args);

		return cmd;
	}

	// Program uh(String s) {
	// return parser.Parser.parseAll(List.of(new parser.Parser(Path.of("/dev/null"),
	// s)),
	// program.TypeSystemFeatures.of());
	// }

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
		Scanner sc = new Scanner(System.in);

		// TODO: Have some /commands
		while (true) {
			String line;
			try {
				line = sc.nextLine();
			} catch (NoSuchElementException e) {
				// User pressed Ctrl D.
				break;
			}

			System.out.println("Got '%s'. TODO: Functionality".formatted(line));
			;
		}
	}
}
