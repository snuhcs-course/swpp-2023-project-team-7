// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("io.github.gmazzo.test.aggregation.coverage") version "2.1.1"
    // and/or
    id("io.github.gmazzo.test.aggregation.results") version "2.1.1"
}

//plugins {
//    id("io.github.gmazzo.test.aggregation.coverage") version "<latest>"
//    // and/or
//    id("io.github.gmazzo.test.aggregation.results") version "<latest>"
//}