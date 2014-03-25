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

	private ArrayList<String[]> transitions;
	private ArrayList<String> finStates;
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
				printForwardClosure();
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
						f.get(t[0] - 1).add(String.valueOf(i + 1));
					}
				}
			}
			level++;
		}

		try {
			for (int i = 0; i < f.size(); i++) {
				for (int j = 0; j < f.get(i).size(); j++) {
					if (j > 0 && j < f.get(i).size()) {
						partionOutput.write(EMPTY_STRING);
					}
					partionOutput.write(f.get(i).get(j));
				}

				if (i < fwdClose.size() - 1 && f.get(i).size() > 0) {
					partionOutput.write(PARTITION_SEPERATOR + EMPTY_STRING);
				}
			}
			partionOutput.write("\n");
			partionOutput.flush();
		} catch (IOException ioexp) {
			System.exit(1);
		}
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
				}
			}
		}

		printGood();
	}

	private void printGood() {
		try {
			testOutput.write("G \n");
			testOutput.flush();
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	private void printBad(String[] t) {
		try {
			testOutput.write(t[0] + " " + t[1] + "\n");
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