import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
	
	public DFA(){
		transitions = new ArrayList<String[]>();
		finStates = new ArrayList<String>();
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
		initFwdClose();
		try {
			while ((line = query.readLine()) != null) {
				runTests(line.split(EMPTY_STRING));
			}
		} catch (IOException ioexp) {
			System.exit(1);
		}
	}

	private void printForwardClosure() {
		// try {
		// FileWriter output = null;
		// output = new FileWriter(PARTITION);
		// for (int i = 0; i < fwdClose.size(); i++) {
		// HashMap<Integer, String> states = fwdClose.get(i);
		//
		// for (int j = 0; j < states.size(); j++) {
		// if (j > 0 && j < states.size() - 1) {
		// output.write(EMPTY_STRING);
		// }
		// output.write(states.get(j));
		// }
		//
		// if (i < fwdClose.size() - 1) {
		// output.write(PARTITION_SEPERATOR + EMPTY_STRING);
		// }
		// }
		// } catch (IOException ioexp) {
		// System.exit(1);
		// }
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
				System.out.println(test[0] + " " + test[1]);
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
		
		System.out.println("G");
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