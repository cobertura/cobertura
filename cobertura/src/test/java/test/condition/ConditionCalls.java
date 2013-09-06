package test.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionCalls {

	/*
	 * See the note at FirstPassMethodInstrumenter.visitJumpInsn()
	 * regarding initialization of static variables.  This next static
	 * is intended to cover that condition.
	 */
	public static String whatEver = null;
	private static final Logger logger = LoggerFactory
			.getLogger(ConditionCalls.class);

	public void call(int i) {
		if (i >= 5) //set CALL_CONDITION_LINE_NUMBER to this line number
		{
			try {
				logger.error("whatEver"); //set CALL_IGNORE_LINE_NUMBER to this line number
				throw new RuntimeException();
			} catch (Throwable t) {
				//eat it
			} finally {
				System.out.println("true");
			}
		} else {
			System.out.println("false");
		}
	}

	public void callLookupSwitch(int branch) {
		switch (branch) //set LOOKUP_SWITCH_LINE_NUMBER to this line number
		{
			case 1 :
				System.out.println("1");
				break;
			case 5 :
				System.out.println("5");
				break;
			default :
				System.out.println("default");
				break;
		}
	}

	public String callTableSwitch(int branch) {
		int[][] multiArray;
		switch (branch) //set TABLE_SWITCH_LINE_NUMBER to this line number
		{
			case 0 :
				return ("0");
			case 1 :
				return ("1");
			case 2 :
				return ("2");
			case 3 :
				return ("3");
			case 4 :
				return ("4");
			case 5 :
				return ("5");
			case 6 :
				return ("6");
			case 7 :
				return ("7");
			case 8 :
				return ("8");
			default :
				multiArray = new int[3][3];
				return ("" + multiArray[1][1]);
		}
	}

	public void callMultiCondition(int a, int b, int c) {
		//The c++ is to get SecondPassMethodInstrumenter.visitIincInsn called.
		if ((a == b) && (b >= 3) || (c++ < a)) //set MULTI_CONDITION_LINE_NUMBER to this line number
		{
			System.out.println("true");
		}
	}

	public void callMultiCondition2(int a, int b, int c) {
		if ((a == b) && (b >= utilEcho(3)) || (c < a)) //set MULTI_CONDITION2_LINE_NUMBER to this line number
		{
			System.out.println("true");
		}
	}

	int utilEcho(int number) {
		return number;
	}

	static {
		whatEver = "whatEver";
	}

	;

	public static final int CALL_CONDITION_LINE_NUMBER = 17;
	public static final int CALL_IGNORE_LINE_NUMBER = 21;
	public static final int LOOKUP_SWITCH_LINE_NUMBER = 39;
	public static final int TABLE_SWITCH_LINE_NUMBER = 56;
	public static final int MULTI_CONDITION_LINE_NUMBER = 74;
	public static final int MULTI_CONDITION2_LINE_NUMBER = 82;
}
