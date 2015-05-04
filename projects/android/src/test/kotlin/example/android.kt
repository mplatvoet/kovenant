package example.android

import nl.mplatvoet.komponents.kovenant.android.successUI
import nl.mplatvoet.komponents.kovenant.async

fun main(args: Array<String>) {

    //Does not run since stub library is used

    async {
        1+1
    } successUI {
        //bla bla
    }
}
