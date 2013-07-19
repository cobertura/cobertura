package mypackage;

public class Main extends Thread {
	public static class MyObject implements MyInterface
	{
		public void myInterfaceMethod()	{
		}
	}

	public static void main(String[] args) {
		Main main = new Main();
		/*
		* Call all methods so they will be considered "covered" unless
		* they are ignored as trivial.
		*
		* These are in no particular order.
		*/
		main.getterTrivial();
		main.empty();
		main.getVoid();
		main.getIntWithIntParm(0);
		main.isBool();
		main.hasBool();
		main.set();
		main.setInt(1);
		main.setIntWithTwoParms(1, 2);
		main.getMultiDimArray();
		main.setIncrement(1);
		main.setConst("");
		main.getArray();
		main.getObject();
		main.getStatic();
		main.setStatic(1);
		main.setISTORE(1);
		main.setLSTORE(1);
		main.setFSTORE((float)1.0);
		main.setDSTORE(1.0);
		main.setASTORE(null);
		main.getINVOKEVIRTUAL();
		main.getINVOKESPECIAL();
		main.getINVOKESTATIC();
		main.setINVOKEINTERFACE(new MyObject());

		// call constructors in no particular order
		new Main(1);
		new Main(true);
		new Main("str");
		new Main("", "");
		new Main("", 0);
		new Main("", true);
		new Main((Thread) null, "string");
		new Main((Thread) null, 0);
	}

	// Be careful when initializing members.  If you instantiate an
	// object, then trivial constructors will become non-trivial.
	// Ex. Integer myInteger = new Integer(1); will cause Main() to be non-trivial.
	int myint;
	boolean mybool;
	private static int mystatic;

	// trivial constructor"
	public Main() {
	}

	// constructors that just call super() are trivial
	public Main(Thread t, String str) {
		super(str);
	}

	// constructors that just call super() are usually trivial, but
	// this constructor uses a constant, so it is considered non-trivial.
	public Main(Thread t, int i) {
		super("string");
	}

	public Main(boolean bool) {
		// non-trivial conditional
		myint = bool ? 0 : 1;
	}

	public Main(int num) {
		// non-trivial switch
		switch(num) {
			default:
		}
	}

	public Main(String str) {
		// setting of statics is non-trivial
		mystatic = 2;
	}

	public Main(String str1, String str2) {
		// non-trivial method call
		privateMethod();
	}

	public Main(String str1, int i) {
		// non-trivial object construction
		new StringBuffer();
	}

	public Main(String str1, boolean bool) {
		// non-trivial this() call
		this(str1, 0);
	}


	// trivial getter
	public int getterTrivial() {
		return myint;
	}

	// trivial getter
	public boolean isBool() {
		return mybool;
	}

	// trivial getter
	public boolean hasBool() {
		return mybool;
	}

	// trivial setter
	public void setInt(int i) {
		myint = i;
	}

	// this would be trivial, but it is a getter that with no return value
	public void getVoid() {
	}

	// "empty" does not start with "get", "is", "has", or "set", so
	// it is considered non-trivial.
	private int empty() {
		return 0;
	}

	// this is a getter that takes a parameter, so it is non-trivial.
	public int getIntWithIntParm(int i) {
		return 0;
	}

	// this would be a trivial setter, but it does not have a parameter.
	public void set() {
	}

	// this would be a trivial setter, but it has more than one parameter.
	public void setIntWithTwoParms(int i, int j) {
		myint = i;
	}

	public int[][] getMultiDimArray() {
		// non-trivial construction of a multi-dimensional array
		return new int[1][1];
	}

	public void setIncrement(int i) {
		// non-trivial increment of local variable
		i++;
	}

	public void setConst(String str) {
		/*
		 * cause visitLdcInsn to be called because "str" is in the
		 * runtime constant pool.  An LDC operation is performed
		 * which is considered non-trivial.
		 */
		System.out.println("str");
	}

	public int[] getArray() {
		// causes visitIntInsn to be called.  Creating an array is a "single int operand".
		// non-trivial.
		return new int[1];
	}

	public Object getObject() {
		// causes visitTypeInsn to be called.  Creating an object is a type instruction.
		// non-trivial.
		return new Object();
	}

	public int getStatic() {
		// getting a static is non-trivial.
		return mystatic;
	}

	public void setStatic(int i) {
		// setting a static is non-trivial.
		mystatic = i;
	}

	// non-trivial local variable instruction (causes visitVarInsn(ISTORE)) (int store to local var)
	public void setISTORE(int i) {
		i = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(LSTORE)) (long store to local var)
	public void setLSTORE(long l) {
		l = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(FSTORE)) (floating store to local var)
	public void setFSTORE(float f) {
		f = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(DSTORE)) (double store to local var)
	public void setDSTORE(double d) {
		d = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(ASTORE)) (object store to local var)
	public void setASTORE(Object obj) {
		obj = null;
	}

	public void publicMethod() {
	}
	private void privateMethod() {
	}
	public static void staticMethod() {
	}

	// non-trivial public method call (causes visitMethodInsn(INVOKEVIRTUAL))
	public int getINVOKEVIRTUAL() {
		publicMethod();
		return 0;
	}

	// non-trivial private method call (causes visitMethodInsn(INVOKESPECIAL)) 
	public int getINVOKESPECIAL() {
		privateMethod();
		return 0;
	}

	// non-trivial static method call (causes visitMethodInsn(INVOKESTATIC)) 
	public int getINVOKESTATIC() {
		staticMethod();
		return 0;
	}

	// non-trivial interface method call (causes visitMethodInsn(INVOKEINTERFACE))
	public void setINVOKEINTERFACE(MyInterface obj) {
		obj.myInterfaceMethod();
	}
}
