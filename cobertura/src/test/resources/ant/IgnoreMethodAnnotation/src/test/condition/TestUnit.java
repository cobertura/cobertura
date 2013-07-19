package test.condition;

import org.junit.Test;

public class TestUnit {
	@Test
	public void testMethod() {
		IgnoreMeAlso.foo();
		IgnoreMeNot.foo();
		IgnoreMe.foo();
	}
}
