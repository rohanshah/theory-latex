import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DFA {
	private final static String DFA_INPUT = "DFA.txt";
	private final static String QUERY_INPUT = "Query.txt";
	private final static String PARTITION = "Partition.txt";
	private final static String TEST = "Test.txt";
	private final static String PARTITION_SEPERATOR = ";";
	private final static String EMPTY_STRING = " ";
	private final static String FIN_STATES_SENTINEL = "F";

	private ArrayList<String[]> transitions;
	private ArrayList<String> finStates;
	private ArrayList<ArrayList<String>> fwdClose;

	public void parseTransitions(BufferedReader dfa) {
		String line = null;
		try {
			while ((line = dfa.readLine()) != null) {
				String[] parsed = line.split(EMPTY_STRING);
				if (parsed != null && !parsed[0].equals(FIN_STATES_SENTINEL))
					transitions.add(parsed);
				else if (parsed != null
						&& parsed[0].equals(FIN_STATES_SENTINEL)) {
					for (int i = 1; i < parsed.length; i++) {
						finStates.add(parsed[i]);
					}
				}
			}
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	public void parseQuery(BufferedReader query) {
		String line = null;
		try {
			while ((line = query.readLine()) != null) {
				runTests(line.split(EMPTY_STRING));
			}
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	public void createForwardClosure() {
		fwdClose.add(finStates); // one forward closue could be all final states

		ArrayList<String> nonFinalStates = new ArrayList<String>();
		for (int i = 0; i < transitions.size(); i++) {
			if (!finStates.contains(String.valueOf(i)))
				nonFinalStates.add(String.valueOf(i));
		}

		fwdClose.add(nonFinalStates); // one forward closue could be all
										// non-final states

		printForwardClosure();
	}

	private void printForwardClosure() {
		try {
			FileWriter output = null;
			output = new FileWriter(PARTITION);
			for (int i = 0; i < fwdClose.size(); i++) {
				ArrayList<String> states = fwdClose.get(i);

				for (int j = 0; j < states.size(); j++) {
					if (j > 0 && j < states.size() - 1) {
						output.write(EMPTY_STRING);
					}
					output.write(states.get(i));
				}

				if (i < fwdClose.size() - 1) {
					output.write(PARTITION_SEPERATOR + EMPTY_STRING);
				}
			}
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	private void runTests(String[] values) {

	}

	private void union() {

	}

	private void find() {

	}

	public static void main(String[] args) {
		BufferedReader dfaInput = null;
		BufferedReader queryInput = null;
		DFA app = new DFA();

		if (args.length == 2) {
			try {
				dfaInput = new BufferedReader(new FileReader(args[0]));
				queryInput = new BufferedReader(new FileReader(args[1]));
			} catch (IOException ioexp) {

			}
		}

		if (dfaInput == null) {
			try {
				dfaInput = new BufferedReader(new FileReader(DFA_INPUT));
			} catch (IOException ioexp) {
				return;
			}
		}

		if (queryInput == null) {
			try {
				queryInput = new BufferedReader(new FileReader(QUERY_INPUT));
			} catch (IOException ioexp) {
				return;
			}
		}

		app.parseTransitions(dfaInput);
		app.createForwardClosure();

		app.parseQuery(queryInput);

		try {
			dfaInput.close();
			queryInput.close();
		} catch (IOException ioexp) {
			System.exit(1);
		}
		System.exit(0);
	}

}