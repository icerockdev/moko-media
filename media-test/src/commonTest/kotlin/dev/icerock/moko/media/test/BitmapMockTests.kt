/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.test

import kotlin.test.Test

class BitmapMockTests {
    @Test
    fun `bitmap byte array`() {
        val bitmap = createBitmapMock()
        bitmap.toByteArray()
        // we should not throw any exceptions
    }
}
