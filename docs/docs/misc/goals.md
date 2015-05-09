#Goals

##Overall Goals

###Easy to use 
First and foremost the API should be easy to use. It has to feel natural and behave as expected. 
Everything should work out of the box without any configuration. No worries about thread pools like shutting them down.
Still, everything should be flexible enough to adjust Kovenant to specific needs.

###Runtime Agnostic
API layer must be pure Kotlin. For now only a Jvm implementation exist but it should be possible to support Javascript
and any other platform Kotlin supports in the future. So no references in the API layer to platform specific functions
and classes.

###Dependency free
Of course not counting the Kotlin standard runtime, Kovenant should be completely free of dependencies. This makes it 
easy to incorporate Kovenant in your projects. Though make sure that necessary hooks are provided so that Kovenant
can be connected to existing services. So loggers and existing thread pools should easily be connected.

###Memory efficient
Try to reduce the overhead as much as possible because I want Kovenant to be a viable alternative to sequential and
existing concurrency approaches. 

##Platform Specific Goals

###Non blocking
Thread safety is of course an implied requirement, but furthermore for any platform allowing non blocking concurrency
everything should be non blocking. Non blocking algorithms generally perform better where lock contention is to be 
expected. Please refer to [Performance](performance.md) for more on the subject.  


