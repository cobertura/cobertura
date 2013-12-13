# Cobertura architecture

## Overall design
Cobertura instruments bytecode to record automated test coverage - typically during execution of unit tests within
a build tool, but other uses are possible. The normal activity steps are shown (from a birds eye perspective) in the
image below:

<img src="images/plantuml/arch_overall_step1.png" style="margin:10px; border:1px solid black;" />

(... overall description to be written ...)

Cobertura responsibilities:
1.	Perform code instrumentation
2.	Gather information about code coverage
3.	Expose code coverage information and metrics in reports

Cobertura should not care about:
1.	Code compilation and original bytecode generation
2.	Running automated tests

## Software component design
Cobertura is/will be structured as a set of collaborating software components, where each software component
has a public projects (optional model, API, optional SPI) and private projects (implementation, example), as well
as distinct responsibilities. This is done to sustainably reduce dependency tanglement and to increase flexibility,
testability and and delivery velocity. Moreover, this structure ensures that implementations can be injected
at runtime, implying that users can implement and substitute custom formats for metrics and reports.

From the view of a calling client (another module within Cobertura, for example), a component is seen as an API
project which optionally may depend on its own (domain) model. This is illustrated in the image below:

<img src="images/plantuml/software_component.png" style="margin:10px; border:1px solid black;" />

This structure has not yet been fully introduced in the Cobertura codebase.

### Development guidelines
Coberturas API projects should be fully decoupled from specific implementation technologies to access it
(ex.: Ant). Instead, implementation projects wrap all dependencies pertaining to specific integrations.
