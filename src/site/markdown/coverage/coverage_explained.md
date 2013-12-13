# Understanding coverage

It is important to understand how Cobertura interprets and shows code coverage, in particular
when there are multiple execution paths. In this context, an "execution path" is a set of
instructions executed in order - thus, an ```if``` statement in your code creates two separate
execution paths (one path executed in case the condition is true, and another path executed in
case the condition is false).

In the general case, it is important to understand how Cobertura handles statements that create
new execution paths - i.e. switch, if, while, etc. Examples of code and Cobertura's handling
and coverage notations are shown below.

*Disclaimer*: Cobertura normally works by transforming compiled bytecode, implying that alterations
in the bytecode structure can affect the way that Cobertura understands coverage metrics. While
alterations of bytecode structure are relatively rare, the JDK team can certainly infer changes
between JDK versions. We will try to cope as best we can.

## 1) If Statements

If statements create two separate execution paths (one when the if condition is true and one when
the if condition is false). Moreover, several source code constructs are compiled to the same bytecode
for various If-like statements. We will therefore cover several scenarios.

### 1.1) Single if statement - 2 execution paths
Example Source Code:

<pre class="brush: java" title="Single if statement"><![CDATA[
if (x == 0) {
  System.out.println("X is zero");
} else {
  System.out.println("X is invalid");
}
]]></pre>

This code has two execution paths (two different sets of statements are executed if the
condition is true vs. false). Therefore, to get "Complete" or "Full" code coverage, the
example code above must be executed twice with different conditions (```X == 0``` and
```X != 0```). This is summed up in the table below:

<table>
    <tr>
        <th width="10%"># times executed</th>
        <th width="20%">Condition</th>
        <th width="70%">Cobertura report</th>
    </tr>
    <tr>
    	<td>1</td>
        <td><code>X == 0</code></td>
        <td><img src="../images/coverage/s11.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
    <tr>
        <td>1</td>
        <td><code>X != 0</code></td>
        <td><img src="../images/coverage/s12.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
    <tr>
    	<td>2</td>
    	<td><code>X == 0</code> and <code>X != 0</code></td>
    	<td><img src="../images/coverage/s13.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
</table>


### 1.2) Linked if-else statements - 3 execution paths
Example Source Code:

<pre class="brush: java" title="linked if-else statements"><![CDATA[
if (x == 0) {
  System.out.println("X is zero");
} else if (x == -1) {
  System.out.println("X is negative one");
} else {
  System.out.println("X is invalid");
}
]]></pre>

If the code above is called 2 times where ```X == 0``` the first time and ```X == 1``` the second,
two out of the three possible execution paths are covered by the tests. (We lack the case where
```X == -1```). This is illustrated in the table below:

<table>
    <tr>
        <th width="10%"># times executed</th>
        <th width="10%">Condition</th>
        <th width="80%">Cobertura report</th>
    </tr>
    <tr>
    	<td>2</td>
    	<td><code>X == 0</code> and <code>X == 1</code></td>
    	<td><img src="../../images/coverage/s21.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
</table>

### 1.3) Single-line if statements - 2 execution paths
Example Source Code:

<pre class="brush: java" title="linked if-else statements"><![CDATA[
if (x == 0) System.out.println("X is zero");
]]></pre>

<table>
    <tr>
        <th width="10%"># times executed</th>
        <th width="10%">Condition</th>
        <th width="80%">Cobertura report</th>
    </tr>
    <tr>
    	<td>1</td>
    	<td><code>X == 1</code></td>
    	<td><img src="../../images/coverage/s31.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
</table>

### 1.4) Single-line if statement using "?" and ":" - 2 execution paths
Example Source Code:

<pre class="brush: java" title="linked if-else statements"><![CDATA[
boolean isZero = (x == 0) ? true : false;
]]></pre>

The notation ```(condition) ? ifTrueStatement : ifFalseStatement; ``` is merely a source-code
syntactic sugar for the more verbose/full notation
```if(condition) { ifTrueStatement } else { ifFalseStatement } ```.
Therefore, the code coverage is identical to scenario 1.3 above:

<table>
    <tr>
        <th width="10%"># times executed</th>
        <th width="10%">Condition</th>
        <th width="80%">Cobertura report</th>
    </tr>
    <tr>
    	<td>1</td>
    	<td><code>X == 1</code></td>
    	<td><img src="../../images/coverage/s41.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
</table>

### 1.5) Compound condition (boolean "and") - 3 execution paths
Example Source Code:

<pre class="brush: java" title="linked if-else statements"><![CDATA[
if (x != 0 && y != 1) {
  System.out.println("X is not 0 and Y is not 1.");
} else {
  System.out.println("X is 0 and Y is 1.");
}
]]></pre>

Compound expressions get evaluated in series, which means that the statement
```if(x != 0 && y != 1) { statement1 } else { statement2 } ``` is source-code
equivalent to the more verbose/full notation
```if(x != 0) { if(y != 1) { statement1 } else { statement2 } } else { statement2 } ```,
with the added complexity that the ```statement2``` is identical in the latter notation.
This, in turn implies that the source code has 3 execution paths.

## 1.6) Compound condition (boolean "or") - 3 execution paths
Example Source Code:

<pre class="brush: java" title="linked if-else statements"><![CDATA[
if (x == 0 || x == 1) {
  System.out.println("X is either 0 or 1.");
} else {
  System.out.println("X is not either 0 or 1.");
}
]]></pre>

Compound expressions get evaluated in series, which means that the statement
```if(x != 0 || y != 1) { statement1 } else { statement2 } ``` is source-code
equivalent to the more verbose/full notation
```if(x != 0) { statement1 } else { if(y != 1) { statement1 } else { ifFalseStatement } }```,
with the added complexity that the ```statement1``` is identical in the latter notation.
This, in turn implies that the source code has 3 execution paths.

# While and Do-While Statements
## Scenario 1
```java
int x = 0;
while(x != 5) {
  x++;
}
```
## Scenario 2
```java
int x = 0;
do{
  x++;
}while(x != 5);
```

# For-Loops
```java
for (int x = 0; x < 5; x++) {
  System.out.println(x);
}
```

# Foreach Loops
A for-each loop iterates though each element in a list. Let us say we have this example:
```java
List<String> x = new ArrayList<String>();
x.add("Hello");
x.add("World");
for(String element : x ) {
  System.out.println(element);
}
```
Info: The way this type of loop is pronounced is "For each String in x".