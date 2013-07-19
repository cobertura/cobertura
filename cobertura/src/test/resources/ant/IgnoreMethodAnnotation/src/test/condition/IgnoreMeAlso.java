package test.condition;

public class IgnoreMeAlso {
  @CoverageIgnore
  public static void foo() {
    System.out.println("Ignore Me Too!");
  }
}
