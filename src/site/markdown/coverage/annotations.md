# Cobertura coverage annotations

Cobertura 2.0 introduced annotation-driven instrumentation of bytecode.
This features allows adding a java 6 annotation to the code, preventing cobertura from
instrumenting the code and reporting it.

Some reasons for not instrumenting code might be:
*	Coverage in code might be a big performance hit.
*	Time sensitive code
*	Cobertura fails to instrument.

Because of these requirements coverage annotations were introduced. To create an annotation, simply add the following to your code:

```java
public @interface CoverageIgnore{
}
```

This creates the ```@CoverageIgnore ``` annotation which allows for insertion of this annotation for any java method or class.

```java
@CoverageIgnore
public class Foo {
...
}
```
Or
```java
public class Foo {
  @CoverageIgnore
  public void start() {
    ...
  }
}
```

To add the annotations, please see either the [Ant Task Reference](https://github.com/cobertura/cobertura/wiki/Ant-Task-Reference) page or [Command Line Reference](https://github.com/cobertura/cobertura/wiki/Command-Line-Reference) page.