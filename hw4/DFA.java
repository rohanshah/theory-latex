import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.print.attribute.standard.OutputDeviceAssigned;

public class DFA {
	private final static String DFA_INPUT = "DFA.txt";
	private final static String QUERY_INPUT = "Query.txt";
	private final static String PARTITION = "Partition.txt";
	private final static String TEST = "Test.txt";

	private final static String PARTITION_SEPERATOR = ";";
	private final static String EMPTY_STRING = " ";
	private final static String FIN_STATES_SENTINEL = "F";
	private final static String NEWLINE = "\n";
	private final static String G_STRING = "G";

	// holds the transition table, an ArrayList of String arrays. Each position
	// in the ArrayList corresopnds to node, each position in the String array
	// corresponds to that transition on a character.
	private ArrayList<String[]> transitions;

	// holds the set of final states, an ArrayList where each value is a final
	// state
	private ArrayList<String> finStates;

	// holds the partition, an ArrayList of int arrays. Each position in the
	// array list corresponds to a possible node in the DFA. Each int array has
	// length 2, with the 0th position holding the parent and 1st position
	// holding the amount of children
	private ArrayList<int[]> fwdClose;

	private FileWriter partionOutput;
	private FileWriter testOutput;

	public DFA() {
		transitions = new ArrayList<String[]>();
		finStates = new ArrayList<String>();
		partionOutput = null;
		testOutput = null;
		try {
			partionOutput = new FileWriter(PARTITION);
			testOutput = new FileWriter(TEST);
		} catch (IOException ioxp) {

		}
	}

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
				initFwdClose();
				runTests(line.split(EMPTY_STRING));
			}
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	private void printForwardClosure() {
		ArrayList<ArrayList<String>> f = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < transitions.size(); i++) {
			f.add(new ArrayList<String>());
		}
		int level = 0;
		while (level < f.size()) {
			int[] t;
			for (int i = 0; i < fwdClose.size(); i++) {
				t = fwdClose.get(i);
				if (t[0] == level) {
					if (level == 0) {
						f.get(i).add(String.valueOf(i + 1));
					} else {
						f.get(Integer.valueOf(find(String.valueOf(t[0]))) - 1)
								.add(String.valueOf(i + 1));
					}
				}
			}
			level++;
		}

		try {
			for (int i = 0; i < f.size(); i++) {
				for (int j = 0; j < f.get(i).size(); j++) {
					if (i > 0 && j == 0) {
						partionOutput.write(PARTITION_SEPERATOR + EMPTY_STRING);
					}
					if (j > 0 && j < f.get(i).size()) {
						partionOutput.write(EMPTY_STRING);
					}
					partionOutput.write(f.get(i).get(j));
				}
			}
			partionOutput.write(NEWLINE);
			partionOutput.flush();
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	private void printForwardClosureSystemOut() {
		ArrayList<ArrayList<String>> f = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < transitions.size(); i++) {
			f.add(new ArrayList<String>());
		}
		int level = 0;
		while (level < f.size()) {
			int[] t;
			for (int i = 0; i < fwdClose.size(); i++) {
				t = fwdClose.get(i);
				if (t[0] == level) {
					if (level == 0) {
						f.get(i).add(String.valueOf(i + 1));
					} else {
						f.get(Integer.valueOf(find(String.valueOf(t[0]))) - 1)
								.add(String.valueOf(i + 1));
					}
				}
			}
			level++;
		}

		for (int i = 0; i < f.size(); i++) {
			if (f.get(i).size() == 0)
				f.remove(i);
		}

		for (int i = 0; i < f.size(); i++) {
			for (int j = 0; j < f.get(i).size(); j++) {
				if (i > 0 && j == 0) {
					System.out.print(PARTITION_SEPERATOR + EMPTY_STRING);
				}
				if (j > 0 && j < f.get(i).size()) {
					System.out.print(EMPTY_STRING);
				}
				System.out.print(f.get(i).get(j));
			}
		}
		System.out.print(NEWLINE);
	}

	private void initFwdClose() {
		fwdClose = new ArrayList<int[]>();
		for (int i = 0; i < transitions.size(); i++) {
			int[] temp = new int[2];
			temp[0] = 0;
			temp[1] = 1;
			fwdClose.add(temp);
		}
	}

	private void runTests(String[] testValues) {
		int numTrans = transitions.get(0).length;
		boolean flag = true;

		Stack<String[]> tests = new Stack<String[]>();
		tests.push(testValues);

		printForwardClosureSystemOut();
		while (!tests.isEmpty() && flag) {
			String[] test = tests.pop();
			if (bad(test[0], test[1])) {
				flag = false;
				printBad(test);
				return;
			} else {
				String u = find(test[0]);
				String v = find(test[1]);
				if (!u.equals(v)) {
					union(u, v);
					int uInt = Integer.valueOf(test[0]).intValue() - 1;
					int vInt = Integer.valueOf(test[1]).intValue() - 1;
					for (int i = 0; i < numTrans; i++) {
						String[] uv = new String[2];
						uv[0] = transitions.get(uInt)[i];
						uv[1] = transitions.get(vInt)[i];
						tests.push(uv);
					}
					printForwardClosureSystemOut();
				}
			}
		}

		printGood();
	}

	private void printGood() {
		try {
			testOutput.write(G_STRING + NEWLINE);
			testOutput.flush();
		} catch (IOException ioexp) {
			System.exit(1);
		}

		printForwardClosure();
	}

	private void printBad(String[] t) {
		try {
			testOutput.write(t[0] + EMPTY_STRING + t[1] + NEWLINE);
			testOutput.flush();
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	private void union(String p, String q) {
		int[] pNode = fwdClose.get(Integer.valueOf(p) - 1);
		int[] qNode = fwdClose.get(Integer.valueOf(q) - 1);

		if (pNode[1] >= qNode[1]) {
			pNode[1] += qNode[1];
			qNode[0] = Integer.valueOf(p);
		} else {
			qNode[1] += pNode[1];
			pNode[0] = Integer.valueOf(q);
		}
	}

	private String find(String p) {
		String retVal = p;
		int[] lookup = fwdClose.get(Integer.valueOf(p) - 1);

		while (lookup[0] != 0) {
			retVal = String.valueOf(lookup[0]);
			lookup = fwdClose.get(lookup[0] - 1);
		}

		return retVal;
	}

	private boolean bad(String p, String q) {
		if (finStates.contains(p) && finStates.contains(q))
			return false;
		if (!finStates.contains(p) && !finStates.contains(q))
			return false;

		return true;
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