#Progress
part of [`kovenant-progress`](../index.md#artifacts)

---
Progress tracking is functionality that is common amongst most Promise libraries and specifications. Though it's useful
in some cases, it's simply not always needed. That's the reason it's not integrated into Kovenant but rather offered as 
a separate project called [Progress](http://progress.komponents.nl). Please refer to the 
[Progress](http://progress.komponents.nl) site for documentation.

This package simply provides a convenient method for attaching Progress to use the Kovenant `Dispatchers`.

```kt
Progress.attachToKovenant()

val control = Progress.control()
control.progress.update {
    println(value)
    if (done) println ("done")
}

val steps = 40
async {
    for (i in 1..steps ) {
        control.value = i / steps.toDouble()
        Thread.sleep(100)
    }
}

```