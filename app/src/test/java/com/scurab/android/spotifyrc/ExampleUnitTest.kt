package com.scurab.android.spotifyrc

import com.scurab.android.spotifyrc.commands.Command
import com.scurab.android.spotifyrc.util.JsonConverter
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        val converter = JsonConverter()
        val json = converter.toJson(Command.Resume())
        val x = converter.fromJson<Command.Resume>(json)
    }
}