#Performance
I haven't done any scientific tests to measure the performance of Kovenant, so no performance claims. 
The test package does contain some rudimentary performance measurement runners though. I've run these on a couple different
machines and the results varied a lot. So I encourage you to test performance yourself to find out what suits your needs best.
  
##perf01.kt
[Perf01.kt](https://github.com/mplatvoet/kovenant/blob/master/src/test/kotlin/performance/perf01.kt) compares Kovenant 
Promises configured with standard Java Executor pools versus the Kovenant Promises configures
with the default non-blocking Dispatcher. The results I've seen so far is that in general the Kovenant Dispatcher performs 
slightly better on systems with few CPU cores and out performs the Javas Executors by far on systems with more cores. 
This is of course what is to expect from non-blocking versus blocking concurrency. 

##perf02.kt
[Perf02.kt](https://github.com/mplatvoet/kovenant/blob/master/src/test/kotlin/performance/perf02.kt) compares Kovenant 
Promises versus Javas Futures. The results I've seen so far that Javas Futures out perform
Kovenant Promises on systems where CPUs are few. This is sounds logical since on this systems lock contention is less likely
and therefor all the CAS loops non-blocking algorithms introduce more overhead. But, just like [perf01.kt](#perf01.kt),
when more cores are used Kovenant Promises out perform Java Executors. 

##conclusion
Again, I encourage you to test for yourself. And keep in mind that the number of physical cores in combination with number 
of threads can have serious effect on performance. So the results you see on your local machine may differ greatly 
from the results on a production server.